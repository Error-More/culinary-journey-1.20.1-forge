package top.bk.culinaryjourney.integration.ftbquests;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;

/**
 * FTB Quests 兼容性扩展 : Minecraft 为了安全性, {@link ClickEvent.Action} 中的 {@link Action#OPEN_FILE} 是无法从文本解析的, FTB Quests 也无法使用.
 *
 * Helper class : 创建 {@code ThreadLocal<Boolean>} 类型变量 {@link StyleSerializerHelper#isAllowedFromServer} 作为 Flag.
 */
public class StyleSerializerHelper {

    /**
     * 是否允许从服务器发送的构建请求.
     *
     * 会被替换为 {@link Action#isAllowedFromServer()} 的返回值.
     */
    public static final ThreadLocal<Boolean> isAllowedFromServer = ThreadLocal.withInitial(() -> false);

}