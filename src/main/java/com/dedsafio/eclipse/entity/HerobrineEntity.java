package com.dedsafio.eclipse.entity;

import com.dedsafio.eclipse.eclipse.EclipseData;
import com.dedsafio.eclipse.item.LinternaItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Herobrine (versión reforzada).
 * - Se queda quieto mirándote. Si pasan 5 s sin alumbrarlo, te persigue y te golpea (el golpe te deja ciego un momento).
 * - Para hacerlo desaparecer hay que acumular 6 s de luz sobre él, con un cono estrecho. Si dejas de alumbrarlo,
 *   la luz acumulada se pierde rápido.
 * - Persiguiendo, la luz solo lo frena (no lo congela) y se teletransporta a tu espalda si te alejas mucho.
 * - Desaparece al acabar el eclipse o si tú o él están en un búnker.
 */
public class HerobrineEntity extends Monster {
    public static final int UNLIT_TICKS_TO_CHASE = 100;  // 5 s sin alumbrarlo -> te persigue
    public static final int LIT_TICKS_TO_VANISH = 120;   // 6 s de luz acumulada -> desaparece
    private static final double LIT_MIN_DOT = 0.955D;    // cono de ~17 grados
    private static final double LIT_RANGE = 24.0D;

    private int litTicks;
    private int unlitTicks;
    private boolean chasing;
    private int attackCooldown;
    private int teleportCooldown;
    private int lonelyTicks;
    private boolean ignoreEclipse;

    public HerobrineEntity(EntityType<? extends HerobrineEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 160.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.36D)
                .add(Attributes.ATTACK_DAMAGE, 16.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    public boolean isIgnoreEclipse() { return ignoreEclipse; }
    public void setIgnoreEclipse(boolean v) { this.ignoreEclipse = v; }
    public boolean isChasing() { return chasing; }

    private boolean isLitBy(Player player) {
        if (!LinternaItem.isHolding(player)) return false;
        Vec3 to = this.getEyePosition().subtract(player.getEyePosition());
        double dist = to.length();
        if (dist > LIT_RANGE || dist < 0.5D) return false;
        if (player.getLookAngle().dot(to.scale(1.0D / dist)) < LIT_MIN_DOT) return false;
        return player.hasLineOfSight(this);
    }

    private void vanish(ServerLevel level) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.0D, getZ(), 25, 0.3D, 0.7D, 0.3D, 0.01D);
        this.discard();
    }

    private static BlockPos findGround(ServerLevel level, int x, int startY, int z) {
        for (int dy = 6; dy >= -8; dy--) {
            BlockPos pos = new BlockPos(x, startY + dy, z);
            if (!level.isLoaded(pos)) return null;
            if (level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
                    && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
                return pos;
            }
        }
        return null;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(this.level() instanceof ServerLevel level)) return;
        EclipseData data = EclipseData.get(level.getServer());

        if (!ignoreEclipse && !data.active) { vanish(level); return; }
        if (data.isInBunker(this.blockPosition())) { vanish(level); return; }

        Player target = level.getNearestPlayer(getX(), getY(), getZ(), 64.0D, e -> e.isAlive() && !e.isSpectator());
        if (target == null) {
            if (++lonelyTicks > 200) vanish(level);
            return;
        }
        lonelyTicks = 0;
        if (data.isInBunker(target.blockPosition())) { vanish(level); return; }

        this.getLookControl().setLookAt(target, 90.0F, 90.0F);

        boolean lit = isLitBy(target);
        if (lit) {
            litTicks++;
            unlitTicks = 0;
            if (!chasing) this.getNavigation().stop();
            if (litTicks % 30 == 0) { // se retuerce cada vez más
                this.playSound(SoundEvents.ENDERMAN_SCREAM, 1.5F, 0.5F + litTicks / 240.0F);
            }
            if (litTicks >= LIT_TICKS_TO_VANISH) {
                level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.5F);
                vanish(level);
                return;
            }
        } else {
            litTicks = Math.max(0, litTicks - 2); // la luz acumulada se pierde rápido
            if (!chasing && ++unlitTicks >= UNLIT_TICKS_TO_CHASE) {
                chasing = true;
                this.playSound(SoundEvents.ENDERMAN_SCREAM, 2.0F, 0.5F);
            }
        }

        if (chasing) {
            double speed = lit ? 0.45D : 1.0D; // la luz lo frena, no lo detiene
            if (this.tickCount % 5 == 0) this.getNavigation().moveTo(target, speed);
            if (this.getNavigation().isDone()) {
                this.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), speed);
            }
            if (this.horizontalCollision && this.onGround()) this.getJumpControl().jump();

            // si te alejas, aparece a tu espalda
            if (teleportCooldown > 0) teleportCooldown--;
            if (!lit && teleportCooldown <= 0 && this.distanceToSqr(target) > 400.0D) {
                Vec3 back = target.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize().scale(-6.0D);
                BlockPos g = findGround(level, Mth.floor(target.getX() + back.x), target.blockPosition().getY(),
                        Mth.floor(target.getZ() + back.z));
                if (g != null && !data.isInBunker(g)) {
                    this.moveTo(g.getX() + 0.5D, g.getY(), g.getZ() + 0.5D, this.getYRot(), 0.0F);
                    this.getNavigation().stop();
                    level.playSound(null, g, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.6F);
                    teleportCooldown = 100;
                }
            }

            if (attackCooldown > 0) attackCooldown--;
            if (attackCooldown <= 0 && this.distanceToSqr(target) < 4.0D) {
                this.swing(InteractionHand.MAIN_HAND);
                if (this.doHurtTarget(target)) {
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                }
                attackCooldown = 12;
            }
        }
    }

    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.PLAYER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PLAYER_DEATH; }
    @Nullable @Override protected SoundEvent getAmbientSound() { return null; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IgnoreEclipse", ignoreEclipse);
        tag.putBoolean("Chasing", chasing);
        tag.putInt("UnlitTicks", unlitTicks);
        tag.putInt("LitTicks", litTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ignoreEclipse = tag.getBoolean("IgnoreEclipse");
        chasing = tag.getBoolean("Chasing");
        unlitTicks = tag.getInt("UnlitTicks");
        litTicks = tag.getInt("LitTicks");
    }
}
