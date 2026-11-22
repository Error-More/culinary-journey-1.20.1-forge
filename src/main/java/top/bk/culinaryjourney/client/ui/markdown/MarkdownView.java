package top.bk.culinaryjourney.client.ui.markdown;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Widget;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineContext;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineLayout.*;
import top.bk.culinaryjourney.client.ui.markdown.widget.*;
import top.bk.culinaryjourney.client.ui.markdown.widget.BlockWidget.Settings;
import top.bk.culinaryjourney.util.markdown.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 我懒 TAT
 * TODO : 注释
 */
public final class MarkdownView {

    private static final int INDENT = 12;

    private MarkdownView() { }

    public static void addBlocks(Panel panel, List<Block> blocks, int width,
                                 MarkdownRenderer renderer,
                                 @Nullable LinkHandler linkHandler) {
        addBlocks(panel, blocks, width, renderer, linkHandler, 0);
    }

    public static void addBlocks(Panel panel, List<Block> blocks, int width,
                                 MarkdownRenderer renderer,
                                 @Nullable LinkHandler linkHandler,
                                 int level) {
        for (Block block : blocks) {
            addBlock(panel, block, width, renderer, linkHandler, level);
        }
    }

    public static void addBlock(Panel panel, Block block, int width,
                                MarkdownRenderer renderer,
                                @Nullable LinkHandler linkHandler,
                                int level) {
        int indentWidth = Math.max(0, level) * INDENT;
        int maxWidth = Math.max(1, width - indentWidth);

        Widget widget;

        if (block instanceof Block.Paragraph paragraph) {
            widget = new ParagraphWidget(panel, InlineContext.parse(paragraph.getInlines()), maxWidth,
                    renderer, renderer, linkHandler,
                    Settings.EMPTY.withMarginTop(4).withMarginBottom(4));
        } else if (block instanceof Block.Heading heading) {
            widget = new HeadingWidget(panel, heading.getInlines(), maxWidth, renderer, linkHandler, heading.getLevel());
        } else if (block instanceof Block.ThematicBreak) {
            widget = new ThematicBreakWidget(panel, renderer, renderer, maxWidth,
                    Settings.EMPTY.withMarginTop(4).withMarginBottom(4));
        } else if (block instanceof Block.FencedCode code) {
            widget = new CodeBlockWidget(panel, code.getLiteral(), maxWidth,
                    renderer, renderer, Settings.EMPTY);
        } else if (block instanceof Block.Quote quote) {
            widget = new QuoteWidget(panel, quote.getBlocks(), maxWidth, renderer, linkHandler);
        } else if (block instanceof Block.BulletList list) {
            widget = new ListWidget(panel, list.getBlocks(), maxWidth,
                    renderer, linkHandler, false, null, 0);
        } else if (block instanceof Block.OrderedList list) {
            widget = new ListWidget(panel, list.getBlocks(), maxWidth,
                    renderer, linkHandler, true, list.getStartNumber(), 0);
        } else {
            return;
        }

        widget.setX(indentWidth);
        panel.add(widget);
    }

}
