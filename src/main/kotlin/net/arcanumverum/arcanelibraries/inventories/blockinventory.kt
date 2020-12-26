package net.arcanumverum.arcanelibraries.inventories

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList

interface BlockInventory : Inventory {
    fun getItems(): DefaultedList<ItemStack>
    override fun canPlayerUse(player: PlayerEntity): Boolean = true
    override fun clear() = getItems().clear()
    override fun getStack(slot: Int): ItemStack = getItems()[slot]
    override fun isEmpty(): Boolean = getItems().isEmpty()
    override fun markDirty() = Unit
    override fun removeStack(slot: Int, amount: Int): ItemStack = Inventories.splitStack(getItems(), slot, amount)
    override fun removeStack(slot: Int): ItemStack = Inventories.removeStack(getItems(), slot)
    override fun size(): Int = getItems().size

    override fun setStack(slot: Int, stack: ItemStack) {
        getItems()[slot] = stack
    }
}