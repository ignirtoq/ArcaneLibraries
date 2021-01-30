package net.arcanumverum.arcanelibraries.screens

import net.arcanumverum.arcanelibraries.Screens
import net.arcanumverum.arcanelibraries.items.ArcaneTomeItem
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text


fun getTome(player: PlayerEntity): ItemStack {
    val mainHand = player.mainHandStack
    return if (mainHand.item is ArcaneTomeItem) mainHand else player.offHandStack
}


class ArcaneTomeScreenHandlerFactory(private val tome: ItemStack) : ExtendedScreenHandlerFactory {
    var pageDataId: String? = null

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val handler = ArcaneTomeScreenHandler(syncId, inv, pageDataId, tome)
        pageDataId = handler.pageDataId
        return handler
    }

    override fun getDisplayName(): Text = tome.name

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        writePageDataToBuf(pageDataId!!, buf)
    }
}


class ArcaneTomeScreenHandler(
    syncId: Int,
    inv: PlayerInventory,
    pageDataId: String?,
    tome: ItemStack
) : TomeScreenHandler<ArcaneTomeScreenHandler, ScreenHandlerType<ArcaneTomeScreenHandler>>(
    Screens.ARCANE_TOME_SCREEN_HANDLER, syncId, inv, pageDataId, tome
) {
    constructor (
        sync_id: Int,
        inv: PlayerInventory,
        buf: PacketByteBuf
    ) : this(sync_id, inv, readPageDataFromBuf(buf), getTome(inv.player)) {

    }
}


class ArcaneTomeScreen(handler: ArcaneTomeScreenHandler, inv: PlayerInventory, title: Text)
: TomeScreen<ArcaneTomeScreenHandler, ScreenHandlerType<ArcaneTomeScreenHandler>, ArcaneTomeScreenHandler>(handler, inv, title)