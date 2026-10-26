package com.dedsafio.eclipse;

import com.dedsafio.eclipse.item.LinternaItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class ModItems {
    private ModItems() {}

    public static final Item LINTERNA = Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath(DedsafioEclipse.MOD_ID, "linterna"),
            new LinternaItem(new Item.Properties().durability(LinternaItem.MAX_DURABILITY)));

    /** Fuerza la carga de la clase (y con ello el registro). */
    public static void register() {}
}
