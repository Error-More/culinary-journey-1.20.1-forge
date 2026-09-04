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

// FTB Ultimine compat: 实现了 FTB Ultimine 在 renderGameOverlay 中的 TODO (指调整 HUD 的位置)
@Mixin(value = dev.ftb.mods.ftbultimine.client.FTBUltimineClient.class, remap = false)
public class MixinFTBUltimineClient {

    private static final IntValue yOffset = FTBUltimineClientConfig.CONFIG.addInt("y_offset", -1).comment(new String[]{"Manual y offset of FTB Ultimine overlay, required for some modpacks"});

    @Shadow
    private void addPressedInfo(List<MutableComponent> list) {}

    @Shadow
    private boolean pressed;

    @Inject(method = "renderGameOverlay", at = @At("HEAD"), cancellable = true)
    public void renderGameOverlay(GuiGraphics graphics, float tickDelta, CallbackInfo ci) {
        if (!ModList.get().isLoaded("ftbultimine")) return;

        try {
            IntValue xOffset = FTBUltimineClientConfig.xOffset;

            if (pressed) {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

                List<MutableComponent> list = new ArrayList<>();
                addPressedInfo(list);
                Minecraft minecraft = Minecraft.getInstance();

                int left = 2;
                int top = 2;
                if (xOffset.get() >= 0)
                    left = xOffset.get();
                if (yOffset.get() >= 0)
                    top = yOffset.get();

                int maxTop = top + minecraft.font.lineHeight * list.size();
                if (maxTop > graphics.guiHeight())
                    top += graphics.guiHeight() - maxTop - 2;

                boolean first = true;
                for (MutableComponent msg : list) {
                    FormattedCharSequence formatted = msg.getVisualOrderText();
                    if (first) {
                        float f = CooldownTracker.getCooldownRemaining(Minecraft.getInstance().player);
                        if (f < 1f) {
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
        } catch (Exception ignored) { }
    }

}
