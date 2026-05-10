package supercoder79.rocketspleef.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import supercoder79.rocketspleef.RocketSpleef;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(ServerExplosion.class)
public class MixinExplosion {
    @Shadow @Final private ServerLevel level;

    @ModifyArg(method = "hurtEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private float reduceDamage(float amount) {
        var gameSpace = GameSpaceManager.get().byLevel(this.level);

        if (gameSpace != null && gameSpace.getBehavior().testRule(RocketSpleef.REDUCE_EXPLOSION_DAMAGE) == EventResult.ALLOW) {
            return Math.min(amount, 2);
        } else {
            return amount;
        }
    }
}
