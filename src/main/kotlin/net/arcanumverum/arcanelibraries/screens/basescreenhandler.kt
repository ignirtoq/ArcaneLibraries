package net.arcanumverum.arcanelibraries.screens

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType

interface PickUpAction {
    fun click(
        slot: Slot,
        clickType: Int,
        playerEntity: PlayerEntity?
    ): ItemStack {
        val playerInventory = playerEntity!!.inventory
        var itemStack = ItemStack.EMPTY
        val slotStack = slot.stack
        val cursorStack = playerInventory.cursorStack

        if (!slotStack.isEmpty) {
            itemStack = slotStack.copy()
        }

        if (slotStack.isEmpty) {
            clickSlotEmpty(slot, cursorStack, clickType, playerEntity)
        } else if (slot.canTakeItems(playerEntity)) {
            clickSlotItemCursorItemPlayerAccessible(slot, cursorStack, clickType, playerEntity)
        }
        slot.markDirty()

        return itemStack
    }

    fun clickSlotEmpty(
        slot: Slot,
        cursorStack: ItemStack,
        clickType: Int,
        playerEntity: PlayerEntity?,
    ) {
        if (cursorStack.isEmpty) return
        if (!slot.canInsert(cursorStack)) return

        var q = if (clickType == 0) cursorStack.count else 1

        if (q > slot.getMaxItemCount(cursorStack)) {
            q = slot.getMaxItemCount(cursorStack)
        }
        slot.stack = cursorStack.split(q)
    }

    fun clickSlotItemCursorEmpty(
        slot: Slot,
        clickType: Int,
        playerEntity: PlayerEntity?
    ) {
        val slotStack = slot.stack
        val playerInventory = playerEntity!!.inventory
        val fullCount = slotStack.count.coerceAtMost(slotStack.item.maxCount)
        val amount = if (clickType == 0) fullCount else (fullCount + 1) / 2

        playerInventory.cursorStack = slot.takeStack(amount)
        slot.onTakeItem(playerEntity, playerInventory.cursorStack)
    }

    fun clickSlotItemCursorItemPlayerAccessible(
        slot: Slot,
        cursorStack: ItemStack,
        clickType: Int,
        playerEntity: PlayerEntity?,
    ) {
        val playerInventory = playerEntity!!.inventory
        var slotStack = slot.stack

        if (cursorStack.isEmpty) {
            clickSlotItemCursorEmpty(slot, clickType, playerEntity)
        } else if (slot.canInsert(cursorStack)) {
            // Clicked slot has a stack, cursor has a stack, and stacks are compatible.
            if (ScreenHandler.canStacksCombine(slotStack, cursorStack)) {
                var q = if (clickType == 0) cursorStack.count else 1
                if (q > slot.getMaxItemCount(cursorStack) - slotStack.count) {
                    q = slot.getMaxItemCount(cursorStack) - slotStack.count
                }
                cursorStack.decrement(q)
                slotStack.increment(q)
            } else if (cursorStack.count <= slot.getMaxItemCount(cursorStack)) {
                slot.stack = cursorStack
                playerInventory.cursorStack = slotStack
            }
        } else if (cursorStack.maxCount > 1 && ScreenHandler.canStacksCombine(slotStack, cursorStack) && !slotStack.isEmpty) {
            val q = slotStack.count
            if (q + cursorStack.count <= cursorStack.maxCount) {
                cursorStack.increment(q)
                slotStack = slot.takeStack(q)
                if (slotStack.isEmpty) {
                    slot.stack = ItemStack.EMPTY
                }
                slot.onTakeItem(playerEntity, playerInventory.cursorStack)
            }
        }
    }
}

class DefaultPickUpAction : PickUpAction

interface SwapAction {
    fun click(
        slot: Slot,
        clickType: Int,
        playerEntity: PlayerEntity?
    ) {
        val playerInventory = playerEntity!!.inventory
        val playerStack = playerInventory.getStack(clickType)
        val slotStack = slot.stack

        if (playerStack.isEmpty) {
            clickWithEmptyStack(slot, clickType, playerEntity)
        } else if (slotStack.isEmpty) {
            clickEmptySlot(slot, clickType, playerEntity, playerStack)
        } else if (slot.canTakeItems(playerEntity) && slot.canInsert(playerStack)) {
            clickWithItemsPlayerAccessible(slot, clickType, playerEntity, playerStack)
        }
    }

