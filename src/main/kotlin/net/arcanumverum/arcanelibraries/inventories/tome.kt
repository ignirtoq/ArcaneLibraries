package net.arcanumverum.arcanelibraries.inventories

import net.arcanumverum.arcanelibraries.items.ArcaneTomeItem
import net.arcanumverum.arcanelibraries.items.TomeTier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList


class TomeInventory(vararg tomeArray: ItemStack) : Inventory {
    private val tomes = tomeArray.toList()
    private val itemStacks = DefaultedList.copyOf(ItemStack.EMPTY, *ArcaneTomeItem.deserialize(tomes).toTypedArray())
    private val stackSources = genSourceList(tomes)

    override fun onClose(player: PlayerEntity) {
        ArcaneTomeItem.serialize(tomes, itemStacks)
    }

    override fun canPlayerUse(player: PlayerEntity): Boolean = tomes.map {it.item is ArcaneTomeItem}.all {it}
    override fun clear() = itemStacks.clear()
    override fun getMaxCountPerStack(): Int = TomeTier.one.maxStackSize
    override fun getStack(slot: Int): ItemStack = itemStacks[slot]
    override fun isEmpty(): Boolean = itemStacks.isEmpty()
    override fun markDirty() = Unit
    override fun removeStack(slot: Int, amount: Int): ItemStack = Inventories.splitStack(itemStacks, slot, amount)
    override fun removeStack(slot: Int): ItemStack = Inventories.removeStack(itemStacks, slot)

    override fun setStack(slot: Int, stack: ItemStack) {
        itemStacks[slot] = stack
    }

    override fun size(): Int = itemStacks.size

    fun getMaxCount(slot: Int): Int = stackSources[slot].tier.maxStackSize
}

class MultiInventoryStackSource(val tome: ItemStack) {
    val tier: TomeTier = ArcaneTomeItem.getTier(tome)
}

fun genSourceList(tomes: List<ItemStack>): List<MultiInventoryStackSource> {
    if (tomes.isEmpty()) return List(0) { MultiInventoryStackSource(tomes[it]) }

    val tiers = tomes.mapNotNull {tome -> if (tome.item is ArcaneTomeItem) ArcaneTomeItem.getTier(tome) else null}
    val fullSize = tiers.map {tier -> tier.size}.sum()
    val sources = MutableList(fullSize) {MultiInventoryStackSource(tomes[0])}
    var sourceInd = 0
    for (ind in tomes.indices) {
        val source = MultiInventoryStackSource(tomes[ind])
        sources.subList(sourceInd, sourceInd + source.tier.size).replaceAll { source }
        sourceInd += source.tier.size
    }

    return sources
}