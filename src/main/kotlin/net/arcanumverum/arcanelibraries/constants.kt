package net.arcanumverum.arcanelibraries

import net.minecraft.util.Identifier

object Constants {
    const val MOD_ID = "arcane_libraries"
    const val MOD_NAME = "ArcaneLibraries"

    // Item registry paths
    const val INK_VIAL_PATH = "ink_vial"
    const val SCRIBING_TOOLS_PATH = "scribing_tools"
    const val ARCANE_TOME_PATH = "arcane_tome"

    val INK_VIAL_IDENTIFIER = Identifier(MOD_ID, INK_VIAL_PATH)
    val SCRIBING_TOOLS_IDENTIFIER = Identifier(MOD_ID, SCRIBING_TOOLS_PATH)
    val ARCANE_TOME_IDENTIFIER = Identifier(MOD_ID, ARCANE_TOME_PATH)

    // Texture paths
    const val TOME_GUI_TEXTURE_PATH = "textures/gui/tome-with-inventory.png"
}
