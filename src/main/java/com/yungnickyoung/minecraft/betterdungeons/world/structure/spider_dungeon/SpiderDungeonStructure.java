package com.yungnickyoung.minecraft.betterdungeons.world.structure.spider_dungeon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.yungnickyoung.minecraft.betterdungeons.BetterDungeons;
import com.yungnickyoung.minecraft.betterdungeons.world.structure.spider_dungeon.piece.SpiderDungeonBigTunnelPiece;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
public class SpiderDungeonStructure extends Structure<NoFeatureConfig> {
    /**
     * Lists of whitelisted dimensions and blacklisted biomes.
     * Will be reinitialized later w/ values from config.
     */
    public static List<String> whitelistedDimensions = Lists.newArrayList("minecraft:overworld");
    public static List<String> blacklistedBiomes = Lists.newArrayList(
        "minecraft:ocean", "minecraft:frozen_ocean", "minecraft:deep_ocean",
        "minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:cold_ocean",
        "minecraft:deep_lukewarm_ocean", "minecraft:deep_cold_ocean", "minecraft:deep_frozen_ocean",
        "minecraft:beach", "minecraft:snowy_beach",
        "minecraft:river", "minecraft:frozen_river"
    );

    public SpiderDungeonStructure() {
        super(NoFeatureConfig.field_236558_a_);
    }

    @Override
    public IStartFactory<NoFeatureConfig> getStartFactory() {
        return Start::new;
    }

    @Override
    public GenerationStage.Decoration getDecorationStage() {
        return GenerationStage.Decoration.UNDERGROUND_STRUCTURES;
    }

    // Spider dungeons can only spawn spiders & cave spiders
    private static final List<MobSpawnInfo.Spawners> STRUCTURE_MONSTERS = ImmutableList.of(
        new MobSpawnInfo.Spawners(EntityType.SPIDER, 100, 4, 15),
        new MobSpawnInfo.Spawners(EntityType.CAVE_SPIDER, 50, 4, 8)
    );

    @Override
    public List<MobSpawnInfo.Spawners> getDefaultSpawnList() {
        return STRUCTURE_MONSTERS;
    }

    @Override
    public boolean getDefaultRestrictsSpawnsToInside() {
        return true;
    }

    public static class Start extends StructureStart<NoFeatureConfig> {
        public Start(Structure<NoFeatureConfig> structureIn, int chunkX, int chunkZ, MutableBoundingBox mutableBoundingBox, int referenceIn, long seedIn) {
            super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void func_230364_a_(DynamicRegistries registryManager, ChunkGenerator chunkGenerator, TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn, NoFeatureConfig config) {
            int startX = chunkX << 4;
            int startZ = chunkZ << 4;

            // Spider dungeons use traditional code-based structure gen instead of Jigsaw
            StructurePiece startPiece = new SpiderDungeonBigTunnelPiece(startX, startZ, this.rand);
            this.components.add(startPiece);
            startPiece.buildComponent(startPiece, this.components, this.rand);

            // Set the bounds of the structure once it's assembled
            this.recalculateStructureSize();

            // Debug log the coordinates of the center starting piece.
            BetterDungeons.LOGGER.debug("Spider Dungeon at {} {} {}",
                this.components.get(0).getBoundingBox().minX,
                this.components.get(0).getBoundingBox().minY,
                this.components.get(0).getBoundingBox().minZ
            );
        }
    }
}
