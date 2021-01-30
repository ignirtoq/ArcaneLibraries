package net.arcanumverum.arcanelibraries

import net.arcanumverum.arcanelibraries.blocks.Bookcase
import net.arcanumverum.arcanelibraries.blocks.BookcaseEntity
import net.minecraft.block.Blocks as MinecraftBlocks
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
import net.arcanumverum.arcanelibraries.items.CodexOfSight
import net.arcanumverum.arcanelibraries.items.InkVialItem
import net.arcanumverum.arcanelibraries.items.ScribingToolsItem
import net.arcanumverum.arcanelibraries.network.initTomePageDataNetworking
import net.arcanumverum.arcanelibraries.screens.*
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem


object Blocks {
    val BOOKCASE = Registry.register(
        Registry.BLOCK,
        Constants.BOOKCASE_BLOCK_IDENTIFIER,
        Bookcase(FabricBlockSettings.copyOf(MinecraftBlocks.CHEST)))
    val ELDRITCH_LOG = Registry.register(
        Registry.BLOCK,
        Constants.ELDRITCH_LOG_IDENTIFIER,
        PillarBlock(FabricBlockSettings.of(Material.WOOD).hardness(2.0f)))
    val ELDRITCH_PLANKS = Registry.register(
        Registry.BLOCK,
        Constants.ELDRITCH_PLANKS_IDENTIFIER,
        Block(FabricBlockSettings.of(Material.WOOD).hardness(2.0f)))
    val ELDRITCH_SLAB = Registry.register(
        Registry.BLOCK,
        Constants.ELDRITCH_SLAB_IDENTIFIER,
        SlabBlock(FabricBlockSettings.of(Material.WOOD).hardness(2.0f)))

    val INK = Registry.register(
        Registry.BLOCK,
        Constants.INK_FLUID_BLOCK_IDENTIFIER,
        object : FluidBlock(Fluids.STILL_INK, FabricBlockSettings.copy(MinecraftBlocks.WATER)){}
    )
}


object BlockEntities {
    val BOOKCASE_BLOCK_ENTITY = Registry.register(
        Registry.BLOCK_ENTITY_TYPE,
        Constants.BOOKCASE_BLOCK_IDENTIFIER,
        BlockEntityType.Builder.create(::BookcaseEntity, Blocks.BOOKCASE).build(null))
}


object Fluids {
    val STILL_INK: InkFluid.Still = Registry.register(Registry.FLUID, Constants.STILL_INK_IDENTIFIER, InkFluid.Still())
    val FLOWING_INK: InkFluid.Flowing = Registry.register(Registry.FLUID, Constants.FLOWING_INK_IDENTIFIER, InkFluid.Flowing())
}


object Items {
    val INK_VIAL = InkVialItem(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))
    val SCRIBING_TOOLS = ScribingToolsItem(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))
    val ARCANE_TOME = ArcaneTomeItem(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))
    val ALL_SEEING_EYE = Item(FabricItemSettings().group(ItemGroup.MISC))
    val CODEX_OF_SIGHT = CodexOfSight(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))

    // Fluid buckets
   val INK_BUCKET: BucketItem = Registry.register(
       Registry.ITEM,
       Constants.INK_BUCKET_IDENTIFIER,
       BucketItem(
           Fluids.STILL_INK,
           Item.Settings().recipeRemainder(MinecraftItems.BUCKET).group(ItemGroup.MISC).maxCount(1)))

    // Block items
    val BOOKCASE_ITEM = Registry.register(
        Registry.ITEM,
        Constants.BOOKCASE_BLOCK_IDENTIFIER,
        BlockItem(Blocks.BOOKCASE, Item.Settings().group(ItemGroup.MISC)))
    val ELDRITCH_LOG_ITEM = Registry.register(
        Registry.ITEM,
        Constants.ELDRITCH_LOG_IDENTIFIER,
        BlockItem(Blocks.ELDRITCH_LOG, Item.Settings().group(ItemGroup.MISC)))
    val ELDRITCH_PLANKS_ITEM = Registry.register(
        Registry.ITEM,
        Constants.ELDRITCH_PLANKS_IDENTIFIER,
        BlockItem(Blocks.ELDRITCH_PLANKS, Item.Settings().group(ItemGroup.MISC)))
    val ELDRITCH_SLAB_ITEM = Registry.register(
        Registry.ITEM,
        Constants.ELDRITCH_SLAB_IDENTIFIER,
        BlockItem(Blocks.ELDRITCH_SLAB, Item.Settings().group(ItemGroup.MISC)))
}


object Screens {
    val ARCANE_TOME_SCREEN_HANDLER: ScreenHandlerType<ArcaneTomeScreenHandler> = ScreenHandlerRegistry.registerExtended(
        Constants.ARCANE_TOME_IDENTIFIER, ::ArcaneTomeScreenHandler)
    val CODEX_SCREEN_HANDLER: ScreenHandlerType<CodexScreenHandler> = ScreenHandlerRegistry.registerExtended(
        Constants.CODEX_OF_SIGHT_IDENTIFIER, ::CodexScreenHandler)
    val BOOKCASE_SCREEN_HANDLER: ScreenHandlerType<BookcaseScreenHandler> = ScreenHandlerRegistry.registerSimple(
        Constants.BOOKCASE_BLOCK_IDENTIFIER, ::BookcaseScreenHandler)
}


fun loadItems() {
    Registry.register(Registry.ITEM, Constants.INK_VIAL_IDENTIFIER, Items.INK_VIAL)
    Registry.register(Registry.ITEM, Constants.SCRIBING_TOOLS_IDENTIFIER, Items.SCRIBING_TOOLS)
    Registry.register(Registry.ITEM, Constants.ARCANE_TOME_IDENTIFIER, Items.ARCANE_TOME)
    Registry.register(Registry.ITEM, Constants.ALL_SEEING_EYE_IDENTIFIER, Items.ALL_SEEING_EYE)
    Registry.register(Registry.ITEM, Constants.CODEX_OF_SIGHT_IDENTIFIER, Items.CODEX_OF_SIGHT)
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
    ScreenRegistry.register(Screens.ARCANE_TOME_SCREEN_HANDLER, ::ArcaneTomeScreen)
    ScreenRegistry.register(Screens.CODEX_SCREEN_HANDLER, ::CodexScreen)
    ScreenRegistry.register(Screens.BOOKCASE_SCREEN_HANDLER, ::BookcaseScreen)
    setupInk()
}


@Environment(EnvType.SERVER)
@Suppress("unused")
fun initServer() {
    initTomePageDataNetworking()
}
