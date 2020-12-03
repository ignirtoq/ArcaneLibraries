package net.arcanumverum.arcanelibraries

import net.minecraft.item.ItemGroup
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

import net.fabricmc.api.Environment
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry

import net.arcanumverum.arcanelibraries.Constants
import net.arcanumverum.arcanelibraries.items.ArcaneTomeItem
import net.arcanumverum.arcanelibraries.items.InkVialItem
import net.arcanumverum.arcanelibraries.items.ScribingToolsItem
import net.arcanumverum.arcanelibraries.screens.tome.TomeScreen
import net.arcanumverum.arcanelibraries.screens.tome.TomeScreenHandler

object Items {
    val INK_VIAL = InkVialItem(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))
    val SCRIBING_TOOLS = ScribingToolsItem(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))
    val ARCANE_TOME = ArcaneTomeItem(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))
}

object Screens {
    val TOME_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(Constants.ARCANE_TOME_IDENTIFIER, ::TomeScreenHandler)
}

fun load_items() {
    Registry.register(Registry.ITEM, Constants.INK_VIAL_IDENTIFIER, Items.INK_VIAL)
    Registry.register(Registry.ITEM, Constants.SCRIBING_TOOLS_IDENTIFIER, Items.SCRIBING_TOOLS)
    Registry.register(Registry.ITEM, Constants.ARCANE_TOME_IDENTIFIER, Items.ARCANE_TOME)
}

@Suppress("unused")
fun init() {
    load_items()
}

@Environment(EnvType.CLIENT)
fun init_client() {
    ScreenRegistry.register(Screens.TOME_SCREEN_HANDLER, ::TomeScreen)
}
