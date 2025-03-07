package com.yungnickyoung.minecraft.betterdungeons.world.processor.small_dungeon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yungnickyoung.minecraft.betterdungeons.BetterDungeons;
import com.yungnickyoung.minecraft.betterdungeons.config.BDConfig;
import com.yungnickyoung.minecraft.betterdungeons.init.BDModProcessors;
import com.yungnickyoung.minecraft.betterdungeons.util.Banner;
import com.yungnickyoung.minecraft.betterdungeons.world.DungeonContext;
import com.yungnickyoung.minecraft.betterdungeons.world.DungeonType;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

/**
 * Replaces wall banners with a banner corresponding to the dungeon type.
 * Also removes existing banners to ensure the number of banners per structure
 * falls within the desired range.
 */
@MethodsReturnNonnullByDefault
public class SmallDungeonBannerProcessor extends StructureProcessor {
    public static final Codec<SmallDungeonBannerProcessor> CODEC = RecordCodecBuilder.create(codecBuilder -> codecBuilder
        .group(
            Codec.STRING
                .fieldOf("dungeon_type")
                .forGetter(smallDungeonBannerProcessor -> smallDungeonBannerProcessor.getDungeonType().getName()))
        .apply(codecBuilder, codecBuilder.stable(SmallDungeonBannerProcessor::new)));

    private SmallDungeonBannerProcessor(String dungeonType) {
        this.dungeonType = DungeonType.fromString(dungeonType);
    }

    private final DungeonType dungeonType;
    public DungeonType getDungeonType() {
        return this.dungeonType;
    }

    // All banners
    public static final Banner SMALL_DUNGEON_SKELETON_BANNER = new Banner.Builder()
        .blockState(Blocks.BLACK_WALL_BANNER.getDefaultState())
        .pattern("cbo", 0)
        .pattern("cs", 0)
        .pattern("bs", 15)
        .pattern("ts", 0)
        .pattern("cre", 0)
        .pattern("gra", 15)
        .customName("betterdungeons.small_dungeon.banner.skeleton")
        .customColor("dark_gray")
        .build();

    public static final Banner SMALL_DUNGEON_ZOMBIE_BANNER = new Banner.Builder()
        .blockState(Blocks.RED_WALL_BANNER.getDefaultState())
        .pattern("bt", 6)
        .pattern("mc", 7)
        .pattern("gra", 15)
        .pattern("tts", 0)
        .pattern("bts", 0)
        .pattern("bo", 13)
        .customName("betterdungeons.small_dungeon.banner.zombie")
        .customColor("dark_green")
        .build();

    public static final Banner SMALL_DUNGEON_SPIDER_BANNER = new Banner.Builder()
        .blockState(Blocks.RED_WALL_BANNER.getDefaultState())
        .pattern("flo", 7)
        .pattern("bo", 7)
        .pattern("sc", 7)
        .pattern("hh", 7)
        .pattern("bs", 7)
        .pattern("gra", 15)
        .customName("betterdungeons.small_dungeon.banner.spider")
        .customColor("dark_red")
        .build();

    @ParametersAreNonnullByDefault
    @Override
    public Template.BlockInfo process(IWorldReader worldReader, BlockPos jigsawPiecePos, BlockPos jigsawPieceBottomCenterPos, Template.BlockInfo blockInfoLocal, Template.BlockInfo blockInfoGlobal, PlacementSettings structurePlacementData, @Nullable Template template) {
        if (blockInfoGlobal.state.getBlock() instanceof AbstractBannerBlock) {
            // Make sure we only operate on the placeholder banners
            if (blockInfoGlobal.state.getBlock() == Blocks.RED_WALL_BANNER && (blockInfoGlobal.nbt.get("Patterns") == null || blockInfoGlobal.nbt.getList("Patterns", 10).size() == 0)) {
                // Fetch thread-local dungeon context
                DungeonContext context = DungeonContext.peek();

                // Check dungeon context to see if we have reached the max banner count for this structure piece
                if (context.getBannerCount() >= BDConfig.smallDungeons.bannerMaxCount.get())
                    return new Template.BlockInfo(blockInfoGlobal.pos, Blocks.CAVE_AIR.getDefaultState(), blockInfoGlobal.nbt);

                // Chance of a banner spawning
                Random random = structurePlacementData.getRandom(blockInfoGlobal.pos);
                if (random.nextFloat() > .1f) {
                    return new Template.BlockInfo(blockInfoGlobal.pos, Blocks.CAVE_AIR.getDefaultState(), blockInfoGlobal.nbt);
                }

                Banner banner = getBannerForType();
                Direction facing = blockInfoGlobal.state.get(BlockStateProperties.HORIZONTAL_FACING);
                BlockState newState = banner.getState().with(BlockStateProperties.HORIZONTAL_FACING, facing);
                CompoundNBT newNBT = copyNBT(banner.getNbt());

                blockInfoGlobal = new Template.BlockInfo(blockInfoGlobal.pos, newState, newNBT);
                context.incrementBannerCount();
            }
        }
        return blockInfoGlobal;
    }

    protected IStructureProcessorType<?> getType() {
        return BDModProcessors.SMALL_DUNGEON_BANNER_PROCESSOR;
    }

    private Banner getBannerForType() {
        switch (this.dungeonType) {
            case SKELETON:
                return SMALL_DUNGEON_SKELETON_BANNER;
            case ZOMBIE:
                return SMALL_DUNGEON_ZOMBIE_BANNER;
            case SPIDER:
                return SMALL_DUNGEON_SPIDER_BANNER;
            default:
                BetterDungeons.LOGGER.warn("Invalid DungeonType {} for BannerProcessor! This shouldn't happen!", this.dungeonType);
                return SMALL_DUNGEON_SKELETON_BANNER;
        }
    }

    private CompoundNBT copyNBT(CompoundNBT other) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.merge(other);
        return nbt;
    }
}
