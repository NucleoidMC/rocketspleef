package supercoder79.rocketspleef;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import supercoder79.rocketspleef.game.RsConfig;
import supercoder79.rocketspleef.game.RsWaiting;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;

public final class RocketSpleef implements ModInitializer {
    public static final GameRuleType REDUCE_EXPLOSION_DAMAGE = GameRuleType.create();
    public static final GameRuleType REJECT_ITEMS = GameRuleType.create();

    @Override
    public void onInitialize() {
        GameType.register(
                new Identifier("rocketspleef", "rocketspleef"),
                RsConfig.CODEC,
                RsWaiting::open
        );
    }
}
