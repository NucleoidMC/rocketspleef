package supercoder79.rocketspleef.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supercoder79.rocketspleef.RocketSpleef;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity {
    @Shadow
    public abstract ItemStack getItem();

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void rejectPlayersWithItem(Player player, CallbackInfo ci) {
        var gameSpace = GameSpaceManager.get().byLevel(player.level());

        if (gameSpace != null && gameSpace.getBehavior().testRule(RocketSpleef.REJECT_ITEMS) == EventResult.ALLOW) {
            // TODO: some sort of registry something for this
            Item item = this.getItem().getItem();

            if (item == Items.IRON_HOE && player.getInventory().countItem(Items.IRON_HOE) > 0) {
                ci.cancel();
                return;
            }

            if (item == Items.GOLDEN_HOE && player.getInventory().countItem(Items.GOLDEN_HOE) > 0) {
                ci.cancel();
                return;
            }

            if (item == Items.DIAMOND_HOE && player.getInventory().countItem(Items.DIAMOND_HOE) > 0) {
                ci.cancel();
                return;
            }
        }
    }
}
