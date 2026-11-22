package top.bk.culinaryjourney.client;

import net.minecraft.network.chat.ComponentContents;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * MarkdownComponent 用于标记 Markdown 开始与结束.
 */
public class MarkdownComponent implements ComponentContents {

    private final boolean begin;

    public MarkdownComponent(boolean begin) {
        super();
        this.begin = begin;
    }

    @Override
    public String toString() {
        return isBegin() ? "{@MarkdownBegin}" : "{@MarkdownEnd}";
    }

    public boolean isBegin() {
        return begin;
    }

}