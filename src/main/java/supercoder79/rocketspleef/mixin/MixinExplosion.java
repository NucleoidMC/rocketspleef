package supercoder79.rocketspleef.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.explosion.ExplosionImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import supercoder79.rocketspleef.RocketSpleef;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(ExplosionImpl.class)
public class MixinExplosion {
    @Shadow @Final private ServerWorld world;

    @ModifyArg(method = "damageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private float reduceDamage(float amount) {
        var gameSpace = GameSpaceManager.get().byWorld(this.world);

        if (gameSpace != null && gameSpace.getBehavior().testRule(RocketSpleef.REDUCE_EXPLOSION_DAMAGE) == EventResult.ALLOW) {
            return Math.min(amount, 2);
        } else {
            return amount;
        }
    }
}
