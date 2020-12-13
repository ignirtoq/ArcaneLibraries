package net.arcanumverum.arcanelibraries.inventories

import net.minecraft.nbt.CompoundTag
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList

import net.arcanumverum.arcanelibraries.items.BaseTomeItem


const val NBT_TAG_INVENTORY = "arcanelibraries_inventory"


class TomeInventory(private val tome: ItemStack, private val max_stack_size: Int = 3 * 7 * 7) : Inventory {
    val item_stacks = deserialize(tome.getOrCreateTag().getCompound(NBT_TAG_INVENTORY), tome.getItem() as BaseTomeItem)

    override fun onClose(player: PlayerEntity) {
        tome.getOrCreateTag().put(NBT_TAG_INVENTORY, serialize(item_stacks))
    }

    override fun canPlayerUse(player: PlayerEntity): Boolean = tome.getItem() is BaseTomeItem 
    override fun clear() = item_stacks.clear()
    override fun getMaxCountPerStack(): Int = max_stack_size
    override fun getStack(slot: Int): ItemStack = item_stacks.get(slot)
    override fun isEmpty(): Boolean = item_stacks.isEmpty()
    override fun markDirty() = Unit
    override fun removeStack(slot: Int, amount: Int): ItemStack = Inventories.splitStack(item_stacks, slot, amount)
    override fun removeStack(slot: Int): ItemStack = Inventories.removeStack(item_stacks, slot)

    override fun setStack(slot: Int, stack: ItemStack): Unit {
        item_stacks.set(slot, stack)
    }

    override fun size(): Int = (tome.item as BaseTomeItem).size()

}


fun serialize(item_stacks: DefaultedList<ItemStack>): CompoundTag {
    val tag = CompoundTag()
    for (slot in 0 until item_stacks.size) {
        tag.put("slot$slot", item_stacks[slot].toTag(CompoundTag()))
    }
    return tag
}


fun deserialize(tag: CompoundTag, tome_item: BaseTomeItem): DefaultedList<ItemStack> {
    val item_stacks = DefaultedList.ofSize(tome_item.size(), ItemStack.EMPTY)
    for (slot in 0 until item_stacks.size) {
        item_stacks[slot] = ItemStack.fromTag(tag.getCompound("slot$slot"))
    }
    return item_stacks
}
