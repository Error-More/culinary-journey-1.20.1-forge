package top.bk.culinaryjourney.mixin;

import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.bk.culinaryjourney.client.WindowTitle;

/**
 * 接管 {@code Window#setTitle}，把标题替换为 {@link WindowTitle} 计算出的动态标题
 *
 * 选 {@code setTitle} 作为注入点， Minecraft 会在启动、切换语言、进出世界等时机主动调用它，在此拦截并取消原实现
 */
@Mixin(Window.class)
public class MixinWindowTitle {

    /** 上一次真正写入窗口的标题，用于去重 */
    private static String lastApplied = "";

    /**
     * 拦截标题设置，替换为 {@link WindowTitle#computeCurrentTitle()} 的结果
     *
     * @param title 原实现想要设置的标题，本实现不使用
     * @param ci    注入回调，无条件 cancel 以屏蔽原实现
     */
    @Inject(method = "setTitle", at = @At("HEAD"), cancellable = true)
    private void onSetTitle(String title, CallbackInfo ci) {
        // 完全进入主菜单前不接管，保留原版窗口标题，同时跳过无谓的标题计算
        if (!WindowTitle.isInitialized()) {
            return;
        }
        ci.cancel();
        String ourTitle = WindowTitle.computeCurrentTitle();
        // 标题无变化时直接跳过，省去开销
        if (ourTitle.equals(lastApplied)) {
            return;
        }
        lastApplied = ourTitle;
        long handle = ((Window) (Object) this).getWindow();
        // handle 为 0 表示窗口尚未创建完成，此时调用 GLFW 没有意义
        if (handle != 0L) {
            GLFW.glfwSetWindowTitle(handle, ourTitle);
        }
    }
}
