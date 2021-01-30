package net.arcanumverum.arcanelibraries.screens

import net.minecraft.inventory.Inventory
import net.minecraft.screen.slot.Slot

open class BaseSlot(inv: Inventory?, index: Int, x: Int, y: Int) : Slot(inv, index, x, y) {
    fun publicOnTake(amount: Int) = super.onTake(amount)
}
