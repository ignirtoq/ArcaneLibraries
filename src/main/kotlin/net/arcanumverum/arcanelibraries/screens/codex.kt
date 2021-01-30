package net.arcanumverum.arcanelibraries.screens

import net.arcanumverum.arcanelibraries.Screens
import net.arcanumverum.arcanelibraries.blocks.BookcaseEntity
import net.arcanumverum.arcanelibraries.items.ArcaneTomeItem
import net.arcanumverum.arcanelibraries.items.TomeTier
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.BlockPos


fun getTomes(playerInv: PlayerInventory, pos: BlockPos): List<ItemStack> {
    val blockEntity = playerInv.player.world.getBlockEntity(pos)
    return if (blockEntity is BookcaseEntity) {
        blockEntity.getTomesInside()
    } else {
        List(0) {ItemStack.EMPTY}
    }
}


fun writeTiers(buf: PacketByteBuf, tomes: List<ItemStack>) {
    buf.writeInt(tomes.size)
    for (tome in tomes) {
        buf.writeInt(when(ArcaneTomeItem.getTier(tome)) {
            TomeTier.one -> 1
            TomeTier.two -> 2
            TomeTier.three -> 3
        })
    }
}


fun buildTomes(buf: PacketByteBuf): Array<ItemStack> {
    return Array(buf.readInt()) {
        ArcaneTomeItem.fromTier(TomeTier.fromInt(buf.readInt()))
    }
}


class CodexScreenHandlerFactory(private val bookcase: BookcaseEntity) : ExtendedScreenHandlerFactory {
    var pageDataId: String? = null

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity?): ScreenHandler {
        val handler = CodexScreenHandler(syncId, inv, pageDataId, *bookcase.getTomesInside().toTypedArray())
        pageDataId = handler.pageDataId
        return handler
    }

    override fun getDisplayName(): Text {
        return TranslatableText("block.arcane_libraries.bookcase")
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        writePageDataToBuf(pageDataId!!, buf)
        writeTiers(buf, getTomes(player.inventory, bookcase.pos))
    }
}


class CodexScreenHandler(
    syncId: Int,
    inv: PlayerInventory,
    pageDataId: String?,
    vararg tomes: ItemStack
) : TomeScreenHandler<CodexScreenHandler, ScreenHandlerType<CodexScreenHandler>>(
    Screens.CODEX_SCREEN_HANDLER!!, syncId, inv, pageDataId, *tomes
) {
    constructor (
        sync_id: Int, inv: PlayerInventory, buf: PacketByteBuf
    ) : this(
        sync_id, inv, readPageDataFromBuf(buf), *buildTomes(buf)
    )
}


class CodexScreen(handler: CodexScreenHandler, inv: PlayerInventory, title: Text)
: TomeScreen<CodexScreenHandler, ScreenHandlerType<CodexScreenHandler>, CodexScreenHandler>(handler, inv, title)