package supercoder79.rocketspleef.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;

public class RsConfig {
    public static final Codec<RsConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig)
    ).apply(instance, RsConfig::new));

    public final PlayerConfig playerConfig;

    public RsConfig(PlayerConfig playerConfig) {
        this.playerConfig = playerConfig;
    }
}
