package supercoder79.rocketspleef.game;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import xyz.nucleoid.fantasy.RuntimeWorld;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

public class FakeFlatWorld extends RuntimeWorld {

    protected FakeFlatWorld(MinecraftServer server, RegistryKey<World> registryKey, RuntimeWorldConfig config, Style style) {
        super(server, registryKey, config, style);
    }

    @Override
    public boolean isFlat() {
        return true;
    }
}
