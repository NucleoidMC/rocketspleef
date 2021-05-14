package supercoder79.rocketspleef.game;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class RsMap {
    private final RsConfig config;

    public RsMap(RsConfig config) {
        this.config = config;
    }

    public ChunkGenerator createGenerator(MinecraftServer server) {
        return new RsChunkGenerator(server);
    }
}
