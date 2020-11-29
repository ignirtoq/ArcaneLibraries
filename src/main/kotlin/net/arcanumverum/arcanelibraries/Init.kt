package net.arcanumverum.arcanelibraries

import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

import net.fabricmc.fabric.api.item.v1.FabricItemSettings

import net.arcanumverum.arcanelibraries.Constants
import net.arcanumverum.arcanelibraries.inkvial.InkVialItem
import net.arcanumverum.arcanelibraries.scribingtools.ScribingToolsItem
import net.arcanumverum.arcanelibraries.arcanetome.ArcaneTomeItem

object Items {
    val INK_VIAL = InkVialItem(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))
    val SCRIBING_TOOLS = ScribingToolsItem(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))
    val ARCANE_TOME = ArcaneTomeItem(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))
}

fun load_items() {
    Registry.register(Registry.ITEM, Identifier(Constants.MOD_ID, Constants.INK_VIAL_PATH), Items.INK_VIAL)
    Registry.register(Registry.ITEM, Identifier(Constants.MOD_ID, Constants.SCRIBING_TOOLS_PATH), Items.SCRIBING_TOOLS)
    Registry.register(Registry.ITEM, Identifier(Constants.MOD_ID, Constants.ARCANE_TOME_PATH), Items.ARCANE_TOME)
}

@Suppress("unused")
fun init() {
    load_items()
}

