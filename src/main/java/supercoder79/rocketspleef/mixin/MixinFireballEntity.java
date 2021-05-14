package supercoder79.rocketspleef.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.FireballEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import supercoder79.rocketspleef.RocketSpleef;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin(FireballEntity.class)
public class MixinFireballEntity {
    @Redirect(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean noDamage(Entity entity, DamageSource source, float amount) {
        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(entity.getEntityWorld());

        if (gameSpace != null && gameSpace.testRule(RocketSpleef.REDUCE_EXPLOSION_DAMAGE) == RuleResult.ALLOW) {
            return entity.damage(source, 0);
        } else {
            return entity.damage(source, amount);
        }
    }
    }
}
