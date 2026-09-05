package top.bk.culinaryjourney.integration.thirst;

import cn.mlus.thirst.foundation.config.CommonConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/**
 * Thirst（口渴值）兼容补丁：把药水的最大堆叠数同步为 Thirst 配置中的水瓶堆叠数。
 *
 * 什么用反射而不用 Mixin：Thirst 自身已通过 Mixin 改过 {@code Item} 的堆叠逻辑，
 * 本模组再叠一层 Mixin 会与它的注入顺序、生效时机互相覆盖，导致设置不稳定。
 * 直接反射改写字段是最短路径，也与 Thirst 的实现细节解耦。
 *
 * 这是全局修改，会影响所有模组中用到药水物品的场合。
 *
 * @since 1.0.0
 */
public class ItemStackModifier {

    /**
     * 应用补丁。仅在 Thirst 已加载时执行
     */
    public static void init() {
        if (ModList.get().isLoaded("thirst")) {
            try {
                setMaxStackSize(Items.POTION, CommonConfig.WATER_BOTTLE_STACKSIZE.get());
            } catch (Exception ignored) {
                // 兼容补丁失败不应阻碍游戏启动，最坏情况退化为原版堆叠数
            }
        }
    }

    /**
     * 反射改写物品的最大堆叠数。
     *
     * @param item 目标物品实例
     * @param size 新的最大堆叠数
     */
    private static void setMaxStackSize(Item item, int size) {
        try {
            // "f_41370_" 是 Item#maxStackSize 的 SRG 混淆名
            ObfuscationReflectionHelper.setPrivateValue(Item.class, item, size, "f_41370_");
        }
        catch (Exception ignored) {
            // 反射失败静默处理
        }
    }
}
