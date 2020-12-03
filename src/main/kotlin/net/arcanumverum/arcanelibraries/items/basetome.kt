package net.arcanumverum.arcanelibraries.items

import net.minecraft.item.Item


abstract class BaseTomeItem(settings: Settings) : Item(settings) {
    abstract fun size(): Int
}
