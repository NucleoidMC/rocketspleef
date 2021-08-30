package supercoder79.rocketspleef.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supercoder79.rocketspleef.RocketSpleef;
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

            if (item == Items.IRON_HOE && player.getInventory().count(Items.IRON_HOE) > 0) {
                ci.cancel();
                return;
            }

            if (item == Items.GOLDEN_HOE && player.getInventory().count(Items.GOLDEN_HOE) > 0) {
                ci.cancel();
                return;
            }

            if (item == Items.DIAMOND_HOE && player.getInventory().count(Items.DIAMOND_HOE) > 0) {
                ci.cancel();
                return;
            }
        }
    }
}
