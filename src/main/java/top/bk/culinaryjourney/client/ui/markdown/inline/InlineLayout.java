package top.bk.culinaryjourney.client.ui.markdown.inline;

import dev.ftb.mods.ftblibrary.util.client.ImageComponent;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineContext.*;
import top.bk.culinaryjourney.util.markdown.MarkdownUtils;
import top.bk.culinaryjourney.util.markdown.Style;
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
public class InlineLayout {

    public interface TextMetrics {
        int width(String text, Style style);
        int rawWidth(String text, Style style);
        int lineHeight();
    }

    public interface Renderer {
        void drawText(GuiGraphics graphics, TextContext ctx, int start, int end, float x, int y, int color);
        default void drawBackground(GuiGraphics graphics, TextContext ctx, int x, int y, int width, int lineHeight) { }
        default void drawImage(GuiGraphics graphics, ImageContext ctx, int x, int y, int width, int height) { }
        default void drawPrefix(GuiGraphics graphics, String prefix, int x, int y, int color) { }
    }

    public interface LinkHandler {
        boolean onClick(LinkContext link);
        Component onHover(LinkContext link);
    }

    public record Span(int x, int width, TextContext ctx, int start, int end) { }
    public record ImageSpan(int x, int width, int height, ImageContext ctx) { }

    public record Line(int top, int height, boolean spacing, List<Span> texts, List<ImageSpan> images) { }

    private final List<Line> lines;
    private final int height;

    public InlineLayout(List<Context> contexts, int width, TextMetrics metrics,
                        int spacing, int itemSpacing, boolean listItem) {
        Layout layout = new Layout(Math.max(1, width), metrics, spacing, itemSpacing, listItem);

        for (Context ctx : contexts) {
            layout.add(ctx);
        }

        layout.finish();

        lines = layout.getLines();

        if (lines.isEmpty()) {
            height = metrics.lineHeight();
        } else {
            Line last = lines.get(lines.size() - 1);
            height = last.top() + last.height();
        }
    }

    public List<Line> getLines() {
        return lines;
    }
    public int getHeight() {
        return height;
    }

    private static final class Layout {
        private final int maxWidth;
        private final TextMetrics metrics;
        private final int itemSpacing;
        private final int spacing;
        private final boolean listItem;

        private final List<Line> lines = new ArrayList<>();
        private final List<Span> spans = new ArrayList<>();
        private final List<ImageSpan> images = new ArrayList<>();

        private int top;
        private int x;
        private int rightX;
        private int lineHeight;

        private Layout(int maxWidth, TextMetrics metrics, int spacing, int itemSpacing, boolean listItem) {
            this.maxWidth = maxWidth;
            this.metrics = metrics;
            this.spacing = spacing;
            this.itemSpacing = itemSpacing;
            this.listItem = listItem;
            this.lineHeight = metrics.lineHeight();
        }

        private void add(Context ctx) {
            if (ctx instanceof Break) {
                newLine(true);
            } else if (ctx instanceof TextContext text) {
                addText(text);
            } else if (ctx instanceof ImageContext image) {
                addImage(image);
            }
        }

        private void addText(TextContext ctx) {
            String text = ctx.getText();

            int i = 0;
            while (i < text.length()) {
                int width = maxWidth - x - rightX;
                if (width <= 0) {
                    newLine(false);
                    continue;
                }

                int end = getEnd(text, i, width, ctx.getStyle());
                if (end == i) {
                    if (x > 0) {
                        newLine(false);
                        continue;
                    }
                    end = i + Character.charCount(text.codePointAt(i));
                }

                if (end < text.length()) {
                    int breakPoint = breakPoint(text, i, end);
                    if (breakPoint > i) end = breakPoint;
                }

                boolean background = ctx.getStyle().isCode() || ctx.getStyle().isKeyBind();
                int margin = background ? 4 : 0;

                String span = text.substring(i, end);
                int w = metrics.width(span, ctx.getStyle());
                spans.add(new Span(x + margin / 2, metrics.rawWidth(span, ctx.getStyle()), ctx, i, end));
                x += w + margin / 2 + 1;
                i = end;

                if (i < text.length()) newLine(false);
            }
        }

        private void addImage(ImageContext ctx) {
            int width = ctx.getWidth();
            int height = ctx.getHeight();
            if (width <= 0 || height <= 0) return;

            if (ctx.isFit()) {
                if (!spans.isEmpty() || !images.isEmpty() || x > 0) newLine(false);

                int drawHeight = (int) Math.max(1, Math.floor(height * ((float) maxWidth / width)));
                images.add(new ImageSpan(x, maxWidth, drawHeight, ctx));

                x += maxWidth;
                lineHeight = Math.max(lineHeight, drawHeight);

                newLine(false);
            } else {
                if (x > 0 && (x + width) > maxWidth) newLine(false);

                int drawWidth = Math.min(width, maxWidth);
                int drawHeight = (drawWidth == width) ? height : (int) Math.max(1, Math.floor(height * ((double) maxWidth / width)));

                int align = switch (ctx.getAlign()) {
                    case LEFT -> 0;
                    case CENTER -> ((maxWidth - x) - drawWidth) / 2;
                    case RIGHT -> ((maxWidth - x) - drawWidth);
                };

                if (x + align + drawWidth > maxWidth) newLine(false);

                images.add(new ImageSpan(x + align, drawWidth, drawHeight, ctx));

                if (ctx.getAlign() == ImageComponent.ImageAlign.CENTER) x += align + drawWidth;
                if (ctx.getAlign() == ImageComponent.ImageAlign.RIGHT) rightX += drawWidth;
                x += drawWidth;

                lineHeight = Math.max(lineHeight, drawHeight);
            }
        }

        private void newLine(boolean hardBreak) {
            boolean hasSpacing = hardBreak && listItem;
            int height = Math.max(1, lineHeight);

            lines.add(new Line(top, height, hasSpacing, List.copyOf(spans), List.copyOf(images)));

            spans.clear();
            images.clear();

            top += height + (hasSpacing ? itemSpacing : spacing);
            x = 0;
            rightX = 0;
            lineHeight = metrics.lineHeight();
        }

        private int getEnd(String text, int start, int width, Style style) {
            int end = start;
            int length = text.length();

            while (end < length) {
                int mid = (end + length + 1) / 2;
                if (metrics.width(text.substring(start, mid), style) <= width) {
                    end = mid;
                } else {
                    length = mid - 1;
                }
            }
            return end;
        }

        private void finish() {
            if (!spans.isEmpty() || !images.isEmpty() || x > 0 || lines.isEmpty()) newLine(false);
        }

        private List<Line> getLines() {
            return List.copyOf(lines);
        }
    }

    private static int breakPoint(String text, int start, int end) {
        for (int i = end - 1; i > start; i--) {
            char c = text.charAt(i);

            if (c == ' ') {
                return i;
            } else if (c == '-' || c == '/') {
                return i + 1;
            } else if (MarkdownUtils.isCJK(text.charAt(i - 1)) && MarkdownUtils.isCJK(c)) {
                return i;
            }
        }

        return end;
    }

}
