package supercoder79.rocketspleef.game;

import supercoder79.rocketspleef.RocketSpleef;
import supercoder79.rocketspleef.util.WeightedList;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinIntent;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.util.ItemStackBuilder;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class RsActive {
    private static final WeightedList<ItemStack> DROPS = new WeightedList<ItemStack>()
            .add(new ItemStack(Blocks.TNT), 10)
            .add(ItemStackBuilder.of(Items.GOLDEN_HOE).setUnbreakable().setName(Component.literal("Fast Fireball Cannon")).build(), 5)
            .add(ItemStackBuilder.of(Items.DIAMOND_HOE).setUnbreakable().setName(Component.literal("Multi Fireball Cannon")).build(), 1);

    private final ServerLevel world;
    private final GameSpace space;
    private final RsMap map;
    private final RsConfig config;
    private final GlobalWidgets widgets;
    private long gameEndTimer = -1;

    public RsActive(ServerLevel world, GameSpace space, RsMap map, RsConfig config, PlayerSet players, GlobalWidgets widgets) {
        this.world = world;
        this.space = space;
        this.map = map;
        this.config = config;
        this.widgets = widgets;
    }

    public static void open(ServerLevel world, GameSpace space, RsMap map, RsConfig config) {
        space.setActivity(game -> {
            GlobalWidgets widgets = GlobalWidgets.addTo(game);
            RsActive active = new RsActive(world, space, map, config, space.getPlayers().participants(), widgets);

            game.setRule(GameRuleType.BREAK_BLOCKS, EventResult.ALLOW);
            game.setRule(GameRuleType.PLACE_BLOCKS, EventResult.ALLOW);
            game.setRule(GameRuleType.CRAFTING, EventResult.DENY);
            game.setRule(GameRuleType.PORTALS, EventResult.DENY);
            game.setRule(GameRuleType.PVP, EventResult.ALLOW);
            game.setRule(GameRuleType.BLOCK_DROPS, EventResult.ALLOW);
            game.setRule(GameRuleType.FALL_DAMAGE, EventResult.DENY);
            game.setRule(GameRuleType.HUNGER, EventResult.DENY);
            game.setRule(GameRuleType.THROW_ITEMS, EventResult.ALLOW);
            game.setRule(GameRuleType.UNSTABLE_TNT, EventResult.ALLOW);
            game.setRule(RocketSpleef.REDUCE_EXPLOSION_DAMAGE, EventResult.ALLOW);
            game.setRule(RocketSpleef.REJECT_ITEMS, EventResult.ALLOW);

            game.listen(GameActivityEvents.CREATE, active::open);
            game.listen(GamePlayerEvents.OFFER, offer -> offer.intent() == JoinIntent.SPECTATE ? offer.accept() : offer.pass());
            game.listen(GamePlayerEvents.ACCEPT, offer -> offer.teleport(world, new Vec3(0, 70, 0)));
            game.listen(GamePlayerEvents.ADD, player -> {});

            game.listen(ItemUseEvent.EVENT, active::onUseItem);

//			game.on(BreakBlockListener.EVENT, active::onBreak);
//			game.on(UseBlockListener.EVENT, active::onUseBlock);
//
            game.listen(PlayerDeathEvent.EVENT, active::onDeath);
//
//			game.on(GameCloseListener.EVENT, active::onClose);
//
            game.listen(GameActivityEvents.TICK, active::tick);
        });
    }

    private static void cooldown(ServerPlayer player, ItemStack item, int ticks) {
        player.getCooldowns().addCooldown(item, ticks);
    }

    private void tick() {
        ServerLevel world = this.world;

        if (world.getGameTime() % 30 == 0) {
            RandomSource random = world.getRandom();
            BlockPos pos = new BlockPos(random.nextInt(32) - random.nextInt(32), 64 + (random.nextInt(32) - random.nextInt(32)), random.nextInt(32) - random.nextInt(32));

            if (world.getBlockState(pos).isAir()) {
                world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), DROPS.pickRandom(random).copy()));
            }
        }

        for (ServerPlayer player : this.space.getPlayers()) {
            if (player.getY() < 16 && player.isAlive() && !player.isSpectator()) {
                player.kill(player.level());
            }
        }

        if (this.gameEndTimer > 0) {
            this.gameEndTimer--;
        }

        if (this.gameEndTimer == 0) {
            this.space.close(GameCloseReason.FINISHED);
        }
    }

    public InteractionResult onUseItem(ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() == Items.IRON_HOE) {
            if (!player.getCooldowns().isOnCooldown(stack)) {
                cooldown(player, stack, 20);

                Vec3 dir = player.getViewVector(1.0F);

                LargeFireball fireballEntity = new LargeFireball(player.level(), player, new Vec3(dir.x * 4, dir.y * 4, dir.z * 4), 3);
                fireballEntity.absSnapTo(player.getX() + dir.x, player.getEyeY() + dir.y, fireballEntity.getZ() + dir.z);
                player.level().addFreshEntity(fireballEntity);

                return InteractionResult.SUCCESS_SERVER;
            }
        }

        if (stack.getItem() == Items.GOLDEN_HOE) {
            if (!player.getCooldowns().isOnCooldown(stack)) {
                cooldown(player, stack,12);

                Vec3 dir = player.getViewVector(1.0F);

                LargeFireball fireballEntity = new LargeFireball(player.level(), player, dir.scale(6), 1);
                fireballEntity.absSnapTo(player.getX() + dir.x, player.getEyeY() + dir.y, fireballEntity.getZ() + dir.z);
                player.level().addFreshEntity(fireballEntity);

                return InteractionResult.SUCCESS_SERVER;
            }
        }

        if (stack.getItem() == Items.DIAMOND_HOE) {
            if (!player.getCooldowns().isOnCooldown(stack)) {
                cooldown(player, stack, 70);

                RandomSource random = player.getRandom();
                for (int i = 0; i < 4; i++) {
                    double dx = random.nextDouble() - random.nextDouble() * random.nextDouble() * 0.1;
                    double dy = random.nextDouble() - random.nextDouble() * random.nextDouble() * 0.1;
                    double dz = random.nextDouble() - random.nextDouble() * random.nextDouble() * 0.1;
                    Vec3 dir = player.getViewVector(1.0F).multiply(dx, dy, dz);

                    LargeFireball fireballEntity = new LargeFireball(player.level(), player, dir.scale(8), 4 + random.nextInt(3));
                    fireballEntity.absSnapTo(player.getX() + dir.x, player.getEyeY() + dir.y, fireballEntity.getZ() + dir.z);
                    player.level().addFreshEntity(fireballEntity);
                }


                /*var yaw = player.getYaw();
                var pitch = player.getPitch();
                float multDir = 64;
                float multSide = 60;
                /*{
                    Vec3d dir = Vec3d.fromPolar(pitch, yaw);

                    FireballEntity fireballEntity = new FireballEntity(player.getWorld(), player, dir.x * multDir, dir.y * multDir, dir.z * multDir, 4);
                    fireballEntity.updatePosition(player.getX() + dir.x * 0.5, player.getEyeY() + dir.y * 0.5, fireballEntity.getZ() + dir.z * 0.5);
                    player.getWorld().spawnEntity(fireballEntity);
                }
                for (int x = 0; x < 2; x++) {
                    for (int y = 0; y < 2; y++) {
                        Vec3d dir = Vec3d.fromPolar((y - 0.5f) * 16, (x - 0.5f) * 16).rotateY(pitch).rotateX(yaw);

                        FireballEntity fireballEntity = new FireballEntity(player.getWorld(), player, dir.x * multSide, dir.y * multSide, dir.z * multSide, 2 + random.nextInt(2));

                        fireballEntity.updatePosition(player.getX() + dir.x * 1.2, player.getEyeY() + dir.y * 1.2, fireballEntity.getZ() + dir.z * 1.2);
                        player.getWorld().spawnEntity(fireballEntity);
                    }
                }*/

                return InteractionResult.SUCCESS_SERVER;
            }
        }

        if (stack.getItem() == Blocks.TNT.asItem()) {

            Vec3 dir = player.getViewVector(1.0F);

            PrimedTnt tnt = new PrimedTnt(player.level(), player.getX() + dir.x, player.getEyeY() + dir.y, player.getZ() + dir.z, player);
            tnt.setDeltaMovement(dir.x * 1.2, dir.y * 1.2, dir.z * 1.2);
            player.level().addFreshEntity(tnt);

            stack.shrink(1);
        }

        return InteractionResult.PASS;
    }

    public EventResult onDeath(ServerPlayer player, DamageSource source) {
        if (player.isSpectator() || this.gameEndTimer != -1) {
            player.teleportTo(player.level(), 0, 66, 0, Set.of(), 0.0F, 0.0F, true);
            RsWaiting.resetPlayer(player, GameType.SPECTATOR);
            return EventResult.DENY;
        }

        this.space.getPlayers().sendMessage(Component.empty().withStyle(ChatFormatting.RED).append(source.getLocalizedDeathMessage(player)));

        RsWaiting.resetPlayer(player, GameType.SPECTATOR);
        player.teleportTo(player.level(), 0, 66, 0, Set.of(), 0.0F, 0.0F, true);

        long remaining = this.space.getPlayers().stream().filter(p -> p.gameMode.isSurvival()).count();
        if (remaining <= 1) {
            if (remaining == 1) {
                ServerPlayer lastPlayer = this.space.getPlayers().stream().filter(p -> p.gameMode.isSurvival()).findFirst().orElse(null);
                if (lastPlayer != null) {
                    this.space.getPlayers().sendMessage(Component.translatable("text.rocket_spleef.player_won", lastPlayer.getName()).withStyle(ChatFormatting.GOLD));
                }
            }
            this.space.getPlayers().sendMessage(Component.translatable("text.rocket_spleef.game_ended").withStyle(ChatFormatting.AQUA));
            this.gameEndTimer = 20 * 5;
        }

        return EventResult.DENY;
    }

    private void open() {
        for (ServerPlayer player : this.space.getPlayers().participants()) {
            player.getInventory().add(ItemStackBuilder.of(Items.IRON_HOE).setUnbreakable().setName(Component.literal("Fireball Cannon")).build());
        }
    }
}
