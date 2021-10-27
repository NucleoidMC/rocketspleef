package supercoder79.rocketspleef.item;

import eu.pb4.polymer.item.VirtualItem;
import net.minecraft.item.Item;

public class FireballCannonItem extends Item implements VirtualItem {
    private final Item virtualItem;

    public FireballCannonItem(Item virtualItem) {
        super(new Item.Settings().maxCount(1));
        this.virtualItem = virtualItem;
    }

    @Override
    public Item getVirtualItem() {
        return this.virtualItem;
    }
}
