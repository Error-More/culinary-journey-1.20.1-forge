package top.bk.culinaryjourney.integration.thirst;

import cn.mlus.thirst.foundation.config.CommonConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

//Thirst compat fix: 直接修改 Item 的 maxStackSize 字段，排除原本口渴值直接 Mixin 可能导致的兼容与不生效问题
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
