package net.arcanumverum.arcanelibraries.screens.tome

import kotlin.math.min

import com.mojang.blaze3d.systems.RenderSystem

import net.minecraft.client.MinecraftClient
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


val TEXTURE = Identifier(Constants.MOD_ID, Constants.TOME_GUI_TEXTURE_PATH)
val TOME_GUI_TEXTURE_WIDTH = 237
val TOME_GUI_TEXTURE_HEIGHT = 256

val TOME_FIRST_LEFT_SLOT = Pair(32, 22)
val TOME_FIRST_RIGHT_SLOT = Pair(196, 22)
val TOME_FIRST_PLAYER_SLOT = Pair(40, 174)
val TOME_FIRST_HOTBAR_SLOT = Pair(40, 232)
const val TOME_SLOTS_PER_COLUMN = 7

val SLOT_HEIGHT = 18
val SLOT_WIDTH = 18

fun get_tome(player: PlayerEntity): ItemStack {
    val mainHand = player.getMainHandStack()
    val offHand = player.getOffHandStack()
    if (mainHand.getItem() is BaseTomeItem) {
        return mainHand
    }
    return offHand
}


class TomeScreenHandlerFactory(tome: ItemStack) : NamedScreenHandlerFactory {
    val tome = tome

    override fun createMenu(sync_id: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return TomeScreenHandler(sync_id, inv, tome)
    }

    override fun getDisplayName(): Text = tome.getName()
}


class TomeScreenHandler(sync_id: Int, inv: PlayerInventory, tome: ItemStack)
  : ScreenHandler(Screens.TOME_SCREEN_HANDLER, sync_id) {
    val tome_inventory = TomeInventory(tome)

    constructor (sync_id: Int, inv: PlayerInventory) : this(sync_id, inv, get_tome(inv.player))

    init {
        addTomeSlots()
        addPlayerInventorySlots(inv)
        addPlayerHotbarSlots(inv)
    }

    override fun canUse(player: PlayerEntity): Boolean = true

    override fun close(player: PlayerEntity) {
        tome_inventory.onClose(player)
        player.inventory.markDirty()
        super.close(player)
    }

    fun addTomeSlots() {
        val num_slots = tome_inventory.size()
        val one_column = num_slots <= TOME_SLOTS_PER_COLUMN

        for (slot in 0 until min(num_slots, TOME_SLOTS_PER_COLUMN)) {
            addSlot(TomeSlot(
                tome_inventory, slot,
                TOME_FIRST_LEFT_SLOT.first, TOME_FIRST_LEFT_SLOT.second + SLOT_HEIGHT * slot))
        }

        if (one_column) return;

        for (slot in TOME_SLOTS_PER_COLUMN until num_slots) {
            addSlot(TomeSlot(
                tome_inventory, slot,
                TOME_FIRST_RIGHT_SLOT.first, TOME_FIRST_RIGHT_SLOT.second + SLOT_HEIGHT * slot))
        }
    }

    fun addPlayerInventorySlots(inv: PlayerInventory) {
        val FIRST_SLOT_X = TOME_FIRST_PLAYER_SLOT.first
        val FIRST_SLOT_Y = TOME_FIRST_PLAYER_SLOT.second
        for (row: Int in 0 until 3) {
            for (col: Int in 0 until 9) {
                addSlot(Slot(inv, col + row*9 + 9, FIRST_SLOT_X + col*SLOT_WIDTH, FIRST_SLOT_Y + row*SLOT_HEIGHT))
            }
        }
    }

    fun addPlayerHotbarSlots(inv: PlayerInventory) {
        val FIRST_SLOT_X = TOME_FIRST_HOTBAR_SLOT.first
        val FIRST_SLOT_Y = TOME_FIRST_HOTBAR_SLOT.second
        for (col: Int in 0 until 9) {
            addSlot(Slot(inv, col, FIRST_SLOT_X + col*SLOT_WIDTH, FIRST_SLOT_Y))
        }
    }

    override fun transferSlot(player: PlayerEntity, invSlot: Int): ItemStack {
        var newStack = ItemStack.EMPTY;
        val slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            val originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < tome_inventory.size()) {
                if (!this.insertItem(originalStack, tome_inventory.size(), slots.size, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, tome_inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
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
        client!!.getTextureManager().bindTexture(TEXTURE)
        val (i, j) = Pair((width - backgroundWidth) / 2, (height - backgroundHeight) / 2)
        drawTexture(matrices, i, j, 0, 0, backgroundWidth, backgroundHeight)
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }
}


class TomeSlot(val inv: TomeInventory, index: Int, x: Int, y: Int) : Slot(inv, index, x, y) {
    override fun getMaxItemCount() = inv.getMaxCountPerStack()
    override fun getMaxItemCount(stack: ItemStack) = inv.getMaxCountPerStack()
}
