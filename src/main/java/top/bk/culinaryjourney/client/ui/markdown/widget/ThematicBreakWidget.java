package top.bk.culinaryjourney.client.ui.markdown.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import top.bk.culinaryjourney.client.ui.markdown.MarkdownRenderer;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineLayout.*;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 我懒 TAT
 * TODO : 注释
 */
public class ThematicBreakWidget extends BlockWidget {

    private static final int COLOR = 0xFF3D444D;

    public ThematicBreakWidget(Panel parent, TextMetrics metrics, Renderer renderer, int width,
                               Settings settings) {
        super(parent, List.of(), width, metrics, renderer, null, settings);
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int rawX, int rawY, int w, int h) {
        if (renderer instanceof MarkdownRenderer markdownRenderer) {
            float scale = markdownRenderer.getScale();

            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.scale(scale, scale, 1.0F);
            graphics.fill(rawX, rawY + h / 2 - 1, rawX + w, rawY + h / 2, COLOR);
            pose.popPose();
        }
    }
}
