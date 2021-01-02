package net.arcanumverum.arcanelibraries.screens

import com.mojang.blaze3d.systems.RenderSystem
import net.arcanumverum.arcanelibraries.Constants
import net.arcanumverum.arcanelibraries.Screens
import net.arcanumverum.arcanelibraries.blocks.BookcaseEntity
import net.arcanumverum.arcanelibraries.inventories.BookcaseInventory
import net.arcanumverum.arcanelibraries.items.ArcaneTomeItem
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

val TEXTURE = Identifier(Constants.MOD_ID, Constants.BOOKCASE_GUI_TEXTURE_PATH)
const val BOOKCASE_GUI_TEXTURE_WIDTH = 175
const val BOOKCASE_GUI_TEXTURE_HEIGHT = 139

val BOOKCASE_FIRST_SLOT = Pair(26, 22)
val BOOKCASE_FIRST_PLAYER_SLOT = Pair(8, 58)
val BOOKCASE_FIRST_HOTBAR_SLOT = Pair(8, 116)

class BookcaseScreenHandlerFactory(private val block: BookcaseEntity) : NamedScreenHandlerFactory {
    override fun createMenu(syncId: Int, inv: PlayerInventory?, player: PlayerEntity?): ScreenHandler? {
        return BookcaseScreenHandler(syncId, inv, block.inventory)
    }

    override fun getDisplayName(): Text = TranslatableText(block.cachedState.block.translationKey)
}

class BookcaseScreenHandler(syncId: Int, playerInventory: PlayerInventory?, private val inventory: BookcaseInventory)
: BaseScreenHandler<BookcaseScreenHandler>(Screens.BOOKCASE_SCREEN_HANDLER, syncId) {
    constructor(syncId: Int, playerInventory: PlayerInventory?) : this(syncId, playerInventory, BookcaseInventory())

    init {
        addBookcaseSlots()
        addPlayerInventorySlots(playerInventory!!, BOOKCASE_FIRST_PLAYER_SLOT.first, BOOKCASE_FIRST_PLAYER_SLOT.second)
        addPlayerHotbarSlots(playerInventory, BOOKCASE_FIRST_HOTBAR_SLOT.first, BOOKCASE_FIRST_HOTBAR_SLOT.second)
    }

    private fun addBookcaseSlots() {
        val numSlots = inventory.size()

        for (slot in 0 until numSlots) {
            addSlot(BookcaseSlot(
                inventory, slot, BOOKCASE_FIRST_SLOT.first + SLOT_WIDTH * slot, BOOKCASE_FIRST_SLOT.second))
        }
    }

    override fun getContainerInventory(): Inventory = inventory
}

class BookcaseScreen(handler: BookcaseScreenHandler, inv: PlayerInventory, title: Text)
: HandledScreen<BookcaseScreenHandler>(handler, inv, title) {
    override fun init() {
        backgroundWidth = BOOKCASE_GUI_TEXTURE_WIDTH
        backgroundHeight = BOOKCASE_GUI_TEXTURE_HEIGHT
        super.init()
        playerInventoryTitleY = backgroundHeight - 94
    }

    override fun getScreenHandler(): BookcaseScreenHandler = handler

    override fun drawBackground(matrices: MatrixStack?, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F)
        client!!.textureManager.bindTexture(TEXTURE)
        val (i, j) = Pair((width - backgroundWidth) / 2, (height - backgroundHeight) / 2)
        drawTexture(matrices, i, j, 0, 0, backgroundWidth, backgroundHeight)
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }
}

class BookcaseSlot(inv: Inventory, index: Int, x: Int, y: Int) : Slot(inv, index, x, y) {
    override fun canInsert(stack: ItemStack): Boolean = stack.item is ArcaneTomeItem
}