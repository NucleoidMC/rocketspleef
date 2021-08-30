package supercoder79.rocketspleef.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.ActionResult;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import supercoder79.rocketspleef.RocketSpleef;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;

@Mixin(Explosion.class)
public class MixinExplosion {
    @Redirect(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean reduceDamage(Entity entity, DamageSource source, float amount) {
        ManagedGameSpace gameSpace = GameSpaceManager.get().byWorld(entity.getEntityWorld());

        if (gameSpace != null && gameSpace.getBehavior().testRule(RocketSpleef.REDUCE_EXPLOSION_DAMAGE) == ActionResult.SUCCESS) {
            return entity.damage(source, Math.min(amount, 2));
        } else {
            return entity.damage(source, amount);
        }
    }
}
