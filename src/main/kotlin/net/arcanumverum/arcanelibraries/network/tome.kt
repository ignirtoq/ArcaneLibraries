package net.arcanumverum.arcanelibraries.network

import net.arcanumverum.arcanelibraries.Constants
import net.arcanumverum.arcanelibraries.screens.ActiveData
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity

const val READ_STRING_SIZE = 32767


fun sendPageData(pageDataId: String, page: Int) {
    val buf = PacketByteBufs.create()
    buf.writeString(pageDataId)
    buf.writeInt(page)
    ClientPlayNetworking.send(Constants.TOME_PAGE_PACKET, buf)
}


fun receivePageData(
    server: MinecraftServer,
    player: ServerPlayerEntity,
    networkHandler: ServerPlayNetworkHandler,
    buf: PacketByteBuf,
    sender: PacketSender,
) {
    val pageDataId = buf.readString(READ_STRING_SIZE)
    val page = buf.readInt()
    server.execute {
        ActiveData.setPage(pageDataId, page)
    }
}


fun initTomePageDataNetworking() {
    ServerPlayNetworking.registerGlobalReceiver(Constants.TOME_PAGE_PACKET, ::receivePageData)
}