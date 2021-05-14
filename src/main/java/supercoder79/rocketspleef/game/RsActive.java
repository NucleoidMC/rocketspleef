package supercoder79.rocketspleef.game;

import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import supercoder79.rocketspleef.RocketSpleef;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

import java.util.Random;

public class RsActive {
    private static final WeightedList<ItemStack> DROPS = new WeightedList<ItemStack>()
            .add(new ItemStack(Blocks.TNT), 10)
            .add(ItemStackBuilder.of(Items.GOLDEN_HOE).setUnbreakable().setName(new LiteralText("Fast Fireball Cannon")).build(), 5)
            .add(ItemStackBuilder.of(Items.DIAMOND_HOE).setUnbreakable().setName(new LiteralText("Multi Fireball Cannon")).build(), 1);

    private final GameSpace space;
    private final RsMap map;
    private final RsConfig config;
    private final PlayerSet players;
    private final GlobalWidgets widgets;
    private long gameEndTimer = -1;

    public RsActive(GameSpace space, RsMap map, RsConfig config, PlayerSet players, GlobalWidgets widgets) {
        this.space = space;
        this.map = map;
        this.config = config;
        this.players = players;
        this.widgets = widgets;
    }

    public static void open(GameSpace space, RsMap map, RsConfig config) {
        space.openGame(game -> {
            GlobalWidgets widgets = new GlobalWidgets(game);
            RsActive active = new RsActive(space, map, config, space.getPlayers(), widgets);

            game.setRule(GameRule.BREAK_BLOCKS, RuleResult.ALLOW);
            game.setRule(GameRule.PLACE_BLOCKS, RuleResult.ALLOW);
            game.setRule(GameRule.CRAFTING, RuleResult.DENY);
            game.setRule(GameRule.PORTALS, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.ALLOW);
            game.setRule(GameRule.BLOCK_DROPS, RuleResult.ALLOW);
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
            game.setRule(GameRule.HUNGER, RuleResult.DENY);
            game.setRule(GameRule.THROW_ITEMS, RuleResult.ALLOW);
            game.setRule(GameRule.UNSTABLE_TNT, RuleResult.ALLOW);
            game.setRule(RocketSpleef.REDUCE_EXPLOSION_DAMAGE, RuleResult.ALLOW);
            game.setRule(RocketSpleef.REJECT_ITEMS, RuleResult.ALLOW);

            game.on(GameOpenListener.EVENT, active::open);
            game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());

            game.on(UseItemListener.EVENT, active::onUseItem);

//			game.on(BreakBlockListener.EVENT, active::onBreak);
//			game.on(UseBlockListener.EVENT, active::onUseBlock);
//
            game.on(PlayerDeathListener.EVENT, active::onDeath);
//
//			game.on(GameCloseListener.EVENT, active::onClose);
//
            game.on(GameTickListener.EVENT, active::tick);
        });
    }

    private static void cooldownAll(ServerPlayerEntity player, int ticks) {
        player.getItemCooldownManager().set(Items.IRON_HOE, ticks);
        player.getItemCooldownManager().set(Items.GOLDEN_HOE, ticks);
        player.getItemCooldownManager().set(Items.DIAMOND_HOE, ticks);
    }

    private void tick() {
        ServerWorld world = this.space.getWorld();

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
                cooldownAll(player, 20);

                Vec3d dir = player.getRotationVec(1.0F);

                FireballEntity fireballEntity = new FireballEntity(player.world, player, dir.x * 4, dir.y * 4, dir.z * 4);
                fireballEntity.explosionPower = 3;
                fireballEntity.updatePosition(player.getX() + dir.x, player.getEyeY() + dir.y, fireballEntity.getZ() + dir.z);
                player.world.spawnEntity(fireballEntity);

                return TypedActionResult.success(stack);
            }
        }

        if (stack.getItem() == Items.GOLDEN_HOE) {
            if (!player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
                cooldownAll(player, 12);

                Vec3d dir = player.getRotationVec(1.0F);

                FireballEntity fireballEntity = new FireballEntity(player.world, player, dir.x * 6, dir.y * 6, dir.z * 6);
                fireballEntity.explosionPower = 1;
                fireballEntity.updatePosition(player.getX() + dir.x, player.getEyeY() + dir.y, fireballEntity.getZ() + dir.z);
                player.world.spawnEntity(fireballEntity);

                return TypedActionResult.success(stack);
            }
        }

        if (stack.getItem() == Items.DIAMOND_HOE) {
            if (!player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
                cooldownAll(player, 70);

                Random random = player.getRandom();
                for (int i = 0; i < 4; i++) {
                    double dx = random.nextDouble() - random.nextDouble() * random.nextDouble() * 0.1;
                    double dy = random.nextDouble() - random.nextDouble() * random.nextDouble() * 0.1;
                    double dz = random.nextDouble() - random.nextDouble() * random.nextDouble() * 0.1;
                    Vec3d dir = player.getRotationVec(1.0F).multiply(dx, dy, dz);

                    FireballEntity fireballEntity = new FireballEntity(player.world, player, dir.x * 8, dir.y * 8, dir.z * 8);
                    fireballEntity.explosionPower = 4 + random.nextInt(3);
                    fireballEntity.updatePosition(player.getX() + dir.x, player.getEyeY() + dir.y, fireballEntity.getZ() + dir.z);
                    player.world.spawnEntity(fireballEntity);
                }


                return TypedActionResult.success(stack);
            }
        }

        if (stack.getItem() == Blocks.TNT.asItem()) {

            Vec3d dir = player.getRotationVec(1.0F);

            TntEntity tnt = new TntEntity(player.world, player.getX() + dir.x, player.getEyeY() + dir.y, player.getZ() + dir.z, player);
            tnt.setVelocity(dir.x * 1.2, dir.y * 1.2, dir.z * 1.2);
            player.world.spawnEntity(tnt);

            stack.decrement(1);
        }

        return TypedActionResult.pass(stack);
    }

    public ActionResult onDeath(ServerPlayerEntity player, DamageSource source) {
        if (source instanceof EntityDamageSource) {
            this.space.getPlayers().sendMessage(new LiteralText(Formatting.RED + player.getEntityName() + " was slain by " + source.getAttacker().getEntityName()));
        } else if (source.isOutOfWorld()) {
            this.space.getPlayers().sendMessage(new LiteralText(Formatting.RED + player.getEntityName() + " fell out of the world"));
        } else if (source.isExplosive()) {
            this.space.getPlayers().sendMessage(new LiteralText(Formatting.RED + player.getEntityName() + " exploded"));
        } else {
            this.space.getPlayers().sendMessage(new LiteralText(Formatting.RED + player.getEntityName() + " died"));
        }

        RsWaiting.resetPlayer(player, GameMode.SPECTATOR);
        player.teleport(player.getServerWorld(), 0, 66, 0, 0.0F, 0.0F);

        long remaining = this.space.getPlayers().stream().filter(p -> p.interactionManager.getGameMode().isSurvivalLike()).count();
        if (remaining <= 1) {

            if (remaining <= 0) {
                this.space.getPlayers().sendMessage(new LiteralText(Formatting.AQUA + "RocketSpleef has finished!"));
            } else {
                ServerPlayerEntity lastPlayer = this.space.getPlayers().stream().filter(p -> p.interactionManager.getGameMode().isSurvivalLike()).findFirst().orElse(null);
                if (lastPlayer != null) {
                    this.space.getPlayers().sendMessage(new LiteralText(Formatting.GOLD + lastPlayer.getEntityName() + " has won!"));
                    this.space.getPlayers().sendMessage(new LiteralText(Formatting.AQUA + "RocketSpleef has finished!"));
                } else {
                    this.space.getPlayers().sendMessage(new LiteralText(Formatting.AQUA + "RocketSpleef has finished!"));
                }
            }

            this.gameEndTimer = 200;
        }

        return ActionResult.FAIL;
    }

    private void open() {
        for (ServerPlayerEntity player : this.players) {
            player.inventory.insertStack(ItemStackBuilder.of(Items.IRON_HOE).setUnbreakable().setName(new LiteralText("Fireball Cannon")).build());
        }
    }
}
