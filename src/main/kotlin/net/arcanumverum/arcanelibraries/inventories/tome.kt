package net.arcanumverum.arcanelibraries.inventories

import net.minecraft.nbt.CompoundTag
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList

import net.arcanumverum.arcanelibraries.items.BaseTomeItem
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory


const val NBT_TAG_INVENTORY = "arcanelibraries_inventory"


class TomeInventory(private val tome: ItemStack, private val max_stack_size: Int = 3 * 7 * 7) : Inventory {
    private val itemStacks = deserialize(
        tome.getOrCreateTag().getCompound(NBT_TAG_INVENTORY), tome.item as BaseTomeItem)

    override fun onClose(player: PlayerEntity) {
        tome.getOrCreateTag().put(NBT_TAG_INVENTORY, serialize(itemStacks))
    }

    override fun canPlayerUse(player: PlayerEntity): Boolean = tome.item is BaseTomeItem
    override fun clear() = itemStacks.clear()
    override fun getMaxCountPerStack(): Int = max_stack_size
    override fun getStack(slot: Int): ItemStack = itemStacks[slot]
    override fun isEmpty(): Boolean = itemStacks.isEmpty()
    override fun markDirty() = Unit
    override fun removeStack(slot: Int, amount: Int): ItemStack = Inventories.splitStack(itemStacks, slot, amount)
    override fun removeStack(slot: Int): ItemStack = Inventories.removeStack(itemStacks, slot)

    override fun setStack(slot: Int, stack: ItemStack) {
        itemStacks[slot] = stack
    }

    override fun size(): Int = (tome.item as BaseTomeItem).size()

}


fun serialize(itemStacks: DefaultedList<ItemStack>): CompoundTag {
    val tag = CompoundTag()
    for (slot in 0 until itemStacks.size) {
        tag.put("slot${slot}", itemStacks[slot].toTag(CompoundTag()))
    }
    return tag
}


fun deserialize(tag: CompoundTag, tome_item: BaseTomeItem): DefaultedList<ItemStack> {
    val itemStacks = DefaultedList.ofSize(tome_item.size(), ItemStack.EMPTY)
    for (slot in 0 until itemStacks.size) {
        itemStacks[slot] = ItemStack.fromTag(tag.getCompound("slot${slot}"))
    }
    return itemStacks
}
