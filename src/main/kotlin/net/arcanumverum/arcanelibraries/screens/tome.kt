package net.arcanumverum.arcanelibraries.screens.tome

import kotlin.math.min

import com.mojang.blaze3d.systems.RenderSystem

import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Identifier

import net.arcanumverum.arcanelibraries.Constants
import net.arcanumverum.arcanelibraries.Screens
import net.arcanumverum.arcanelibraries.inventories.TomeInventory
import net.arcanumverum.arcanelibraries.items.BaseTomeItem
import net.arcanumverum.arcanelibraries.screens.BaseScreenHandler


val TEXTURE = Identifier(Constants.MOD_ID, Constants.TOME_GUI_TEXTURE_PATH)
const val TOME_GUI_TEXTURE_WIDTH = 237
const val TOME_GUI_TEXTURE_HEIGHT = 256

val TOME_FIRST_LEFT_SLOT = Pair(32, 22)
val TOME_FIRST_RIGHT_SLOT = Pair(196, 22)
val TOME_FIRST_PLAYER_SLOT = Pair(40, 174)
val TOME_FIRST_HOTBAR_SLOT = Pair(40, 232)
const val TOME_SLOTS_PER_COLUMN = 7

const val SLOT_HEIGHT = 18
const val SLOT_WIDTH = 18

fun getTome(player: PlayerEntity): ItemStack {
    val mainHand = player.mainHandStack
    val offHand = player.offHandStack
    if (mainHand.item is BaseTomeItem) return mainHand
    return offHand
}


class TomeScreenHandlerFactory(private val tome: ItemStack) : NamedScreenHandlerFactory {
    override fun createMenu(sync_id: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return TomeScreenHandler(sync_id, inv, tome)
    }

    override fun getDisplayName(): Text = tome.name
}


