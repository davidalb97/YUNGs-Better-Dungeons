package com.yungnickyoung.minecraft.betterdungeons.world.processor.small_dungeon;

import com.mojang.serialization.Codec;
import com.yungnickyoung.minecraft.betterdungeons.init.BDModProcessors;
import com.yungnickyoung.minecraft.yungsapi.world.BlockSetSelector;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

/**
 * Dynamically generates support legs below small dungeons.
 * Yellow stained glass is used to mark the corner positions where the legs will spawn for simplicity.
 */
@MethodsReturnNonnullByDefault
public class SmallDungeonLegProcessor extends StructureProcessor {
    public static final SmallDungeonLegProcessor INSTANCE = new SmallDungeonLegProcessor();
    public static final Codec<SmallDungeonLegProcessor> CODEC = Codec.unit(() -> INSTANCE);

    private static final BlockSetSelector STONE_BRICK_SELECTOR = new BlockSetSelector(Blocks.STONE_BRICKS.getDefaultState())
        .addBlock(Blocks.MOSSY_STONE_BRICKS.getDefaultState(), 0.5f)
        .addBlock(Blocks.CRACKED_STONE_BRICKS.getDefaultState(), 0.2f);

    @ParametersAreNonnullByDefault
    @Override
    public Template.BlockInfo process(IWorldReader worldReader, BlockPos jigsawPiecePos, BlockPos jigsawPieceBottomCenterPos, Template.BlockInfo blockInfoLocal, Template.BlockInfo blockInfoGlobal, PlacementSettings structurePlacementData, @Nullable Template template) {
        if (blockInfoGlobal.state.getBlock() == Blocks.YELLOW_STAINED_GLASS) {
            ChunkPos currentChunkPos = new ChunkPos(blockInfoGlobal.pos);
            IChunk currentChunk = worldReader.getChunk(currentChunkPos.x, currentChunkPos.z);
            Random random = structurePlacementData.getRandom(blockInfoGlobal.pos);

            // Always replace the glass itself with mossy cobble
            currentChunk.setBlockState(blockInfoGlobal.pos, Blocks.MOSSY_COBBLESTONE.getDefaultState(), false);
            blockInfoGlobal = new Template.BlockInfo(blockInfoGlobal.pos, Blocks.MOSSY_COBBLESTONE.getDefaultState(), blockInfoGlobal.nbt);

            // Generate vertical pillar down
            BlockPos.Mutable mutable = blockInfoGlobal.pos.down().toMutable();
            BlockState currBlock = worldReader.getBlockState(mutable);
            while (mutable.getY() > 0 && (currBlock.getMaterial() == Material.AIR || currBlock.getMaterial() == Material.WATER || currBlock.getMaterial() == Material.LAVA)) {
                currentChunk.setBlockState(mutable, STONE_BRICK_SELECTOR.get(random), false);
                mutable.move(Direction.DOWN);
                currBlock = worldReader.getBlockState(mutable);
            }
        }

        return blockInfoGlobal;
    }

    protected IStructureProcessorType<?> getType() {
        return BDModProcessors.SMALL_DUNGEON_LEG_PROCESSOR;
    }
}
