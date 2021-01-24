package net.arcanumverum.arcanelibraries

import net.minecraft.util.Identifier

object Constants {
    const val MOD_ID = "arcane_libraries"
    const val MOD_NAME = "ArcaneLibraries"

    // Block registry paths
    const val BOOKCASE_BLOCK_PATH = "bookcase"
    const val ELDRITCH_LOG_PATH = "eldritch_log"
    const val ELDRITCH_PLANKS_PATH = "eldritch_planks"
    const val ELDRITCH_SLAB_PATH = "eldritch_slab"

    val BOOKCASE_BLOCK_IDENTIFIER = Identifier(MOD_ID, BOOKCASE_BLOCK_PATH)
    val ELDRITCH_LOG_IDENTIFIER = Identifier(MOD_ID, ELDRITCH_LOG_PATH)
    val ELDRITCH_PLANKS_IDENTIFIER = Identifier(MOD_ID, ELDRITCH_PLANKS_PATH)
    val ELDRITCH_SLAB_IDENTIFIER = Identifier(MOD_ID, ELDRITCH_SLAB_PATH)

    // Fluid registry paths
    const val INK_FLUID_BLOCK_PATH = "ink"
    const val STILL_INK_FLUID_PATH = "ink"
    const val FLOWING_INK_FLUID_PATH = "flowing_ink"
    const val INK_FLUID_BUCKET_PATH = "ink_bucket"

    val INK_FLUID_BLOCK_IDENTIFIER = Identifier(MOD_ID, INK_FLUID_BLOCK_PATH)
    val STILL_INK_IDENTIFIER = Identifier(MOD_ID, STILL_INK_FLUID_PATH)
    val FLOWING_INK_IDENTIFIER = Identifier(MOD_ID, FLOWING_INK_FLUID_PATH)
    val INK_BUCKET_IDENTIFIER = Identifier(MOD_ID, INK_FLUID_BUCKET_PATH)

    // Item registry paths
    const val INK_VIAL_PATH = "ink_vial"
    const val SCRIBING_TOOLS_PATH = "scribing_tools"
    const val ARCANE_TOME_PATH = "arcane_tome"
    const val ALL_SEEING_EYE_PATH = "all_seeing_eye"
    const val CODEX_OF_SIGHT_PATH = "codex_of_sight"

    val INK_VIAL_IDENTIFIER = Identifier(MOD_ID, INK_VIAL_PATH)
    val SCRIBING_TOOLS_IDENTIFIER = Identifier(MOD_ID, SCRIBING_TOOLS_PATH)
    val ARCANE_TOME_IDENTIFIER = Identifier(MOD_ID, ARCANE_TOME_PATH)
    val ALL_SEEING_EYE_IDENTIFIER = Identifier(MOD_ID, ALL_SEEING_EYE_PATH)
    val CODEX_OF_SIGHT_IDENTIFIER = Identifier(MOD_ID, CODEX_OF_SIGHT_PATH)

    // Texture paths
    const val BOOKCASE_GUI_TEXTURE_PATH = "textures/gui/bookcase-with-inventory.png"
    const val TOME_GUI_TEXTURE_PATH = "textures/gui/tome-with-inventory.png"
    const val TOME_GUI_BUTTONS_TEXTURE_PATH = "textures/gui/page-arrows.png"

    // Networking

    const val TOME_PAGE_PACKET_NAME = "tome_page_packet"

    val TOME_PAGE_PACKET = Identifier(MOD_ID, TOME_PAGE_PACKET_NAME)
}
