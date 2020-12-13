package net.arcanumverum.arcanelibraries.fluids

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.FlowableFluid
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView


abstract class BaseFluid : FlowableFluid() {

    override fun beforeBreakingBlock(world: WorldAccess, pos: BlockPos, state: BlockState): Unit {
        val blockEntity = if (state.getBlock().hasBlockEntity()) world.getBlockEntity(pos) else null
        Block.dropStacks(state, world, pos, blockEntity)
    }

    override fun canBeReplacedWith(
        fluidState: FluidState,
        blockView: BlockView,
        blockPos: BlockPos,
        fluid: Fluid,
        direction: Direction
    ): Boolean = false

    override fun getBlastResistance(): Float = 100.0F
    override fun getFlowSpeed(view: WorldView): Int = 4
    override fun getLevelDecreasePerBlock(view: WorldView): Int = 1
    override fun getTickRate(view: WorldView): Int = 5
    override fun isInfinite(): Boolean = false
    override fun matchesType(fluid: Fluid): Boolean = fluid == getStill() || fluid == getFlowing()

}
