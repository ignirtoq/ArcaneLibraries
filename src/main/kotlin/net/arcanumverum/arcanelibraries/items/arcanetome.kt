package net.arcanumverum.arcanelibraries.items

import net.arcanumverum.arcanelibraries.screens.ArcaneTomeScreenHandlerFactory
import net.arcanumverum.arcanelibraries.Items as ALItems
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World


const val DEFAULT_TIER = "one"
const val NBT_TAG_INVENTORY = "arcanelibraries_inventory"
const val NBT_TAG_TIER = "arcanelibraries_tier"
const val TOOLTIP_KEY = "item.arcane_libraries.arcane_tome.tooltip"


class ArcaneTomeItem(settings: Settings) : Item(settings) {
    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        if (!world.isClient) {
            user.openHandledScreen(ArcaneTomeScreenHandlerFactory(stack))
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

    companion object {
        private fun serialize(tome: ItemStack, itemStacks: List<ItemStack>) {
            val tag = CompoundTag()
            for (slot in itemStacks.indices) {
                tag.put("slot${slot}", itemStacks[slot].toTag(CompoundTag()))
            }
            tome.getOrCreateTag().put(NBT_TAG_INVENTORY, tag)
        }

        fun serialize(tomes: List<ItemStack>, itemStacks: DefaultedList<ItemStack>) {
            var startIndex = 0
            for (tome in tomes) {
                val tomeSize = getTier(tome).size
                serialize(tome, itemStacks.subList(startIndex, startIndex + tomeSize))
                startIndex += tomeSize
            }
        }

        private fun deserialize(tome: ItemStack): List<ItemStack> {
            val tag = tome.getOrCreateTag().getCompound(NBT_TAG_INVENTORY)
            return List<ItemStack>(getTier(tome).size) {
                ItemStack.fromTag(tag.getCompound("slot${it}"))
            }
        }

        fun deserialize(tomes: List<ItemStack>): List<ItemStack> {
            return listOf(*tomes.map { tome ->
                deserialize(tome)
            }.flatten().toTypedArray())
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

        fun fromTier(tier: TomeTier?): ItemStack {
            val newTome = ItemStack(ALItems.ARCANE_TOME, 1)
            setTier(newTome, tier!!)
            return newTome
        }
    }
}


enum class TomeTier(val size: Int, val maxStackSize: Int) {
    one(3, 3 * 7 * 7),
    two(7, 3 * 7 * 7 * 3),
    three(3 * 7, 3 * 7 * 7 * 7);

    companion object {
        private val map = mapOf(1 to one, 2 to two, 3 to three)

        fun fromInt(i: Int): TomeTier? = map[i]
    }
}
