package net.arcanumverum.arcanelibraries.inventories

import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList

const val BOOKCASE_SIZE = 7

class BookcaseInventory : BlockInventory {
    private val inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(BOOKCASE_SIZE, ItemStack.EMPTY)

    override fun getItems(): DefaultedList<ItemStack> = inventory
    override fun getMaxCountPerStack(): Int = 1
}