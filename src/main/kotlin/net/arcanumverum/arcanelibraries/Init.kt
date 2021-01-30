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
    var BOOKCASE: Block = Bookcase(FabricBlockSettings.copyOf(MinecraftBlocks.CHEST))
    var ELDRITCH_LOG: Block = PillarBlock(FabricBlockSettings.of(Material.WOOD).hardness(2.0f))
    var ELDRITCH_PLANKS: Block = Block(FabricBlockSettings.of(Material.WOOD).hardness(2.0f))
    var ELDRITCH_SLAB: Block = SlabBlock(FabricBlockSettings.of(Material.WOOD).hardness(2.0f))
    var INK: Block = object : FluidBlock(Fluids.STILL_INK, FabricBlockSettings.copy(MinecraftBlocks.WATER)){}
}


object BlockEntities {
    var BOOKCASE_BLOCK_ENTITY: BlockEntityType<BookcaseEntity> = BlockEntityType.Builder.create(
        ::BookcaseEntity,
        Blocks.BOOKCASE
    ).build(null)
}


object Fluids {
    var STILL_INK: InkFluid.Still = InkFluid.Still()
    var FLOWING_INK: InkFluid.Flowing = InkFluid.Flowing()
}


object Items {
    var INK_VIAL: Item = InkVialItem(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))
    var SCRIBING_TOOLS: Item = ScribingToolsItem(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))
    var ARCANE_TOME: Item = ArcaneTomeItem(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))
    var ALL_SEEING_EYE: Item = Item(FabricItemSettings().group(ItemGroup.MISC))
    var CODEX_OF_SIGHT: Item = CodexOfSight(FabricItemSettings().group(ItemGroup.MISC).maxCount(1))

    // Fluid buckets
   var INK_BUCKET: BucketItem = BucketItem(
        Fluids.STILL_INK,
        Item.Settings().recipeRemainder(MinecraftItems.BUCKET).group(ItemGroup.MISC).maxCount(1))

    // Block items
    var BOOKCASE_ITEM: Item = BlockItem(Blocks.BOOKCASE, Item.Settings().group(ItemGroup.MISC))
    var ELDRITCH_LOG_ITEM: Item = BlockItem(Blocks.ELDRITCH_LOG, Item.Settings().group(ItemGroup.MISC))
    var ELDRITCH_PLANKS_ITEM: Item = BlockItem(Blocks.ELDRITCH_PLANKS, Item.Settings().group(ItemGroup.MISC))
    var ELDRITCH_SLAB_ITEM: Item = BlockItem(Blocks.ELDRITCH_SLAB, Item.Settings().group(ItemGroup.MISC))
}


object Screens {
    var ARCANE_TOME_SCREEN_HANDLER: ScreenHandlerType<ArcaneTomeScreenHandler>? = null
    var CODEX_SCREEN_HANDLER: ScreenHandlerType<CodexScreenHandler>? = null
    var BOOKCASE_SCREEN_HANDLER: ScreenHandlerType<BookcaseScreenHandler>? = null
}


fun registerBlocks() {
    Blocks.BOOKCASE = Registry.register(Registry.BLOCK, Constants.BOOKCASE_BLOCK_IDENTIFIER, Blocks.BOOKCASE)
    Blocks.ELDRITCH_LOG = Registry.register(Registry.BLOCK, Constants.ELDRITCH_LOG_IDENTIFIER, Blocks.ELDRITCH_LOG)
    Blocks.ELDRITCH_PLANKS = Registry.register(Registry.BLOCK, Constants.ELDRITCH_PLANKS_IDENTIFIER, Blocks.ELDRITCH_PLANKS)
    Blocks.ELDRITCH_SLAB = Registry.register(Registry.BLOCK, Constants.ELDRITCH_SLAB_IDENTIFIER, Blocks.ELDRITCH_SLAB)

    Blocks.INK = Registry.register(Registry.BLOCK, Constants.INK_FLUID_BLOCK_IDENTIFIER, Blocks.INK)
}


fun registerBlockEntities() {
    BlockEntities.BOOKCASE_BLOCK_ENTITY = Registry.register(
        Registry.BLOCK_ENTITY_TYPE,
        Constants.BOOKCASE_BLOCK_IDENTIFIER,
        BlockEntities.BOOKCASE_BLOCK_ENTITY
    )}


fun registerFluids() {
    Fluids.STILL_INK = Registry.register(Registry.FLUID, Constants.STILL_INK_IDENTIFIER, Fluids.STILL_INK)
    Fluids.FLOWING_INK = Registry.register(Registry.FLUID, Constants.FLOWING_INK_IDENTIFIER, Fluids.FLOWING_INK)
}


fun registerItems() {
    Items.INK_VIAL = Registry.register(Registry.ITEM, Constants.INK_VIAL_IDENTIFIER, Items.INK_VIAL)
    Items.SCRIBING_TOOLS = Registry.register(Registry.ITEM, Constants.SCRIBING_TOOLS_IDENTIFIER, Items.SCRIBING_TOOLS)
    Items.ARCANE_TOME = Registry.register(Registry.ITEM, Constants.ARCANE_TOME_IDENTIFIER, Items.ARCANE_TOME)
    Items.ALL_SEEING_EYE = Registry.register(Registry.ITEM, Constants.ALL_SEEING_EYE_IDENTIFIER, Items.ALL_SEEING_EYE)
    Items.CODEX_OF_SIGHT = Registry.register(Registry.ITEM, Constants.CODEX_OF_SIGHT_IDENTIFIER, Items.CODEX_OF_SIGHT)

    // Fluid buckets
    Items.INK_BUCKET = Registry.register(Registry.ITEM, Constants.INK_BUCKET_IDENTIFIER, Items.INK_BUCKET)

    // Block items
    Items.BOOKCASE_ITEM = Registry.register(Registry.ITEM, Constants.BOOKCASE_BLOCK_IDENTIFIER, Items.BOOKCASE_ITEM)
    Items.ELDRITCH_LOG_ITEM = Registry.register(Registry.ITEM, Constants.ELDRITCH_LOG_IDENTIFIER, Items.ELDRITCH_LOG_ITEM)
    Items.ELDRITCH_PLANKS_ITEM = Registry.register(Registry.ITEM, Constants.ELDRITCH_PLANKS_IDENTIFIER, Items.ELDRITCH_PLANKS_ITEM)
    Items.ELDRITCH_SLAB_ITEM = Registry.register(Registry.ITEM, Constants.ELDRITCH_SLAB_IDENTIFIER, Items.ELDRITCH_SLAB_ITEM)
}


fun registerScreens() {
    Screens.ARCANE_TOME_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(
        Constants.ARCANE_TOME_IDENTIFIER, ::ArcaneTomeScreenHandler)
    Screens.CODEX_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(
        Constants.CODEX_OF_SIGHT_IDENTIFIER, ::CodexScreenHandler)
    Screens.BOOKCASE_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(
        Constants.BOOKCASE_BLOCK_IDENTIFIER, ::BookcaseScreenHandler)
}


@Suppress("unused")
fun init() {
    registerBlocks()
    registerBlockEntities()
    registerFluids()
    registerItems()
    registerScreens()
}


fun setupFluidRendering(still: Fluid?, flowing: Fluid?, textureFluidId: Identifier, color: Int) {
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
