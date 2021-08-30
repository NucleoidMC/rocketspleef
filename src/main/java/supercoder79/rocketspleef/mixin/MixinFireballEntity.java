package supercoder79.rocketspleef.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import supercoder79.rocketspleef.RocketSpleef;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;

@Mixin(FireballEntity.class)
public class MixinFireballEntity {
    @Redirect(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean noDamage(Entity entity, DamageSource source, float amount) {
        ManagedGameSpace gameSpace = GameSpaceManager.get().byWorld(entity.getEntityWorld());

        if (gameSpace != null && gameSpace.getBehavior().testRule(RocketSpleef.REDUCE_EXPLOSION_DAMAGE) == ActionResult.SUCCESS) {
            return entity.damage(source, 0);
        } else {
            return entity.damage(source, amount);
        }
    }
}
