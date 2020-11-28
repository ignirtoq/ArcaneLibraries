package net.arcanumverum.arcanelibraries

import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

import net.fabricmc.fabric.api.item.v1.FabricItemSettings

import net.arcanumverum.arcanelibraries.Constants
import net.arcanumverum.arcanelibraries.inkvial.InkVialItem

object Items {
    val INK_VIAL = InkVialItem(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))
}

fun load_items() {
    Registry.register(Registry.ITEM, Identifier(Constants.MOD_ID, Constants.INK_VIAL_PATH), Items.INK_VIAL)
}

@Suppress("unused")
fun init() {
    load_items()
}

