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
 * FTB Ultimine 兼容性修复: 实现 {@link dev.ftb.mods.ftbultimine.client.FTBUltimineClient#renderGameOverlay} 的 TODO (允许配置 HUD 位置偏移).
 *
 * 通过 Mixin Inject 注入 {@code renderGameOverlay} 方法的 HEAD 跳过原本逻辑重新实现.
 */
@Mixin(value = dev.ftb.mods.ftbultimine.client.FTBUltimineClient.class, remap = false)
public class MixinFTBUltimineClient {

    /**
     * 纵向偏移配置项.
     *
     * 复用 FTB Ultimine 的 Config 接口, 可以直接通过 FTB Library 的 UI 界面进行配置.
     * 哨兵值约定: 沿用 FTB 的逻辑 {@code -1} -1 表示未设置, 此时沿用原 FTB Ultimine 的默认位置 (2).
     */
    private static final IntValue yOffset = FTBUltimineClientConfig.CONFIG.addInt("y_offset", -1).comment(new String[]{"Manual y offset of FTB Ultimine overlay, required for some modpacks"});

    /**
     * 按顺序把当前要显示的文本填入 {@code List}
     *
     * @see dev.ftb.mods.ftbultimine.client.FTBUltimineClient#addPressedInfo(List)
     * @param list 填充的 list
     */
    @Shadow
    private void addPressedInfo(List<MutableComponent> list) {}

    /**
     * 是否按下 FTB Ultimine 的功能键.
     *
     * @see dev.ftb.mods.ftbultimine.client.FTBUltimineClient#pressed
     */
    @Shadow
    private boolean pressed;

    /**
     * Mixin Inject {@link dev.ftb.mods.ftbultimine.client.FTBUltimineClient#renderGameOverlay} 增加位置偏移逻辑.
     *
     * @param graphics  GUI 绘制上下文
     * @param tickDelta 部分刻插值
     * @param ci        注入回调，成功执行则 {@code cancel()} 跳过原实现
     */
    @Inject(method = "renderGameOverlay", at = @At("HEAD"), cancellable = true)
    public void renderGameOverlay(GuiGraphics graphics, float tickDelta, CallbackInfo ci) {
        if (!ModList.get().isLoaded("ftbultimine")) return;

        try {
            IntValue xOffset = FTBUltimineClientConfig.xOffset;

            if (pressed) {
                // 配色沿用 FTB Ultimine 默认主题
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

                List<MutableComponent> list = new ArrayList<>();
                addPressedInfo(list);
                Minecraft mc = Minecraft.getInstance();

                // 检查 Offset 参数是否越界, 越界则 Fallback
                int left = xOffset.get() >= 0 ? xOffset.get() : 2;
                int top = yOffset.get() >= 0 ? yOffset.get() : 2;

                // 确保 HUD 的渲染不会超出范围
                int maxTop = top + mc.font.lineHeight * list.size();
                if (maxTop > graphics.guiHeight())
                    top += graphics.guiHeight() - maxTop - 2;

                int maxWidth = 0;
                for (MutableComponent msg : list) {
                    int width = mc.font.width(msg.getVisualOrderText());
                    if (width > maxWidth) maxWidth = width;
                }
                int maxLeft = left + maxWidth;
                if (maxLeft > graphics.guiWidth())
                    left += graphics.guiWidth() - maxLeft - 2;


                // 具体绘制逻辑与 FTB Ultimine 的原版逻辑完全相同, 区别仅在于绘制的位置参数
                boolean first = true;
                for (MutableComponent msg : list) {
                    FormattedCharSequence formatted = msg.getVisualOrderText();
                    if (first) {
                        float f = CooldownTracker.getCooldownRemaining(Minecraft.getInstance().player);
                        if (f < 1f) {
                            graphics.fill(left - 1, top - 1, left + (int)(mc.font.width(formatted) * f) + 1, top + mc.font.lineHeight - 1, 0xAA_2E3440);
                        } else {
                            graphics.fill(left - 1, top - 1, left + mc.font.width(formatted) + 1, top + mc.font.lineHeight - 1, 0xAA_2E3440);
                        }
                    } else {
                        graphics.fill(left - 1, top - 1, left + mc.font.width(formatted) + 1, top + mc.font.lineHeight - 1, 0xAA_2E3440);
                    }
                    graphics.drawString(mc.font, formatted, left, top, 0xECEFF4, true);
                    top += mc.font.lineHeight;
                    first = false;
                }
            }

            ci.cancel();
        } catch (Exception ignored) {
            // Fallback
        }
    }

}
