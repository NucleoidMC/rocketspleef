package supercoder79.rocketspleef.game;

import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import supercoder79.rocketspleef.RocketSpleef;
import supercoder79.rocketspleef.util.WeightedList;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class RsActive {
    private static final WeightedList<ItemStack> DROPS = new WeightedList<ItemStack>()
            .add(new ItemStack(Blocks.TNT), 10)
            .add(ItemStackBuilder.of(Items.GOLDEN_HOE).setUnbreakable().setName(Text.literal("Fast Fireball Cannon")).build(), 5)
            .add(ItemStackBuilder.of(Items.DIAMOND_HOE).setUnbreakable().setName(Text.literal("Multi Fireball Cannon")).build(), 1);

    private final ServerWorld world;
    private final GameSpace space;
    private final RsMap map;
    private final RsConfig config;
    private final PlayerSet players;
    private final GlobalWidgets widgets;
    private long gameEndTimer = -1;

    public RsActive(ServerWorld world, GameSpace space, RsMap map, RsConfig config, PlayerSet players, GlobalWidgets widgets) {
        this.world = world;
        this.space = space;
        this.map = map;
        this.config = config;
        this.players = players;
        this.widgets = widgets;
    }

    public static void open(ServerWorld world, GameSpace space, RsMap map, RsConfig config) {
        space.setActivity(game -> {
            GlobalWidgets widgets = GlobalWidgets.addTo(game);
            RsActive active = new RsActive(world, space, map, config, space.getPlayers(), widgets);

            game.setRule(GameRuleType.BREAK_BLOCKS, ActionResult.SUCCESS);
            game.setRule(GameRuleType.PLACE_BLOCKS, ActionResult.SUCCESS);
            game.setRule(GameRuleType.CRAFTING, ActionResult.FAIL);
            game.setRule(GameRuleType.PORTALS, ActionResult.FAIL);
            game.setRule(GameRuleType.PVP, ActionResult.SUCCESS);
            game.setRule(GameRuleType.BLOCK_DROPS, ActionResult.SUCCESS);
            game.setRule(GameRuleType.FALL_DAMAGE, ActionResult.FAIL);
            game.setRule(GameRuleType.HUNGER, ActionResult.FAIL);
            game.setRule(GameRuleType.THROW_ITEMS, ActionResult.SUCCESS);
            game.setRule(GameRuleType.UNSTABLE_TNT, ActionResult.SUCCESS);
            game.setRule(RocketSpleef.REDUCE_EXPLOSION_DAMAGE, ActionResult.SUCCESS);
            game.setRule(RocketSpleef.REJECT_ITEMS, ActionResult.SUCCESS);

            game.listen(GameActivityEvents.CREATE, active::open);
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

    private static void cooldown(ServerPlayerEntity player, Item item, int ticks) {
        player.getItemCooldownManager().set(item, ticks);
    }

    private void tick() {
        ServerWorld world = this.world;

        if (world.getTime() % 30 == 0) {
            Random random = world.getRandom();
            BlockPos pos = new BlockPos(random.nextInt(32) - random.nextInt(32), 64 + (random.nextInt(32) - random.nextInt(32)), random.nextInt(32) - random.nextInt(32));

            if (world.getBlockState(pos).isAir()) {
                world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), DROPS.pickRandom(random).copy()));
            }
        }

        for (ServerPlayerEntity player : this.space.getPlayers()) {
            if (player.getY() < 16 && player.isAlive() && !player.isSpectator()) {
                player.kill();
            }
        }

        if (this.gameEndTimer > 0) {
            this.gameEndTimer--;
        }

        if (this.gameEndTimer == 0) {
            this.space.close(GameCloseReason.FINISHED);
        }
    }

    public TypedActionResult<ItemStack> onUseItem(ServerPlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.getItem() == Items.IRON_HOE) {
            if (!player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
                cooldown(player, stack.getItem(), 20);

                Vec3d dir = player.getRotationVec(1.0F);

                FireballEntity fireballEntity = new FireballEntity(player.getWorld(), player, dir.x * 4, dir.y * 4, dir.z * 4, 3);
                fireballEntity.updatePosition(player.getX() + dir.x, player.getEyeY() + dir.y, fireballEntity.getZ() + dir.z);
                player.getWorld().spawnEntity(fireballEntity);

                return TypedActionResult.success(stack);
            }
        }

        if (stack.getItem() == Items.GOLDEN_HOE) {
            if (!player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
                cooldown(player, stack.getItem(),12);

                Vec3d dir = player.getRotationVec(1.0F);

                FireballEntity fireballEntity = new FireballEntity(player.getWorld(), player, dir.x * 6, dir.y * 6, dir.z * 6, 1);
                fireballEntity.updatePosition(player.getX() + dir.x, player.getEyeY() + dir.y, fireballEntity.getZ() + dir.z);
                player.getWorld().spawnEntity(fireballEntity);

                return TypedActionResult.success(stack);
            }
        }

        if (stack.getItem() == Items.DIAMOND_HOE) {
            if (!player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
                cooldown(player, stack.getItem(), 70);

                Random random = player.getRandom();
                for (int i = 0; i < 4; i++) {
                    double dx = random.nextDouble() - random.nextDouble() * random.nextDouble() * 0.1;
                    double dy = random.nextDouble() - random.nextDouble() * random.nextDouble() * 0.1;
                    double dz = random.nextDouble() - random.nextDouble() * random.nextDouble() * 0.1;
                    Vec3d dir = player.getRotationVec(1.0F).multiply(dx, dy, dz);

                    FireballEntity fireballEntity = new FireballEntity(player.getWorld(), player, dir.x * 8, dir.y * 8, dir.z * 8, 4 + random.nextInt(3));
                    fireballEntity.updatePosition(player.getX() + dir.x, player.getEyeY() + dir.y, fireballEntity.getZ() + dir.z);
                    player.getWorld().spawnEntity(fireballEntity);
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

                return TypedActionResult.success(stack);
            }
        }

        if (stack.getItem() == Blocks.TNT.asItem()) {

            Vec3d dir = player.getRotationVec(1.0F);

            TntEntity tnt = new TntEntity(player.getWorld(), player.getX() + dir.x, player.getEyeY() + dir.y, player.getZ() + dir.z, player);
            tnt.setVelocity(dir.x * 1.2, dir.y * 1.2, dir.z * 1.2);
            player.getWorld().spawnEntity(tnt);

            stack.decrement(1);
        }

        return TypedActionResult.pass(stack);
    }

    public ActionResult onDeath(ServerPlayerEntity player, DamageSource source) {
        if (player.isSpectator() || this.gameEndTimer != -1) {
            player.teleport(player.getServerWorld(), 0, 66, 0, 0.0F, 0.0F);
            RsWaiting.resetPlayer(player, GameMode.SPECTATOR);
            return ActionResult.FAIL;
        }

        this.space.getPlayers().sendMessage(Text.empty().formatted(Formatting.RED).append(source.getDeathMessage(player)));

        RsWaiting.resetPlayer(player, GameMode.SPECTATOR);
        player.teleport(player.getServerWorld(), 0, 66, 0, 0.0F, 0.0F);

        long remaining = this.space.getPlayers().stream().filter(p -> p.interactionManager.getGameMode().isSurvivalLike()).count();
        if (remaining <= 1) {
            if (remaining == 1) {
                ServerPlayerEntity lastPlayer = this.space.getPlayers().stream().filter(p -> p.interactionManager.getGameMode().isSurvivalLike()).findFirst().orElse(null);
                if (lastPlayer != null) {
                    this.space.getPlayers().sendMessage(Text.translatable("text.rocket_spleef.player_won", lastPlayer.getEntityName()).formatted(Formatting.GOLD));
                }
            }
            this.space.getPlayers().sendMessage(Text.translatable("text.rocket_spleef.game_ended").formatted(Formatting.AQUA));
            this.gameEndTimer = 20 * 5;
        }

        return ActionResult.FAIL;
    }

    private void open() {
        for (ServerPlayerEntity player : this.players) {
            player.getInventory().insertStack(ItemStackBuilder.of(Items.IRON_HOE).setUnbreakable().setName(Text.literal("Fireball Cannon")).build());
        }
    }
}
