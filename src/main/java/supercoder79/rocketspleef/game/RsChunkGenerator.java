package supercoder79.rocketspleef.game;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.noise.NoiseConfig;
import xyz.nucleoid.plasmid.game.world.generator.GameChunkGenerator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RsChunkGenerator extends GameChunkGenerator {
    private final PerlinNoiseSampler colorNoise;

    public RsChunkGenerator(MinecraftServer server) {
        super(createBiomeSource(server, BiomeKeys.PLAINS));
        this.colorNoise = new PerlinNoiseSampler(Random.create(server.getOverworld().getSeed()));
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig config, StructureAccessor accessor, Chunk chunk) {
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();

        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                for (int y = 0; y < 256; y++) {
                    int manhattan = Math.abs(x / 2) + Math.abs(z / 2) + Math.abs((y - 64) / 2);

                    double progress = (y - 31) / 66.0;

                    progress += this.colorNoise.sample(x / 8.0, y / 8.0, z / 8.0) * 0.05;

                    Block glass;
                    if (progress < (1 / 6.0)) {
                        glass = Blocks.RED_STAINED_GLASS;
                    } else if (progress < (1 / 3.0)) {
                        glass = Blocks.ORANGE_STAINED_GLASS;
                    } else if (progress < (1 / 2.0)) {
                        glass = Blocks.YELLOW_STAINED_GLASS;
                    } else if (progress < (2 / 3.0)) {
                        glass = Blocks.LIME_STAINED_GLASS;
                    } else if (progress < (5 / 6.0)) {
                        glass = Blocks.BLUE_STAINED_GLASS;
                    } else {
                        glass = Blocks.PURPLE_STAINED_GLASS;
                    }

                    if (manhattan <= 16) {
                        if (manhattan >= 12) {
                            chunk.setBlockState(new BlockPos(x, y, z), glass.getDefaultState(), false);
                        }

                        if (y <= 64 && y % 4 == 0) {
                            chunk.setBlockState(new BlockPos(x, y, z), glass.getDefaultState(), false);
                        }

                        if (manhattan == 11 && ((x - 1) % 2 == 0 && (z - 1) % 2 == 0 && (y - 1) % 2 == 0)) {
                            chunk.setBlockState(new BlockPos(x, y, z), glass .getDefaultState(), false);
                        }
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }
}