package top.bk.culinaryjourney.mixin;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.bk.culinaryjourney.client.WindowTitle;

/**
 * 单人世界开放 / 关闭局域网时通知 {@link WindowTitle} 刷新标题
 *
 * 注入 {@code IntegratedServer#publishServer} 的返回处捕获开放，
 * 注入 {@code IntegratedServer#stopServer} 捕获关闭
 *
 * @since 1.0.0
 */
@Mixin(IntegratedServer.class)
public class MixinIntegratedServerLan {

    /**
     * 开放成功后排布一次标题刷新
     *
     * @param gameType 默认游戏模式
     * @param cheats   是否允许作弊
     * @param port     端口号
     * @param cir      注入回调，携带原实现的返回值
     */
    @Inject(method = "publishServer", at = @At("RETURN"))
    private void onPublishServer(GameType gameType, boolean cheats, int port, CallbackInfoReturnable<Boolean> cir) {
        // 运行在服务端线程，此处只置标记，读写窗口仍回到客户端 tick
        if (Boolean.TRUE.equals(cir.getReturnValue())) {
            WindowTitle.onLanOpened();
        }
    }

    /**
     * 服务器关闭时排布一次标题刷新
     *
     * @param ci 注入回调
     */
    @Inject(method = "stopServer", at = @At("RETURN"))
    private void onStopServer(CallbackInfo ci) {
        // 运行在服务端线程，此处只置标记，读写窗口仍回到客户端 tick
        WindowTitle.onLanClosed();
    }
}
