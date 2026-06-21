package supercoder79.rocketspleef.game;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import xyz.nucleoid.plasmid.api.game.level.generator.GameChunkGenerator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RsChunkGenerator extends GameChunkGenerator {
    private final ImprovedNoise colorNoise;

    public RsChunkGenerator(MinecraftServer server) {
        super(createBiomeSource(server, Biomes.PLAINS));
        this.colorNoise = new ImprovedNoise(RandomSource.create(server.overworld().getSeed()));
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState noiseConfig, StructureManager structureAccessor, ChunkAccess chunk) {
        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();

        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                for (int y = 0; y < 256; y++) {
                    int manhattan = Math.abs(x / 2) + Math.abs(z / 2) + Math.abs((y - 64) / 2);

                    double progress = (y - 31) / 66.0;

                    progress += this.colorNoise.noise(x / 8.0, y / 8.0, z / 8.0) * 0.05;

                    Block glass;
                    if (progress < (1 / 6.0)) {
                        glass = Blocks.STAINED_GLASS.red();
                    } else if (progress < (1 / 3.0)) {
                        glass = Blocks.STAINED_GLASS.orange();
                    } else if (progress < (1 / 2.0)) {
                        glass = Blocks.STAINED_GLASS.yellow();
                    } else if (progress < (2 / 3.0)) {
                        glass = Blocks.STAINED_GLASS.lime();
                    } else if (progress < (5 / 6.0)) {
                        glass = Blocks.STAINED_GLASS.blue();
                    } else {
                        glass = Blocks.STAINED_GLASS.purple();
                    }

                    if (manhattan <= 16) {
                        if (manhattan >= 12) {
                            chunk.setBlockState(new BlockPos(x, y, z), glass.defaultBlockState());
                        }

                        if (y <= 64 && y % 4 == 0) {
                            chunk.setBlockState(new BlockPos(x, y, z), glass.defaultBlockState());
                        }

                        if (manhattan == 11 && ((x - 1) % 2 == 0 && (z - 1) % 2 == 0 && (y - 1) % 2 == 0)) {
                            chunk.setBlockState(new BlockPos(x, y, z), glass.defaultBlockState());
                        }
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }
}