package top.bk.culinaryjourney.client.ui.markdown.widget;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import top.bk.culinaryjourney.client.ui.markdown.MarkdownRenderer;
import top.bk.culinaryjourney.client.ui.markdown.MarkdownView;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineLayout.*;
import top.bk.culinaryjourney.util.markdown.Block;
import top.bk.culinaryjourney.util.markdown.Style;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 我懒 TAT
 * TODO : 注释
 */
public class ListWidget extends Panel {

    private static final int INDENT = 12;

    private final boolean ordered;

    public ListWidget(Panel panel, List<Block> blocks, int width,
                      MarkdownRenderer renderer,
                      @Nullable LinkHandler linkHandler,
                      boolean ordered, @Nullable Integer startNumber, int x) {
        super(panel);
        setX(x);
        setSize(width, 0);

        this.ordered = ordered;

        int number = startNumber == null ? 1 : startNumber;

        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);

            if (block instanceof Block.Paragraph paragraph) {
                String prefix = ordered ? number + ". " :
                        switch (nested(this)) {
                            case 0 -> "• ";
                            case 1 -> "◦ ";
                            default -> "▪ ";
                        };
                int prefixWidth = renderer.width(prefix, Style.EMPTY) + 4;

                ListItemWidget item = new ListItemWidget(this, paragraph.getInlines(), width,
                        renderer, linkHandler, prefix, prefixWidth);
                item.setX(0);
                add(item);
                number++;
            } else if (block instanceof Block.BulletList list) {
                add(new ListWidget(this, list.getBlocks(), Math.max(1, width - INDENT),
                        renderer, linkHandler, false, null, INDENT));
            } else if (block instanceof Block.OrderedList list) {
                add(new ListWidget(this, list.getBlocks(), Math.max(1, width - INDENT),
                        renderer, linkHandler, true, list.getStartNumber(), INDENT));
            } else {
                MarkdownView.addBlock(this, block, width, renderer, linkHandler, 1);
            }
        }

        setHeight(align(new WidgetLayout.Vertical(this.getParent() instanceof ListWidget ? 0 : 4,
                BlockWidget.Settings.EMPTY.getItemSpacing(), 2)));
    }

    private int nested(Panel parent) {
        int number = 0;
        if (parent.getParent() instanceof ListWidget listWidget && !listWidget.isOrdered()) {
            number++;
            number += nested(parent.getParent());
        }
        return number;
    }

    private boolean isOrdered() {
        return ordered;
    }

    @Override
    public void addWidgets() { }

    @Override
    public void alignWidgets() { }

    @Override
    public void refreshWidgets() { }

}