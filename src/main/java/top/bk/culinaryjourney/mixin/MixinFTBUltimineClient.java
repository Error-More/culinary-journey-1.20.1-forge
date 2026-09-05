package top.bk.culinaryjourney.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.snbt.config.IntValue;
import dev.ftb.mods.ftbultimine.CooldownTracker;
import dev.ftb.mods.ftbultimine.config.FTBUltimineClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * FTB Ultimine 兼容补丁：补上它 {@code renderGameOverlay} 里的 TODO：让 HUD 的纵向位置可配置。
 *
 * 原版 FTB Ultimine 只提供了 {@code xOffset}（横向偏移），纵向位置被硬编码在左上角，
 * 这里在注入点 {@code HEAD} 整段接管渲染，
 * 复刻原逻辑并额外读取 {@code yOffset}。
 *
 * @since 1.0.0
 */
@Mixin(value = dev.ftb.mods.ftbultimine.client.FTBUltimineClient.class, remap = false)
public class MixinFTBUltimineClient {

    /**
     * 新增的纵向偏移配置项。
     *
     * 直接往 FTB Ultimine 的配置对象里追加一项，好处是复用它的配置文件与配置界面，
     * 玩家不必再到本模组的配置里单独设置。
     *
     * 哨兵值约定：{@code -1} 表示"玩家未设置"，此时沿用原版的固定位置（左上角）；
     * 只有 {@code >= 0} 的值才会被采纳，因此 {@code 0} 是合法的有效值（紧贴屏幕顶边）。
     */
    private static final IntValue yOffset = FTBUltimineClientConfig.CONFIG.addInt("y_offset", -1).comment(new String[]{"Manual y offset of FTB Ultimine overlay, required for some modpacks"});

    /** 影子方法：把当前要显示的提示文本填充进 list，实现由目标类提供。 */
    @Shadow
    private void addPressedInfo(List<MutableComponent> list) {}

    /** 影子字段：玩家是否正按住 FTB Ultimine 的功能键。 */
    @Shadow
    private boolean pressed;

    /**
     * 接管 {@code FTBUltimineClient#renderGameOverlay} 的渲染。
     *
     * 在 {@code HEAD} 注入并取消原方法，改为执行本方法内的复刻逻辑。
     *
     * @param graphics  GUI 绘制上下文
     * @param tickDelta 部分刻插值
     * @param ci        注入回调，方法末尾无条件 {@code cancel()} 以屏蔽原实现
     */
    @Inject(method = "renderGameOverlay", at = @At("HEAD"), cancellable = true)
    public void renderGameOverlay(GuiGraphics graphics, float tickDelta, CallbackInfo ci) {
        // Mixin 配置里已声明依赖，这里复查一次，防止 Mixin 生效但目标模组不存在时抛 NoClassDefFoundError
        if (!ModList.get().isLoaded("ftbultimine")) return;

        try {
            IntValue xOffset = FTBUltimineClientConfig.xOffset;

            if (pressed) {
                // 配色沿用 Nord 主题
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

                List<MutableComponent> list = new ArrayList<>();
                addPressedInfo(list);
                Minecraft minecraft = Minecraft.getInstance();

                // 默认值 2 对应原版硬编码的左上角边距；只有配置值 >= 0 才覆盖
                int left = 2;
                int top = 2;
                if (xOffset.get() >= 0)
                    left = xOffset.get();
                if (yOffset.get() >= 0)
                    top = yOffset.get();

                // 溢出回缩：玩家把 y_offset 调得过大时整块 HUD 会超出屏幕下沿，这里把起点上移使其底部贴住屏幕
                int maxTop = top + minecraft.font.lineHeight * list.size();
                if (maxTop > graphics.guiHeight())
                    top += graphics.guiHeight() - maxTop - 2;

                boolean first = true;
                for (MutableComponent msg : list) {
                    FormattedCharSequence formatted = msg.getVisualOrderText();
                    if (first) {
                        // 冷却进度只在首行绘制：CooldownTracker 的剩余量只对应"当前正在挖掘的这一个目标"，
                        // 后续的附加信息行没有独立的冷却概念，一律画满整宽
                        float f = CooldownTracker.getCooldownRemaining(Minecraft.getInstance().player);
                        if (f < 1f) {
                            // 冷却未完成时背景条按剩余比例 f 收缩，形成进度条效果
                            graphics.fill(left - 1, top - 1, left + (int)(minecraft.font.width(formatted) * f) + 1, top + minecraft.font.lineHeight - 1, 0xAA_2E3440);
                        } else {
                            graphics.fill(left - 1, top - 1, left + minecraft.font.width(formatted) + 1, top + minecraft.font.lineHeight - 1, 0xAA_2E3440);
                        }
                    } else {
                        graphics.fill(left - 1, top - 1, left + minecraft.font.width(formatted) + 1, top + minecraft.font.lineHeight - 1, 0xAA_2E3440);
                    }
                    graphics.drawString(minecraft.font, formatted, left, top, 0xECEFF4, true);
                    top += minecraft.font.lineHeight;
                    first = false;
                }
            }
            ci.cancel();
        } catch (Exception ignored) {
            // 静默失败是刻意设计
        }
    }

}
