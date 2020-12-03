package net.arcanumverum.arcanelibraries.items

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

import net.arcanumverum.arcanelibraries.screens.tome.TomeScreenHandlerFactory


const val SLOT_AMOUNT = 3


class ArcaneTomeItem(settings: Settings) : BaseTomeItem(settings) {
    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        if (!world.isClient) {
            user.openHandledScreen(TomeScreenHandlerFactory(stack))
        }
        return TypedActionResult.success(stack)
    }

    override fun size(): Int = SLOT_AMOUNT
}
