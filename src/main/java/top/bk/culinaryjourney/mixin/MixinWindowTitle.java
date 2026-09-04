package top.bk.culinaryjourney.mixin;

import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.bk.culinaryjourney.client.WindowTitle;

// 接管窗口标题
@Mixin(Window.class)
public class MixinWindowTitle {

    private static String lastApplied = "";
    private static int lastEpoch = -1;

    @Inject(method = "setTitle", at = @At("HEAD"), cancellable = true)
    private void onSetTitle(String title, CallbackInfo ci) {
        ci.cancel();
        String ourTitle = WindowTitle.computeCurrentTitle();
        int epoch = WindowTitle.getTitleEpoch();
        if (ourTitle.equals(lastApplied) && epoch == lastEpoch) {
            return;
        }
        lastApplied = ourTitle;
        lastEpoch = epoch;
        long handle = ((Window) (Object) this).getWindow();
        if (handle != 0L) {
            GLFW.glfwSetWindowTitle(handle, ourTitle);
        }
    }
}
