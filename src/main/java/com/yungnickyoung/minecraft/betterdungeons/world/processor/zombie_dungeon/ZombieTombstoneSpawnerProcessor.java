package com.yungnickyoung.minecraft.betterdungeons.world.processor.zombie_dungeon;


import com.mojang.serialization.Codec;
import com.yungnickyoung.minecraft.betterdungeons.init.BDModProcessors;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Sets mob spawners to spawn skeletons w/ swords.
 */
@MethodsReturnNonnullByDefault
public class ZombieTombstoneSpawnerProcessor extends StructureProcessor {
    public static final ZombieTombstoneSpawnerProcessor INSTANCE = new ZombieTombstoneSpawnerProcessor();
    public static final Codec<ZombieTombstoneSpawnerProcessor> CODEC = Codec.unit(() -> INSTANCE);

    @ParametersAreNonnullByDefault
    @Override
    public Template.BlockInfo process(IWorldReader worldReader, BlockPos jigsawPiecePos, BlockPos jigsawPieceBottomCenterPos, Template.BlockInfo blockInfoLocal, Template.BlockInfo blockInfoGlobal, PlacementSettings structurePlacementData, @Nullable Template template) {
        if (blockInfoGlobal.state.getBlock() == Blocks.BLACK_STAINED_GLASS) {
            // First initialize NBT if it's null for some reason
            if (blockInfoGlobal.nbt == null) {
                CompoundNBT newNBT = new CompoundNBT();
                newNBT.putShort("SpawnCount", (short) 4);
                newNBT.putString("id", "minecraft:mob_spawner");
                newNBT.putShort("MinSpawnDelay", (short) 200);
                blockInfoGlobal.nbt = newNBT;
            }

            blockInfoGlobal = new Template.BlockInfo(blockInfoGlobal.pos, Blocks.SPAWNER.getDefaultState(), blockInfoGlobal.nbt);

            // Update the spawner block's NBT
            // SpawnData
            CompoundNBT spawnData = new CompoundNBT();
            spawnData.putString("id", "minecraft:skeleton");
            // HandDropChances
            ListNBT handDropChances = new ListNBT();
            handDropChances.add(FloatNBT.valueOf(.2f));
            handDropChances.add(FloatNBT.valueOf(0f));
            spawnData.put("HandDropChances", handDropChances);
            // HandItems
            ListNBT handItems = new ListNBT();
            ItemStack itemStack = new ItemStack(Items.IRON_SWORD);
            CompoundNBT ironSwordNBT = new CompoundNBT();
            itemStack.write(ironSwordNBT);
            handItems.add(ironSwordNBT);
            handItems.add(new CompoundNBT());
            spawnData.put("HandItems", handItems);

            blockInfoGlobal.nbt.put("SpawnData", spawnData);

            // SpawnPotentials
            CompoundNBT spawnPotentials = new CompoundNBT();
            CompoundNBT spawnPotentialsEntity = new CompoundNBT();
            spawnPotentialsEntity.putString("id", "minecraft:skeleton");
            spawnPotentials.put("Entity", spawnPotentialsEntity);
            spawnPotentials.put("Weight", IntNBT.valueOf(1));
            blockInfoGlobal.nbt.getList("SpawnPotentials", spawnPotentials.getId()).clear();
            blockInfoGlobal.nbt.getList("SpawnPotentials", spawnPotentials.getId()).add(0, spawnPotentials);

            // Player range (default 16)
            blockInfoGlobal.nbt.putShort("RequiredPlayerRange", (short)16);

            // Range at which skeletons can spawn from spawner (default 4?)
            blockInfoGlobal.nbt.putShort("SpawnRange", (short)4);

            // Max nearby entities allowed (default 6)
            blockInfoGlobal.nbt.putShort("MaxNearbyEntities", (short)6);

            // Time between spawn attempts (default 800)
            blockInfoGlobal.nbt.putShort("MaxSpawnDelay", (short)800);
        }
        return blockInfoGlobal;
    }

    protected IStructureProcessorType<?> getType() {
        return BDModProcessors.ZOMBIE_TOMBSTONE_SPAWNER_PROCESSOR;
    }
}
