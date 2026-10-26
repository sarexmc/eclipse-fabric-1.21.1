package com.dedsafio.eclipse;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final CreativeModeTab MAIN = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(DedsafioEclipse.MOD_ID, "main"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup." + DedsafioEclipse.MOD_ID))
                    .icon(() -> new ItemStack(ModItems.LINTERNA))
                    .displayItems((params, output) -> output.accept(ModItems.LINTERNA))
                    .build());

    public static void register() {}
}
