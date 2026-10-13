package top.bk.culinaryjourney.mixin;

import com.google.gson.JsonParseException;
import dev.ftb.mods.ftbquests.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.bk.culinaryjourney.integration.ftbquests.StyleSerializerHelper;

/**
 * FTB Quests 兼容性扩展 : Minecraft 为了安全性, {@link ClickEvent.Action} 中的 {@link Action#OPEN_FILE} 是无法从文本解析的, FTB Quests 也无法使用.
 *
 * 通过 Mixin Inject 注入 {@code deserializeRawJsonText} 方法的 HEAD 跳过原本逻辑重新实现.
 */
@Mixin(value = TextUtils.class, remap = false)
public class MixinTextUtils {

    /**
     * {@link TextUtils#deserializeRawJsonText(String)} 的 origin.
     *
     * @param raw        原始字符串
     * @return component 构建的 component
     */
    @Unique
    private static Component culinary_journey_1_20_1_forge$deserializeRawJsonText(String raw) {
        try {
            return Serializer.fromJson(raw);
        } catch (JsonParseException e) {
            return Component.literal("ERROR: " + e.getMessage()).withStyle(ChatFormatting.RED);
        }
    }

    /**
     * Mixin Inject {@link TextUtils#deserializeRawJsonText(String)}
     *
     * @param json json 字符串
     * @param cir  注入回调, 修改返回值与跳过原逻辑
     */
    @Inject(method = "deserializeRawJsonText", at = @At(value = "HEAD"), cancellable = true)
    private static void deserializeRawJsonText(String json, CallbackInfoReturnable<Component> cir) {
        // 设置线程变量
        StyleSerializerHelper.isAllowedFromServer.set(true);

        try {
            // original
            cir.setReturnValue(culinary_journey_1_20_1_forge$deserializeRawJsonText(json));
        } finally {
            // 执行后删除
            StyleSerializerHelper.isAllowedFromServer.set(false);
        }

        // 跳过原逻辑
        cir.cancel();
    }

}
