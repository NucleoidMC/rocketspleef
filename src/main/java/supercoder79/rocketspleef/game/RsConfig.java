package supercoder79.rocketspleef.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;

public record RsConfig(WaitingLobbyConfig playerConfig) {
    public static final MapCodec<RsConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig)
    ).apply(instance, RsConfig::new));
}
