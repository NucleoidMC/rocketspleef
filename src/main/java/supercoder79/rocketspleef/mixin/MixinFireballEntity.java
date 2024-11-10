package supercoder79.rocketspleef.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import supercoder79.rocketspleef.RocketSpleef;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(FireballEntity.class)
public class MixinFireballEntity {
    @WrapWithCondition(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean noDamage(Entity instance, ServerWorld serverWorld, DamageSource source, float v) {
        var gameSpace = GameSpaceManager.get().byWorld(serverWorld);

        return gameSpace == null || gameSpace.getBehavior().testRule(RocketSpleef.REDUCE_EXPLOSION_DAMAGE) != EventResult.ALLOW;
    }
}
