package net.arcanumverum.arcanelibraries.items

import net.minecraft.item.Item

class ScribingToolsItem(settings: Settings) : Item(settings) {
    init {
        this.recipeRemainder = this
    }
}