class TomeScreenHandler(sync_id: Int, inv: PlayerInventory, tome: ItemStack)
: BaseScreenHandler<TomeScreenHandler>(Screens.TOME_SCREEN_HANDLER, sync_id) {
    private val tomeInventory = TomeInventory(tome)

    constructor (sync_id: Int, inv: PlayerInventory) : this(sync_id, inv, getTome(inv.player))

    init {
        addTomeSlots()
        addPlayerInventorySlots(inv)
        addPlayerHotbarSlots(inv)
    }

    override fun canUse(player: PlayerEntity): Boolean = true

    override fun close(player: PlayerEntity) {
        tomeInventory.onClose(player)
        player.inventory.markDirty()
        super.close(player)
    }

    private fun addTomeSlots() {
        val numSlots = tomeInventory.size()
        val oneColumn = numSlots <= TOME_SLOTS_PER_COLUMN

        for (slot in 0 until min(numSlots, TOME_SLOTS_PER_COLUMN)) {
            addSlot(TomeSlot(
                tomeInventory, slot,
                TOME_FIRST_LEFT_SLOT.first, TOME_FIRST_LEFT_SLOT.second + SLOT_HEIGHT * slot))
        }

        if (oneColumn) return;

        for (slot in TOME_SLOTS_PER_COLUMN until numSlots) {
            addSlot(TomeSlot(
                tomeInventory, slot,
                TOME_FIRST_RIGHT_SLOT.first, TOME_FIRST_RIGHT_SLOT.second + SLOT_HEIGHT * slot))
        }
    }

    private fun addPlayerInventorySlots(inv: PlayerInventory) {
        val FIRST_SLOT_X = TOME_FIRST_PLAYER_SLOT.first
        val FIRST_SLOT_Y = TOME_FIRST_PLAYER_SLOT.second
        for (row: Int in 0 until 3) {
            for (col: Int in 0 until 9) {
                addSlot(Slot(inv, col + row*9 + 9, FIRST_SLOT_X + col*SLOT_WIDTH, FIRST_SLOT_Y + row*SLOT_HEIGHT))
            }
        }
    }

    private fun addPlayerHotbarSlots(inv: PlayerInventory) {
        val FIRST_SLOT_X = TOME_FIRST_HOTBAR_SLOT.first
        val FIRST_SLOT_Y = TOME_FIRST_HOTBAR_SLOT.second
        for (col: Int in 0 until 9) {
            addSlot(Slot(inv, col, FIRST_SLOT_X + col*SLOT_WIDTH, FIRST_SLOT_Y))
        }
    }

    override fun transferSlot(player: PlayerEntity, invSlot: Int): ItemStack {
        var newStack = ItemStack.EMPTY;
        val slot = this.slots[invSlot];
        if (slot != null && slot.hasStack()) {
            val originalStack = slot.stack;
            newStack = originalStack.copy();
            if (invSlot < tomeInventory.size()) {
                if (!this.insertItem(originalStack, tomeInventory.size(), slots.size, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, tomeInventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty) {
                slot.stack = ItemStack.EMPTY;
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    override fun insertItem(insertingStack: ItemStack, startIndex: Int, endIndex: Int, fromLast: Boolean): Boolean {
        var bl = false
        var i = startIndex
        if (fromLast) {
            i = endIndex - 1
        }
        var slot: Slot
        var slotStack: ItemStack
        if (insertingStack.isStackable) {
            while (!insertingStack.isEmpty) {
                if (fromLast) {
                    if (i < startIndex) {
                        break
                    }
                } else if (i >= endIndex) {
                    break
                }
                slot = slots[i] as Slot
                slotStack = slot.stack
                if (!slotStack.isEmpty && canStacksCombine(insertingStack, slotStack)) {
                    val j = slotStack.count + insertingStack.count
                    if (j <= slot.maxItemCount) {
                        insertingStack.count = 0
                        slotStack.count = j
                        slot.markDirty()
                        bl = true
                    } else if (slotStack.count < slot.maxItemCount) {
                        insertingStack.decrement(slot.maxItemCount - slotStack.count)
                        slotStack.count = slot.maxItemCount
                        slot.markDirty()
                        bl = true
                    }
                }
                if (fromLast) {
                    --i
                } else {
                    ++i
                }
            }
        }
        if (!insertingStack.isEmpty) {
            i = if (fromLast) {
                endIndex - 1
            } else {
                startIndex
            }
            while (true) {
                if (fromLast) {
                    if (i < startIndex) {
                        break
                    }
                } else if (i >= endIndex) {
                    break
                }
                slot = slots[i] as Slot
                slotStack = slot.stack
                if (slotStack.isEmpty && slot.canInsert(insertingStack)) {
                    if (insertingStack.count > slot.maxItemCount) {
                        slot.stack = insertingStack.split(slot.maxItemCount)
                    } else {
                        slot.stack = insertingStack.split(insertingStack.count)
                    }
                    slot.markDirty()
                    bl = true
                    break
                }
                if (fromLast) {
                    --i
                } else {
                    ++i
                }
            }
        }
        return bl
    }
}


class TomeScreen(handler: TomeScreenHandler, inv: PlayerInventory, title: Text)
    : HandledScreen<TomeScreenHandler>(handler, inv, title) {

    override fun init() {
        backgroundWidth = TOME_GUI_TEXTURE_WIDTH
        backgroundHeight = TOME_GUI_TEXTURE_HEIGHT
        super.init()
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2
    }

    override fun getScreenHandler(): TomeScreenHandler = handler

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F)
        client!!.textureManager.bindTexture(TEXTURE)
        val (i, j) = Pair((width - backgroundWidth) / 2, (height - backgroundHeight) / 2)
        drawTexture(matrices, i, j, 0, 0, backgroundWidth, backgroundHeight)
    }

    /* Override drawForeground to not render the player inventory title. */
    override fun drawForeground(matrices: MatrixStack?, mouseX: Int, mouseY: Int) {
        textRenderer.draw(matrices, title, titleX.toFloat(), titleY.toFloat(), 4210752)
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }
}


class TomeSlot(private val inv: TomeInventory, index: Int, x: Int, y: Int) : Slot(inv, index, x, y) {
    override fun getMaxItemCount() = inv.maxCountPerStack
    override fun getMaxItemCount(stack: ItemStack): Int = inv.maxCountPerStack
}
