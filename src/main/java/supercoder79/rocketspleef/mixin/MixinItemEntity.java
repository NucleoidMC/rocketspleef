package supercoder79.rocketspleef.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supercoder79.rocketspleef.RocketSpleef;
import supercoder79.rocketspleef.item.RsItems;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity {
    @Shadow
    public abstract ItemStack getStack();

    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    private void rejectPlayersWithItem(PlayerEntity player, CallbackInfo ci) {
        ManagedGameSpace gameSpace = GameSpaceManager.get().byWorld(player.getEntityWorld());

        if (gameSpace != null && gameSpace.getBehavior().testRule(RocketSpleef.REJECT_ITEMS) == ActionResult.SUCCESS) {
            // TODO: some sort of registry something for this
            Item item = this.getStack().getItem();

            if (item == RsItems.FIREBALL_CANNON && player.getInventory().count(RsItems.FIREBALL_CANNON) > 0) {
                ci.cancel();
                return;
            }

            if (item == RsItems.FAST_FIREBALL_CANNON && player.getInventory().count(RsItems.FAST_FIREBALL_CANNON) > 0) {
                ci.cancel();
                return;
            }

            if (item == RsItems.MULTI_FIREBALL_CANNON && player.getInventory().count(RsItems.MULTI_FIREBALL_CANNON) > 0) {
                ci.cancel();
                return;
            }
        }
    }
}
