package net.arcanumverum.arcanelibraries.inventories

import net.arcanumverum.arcanelibraries.items.ArcaneTomeItem
import net.arcanumverum.arcanelibraries.items.TomeTier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack


class TomeInventory(private val tome: ItemStack) : Inventory {
    private val tomeItem: ArcaneTomeItem = tome.item as ArcaneTomeItem
    private val tomeTier: TomeTier = tomeItem.getTier(tome)
    private val itemStacks = tomeItem.deserialize(tome)

    override fun onClose(player: PlayerEntity) {
        tomeItem.serialize(tome, itemStacks)
    }

    override fun canPlayerUse(player: PlayerEntity): Boolean = tome.item is ArcaneTomeItem
    override fun clear() = itemStacks.clear()
    override fun getMaxCountPerStack(): Int = tomeTier.maxStackSize
    override fun getStack(slot: Int): ItemStack = itemStacks[slot]
    override fun isEmpty(): Boolean = itemStacks.isEmpty()
    override fun markDirty() = Unit
    override fun removeStack(slot: Int, amount: Int): ItemStack = Inventories.splitStack(itemStacks, slot, amount)
    override fun removeStack(slot: Int): ItemStack = Inventories.removeStack(itemStacks, slot)

    override fun setStack(slot: Int, stack: ItemStack) {
        itemStacks[slot] = stack
    }

    override fun size(): Int = tomeTier.size
}
