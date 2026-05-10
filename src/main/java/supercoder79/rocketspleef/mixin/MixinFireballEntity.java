package supercoder79.rocketspleef.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import supercoder79.rocketspleef.RocketSpleef;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(LargeFireball.class)
public class MixinFireballEntity {
    @WrapOperation(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean noDamage(Entity instance, ServerLevel serverWorld, DamageSource source, float v, Operation<Boolean> original) {
        var gameSpace = GameSpaceManager.get().byLevel(serverWorld);

        if (gameSpace == null || gameSpace.getBehavior().testRule(RocketSpleef.REDUCE_EXPLOSION_DAMAGE) != EventResult.ALLOW) {
            return original.call(instance, serverWorld, source, v);
        } else {
            return false;
        }
    }
}
