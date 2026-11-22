package top.bk.culinaryjourney.client.ui.markdown.widget;

import dev.ftb.mods.ftblibrary.ui.Panel;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineContext.*;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineLayout.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 我懒 TAT
 * TODO : 注释
 */
public class ParagraphWidget extends BlockWidget {

    public ParagraphWidget(Panel parent, List<Context> contexts, int width,
                           TextMetrics metrics, Renderer renderer, @Nullable LinkHandler linkHandler,
                           Settings settings) {
        super(parent, contexts, width, metrics, renderer, linkHandler, settings);
    }

}