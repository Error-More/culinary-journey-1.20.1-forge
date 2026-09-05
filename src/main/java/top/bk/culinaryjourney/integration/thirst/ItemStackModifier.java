package top.bk.culinaryjourney.integration.thirst;

import cn.mlus.thirst.foundation.config.CommonConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/**
 * Thirst 兼容性修复: 修复口渴值的水瓶堆叠兼容性问题.
 *
 * Thirst 原本通过 Mixin Inject {@link net.minecraft.world.item.ItemStack#getMaxStackSize} HEAD 修改返回值实现.
 * Mixin 修改返回值极有可能导致兼容问题, 此处通过反射直接更改 {@link net.minecraft.world.item.Item#maxStackSize} 提供 Fallback 方案,
 */
public class ItemStackModifier {

    /**
     * 初始化方法, 需要被手动调用
     */
    public static void init() {
        // 确保 Thirst 已经被 ModLoader 加载
        if (ModList.get().isLoaded("thirst")) {
            // 异常捕获 (即使 Thirst 已被加载, 直接的 API 调用仍然可能导致崩溃)
            try {
                // 复用 Thirst 的配置项
                setMaxStackSize(Items.POTION, CommonConfig.WATER_BOTTLE_STACKSIZE.get());
            } catch (Exception ignored) { }
        }
    }

    /**
     * 改写物品的最大堆叠数 (通过反射).
     *
     * @see ObfuscationReflectionHelper
     * @param item 目标物品
     * @param size 最大堆叠数
     */
    private static void setMaxStackSize(Item item, int size) {
        try {
            // "f_41370_" 是 Item#maxStackSize 的 SRG 混淆名
            ObfuscationReflectionHelper.setPrivateValue(Item.class, item, size, "f_41370_");
        }
        catch (Exception ignored) { }
    }
}