    fun clickWithEmptyStack(
        slot: Slot,
        clickType: Int,
        playerEntity: PlayerEntity?,
    ) {
        val playerInventory = playerEntity!!.inventory
        val wrappedSlot = slot as WrapperSlot
        val slotStack = slot.stack

        if (slot.canTakeItems(playerEntity)) {
            playerInventory.setStack(clickType, slotStack)
            wrappedSlot.publicOnTake(slotStack.count)
            slot.stack = ItemStack.EMPTY
            slot.onTakeItem(playerEntity, slotStack)
        }
    }

    fun clickEmptySlot(
        slot: Slot,
        clickType: Int,
        playerEntity: PlayerEntity?,
        playerStack: ItemStack,
    ) {
        val playerInventory = playerEntity!!.inventory

        if (slot.canInsert(playerStack)) {
            val q = slot.getMaxItemCount(playerStack)
            if (playerStack.count > q) {
                slot.stack = playerStack.split(q)
            } else {
                slot.stack = playerStack
                playerInventory.setStack(clickType, ItemStack.EMPTY)
            }
        }
    }

    fun clickWithItemsPlayerAccessible(
        slot: Slot,
        clickType: Int,
        playerEntity: PlayerEntity?,
        playerStack: ItemStack,
    ) {
        val playerInventory = playerEntity!!.inventory
        val slotStack = slot.stack
        val q = slot.getMaxItemCount(playerStack)

        if (playerStack.count > q) {
            slot.stack = playerStack.split(q)
            slot.onTakeItem(playerEntity, slotStack)
            if (!playerInventory.insertStack(slotStack)) {
                playerEntity.dropItem(slotStack, true)
            }
        } else {
            slot.stack = playerStack
            playerInventory.setStack(clickType, slotStack)
            slot.onTakeItem(playerEntity, slotStack)
        }
    }
}

class DefaultSwapAction : SwapAction

