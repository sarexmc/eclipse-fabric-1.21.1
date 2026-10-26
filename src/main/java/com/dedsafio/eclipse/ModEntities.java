package com.dedsafio.eclipse;

import com.dedsafio.eclipse.entity.HerobrineEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    private ModEntities() {}

    public static final EntityType<HerobrineEntity> HEROBRINE = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(DedsafioEclipse.MOD_ID, "herobrine"),
            EntityType.Builder.of(HerobrineEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build("herobrine"));

    public static void register() {
        FabricDefaultAttributeRegistry.register(HEROBRINE, HerobrineEntity.createAttributes());
    }
}
