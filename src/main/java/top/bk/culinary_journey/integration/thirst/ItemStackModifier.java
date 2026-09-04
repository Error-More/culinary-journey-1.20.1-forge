package top.bk.culinary_journey.integration.thirst;

import cn.mlus.thirst.foundation.config.CommonConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

// Thirst 兼容：按配置调整水瓶堆叠上限
public class ItemStackModifier {

    public static void init() {
        if (ModList.get().isLoaded("thirst")) {
            try {
                setMaxStackSize(Items.POTION, CommonConfig.WATER_BOTTLE_STACKSIZE.get());
            } catch (Exception ignored) { }
        }
    }

    private static void setMaxStackSize(Item item, int size) {
        try {
            ObfuscationReflectionHelper.setPrivateValue(Item.class, item, size, "f_41370_");
        }
        catch (Exception ignored) { }
    }
}
