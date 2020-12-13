package net.arcanumverum.arcanelibraries.fluids

import net.minecraft.block.BlockState
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.Item
import net.minecraft.state.property.Properties
import net.minecraft.state.StateManager

import net.arcanumverum.arcanelibraries.Blocks
import net.arcanumverum.arcanelibraries.Fluids
import net.arcanumverum.arcanelibraries.Items
import net.arcanumverum.arcanelibraries.fluids.BaseFluid


abstract class InkFluid : BaseFluid() {
   override fun getBucketItem(): Item = Items.INK_BUCKET
   override fun getFlowing(): Fluid = Fluids.FLOWING_INK
   override fun getStill(): Fluid = Fluids.STILL_INK

   override fun toBlockState(state: FluidState): BlockState {
       return Blocks.INK.getDefaultState().with(Properties.LEVEL_15, method_15741(state))
   }

   public class Flowing : InkFluid() {
       override fun appendProperties(builder: StateManager.Builder<Fluid, FluidState>) {
           super.appendProperties(builder)
           builder.add(LEVEL)
       }

       override fun getLevel(state: FluidState): Int = state.get(LEVEL)
       override fun isStill(state: FluidState): Boolean = false
   }

   public class Still : InkFluid() {
       override fun getLevel(state: FluidState): Int = 8
       override fun isStill(state: FluidState): Boolean = true
   }
}
