package net.arcanumverum.arcanelibraries

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

// For support join https://discord.gg/v6v4pMv

@Suppress("unused")
fun init() {
    // This code runs as soon as Minecraft is in a mod-load-ready state.
    // However, some things (like resources) may still be uninitialized.
    // Proceed with mild caution.

    println("Hello Fabric world!")
    arcanelibraries().onInitialize()
}

class arcanelibraries : ModInitializer {

    val ARCANE_BOOK = Item(FabricItemSettings().group(ItemGroup.MISC));

    override fun onInitialize() {
        Registry.register(Registry.ITEM, Identifier("arcane-libraries", "fabric_item"), ARCANE_BOOK);
    }
}