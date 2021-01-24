package net.arcanumverum.arcanelibraries.blocks

import net.arcanumverum.arcanelibraries.BlockEntities
import net.arcanumverum.arcanelibraries.inventories.BookcaseInventory
import net.arcanumverum.arcanelibraries.items.ArcaneTomeItem
import net.arcanumverum.arcanelibraries.items.CodexOfSight
import net.arcanumverum.arcanelibraries.screens.BookcaseScreenHandlerFactory
import net.arcanumverum.arcanelibraries.screens.CodexScreenHandlerFactory
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

class Bookcase(settings: Settings) : BlockWithEntity(settings) {
    override fun createBlockEntity(world: BlockView?): BlockEntity = BookcaseEntity()
    override fun getRenderType(state: BlockState?): BlockRenderType = BlockRenderType.MODEL

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos?,
        player: PlayerEntity,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {
        if (world.isClient) return ActionResult.SUCCESS

        val bookcaseBlockEntity = world.getBlockEntity(pos)
        if (bookcaseBlockEntity is BookcaseEntity) {
            val bookcaseItems = bookcaseBlockEntity.inventory.getItems()
            val tomes = bookcaseItems.mapNotNull { if(it.item is ArcaneTomeItem) it else null }.toTypedArray()
            if (tomes.isNotEmpty() &&
                (player.mainHandStack.item is CodexOfSight || player.offHandStack.item is CodexOfSight)) {
                player.openHandledScreen(CodexScreenHandlerFactory(bookcaseBlockEntity))
            } else {
                player.openHandledScreen(BookcaseScreenHandlerFactory(bookcaseBlockEntity))
            }
        }

        return ActionResult.SUCCESS
    }

    override fun onStateReplaced(
        state: BlockState,
        world: World,
        pos: BlockPos?,
        newState: BlockState,
        moved: Boolean
    ) {
        // Drop all items when the block is broken.
        if (state.block == newState.block) return
        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity is BookcaseEntity) {
            ItemScatterer.spawn(world, pos, blockEntity.inventory)
            world.updateComparators(pos, this)
        }
        super.onStateReplaced(state, world, pos, newState, moved)
    }

    override fun hasComparatorOutput(state: BlockState?): Boolean = true

    override fun getComparatorOutput(state: BlockState, world: World, pos: BlockPos?): Int {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos))
    }
}

class BookcaseEntity : BlockEntity(BlockEntities.BOOKCASE_BLOCK_ENTITY) {
    val inventory = BookcaseInventory()

    override fun markDirty() = Unit

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        Inventories.fromTag(tag, this.inventory.getItems())
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)
        Inventories.toTag(tag, this.inventory.getItems())
        return tag
    }

    fun getTomesInside(): List<ItemStack> {
        return inventory.getItems().mapNotNull {if (it.item is ArcaneTomeItem) it else null}
    }
}