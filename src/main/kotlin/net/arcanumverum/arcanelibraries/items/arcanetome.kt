package net.arcanumverum.arcanelibraries.items

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

import net.arcanumverum.arcanelibraries.screens.tome.TomeScreenHandlerFactory
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.collection.DefaultedList


const val DEFAULT_TIER = "one"
const val NBT_TAG_INVENTORY = "arcanelibraries_inventory"
const val NBT_TAG_TIER = "arcanelibraries_tier"
const val TOOLTIP_KEY = "item.arcane_libraries.arcane_tome.tooltip"


class ArcaneTomeItem(settings: Settings) : Item(settings) {
    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        if (!world.isClient) {
            user.openHandledScreen(TomeScreenHandlerFactory(stack))
        }
        return TypedActionResult.success(stack)
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        super.appendTooltip(stack, world, tooltip, context)
        val containedItems = deserialize(stack!!)
        for (innerStack in containedItems) {
            if (innerStack.item == Items.AIR) continue
            tooltip!!.add(TranslatableText(TOOLTIP_KEY, innerStack.count, innerStack.name))
        }
    }

    fun serialize(tome: ItemStack, itemStacks: DefaultedList<ItemStack>) {
        val tag = CompoundTag()
        for (slot in 0 until itemStacks.size) {
            tag.put("slot${slot}", itemStacks[slot].toTag(CompoundTag()))
        }
        tome.getOrCreateTag().put(NBT_TAG_INVENTORY, tag)
    }

    fun deserialize(tome: ItemStack): DefaultedList<ItemStack> {
        val tag = tome.getOrCreateTag().getCompound(NBT_TAG_INVENTORY)
        val itemStacks = DefaultedList.ofSize(getTier(tome).size, ItemStack.EMPTY)
        for (slot in 0 until itemStacks.size) {
            itemStacks[slot] = ItemStack.fromTag(tag.getCompound("slot${slot}"))
        }
        return itemStacks
    }

    fun getTier(tome: ItemStack): TomeTier {
        var tierNbt = tome.getOrCreateTag().getString(NBT_TAG_TIER)
        if (tierNbt == "") {
            tierNbt = DEFAULT_TIER
            setTier(tome, tierNbt)
        }
        return TomeTier.valueOf(tierNbt)
    }

    fun setTier(tome: ItemStack, tier: TomeTier) {
        tome.getOrCreateTag().putString(NBT_TAG_TIER, tier.name)
    }

    fun setTier(tome: ItemStack, tier: String) = setTier(tome, TomeTier.valueOf(tier))
}


enum class TomeTier(val size: Int, val maxStackSize: Int) {
    one(3, 3 * 7 * 7),
    two(7, 3 * 7 * 7 * 3),
    three(3 * 7, 3 * 7 * 7 * 7),
}
