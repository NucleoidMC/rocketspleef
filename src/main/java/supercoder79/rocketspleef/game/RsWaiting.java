package supercoder79.rocketspleef.game;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.plasmid.api.game.*;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.Set;

public final class RsWaiting {
    private final ServerLevel world;
    private final GameSpace space;
    private final RsMap map;
    private final RsConfig config;

    private RsWaiting(GameSpace space, RsMap map, RsConfig config, ServerLevel world) {
        this.world = world;
        this.space = space;
        this.map = map;
        this.config = config;
    }

    public static GameOpenProcedure open(GameOpenContext<RsConfig> context) {
//        RsConfig config = context.getConfig();
//
//        RsMap map = new RsMap(config);
//        BubbleWorldConfig worldConfig = new BubbleWorldConfig()
//                .setGenerator(map.createGenerator(context.getServer()))
//                .setDefaultGameMode(GameMode.SPECTATOR)
//                .setSpawner(BubbleWorldSpawner.atSurface(0, 0))
//                .setTimeOfDay(6000)
//                .setDifficulty(Difficulty.NORMAL);
//
//        return context.createOpenProcedure(worldConfig, (game) -> {
//            RsWaiting waiting = new RsWaiting(game.getSpace(), map, context.getConfig());
//
//            GameWaitingLobby.applyTo(game, context.getConfig().playerConfig);
//
//            game.setRule(GameRule.CRAFTING, RuleResult.DENY);
//            game.setRule(GameRule.PORTALS, RuleResult.DENY);
//            game.setRule(GameRule.PVP, RuleResult.DENY);
//            game.setRule(GameRule.BLOCK_DROPS, RuleResult.DENY);
//            game.setRule(GameRule.HUNGER, RuleResult.DENY);
//            game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
//
//            game.on(RequestStartListener.EVENT, waiting::requestStart);
//
//            game.on(PlayerAddListener.EVENT, waiting::addPlayer);
//            game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
//        });

        RsConfig config = context.config();

        RsMap map = new RsMap(config);

        RuntimeLevelConfig worldConfig = new RuntimeLevelConfig()
                .setFlat(true)
                .setGenerator(map.createGenerator(context.server()));


        return context.openWithLevel(worldConfig, (game, world) -> {
            RsWaiting waiting = new RsWaiting(game.getGameSpace(), map, context.config(), world);
            GameWaitingLobby.addTo(game, context.config().playerConfig());

            game.listen(GameActivityEvents.REQUEST_START, () -> waiting.requestStart(world));
            game.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
            game.listen(GamePlayerEvents.ACCEPT, offer -> offer.teleport(world, new Vec3(0, 70, 0)));
        });
    }

    public static void resetPlayer(ServerPlayer player, GameType mode) {
        player.getInventory().clearContent();
        player.getEnderChestInventory().clearContent();
        player.removeAllEffects();
        player.setHealth(20.0F);
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().eat(5, 0.5F);
        player.fallDistance = 0.0F;
        player.setGameMode(mode);
        player.setExperienceLevels(0);
        player.setExperiencePoints(0);
    }

    private GameResult requestStart(ServerLevel world) {
        RsActive.open(this.world, this.space, this.map, this.config);
        return GameResult.ok();
    }

    private void addPlayer(ServerPlayer player) {
        this.spawnPlayer(player);
    }

    private EventResult onPlayerDeath(ServerPlayer player, DamageSource source) {
        this.spawnPlayer(player);
        return EventResult.DENY;
    }

    private void spawnPlayer(ServerPlayer player) {
        resetPlayer(player, GameType.SURVIVAL);

        ChunkPos chunkPos = new ChunkPos(0, 0);
        this.world.getChunkSource().addTicket(new Ticket(TicketType.PLAYER_LOADING, 1), chunkPos);

        player.teleportTo(this.world, 0, 66, 0, Set.of(), 0.0F, 0.0F, true);
    }
}