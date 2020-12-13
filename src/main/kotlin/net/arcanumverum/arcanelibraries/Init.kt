package net.arcanumverum.arcanelibraries

import net.minecraft.block.Blocks as MinecraftBlocks
import net.minecraft.block.FluidBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.BucketItem
import net.minecraft.item.Item
import net.minecraft.item.Items as MinecraftItems
import net.minecraft.item.ItemGroup
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.BlockRenderView

import net.fabricmc.api.Environment
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry

import net.arcanumverum.arcanelibraries.fluids.InkFluid
import net.arcanumverum.arcanelibraries.items.ArcaneTomeItem
import net.arcanumverum.arcanelibraries.items.InkVialItem
import net.arcanumverum.arcanelibraries.items.ScribingToolsItem
import net.arcanumverum.arcanelibraries.screens.tome.TomeScreen
import net.arcanumverum.arcanelibraries.screens.tome.TomeScreenHandler


object Blocks {
   val INK = Registry.register(
       Registry.BLOCK,
       Constants.INK_FLUID_BLOCK_IDENTIFIER,
       object : FluidBlock(Fluids.STILL_INK, FabricBlockSettings.copy(MinecraftBlocks.WATER)){}
   )
}

object Fluids {
   val STILL_INK: InkFluid.Still = Registry.register(Registry.FLUID, Constants.STILL_INK_IDENTIFIER, InkFluid.Still())
   val FLOWING_INK: InkFluid.Flowing = Registry.register(Registry.FLUID, Constants.FLOWING_INK_IDENTIFIER, InkFluid.Flowing())
}

object Items {
    val INK_VIAL = InkVialItem(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))
    val SCRIBING_TOOLS = ScribingToolsItem(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))
    val ARCANE_TOME = ArcaneTomeItem(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))

    // Fluid buckets
   val INK_BUCKET: BucketItem = Registry.register(
       Registry.ITEM,
       Constants.INK_BUCKET_IDENTIFIER,
       BucketItem(
           Fluids.STILL_INK,
           Item.Settings().recipeRemainder(MinecraftItems.BUCKET).group(ItemGroup.MISC).maxCount(1)))
}

object Screens {
    val TOME_SCREEN_HANDLER: ScreenHandlerType<TomeScreenHandler> = ScreenHandlerRegistry.registerSimple(
        Constants.ARCANE_TOME_IDENTIFIER, ::TomeScreenHandler)
}

fun loadItems() {
    Registry.register(Registry.ITEM, Constants.INK_VIAL_IDENTIFIER, Items.INK_VIAL)
    Registry.register(Registry.ITEM, Constants.SCRIBING_TOOLS_IDENTIFIER, Items.SCRIBING_TOOLS)
    Registry.register(Registry.ITEM, Constants.ARCANE_TOME_IDENTIFIER, Items.ARCANE_TOME)
}

@Suppress("unused")
fun init() {
    loadItems()
}


fun setupFluidRendering(still: Fluid, flowing: Fluid, textureFluidId: Identifier, color: Int) {
    val stillSpriteId = Identifier(textureFluidId.namespace, "block/" + textureFluidId.path.toString() + "_still")
    val flowingSpriteId = Identifier(textureFluidId.namespace, "block/" + textureFluidId.path.toString() + "_flow")

    // If they're not already present, add the sprites to the block atlas
    ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
        .register(ClientSpriteRegistryCallback { _: SpriteAtlasTexture?, registry: ClientSpriteRegistryCallback.Registry ->
            registry.register(stillSpriteId)
            registry.register(flowingSpriteId)
        })

    val fluidId = Registry.FLUID.getId(still)
    val listenerId = Identifier(fluidId.namespace, fluidId.path + "_reload_listener")

    val fluidSprites = arrayOf<Sprite?>(null, null)

    ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
        .registerReloadListener(object : SimpleSynchronousResourceReloadListener {
            override fun getFabricId(): Identifier {
                return listenerId
            }

            /**
             * Get the sprites from the block atlas when resources are reloaded
             */
            override fun apply(resourceManager: ResourceManager?) {
                val atlas = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
                fluidSprites[0] = atlas.apply(stillSpriteId)
                fluidSprites[1] = atlas.apply(flowingSpriteId)
            }
        })

    // The FluidRenderer gets the sprites and color from a FluidRenderHandler during rendering
    val renderHandler: FluidRenderHandler = object : FluidRenderHandler {
        override fun getFluidSprites(view: BlockRenderView?, pos: BlockPos?, state: FluidState): Array<Sprite?> {
            return fluidSprites
        }

        override fun getFluidColor(view: BlockRenderView?, pos: BlockPos?, state: FluidState): Int {
            return color
        }
    }

    FluidRenderHandlerRegistry.INSTANCE.register(still, renderHandler)
    FluidRenderHandlerRegistry.INSTANCE.register(flowing, renderHandler)
}


fun setupInk() {
    setupFluidRendering(Fluids.STILL_INK, Fluids.FLOWING_INK, Identifier("minecraft", "water"), 0x000000)
    BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), Fluids.STILL_INK, Fluids.FLOWING_INK)
}


@Environment(EnvType.CLIENT)
@Suppress("unused")
fun initClient() {
    ScreenRegistry.register(Screens.TOME_SCREEN_HANDLER, ::TomeScreen)
    setupInk()
}