abstract class BaseScreenHandler<T : ScreenHandler?>(
    screenHandlerType: ScreenHandlerType<T>?,
    syncId: Int,
    private val pickUpAction: PickUpAction = DefaultPickUpAction(),
    private val swapAction: SwapAction = DefaultSwapAction(),
) : ScreenHandler(screenHandlerType, syncId) {
    private var quickCraftButton: Int? = 0
    private var quickCraftStage: Int = -1
    private val quickCraftSlots = HashSet<Slot>()

    override fun onSlotClick(
        slotIndex: Int,
        clickType: Int,
        actionType: SlotActionType?,
        playerEntity: PlayerEntity?,
    ): ItemStack? {
        when {
            actionType == SlotActionType.QUICK_CRAFT -> {
                val k: Int? = quickCraftButton
                quickCraftButton = unpackQuickCraftButton(clickType)
                doQuickCraft(slotIndex, clickType, playerEntity, k)
            }
            quickCraftButton != 0 -> endQuickCraft()
            actionType == SlotActionType.PICKUP -> return doPickUp(slotIndex, clickType, playerEntity)
            actionType == SlotActionType.QUICK_MOVE -> return doQuickMove(slotIndex, clickType, playerEntity)
            actionType == SlotActionType.SWAP -> return doSwap(slotIndex, clickType, playerEntity)
            actionType == SlotActionType.CLONE -> return doClone(slotIndex, clickType, playerEntity)
            actionType == SlotActionType.THROW -> return doThrow(slotIndex, clickType, playerEntity)
            actionType == SlotActionType.PICKUP_ALL -> return doPickUpAll(slotIndex, clickType, playerEntity)
        }
        return ItemStack.EMPTY
    }

    protected fun doQuickCraft(
        slotIndex: Int,
        clickType: Int,
        playerEntity: PlayerEntity?,
        k: Int?,
    ) {
        val playerInventory = playerEntity!!.inventory

        if ((k != 1 || this.quickCraftButton != 2) && k != this.quickCraftButton) {
            endQuickCraft()
        } else if (playerInventory.cursorStack.isEmpty) {
            endQuickCraft()
        } else if (this.quickCraftButton == 0) {
            quickCraftStage = unpackQuickCraftButton(clickType)
            if (shouldQuickCraftContinue(quickCraftStage, playerEntity)) {
                this.quickCraftButton = 1
                quickCraftSlots.clear()
            } else {
                endQuickCraft()
            }
        } else if (this.quickCraftButton == 1) {
            val slot = slots[slotIndex]
            val cursorStack = playerInventory.cursorStack

            if (
                slot != null && canInsertItemIntoSlot(slot, cursorStack, true)
                && slot.canInsert(cursorStack) && (quickCraftStage == 2 || cursorStack.count > quickCraftSlots.size)
                && this.canInsertIntoSlot(slot)
            ) {
                quickCraftSlots.add(slot)
            }
        } else if (this.quickCraftButton == 2) {
            if (quickCraftSlots.isNotEmpty()) {
                val cursorStackCopy = playerInventory.cursorStack.copy()
                var l = playerInventory.cursorStack.count
                val quickCraftSlotIterator: Iterator<*> = quickCraftSlots.iterator()

                crazyLoop@ while (true) {
                    var slot: Slot?
                    var cursorStack: ItemStack
                    do {
                        do {
                            do {
                                do {
                                    if (!quickCraftSlotIterator.hasNext()) {
                                        cursorStackCopy.count = l
                                        playerInventory.cursorStack = cursorStackCopy
                                        break@crazyLoop
                                    }
                                    slot = quickCraftSlotIterator.next() as Slot?
                                    cursorStack = playerInventory.cursorStack
                                } while (slot == null)
                            } while (!canInsertItemIntoSlot(slot, cursorStack, true))
                        } while (!slot!!.canInsert(cursorStack))
                    } while (quickCraftStage != 2 && cursorStack.count < quickCraftSlots.size)

                    if (this.canInsertIntoSlot(slot)) {
                        val cursorStackExtraCopy: ItemStack = cursorStackCopy.copy()
                        val m = if (slot!!.hasStack()) slot.stack.count else 0

                        calculateStackSize(quickCraftSlots, quickCraftStage, cursorStackExtraCopy, m)

                        val n = Math.min(cursorStackExtraCopy.maxCount, slot.getMaxItemCount(cursorStackExtraCopy))

                        if (cursorStackExtraCopy.count > n) {
                            cursorStackExtraCopy.count = n
                        }
                        l -= cursorStackExtraCopy.count - m
                        slot.stack = cursorStackExtraCopy
                    }
                }
            }
            endQuickCraft()
        } else {
            endQuickCraft()
        }
    }

    override fun endQuickCraft() {
        quickCraftButton = 0
        quickCraftSlots.clear()
        super.endQuickCraft()
    }

    protected fun doSwap(slotIndex: Int, clickType: Int, playerEntity: PlayerEntity?): ItemStack {
        val playerInventory = playerEntity!!.inventory
        val slot = slots[slotIndex]
        val playerStack = playerInventory.getStack(clickType)
        val slotStack = slot.stack

        // Sanity check
        if (playerStack.isEmpty && slotStack.isEmpty) return ItemStack.EMPTY

        swapAction.click(slot, clickType, playerEntity)

        return ItemStack.EMPTY
    }

    protected fun doQuickMove(slotIndex: Int, clickType: Int, playerEntity: PlayerEntity?): ItemStack {
        var itemStack: ItemStack = ItemStack.EMPTY

        // I presume these are sanity checks; wth are i and j?
        if (!(clickType == 0 || clickType == 1)) return itemStack
        if (slotIndex == -999) return doClickOutside(slotIndex, clickType, playerEntity)
        if (slotIndex < 0) return itemStack

        val slot = slots[slotIndex]
        if (slot == null || !slot.canTakeItems(playerEntity)) {
            return itemStack
        }

        var playerSlotItemStack = transferSlot(playerEntity, slotIndex)
        while (!playerSlotItemStack.isEmpty && ItemStack.areItemsEqualIgnoreDamage(slot.stack, playerSlotItemStack)) {
            itemStack = playerSlotItemStack.copy()
            playerSlotItemStack = transferSlot(playerEntity, slotIndex)
        }
        return itemStack
    }

    protected fun doPickUp(slotIndex: Int, clickType: Int, playerEntity: PlayerEntity?): ItemStack {
        var itemStack = ItemStack.EMPTY

        if (!(clickType == 0 || clickType == 1)) return itemStack
        if (slotIndex == -999) return doClickOutside(slotIndex, clickType, playerEntity)
        if (slotIndex < 0) return itemStack

        val slot = slots[slotIndex]
        if (slot != null) {
            itemStack = pickUpAction.click(slot, clickType, playerEntity)
        }

        return itemStack
    }

    private fun doClickOutside(slotIndex: Int, clickType: Int, playerEntity: PlayerEntity?): ItemStack {
        val playerInventory = playerEntity!!.inventory

        if (!playerInventory.cursorStack.isEmpty) {
            if (clickType == 0) {
                playerEntity.dropItem(playerInventory.cursorStack, true)
                playerInventory.cursorStack = ItemStack.EMPTY
            }
            if (clickType == 1) {
                playerEntity.dropItem(playerInventory.cursorStack.split(1), true)
            }
        }

        return ItemStack.EMPTY
    }

    protected fun doClone(slotIndex: Int, clickType: Int, playerEntity: PlayerEntity?): ItemStack {
        val playerInventory = playerEntity!!.inventory

        val slot = slots[slotIndex]

        if (!(playerEntity.abilities.creativeMode && playerInventory.cursorStack.isEmpty && slotIndex >= 0)) return ItemStack.EMPTY

        if (slot != null && slot.hasStack()) {
            val copiedStack = slot.stack.copy()
            copiedStack.count = copiedStack.maxCount
            playerInventory.cursorStack = copiedStack
        }

        return ItemStack.EMPTY
    }

    protected fun doThrow(slotIndex: Int, clickType: Int, playerEntity: PlayerEntity?): ItemStack {
        if (!(playerEntity!!.inventory.cursorStack.isEmpty && slotIndex >= 0)) return ItemStack.EMPTY

        val slot = slots[slotIndex]
        if (slot != null && slot.hasStack() && slot.canTakeItems(playerEntity)) {
            val removedStack = slot.takeStack(if (clickType == 0) 1 else slot.stack.count)
            slot.onTakeItem(playerEntity, removedStack)
            playerEntity.dropItem(removedStack, true)
        }
        return ItemStack.EMPTY
    }

    protected fun doPickUpAll(slotIndex: Int, clickType: Int, playerEntity: PlayerEntity?): ItemStack {
        if (slotIndex < 0) return ItemStack.EMPTY

        val slot = slots[slotIndex]
        val cursorStack = playerEntity!!.inventory.cursorStack

        if (!cursorStack.isEmpty && (slot == null || !slot.hasStack() || !slot.canTakeItems(playerEntity))) {
            val l = if (clickType == 0) 0 else slots.size - 1
            val q = if (clickType == 0) 1 else -1
            for (w in 0..1) {
                var x: Int = l
                while (x >= 0 && x < slots.size && cursorStack.count < cursorStack.maxCount) {
                    val otherSlot = slots[x] as Slot
                    if (otherSlot.hasStack() && canInsertItemIntoSlot(otherSlot, cursorStack, true)
                        && otherSlot.canTakeItems(playerEntity) && this.canInsertIntoSlot(cursorStack, otherSlot)
                    ) {
                        val otherStack = otherSlot.stack
                        if (w != 0 || otherStack.count != otherStack.maxCount) {
                            val n = Math.min(cursorStack.maxCount - cursorStack.count, otherStack.count)
                            val removedStack = otherSlot.takeStack(n)
                            cursorStack.increment(n)
                            if (removedStack.isEmpty) {
                                otherSlot.stack = ItemStack.EMPTY
                            }
                            otherSlot.onTakeItem(playerEntity, removedStack)
                        }
                    }
                    x += q
                }
            }
        }

        sendContentUpdates()

        return ItemStack.EMPTY
    }
}


class WrapperSlot(inv: Inventory, index: Int, x: Int, y: Int) : Slot(inv, index, x, y) {
    fun publicOnTake(amount: Int) = super.onTake(amount)
}