package supercoder79.rocketspleef.game;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.fantasy.BubbleWorldSpawner;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

public final class RsWaiting {
    private final GameSpace world;
    private final RsMap map;
    private final RsConfig config;

    private RsWaiting(GameSpace world, RsMap map, RsConfig config) {
        this.world = world;
        this.map = map;
        this.config = config;
    }

    public static GameOpenProcedure open(GameOpenContext<RsConfig> context) {
        RsConfig config = context.getConfig();

        RsMap map = new RsMap(config);
        BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                .setGenerator(map.createGenerator(context.getServer()))
                .setDefaultGameMode(GameMode.SPECTATOR)
                .setSpawner(BubbleWorldSpawner.atSurface(0, 0))
                .setTimeOfDay(6000)
                .setDifficulty(Difficulty.NORMAL);

        return context.createOpenProcedure(worldConfig, (game) -> {
            RsWaiting waiting = new RsWaiting(game.getSpace(), map, context.getConfig());

            GameWaitingLobby.applyTo(game, context.getConfig().playerConfig);

            game.setRule(GameRule.CRAFTING, RuleResult.DENY);
            game.setRule(GameRule.PORTALS, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.DENY);
            game.setRule(GameRule.BLOCK_DROPS, RuleResult.DENY);
            game.setRule(GameRule.HUNGER, RuleResult.DENY);
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);

            game.on(RequestStartListener.EVENT, waiting::requestStart);

            game.on(PlayerAddListener.EVENT, waiting::addPlayer);
            game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
        });
    }

    public static void resetPlayer(ServerPlayerEntity player, GameMode mode) {
        player.inventory.clear();
        player.getEnderChestInventory().clear();
        player.clearStatusEffects();
        player.setHealth(20.0F);
        player.getHungerManager().setFoodLevel(20);
        player.getHungerManager().add(5, 0.5F);
        player.fallDistance = 0.0F;
        player.setGameMode(mode);
        player.setExperienceLevel(0);
        player.setExperiencePoints(0);
    }

    private StartResult requestStart() {
        RsActive.open(this.world, this.map, this.config);
        return StartResult.OK;
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

        ServerWorld world = this.world.getWorld();

        ChunkPos chunkPos = new ChunkPos(0, 0);
        world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getEntityId());

        player.teleport(world, 0, 66, 0, 0.0F, 0.0F);
    }
}