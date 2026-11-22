package top.bk.culinaryjourney.client.ui.markdown.widget;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import top.bk.culinaryjourney.client.ui.markdown.MarkdownRenderer;
import top.bk.culinaryjourney.client.ui.markdown.MarkdownView;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineLayout.*;
import top.bk.culinaryjourney.util.markdown.Block;
import top.bk.culinaryjourney.util.markdown.Inline;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 我懒 TAT
 * TODO : 注释
 */
public class QuoteWidget extends Panel {

    private static final int WIDTH = 5;
    private static final int SPACING = 8;
    private static final int COLOR = 0xFF3D444D;

    public QuoteWidget(Panel parent, List<Block> blocks, int width,
                       MarkdownRenderer renderer,
                       @Nullable LinkHandler linkHandler) {
        super(parent);
        setSize(width, 0);

        for (Block block : blocks) {
            if (block instanceof Block.Paragraph paragraph) {
                List<Inline> inlines = paragraph.getInlines();
                for (int i = 0; i < inlines.size(); i++) {
                    if (inlines.get(i) instanceof Inline.SoftBreak)
                        inlines.set(i, new Inline.HardBreak());
                }
            }
        }

        MarkdownView.addBlocks(this, blocks, Math.max(1, width - WIDTH - SPACING),
                renderer, linkHandler);

        for (Widget widget : getWidgets()) {
            widget.setX(widget.posX + WIDTH + SPACING);
        }

        setHeight(align(new WidgetLayout.Vertical(0, 1, 0)));
    }

    @Override
    public void addWidgets() { }

    @Override
    public void alignWidgets() { }

    @Override
    public void refreshWidgets() { }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        graphics.fill(x, y - 1, x + WIDTH, y + h - 2, COLOR);

        super.draw(graphics, theme, x, y, w, h);
    }

}