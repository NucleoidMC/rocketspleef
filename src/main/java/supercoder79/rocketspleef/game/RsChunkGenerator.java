package supercoder79.rocketspleef.game;

import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.StructuresConfig;
import xyz.nucleoid.plasmid.game.world.generator.GameChunkGenerator;

import java.util.Collections;
import java.util.Optional;

public class RsChunkGenerator extends GameChunkGenerator {

    public RsChunkGenerator(MinecraftServer server) {
        super(createBiomeSource(server, BiomeKeys.PLAINS), new StructuresConfig(Optional.empty(), Collections.emptyMap()));
    }

    @Override
    public void populateNoise(WorldAccess world, StructureAccessor structures, Chunk chunk) {
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();

        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                for (int y = 0; y < 256; y++) {
                    int manhattan = Math.abs(x / 2) + Math.abs(z / 2) + Math.abs((y - 64) / 2);

                    if (manhattan <= 16) {
                        if (manhattan >= 12) {
                            world.setBlockState(new BlockPos(x, y, z), Blocks.BLACK_STAINED_GLASS.getDefaultState(), 3);
                        }

                        if (y <= 64 && y % 4 == 0) {
                            world.setBlockState(new BlockPos(x, y, z), Blocks.BLACK_STAINED_GLASS.getDefaultState(), 3);
                        }

                        if (manhattan == 11 && ((x - 1) % 2 == 0 && (z - 1) % 2 == 0 && (y - 1) % 2 == 0)) {
                            world.setBlockState(new BlockPos(x, y, z), Blocks.BLACK_STAINED_GLASS.getDefaultState(), 3);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void carve(long seed, BiomeAccess access, Chunk chunk, GenerationStep.Carver carver) {

    }
}
