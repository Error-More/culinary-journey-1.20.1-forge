package top.bk.culinaryjourney.client.ui.markdown.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.ui.CursorType;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import top.bk.culinaryjourney.client.ui.markdown.MarkdownRenderer;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineContext.*;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineLayout.*;
import top.bk.culinaryjourney.util.markdown.Style;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 我懒 TAT
 * TODO : 注释
 */
public class CodeBlockWidget extends BlockWidget {

    private static final int MARGIN = 4;

    private final String literal;

    public CodeBlockWidget(Panel parent, String literal, int width,
                           TextMetrics metrics, Renderer renderer, Settings settings) {
        super(parent, parseContexts(literal), width - MARGIN, metrics, renderer, null,
                settings.withIndent(MARGIN / 2).withMarginTop(MARGIN + 2).withMarginBottom(MARGIN + 2));
        this.literal = literal;
        setX(MARGIN / 2);
    }

    private static List<Context> parseContexts(String literal) {
        if (literal == null || literal.isEmpty()) return List.of();

        String[] split = literal.split("\n", -1);
        List<Context> contexts = new ArrayList<>();

        for (int i = 0; i < split.length; i++) {
            String text = split[i];
            contexts.add(new TextContext(text, Style.EMPTY, null));
            if (i < split.length - 1) contexts.add(new Break());
        }

        return contexts;
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int rawX, int rawY, int w, int h) {
        int x = rawX + MARGIN / 2;

        if (renderer instanceof MarkdownRenderer markdownRenderer) {
            float scale = markdownRenderer.getScale();
            int codeColor = markdownRenderer.getSettings().getCodeColor();

            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.scale(scale, scale, 1.0F);
            graphics.fill(x - MARGIN / 2, rawY - MARGIN / 4,
                    x + w + MARGIN / 2, rawY + h + MARGIN / 4, codeColor);
            pose.popPose();

            super.draw(graphics, theme, x, rawY, w, h);
        }
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (!button.isLeft() || !isMouseOver()) return false;

        Minecraft.getInstance().keyboardHandler.setClipboard(literal);
        return true;
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        if (!isMouseOver()) return;
        list.add(Component.translatable("ftbquests.gui.copy"));
    }

    @Override
    public CursorType getCursor() {
        return CursorType.HAND;
    }
}