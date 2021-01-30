package net.arcanumverum.arcanelibraries.screens

import com.mojang.blaze3d.systems.RenderSystem

import net.arcanumverum.arcanelibraries.Constants
import net.arcanumverum.arcanelibraries.inventories.TomeInventory
import net.arcanumverum.arcanelibraries.network.sendPageData
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.TexturedButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.Items
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.lang.ref.WeakReference
import java.util.*


val TOME_TEXTURE = Identifier(Constants.MOD_ID, Constants.TOME_GUI_TEXTURE_PATH)
const val TOME_GUI_TEXTURE_WIDTH = 237
const val TOME_GUI_TEXTURE_HEIGHT = 256

val TOME_FIRST_LEFT_SLOT = Pair(32, 22)
val TOME_FIRST_RIGHT_SLOT = Pair(196, 22)
val TOME_FIRST_PLAYER_SLOT = Pair(40, 174)
val TOME_FIRST_HOTBAR_SLOT = Pair(40, 232)
const val TOME_SLOTS_PER_COLUMN = 7
const val TOME_COLUMNS = 2
const val TOME_SLOTS = TOME_SLOTS_PER_COLUMN * TOME_COLUMNS

const val BUTTON_X_SIZE = 20
const val BUTTON_Y_SIZE = 13
const val BUTTON_TEXTURE_LEFT_X_POS = 0
const val BUTTON_TEXTURE_RIGHT_X_POS = 21
const val BUTTON_TEXTURE_GRAY_Y_POS = 0
val BUTTON_TEXTURE = Identifier(Constants.MOD_ID, Constants.TOME_GUI_BUTTONS_TEXTURE_PATH)
val LEFT_BUTTON = Pair(31, 149)
val RIGHT_BUTTON = Pair(193, 149)


object ActiveData {
    private val data = HashMap<UUID, Data>()

    fun addTome(handler: PaginatedTomeScreen, id: String? = null, page: Int = 0): String {
        val tomeId = getNewId(id)
        data[tomeId] = Data(WeakReference(handler), page)
        return tomeId.toString()
    }

    fun getPage(id: String): Int = data[UUID.fromString(id)]!!.page

    fun setPage(id: String, page: Int) {
        val uuid = UUID.fromString(id)
        if (uuid !in data) return
        val tomeData = data[uuid]!!

        tomeData.page = page
        tomeData.handler.get()?.updateTomeSlots()
    }

    private fun getNewId(id: String?): UUID {
        if (id != null) return UUID.fromString(id)

        do {
            val tomeId = UUID.randomUUID()
            if (tomeId !in data) return tomeId
        } while (true)
    }

    private class Data(val handler: WeakReference<PaginatedTomeScreen>, var page: Int = 0)

    interface PaginatedTomeScreen {
        fun updateTomeSlots()
    }
}


fun readPageDataFromBuf(buf: PacketByteBuf): String = buf.readString()


fun writePageDataToBuf(pageDataId: String, buf: PacketByteBuf) {
    buf.writeString(pageDataId)
}


