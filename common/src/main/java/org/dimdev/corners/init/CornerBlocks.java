package org.dimdev.corners.init;

import org.dimdev.corners.TheCorners;
import org.dimdev.corners.TheCornersSided;
import org.dimdev.corners.block.*;
import org.dimdev.corners.entity.CornerBoatDispensorBehavior;
import org.dimdev.corners.entity.CornerBoatEntity.CornerBoat;
import org.dimdev.corners.mixin.SignTypeAccessor;
import org.dimdev.corners.world.feature.GaiaSaplingGenerator;
import net.minecraft.core.Direction;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

public class CornerBlocks {

	public static final Block STONE_PILLAR = get("stone_pillar",
		new ThinPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)));
	public static final RadioBlock GROWN_RADIO = get("grown_radio",
		new RadioBlock(null, null, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD)));
	public static final RadioBlock BROKEN_RADIO = get("broken_radio",
		new RadioBlock(null, GROWN_RADIO, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD)));
	public static final RadioBlock WOODEN_RADIO = get("wooden_radio",
		new RadioBlock(Items.GOLD_INGOT, BROKEN_RADIO, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD)));
	public static final RadioBlock TUNED_RADIO = get("tuned_radio",
		new RadioBlock(Items.AMETHYST_SHARD, BROKEN_RADIO, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD)));
	public static final Block DRYWALL = get("drywall", new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));
	public static final Block NYLON_FIBER_BLOCK = get("nylon_fiber_block",
		new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL)));
	public static final Block NYLON_FIBER_STAIRS = get("nylon_fiber_stairs",
		new CornerStairsBlock(NYLON_FIBER_BLOCK.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL)));
	public static final Block NYLON_FIBER_SLAB = get("nylon_fiber_slab",
		new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL)));
	public static final Block SNOWY_GLASS = get("snowy_glass",
		new SkyboxGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).lightLevel(state -> 3)));
	public static final Block SNOWY_GLASS_PANE = get("snowy_glass_pane",
		new SkyboxGlassPaneBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).lightLevel(state -> 3)));
	public static final Block SNOWY_GLASS_SLAB = get("snowy_glass_slab",
		new SkyboxGlassSlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).lightLevel(state -> 3)));
	public static final Block DARK_RAILING = get("dark_railing", new RailingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));
	public static final Block DEEP_BOOKSHELF = get("deep_bookshelf",
		new ChiseledBookShelfBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD)));
	// Gaia
	public static final BlockSetType GAIA_SET_TYPE = BlockSetType.register(new BlockSetType("corners:gaia"));
	public static final WoodType GAIA_SIGN_TYPE = SignTypeAccessor
		.callRegister(new WoodType("corners:gaia", BlockSetType.SPRUCE));
	public static final Block GAIA_PLANKS = get("gaia_planks", new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS)));
	public static final Block CARVED_GAIA = get("carved_gaia",
		new OrientableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS)));
	public static final Block GAIA_SAPLING = get("gaia_sapling", new SaplingBlock(GaiaSaplingGenerator.GAIA,
		BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_SAPLING).mapColor(MapColor.GOLD)));
	public static final Block GAIA_LOG = get("gaia_log",
		new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_LOG)
			.mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.PODZOL : MapColor.GOLD)));
	public static final Block STRIPPED_GAIA_LOG = get("stripped_gaia_log", new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_LOG).mapColor(MapColor.PODZOL)));
	public static final Block GAIA_WOOD = get("gaia_wood", new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_WOOD).mapColor(MapColor.GOLD)));
	public static final Block STRIPPED_GAIA_WOOD = get("stripped_gaia_wood", new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_SPRUCE_WOOD)));
	public static final Block GAIA_LEAVES = get("gaia_leaves", new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_LEAVES)));
	public static final Block GAIA_SIGN = getSingle("gaia_sign", new StandingSignBlock(GAIA_SIGN_TYPE, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_SIGN)));
	public static final Block GAIA_WALL_SIGN = getSingle("gaia_wall_sign", new WallSignBlock(GAIA_SIGN_TYPE, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_WALL_SIGN).dropsLike(GAIA_SIGN)));
	public static final Block GAIA_HANGING_SIGN = getSingle("gaia_hanging_sign", new CeilingHangingSignBlock(GAIA_SIGN_TYPE, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN)));
    public static final Block GAIA_WALL_HANGING_SIGN = getSingle("gaia_wall_hanging_sign", new WallHangingSignBlock(GAIA_SIGN_TYPE, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).dropsLike(GAIA_HANGING_SIGN)));
	public static final Block GAIA_PRESSURE_PLATE = get("gaia_pressure_plate", new PressurePlateBlock(GAIA_SET_TYPE, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PRESSURE_PLATE)));
	public static final Block GAIA_TRAPDOOR = get("gaia_trapdoor", new TrapDoorBlock(GAIA_SET_TYPE, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_TRAPDOOR)));
	public static final Block POTTED_GAIA_SAPLING = getSingle("potted_gaia_sapling", Blocks.flowerPot(GAIA_SAPLING));
	public static final Block GAIA_BUTTON = get("gaia_button", Blocks.woodenButton(GAIA_SET_TYPE));
	public static final Block GAIA_STAIRS = get("gaia_stairs", new StairBlock(GAIA_PLANKS.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(GAIA_PLANKS)));
	public static final Block GAIA_SLAB = get("gaia_slab", new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_SLAB)));
	public static final Block GAIA_FENCE_GATE = get("gaia_fence_gate", new FenceGateBlock(GAIA_SIGN_TYPE, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_FENCE_GATE)));
	public static final Block GAIA_FENCE = get("gaia_fence", new FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_FENCE)));
	public static final Block GAIA_DOOR = getSingle("gaia_door", new DoorBlock(GAIA_SET_TYPE, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_DOOR)));
	public static final Item GAIA_BOAT = get("gaia_boat", new CornerBoatItem(false, CornerBoat.GAIA, new Item.Properties().stacksTo(1)));
	public static final Item GAIA_CHEST_BOAT = get("gaia_chest_boat", new CornerBoatItem(true, CornerBoat.GAIA, new Item.Properties().stacksTo(1)));
	public static final Item GAIA_SIGN_ITEM = get("gaia_sign", new SignItem(new Item.Properties().stacksTo(16), GAIA_SIGN, GAIA_WALL_SIGN));
	public static final Item GAIA_HANGING_SIGN_ITEM = get("gaia_hanging_sign", new HangingSignItem(GAIA_HANGING_SIGN, GAIA_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16)));
	public static final Item GAIA_DOOR_ITEM = get("gaia_door", new DoubleHighBlockItem(GAIA_DOOR, new Item.Properties()));

	public static void init(TheCornersSided<?> sided) {
		DispenserBlock.registerBehavior(GAIA_BOAT, new CornerBoatDispensorBehavior(CornerBoat.GAIA, false));
		DispenserBlock.registerBehavior(GAIA_CHEST_BOAT, new CornerBoatDispensorBehavior(CornerBoat.GAIA, true));

		sided.registryFlammable(NYLON_FIBER_BLOCK, 30, 60);
		sided.registryFlammable(NYLON_FIBER_STAIRS, 30, 60);
		sided.registryFlammable(NYLON_FIBER_SLAB, 30, 60);
		sided.registryFlammable(DRYWALL, 5, 20);
		sided.registryFlammable(WOODEN_RADIO, 10, 20);
		sided.registryFlammable(TUNED_RADIO, 10, 20);
		sided.registryFlammable(BROKEN_RADIO, 10, 20);
		sided.registryFlammable(GROWN_RADIO, 10, 20);
		sided.registryFlammable(STRIPPED_GAIA_LOG, 5, 5);
		sided.registryFlammable(STRIPPED_GAIA_WOOD, 5, 5);
		sided.registryFlammable(GAIA_LOG, 5, 5);
		sided.registryFlammable(GAIA_WOOD, 5, 5);
		sided.registryFlammable(GAIA_STAIRS, 5, 20);
		sided.registryFlammable(GAIA_SLAB, 5, 20);
		sided.registryFlammable(GAIA_PLANKS, 5, 20);
		sided.registryFlammable(CARVED_GAIA, 5, 20);
		sided.registryFlammable(GAIA_FENCE, 5, 20);
		sided.registryFlammable(GAIA_FENCE_GATE, 5, 20);
		sided.registryFlammable(GAIA_LEAVES, 30, 60);

		sided.registerFuel(GAIA_FENCE, 300);
		sided.registerFuel(GAIA_FENCE_GATE, 300);
		sided.registerFuel(NYLON_FIBER_BLOCK, 100);
		sided.registerFuel(NYLON_FIBER_STAIRS, 100);
		sided.registerFuel(NYLON_FIBER_SLAB, 100);
		sided.registerFuel(DRYWALL, 300);
		sided.registerFuel(GROWN_RADIO, 300);
		sided.registerFuel(BROKEN_RADIO, 300);
		sided.registerFuel(WOODEN_RADIO, 300);
		sided.registerFuel(TUNED_RADIO, 300);
		sided.registerFuel(DEEP_BOOKSHELF, 300);

		sided.registerStrippable(GAIA_LOG, STRIPPED_GAIA_LOG);
		sided.registerStrippable(GAIA_WOOD, STRIPPED_GAIA_WOOD);

		sided.modifyCreativeTab(CreativeModeTabs.BUILDING_BLOCKS, entries -> {
            entries.accept(STONE_PILLAR);
            entries.accept(DARK_RAILING);
            entries.accept(DRYWALL);
            entries.accept(NYLON_FIBER_BLOCK);
            entries.accept(NYLON_FIBER_STAIRS);
            entries.accept(NYLON_FIBER_SLAB);
            entries.accept(CARVED_GAIA);
            entries.addAfter(Items.CHERRY_BUTTON, GAIA_LOG, GAIA_WOOD, STRIPPED_GAIA_LOG, STRIPPED_GAIA_WOOD, GAIA_PLANKS,
                    GAIA_STAIRS, GAIA_SLAB, GAIA_FENCE, GAIA_FENCE_GATE, GAIA_DOOR_ITEM, GAIA_TRAPDOOR,
                    GAIA_PRESSURE_PLATE, GAIA_BUTTON);
        });
		sided.modifyCreativeTab(CreativeModeTabs.COLORED_BLOCKS, entries -> entries.addAfter(Items.PINK_STAINED_GLASS_PANE, SNOWY_GLASS, SNOWY_GLASS_PANE, SNOWY_GLASS_SLAB));
		sided.modifyCreativeTab(CreativeModeTabs.REDSTONE_BLOCKS, entries -> {
            entries.addAfter(Items.CHISELED_BOOKSHELF, DEEP_BOOKSHELF);
            entries.accept(WOODEN_RADIO);
            entries.accept(TUNED_RADIO);
            entries.accept(BROKEN_RADIO);
            entries.accept(GROWN_RADIO);
        });
		sided.modifyCreativeTab(CreativeModeTabs.NATURAL_BLOCKS, entries -> {
            entries.addAfter(Items.CHERRY_LOG, GAIA_LOG);
            entries.addAfter(Items.CHERRY_LEAVES, GAIA_LEAVES);
            entries.addAfter(Items.CHERRY_SAPLING, GAIA_SAPLING);
        });
		sided.modifyCreativeTab(CreativeModeTabs.FUNCTIONAL_BLOCKS, entries -> {
            entries.addAfter(Items.CHERRY_HANGING_SIGN, GAIA_SIGN_ITEM, GAIA_HANGING_SIGN_ITEM);
            entries.addAfter(Items.CHISELED_BOOKSHELF, DEEP_BOOKSHELF);
        });
		sided.modifyCreativeTab(CreativeModeTabs.TOOLS_AND_UTILITIES, entries -> entries.addAfter(Items.CHERRY_CHEST_BOAT, GAIA_BOAT, GAIA_CHEST_BOAT));
	}

	private static <B extends Block> B getSingle(String id, B block) {
		return TheCorners.getSided().registerBlock(id, block);
	}

	private static <B extends Block> B get(String id, B block) {
		TheCorners.getSided().registerItem(id, new BlockItem(block, new Item.Properties()));
		return TheCorners.getSided().registerBlock(id, block);
	}

	private static <I extends Item> I get(String id, I item) {
		return TheCorners.getSided().registerItem(id, item);
	}

}
