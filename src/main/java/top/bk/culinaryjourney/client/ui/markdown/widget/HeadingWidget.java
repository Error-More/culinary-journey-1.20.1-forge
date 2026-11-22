package top.bk.culinaryjourney.client.ui.markdown.widget;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import top.bk.culinaryjourney.client.ui.markdown.MarkdownRenderer;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineContext;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineContext.*;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineLayout.*;
import top.bk.culinaryjourney.util.markdown.Inline;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 我懒 TAT
 * TODO : 注释
 */
public class HeadingWidget extends BlockWidget {

    private static final float[] SCALE = {1.5F, 1.3F, 1.15F, 1.05F, 0.9F, 0.85F};

    private final ThematicBreakWidget thematicBreakWidget;

    public HeadingWidget(Panel parent, List<Inline> inlines, int width,
                         MarkdownRenderer renderer, @Nullable LinkHandler linkHandler,
                         int level) {
        super(parent, withBold(InlineContext.parse(inlines)), width,
                renderer.scaled(getScale(level)), renderer.scaled(getScale(level)), linkHandler,
                Settings.EMPTY.withMarginTop(4).withMarginBottom(2));

        if (level <= 2) {
            thematicBreakWidget = new ThematicBreakWidget(parent, renderer, renderer, width, Settings.EMPTY);
        } else {
            thematicBreakWidget = null;
        }
    }

    private static int getIndex(int level) {
        return Math.max(0, Math.min(SCALE.length - 1, level - 1));
    }
    private static float getScale(int level) {
        return SCALE[getIndex(level)];
    }

    private static List<Context> withBold(List<Context> contexts) {
        List<Context> out = new ArrayList<>(contexts.size());

        for (Context ctx : contexts) {
            if (ctx instanceof TextContext text) {
                out.add(new TextContext(text.getText(), text.getStyle().withBold(true), text.getLink()));
            } else {
                out.add(ctx);
            }
        }

        return out;
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int rawX, int rawY, int w, int h) {
        if (thematicBreakWidget != null) {
            thematicBreakWidget.draw(graphics, theme, rawX,
                    rawY + layout.getHeight() - layout.getHeight() / 5, w, h);
        }

        super.draw(graphics, theme, rawX, rawY, w, h);
    }
}