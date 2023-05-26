package supercoder79.rocketspleef.game;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public final class RsWaiting {
    private final ServerWorld world;
    private final GameSpace space;
    private final RsMap map;
    private final RsConfig config;

    private RsWaiting(GameSpace space, RsMap map, RsConfig config, ServerWorld world) {
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

        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setTimeOfDay(6000)
                .setWorldConstructor(FakeFlatWorld::new)
                .setGenerator(map.createGenerator(context.server()));


        return context.openWithWorld(worldConfig, (game, world) -> {
            RsWaiting waiting = new RsWaiting(game.getGameSpace(), map, context.config(), world);
            GameWaitingLobby.addTo(game, context.config().playerConfig);

            game.listen(GameActivityEvents.REQUEST_START, () -> waiting.requestStart(world));
            game.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
            game.listen(GamePlayerEvents.OFFER, offer -> offer.accept(world, new Vec3d(0, 70, 0)));
        });
    }

    public static void resetPlayer(ServerPlayerEntity player, GameMode mode) {
        player.getInventory().clear();
        player.getEnderChestInventory().clear();
        player.clearStatusEffects();
        player.setHealth(20.0F);
        player.getHungerManager().setFoodLevel(20);
        player.getHungerManager().add(5, 0.5F);
        player.fallDistance = 0.0F;
        player.interactionManager.changeGameMode(mode);
        player.setExperienceLevel(0);
        player.setExperiencePoints(0);
    }

    private GameResult requestStart(ServerWorld world) {
        RsActive.open(this.world, this.space, this.map, this.config);
        return GameResult.ok();
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnPlayer(player);
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        this.spawnPlayer(player);
        return ActionResult.FAIL;
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        resetPlayer(player, GameMode.SURVIVAL);

        ChunkPos chunkPos = new ChunkPos(0, 0);
        this.world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getId());

        player.teleport(this.world, 0, 66, 0, 0.0F, 0.0F);
    }
}