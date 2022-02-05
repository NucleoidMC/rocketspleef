package supercoder79.rocketspleef.item;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class RsItems {
    public static final Item FIREBALL_CANNON = new FireballCannonItem(Items.IRON_HOE);
    public static final Item FAST_FIREBALL_CANNON = new FireballCannonItem(Items.GOLDEN_HOE);
    public static final Item MULTI_FIREBALL_CANNON = new FireballCannonItem(Items.DIAMOND_HOE);

    private static void register(String path, Item item) {
        Registry.register(Registry.ITEM, new Identifier("rocketspleef", path), item);
    }

    public static void register() {
        register("fireball_cannon", FIREBALL_CANNON);
        register("fast_fireball_cannon", FAST_FIREBALL_CANNON);
        register("multi_fireball_cannon", MULTI_FIREBALL_CANNON);
    }
}
