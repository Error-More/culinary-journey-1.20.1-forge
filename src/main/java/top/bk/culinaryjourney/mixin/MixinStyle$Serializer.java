package top.bk.culinaryjourney.mixin;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Style.Serializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.bk.culinaryjourney.integration.ftbquests.StyleSerializerHelper;

/**
 * FTB Quests 兼容性扩展 : Minecraft 为了安全性, {@link ClickEvent.Action} 中的 {@link Action#OPEN_FILE} 是无法从文本解析的, FTB Quests 也无法使用.
 *
 * 通过 Mixin Redirect 重定向 {@code getClickEvent} 中的 {@code isAllowedFromServer} 方法调用, 修改为自定义 Flag.
 */
@Mixin(Style.Serializer.class)
public class MixinStyle$Serializer {

    /**
     * Mixin Redirect {@link Serializer#getClickEvent(JsonObject)} 中的 {@link Action#isAllowedFromServer()} 修改为自定义 Flag.
     *
     * @param instance             ClickEvent.Action 实例
     * @return isAllowedFromServer 自定义 Flag
     */
    @Redirect(method = "getClickEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/ClickEvent$Action;isAllowedFromServer()Z"))
    private static boolean isAllowedFromServer(ClickEvent.Action instance) {
        return StyleSerializerHelper.isAllowedFromServer.get();
    }

}