package supercoder79.rocketspleef;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import supercoder79.rocketspleef.game.RsConfig;
import supercoder79.rocketspleef.game.RsWaiting;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.game.rule.GameRule;

public final class RocketSpleef implements ModInitializer {
    public static final GameRule REDUCE_EXPLOSION_DAMAGE = new GameRule();
    public static final GameRule REJECT_ITEMS = new GameRule();

    @Override
    public void onInitialize() {
        GameType.register(
                new Identifier("rocketspleef", "rocketspleef"),
                RsWaiting::open,
                RsConfig.CODEC
        );
    }
}