open class TomeScreenHandler<R : ScreenHandler, T : ScreenHandlerType<R>>(
    handlerType: T,
    syncId: Int,
    inv: PlayerInventory,
    pageDataId: String?,
    vararg tomes: ItemStack
) : BaseScreenHandler<R, T>(handlerType, syncId), ActiveData.PaginatedTomeScreen {
    private val tomeInventory = TomeInventory(*tomes)
    val pageDataId = ActiveData.addTome(this, pageDataId)
    private var world = inv.player.world
    private var page: Int
        get() = ActiveData.getPage(pageDataId)
        set(value) = ActiveData.setPage(pageDataId, value)

    init {
        addTomeSlots()
        addPlayerInventorySlots(inv, TOME_FIRST_PLAYER_SLOT.first, TOME_FIRST_PLAYER_SLOT.second)
        addPlayerHotbarSlots(inv, TOME_FIRST_HOTBAR_SLOT.first, TOME_FIRST_HOTBAR_SLOT.second)
    }

    override fun close(player: PlayerEntity) {
        tomeInventory.onClose(player)
        player.inventory.markDirty()
        super.close(player)
    }

    private fun addTomeSlots() {
        loopOverSlots { slotIndex, x, y -> addSlot(createSlot(slotIndex, x, y)) }
    }

    override fun updateTomeSlots() {
        val startIndex = page * TOME_SLOTS
        loopOverSlots { slotIndex, x, y ->
            val newSlot = createSlot(slotIndex + startIndex, x, y)
            newSlot.id = slotIndex
            slots[slotIndex] = newSlot
        }
    }

    fun nextPage() {
        if (!hasMorePages()) return
        page += 1
        if (world.isClient) sendPageData(pageDataId, page)
    }

    fun previousPage() {
        if (onFirstPage()) return
        page -= 1
        if (world.isClient) sendPageData(pageDataId, page)
    }

    fun hasMorePages(): Boolean = (page + 1) * TOME_SLOTS < tomeInventory.size()

    fun onFirstPage(): Boolean = page == 0

    private fun createSlot(invIndex: Int, x: Int, y: Int): Slot {
        if (invIndex < tomeInventory.size()) return TomeSlot(tomeInventory, invIndex, x, y)
        return DisabledTomeSlot(x, y)
    }

    private fun loopOverSlots(onEach: (slotIndex: Int, x: Int, y: Int) -> Unit) {
        for (slotIndex in 0 until TOME_SLOTS) {
            val x = if (slotIndex < TOME_SLOTS_PER_COLUMN) TOME_FIRST_LEFT_SLOT.first else TOME_FIRST_RIGHT_SLOT.first
            val y = TOME_FIRST_LEFT_SLOT.second + SLOT_HEIGHT * (slotIndex % TOME_SLOTS_PER_COLUMN)
            onEach(slotIndex, x, y)
        }
    }

    override fun getContainerInventory(): Inventory = tomeInventory

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


open class TomeScreen<R: ScreenHandler, S: ScreenHandlerType<R>, T : TomeScreenHandler<R, S>>(
    handler: T, inv: PlayerInventory, title: Text
)
: HandledScreen<T>(handler, inv, title) {

    override fun init() {
        backgroundWidth = TOME_GUI_TEXTURE_WIDTH
        backgroundHeight = TOME_GUI_TEXTURE_HEIGHT
        super.init()
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2
        buttons.add(addButton(newLeftButton()))
        buttons.add(addButton(newRightButton()))
    }

    override fun getScreenHandler(): T = handler

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F)
        client!!.textureManager.bindTexture(TOME_TEXTURE)
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

    fun newLeftButton(): LeftButton {
        return LeftButton(x + LEFT_BUTTON.first, y + LEFT_BUTTON.second) { handler.previousPage() }
    }

    fun newRightButton(): RightButton {
        return RightButton(x + RIGHT_BUTTON.first, y + RIGHT_BUTTON.second) { handler.nextPage() }
    }
}


class TomeSlot(private val inv: TomeInventory, private val index: Int, x: Int, y: Int) : BaseSlot(inv, index, x, y) {
    override fun getMaxItemCount() = inv.getMaxCount(index)
    override fun getMaxItemCount(stack: ItemStack): Int = this.maxItemCount
}


class DisabledTomeSlot(x: Int, y: Int) : BaseSlot(null, 0, x, y) {
    companion object {
        val BARRIER = ItemStack(Items.BARRIER, 1)
    }
    override fun canInsert(stack: ItemStack?): Boolean = false
    override fun canTakeItems(playerEntity: PlayerEntity?): Boolean = false
    override fun doDrawHoveringEffect(): Boolean = true
    override fun getMaxItemCount(): Int = 0
    override fun getMaxItemCount(stack: ItemStack?): Int = 0
    override fun getStack(): ItemStack = BARRIER
    override fun hasStack(): Boolean = true
    override fun markDirty() = Unit
    override fun setStack(stack: ItemStack?) = Unit
    override fun takeStack(amount: Int): ItemStack = ItemStack.EMPTY
}


abstract class PageButton(xPos: Int, yPos: Int, u: Int, action: PressAction)
: TexturedButtonWidget(
    xPos, yPos,  // x, y position on the screen
    BUTTON_X_SIZE, BUTTON_Y_SIZE,  // size of the button in the texture and on the screen
    u, BUTTON_TEXTURE_GRAY_Y_POS,  // position (in pixels) of the button's upper left pixel in the texture
    BUTTON_Y_SIZE,  // v (vertical) offset to add to texture position on hover
    BUTTON_TEXTURE,  // texture location identifier
    action  // function to call on click
)


class LeftButton(xPos: Int, yPos: Int, action: PressAction) : PageButton(xPos, yPos, BUTTON_TEXTURE_LEFT_X_POS, action)


class RightButton(xPos: Int, yPos: Int, action: PressAction) : PageButton(xPos, yPos, BUTTON_TEXTURE_RIGHT_X_POS, action)
