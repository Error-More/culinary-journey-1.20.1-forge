package top.bk.culinaryjourney.client.ui.markdown.widget;

import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.KeyboardHandler;
import top.bk.culinaryjourney.client.ui.markdown.MarkdownImageOverlay;
import top.bk.culinaryjourney.client.ui.markdown.MarkdownRenderer;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineContext.*;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineLayout;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineLayout.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 我懒 TAT
 * TODO : 注释
 */
public class BlockWidget extends Widget {

    public static final class Settings {
        public static final Settings EMPTY = new Settings(0, 0, 4,
                5, "", 0, 0, false);

        private final int marginTop;
        private final int marginBottom;
        private final int spacing;
        private final int itemSpacing;

        private final String prefix;
        private final int prefixWidth;

        private final int indent;
        private final boolean listItem;

        public Settings(int marginTop, int marginBottom, int spacing,
                        int itemSpacing, String prefix, int prefixWidth, int indent, boolean listItem) {
            this.marginTop = marginTop;
            this.marginBottom = marginBottom;
            this.spacing = spacing;
            this.itemSpacing = itemSpacing;
            this.prefix = prefix == null ? "" : prefix;
            this.prefixWidth = prefixWidth;
            this.indent = indent;
            this.listItem = listItem;
        }

        public int getMarginTop() {
            return marginTop;
        }
        public int getMarginBottom() {
            return marginBottom;
        }
        public int getSpacing() {
            return spacing;
        }
        public int getItemSpacing() {
            return itemSpacing;
        }
        public String getPrefix() {
            return prefix;
        }
        public int getPrefixWidth() {
            return prefixWidth;
        }
        public int getIndent() {
            return indent;
        }
        public boolean isListItem() {
            return listItem;
        }

        public Settings withMarginTop(int marginTop) {
            return new Settings(marginTop, marginBottom, spacing, itemSpacing, prefix, prefixWidth, indent, listItem);
        }
        public Settings withMarginBottom(int marginBottom) {
            return new Settings(marginTop, marginBottom, spacing, itemSpacing, prefix, prefixWidth, indent, listItem);
        }
        public Settings withSpacing(int spacing) {
            return new Settings(marginTop, marginBottom, spacing, itemSpacing, prefix, prefixWidth, indent, listItem);
        }
        public Settings withItemSpacing(int itemSpacing) {
            return new Settings(marginTop, marginBottom, spacing, itemSpacing, prefix, prefixWidth, indent, listItem);
        }
        public Settings withPrefix(String prefix) {
            return new Settings(marginTop, marginBottom, spacing, itemSpacing, prefix, prefixWidth, indent, listItem);
        }
        public Settings withPrefixWidth(int prefixWidth) {
            return new Settings(marginTop, marginBottom, spacing, itemSpacing, prefix, prefixWidth, indent, listItem);
        }
        public Settings withIndent(int indent) {
            return new Settings(marginTop, marginBottom, spacing, itemSpacing, prefix, prefixWidth, indent, listItem);
        }
        public Settings withListItem(boolean listItem) {
            return new Settings(marginTop, marginBottom, spacing, itemSpacing, prefix, prefixWidth, indent, listItem);
        }
    }

    protected final InlineLayout layout;

    protected final Renderer renderer;
    @Nullable
    protected final LinkHandler linkHandler;

    protected final Settings settings;

    protected BlockWidget(Panel parent, List<Context> contexts, int width,
                          TextMetrics metrics, Renderer renderer, @Nullable LinkHandler linkHandler,
                          Settings settings) {
        super(parent);

        this.renderer = renderer;
        this.linkHandler = linkHandler;
        this.settings = settings;
        this.layout = new InlineLayout(contexts, width - settings.getIndent(), metrics,
                settings.getSpacing(), settings.getItemSpacing(), settings.isListItem());

        setSize(width, layout.getHeight() + settings.getMarginTop() + settings.getMarginBottom());
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int rawX, int rawY, int w, int h) {
        int color = 0xFFFFFFFF;
        int x = rawX + settings.getIndent();
        int y = rawY + settings.getMarginTop();

        if (!settings.getPrefix().isEmpty() && !layout.getLines().isEmpty()) {
            int prefixX = x - settings.getPrefixWidth();
            Line line = layout.getLines().get(0);
            int prefixY = y + line.top();
            if (!line.images().isEmpty())
                prefixY += line.height() / 2 - ((TextMetrics) renderer).lineHeight() / 2;
            renderer.drawPrefix(graphics, settings.getPrefix(), prefixX, prefixY, color);
        }

        for (Line line : layout.getLines()) {
            int lineY = y + line.top();

            if (!line.images().isEmpty())
                lineY = lineY + line.height() / 2;

            for (Span span : line.texts()) {
                int spanY = lineY;
                if (!line.images().isEmpty())
                    spanY = lineY - ((TextMetrics) renderer).lineHeight() / 2;

                TextColor textColor = span.ctx().getStyle().getColor();
                if (span.ctx().getStyle().isCode() || span.ctx().getStyle().isKeyBind()) {
                    renderer.drawBackground(graphics, span.ctx(), x + span.x(), spanY,
                            span.width(), line.height());
                    if (span.ctx().getStyle().isKeyBind())
                        span = new Span(span.x(), span.width(),
                                new TextContext(span.ctx().getText(), span.ctx().getStyle(), null),
                                span.start(), span.end());
                    renderer.drawText(graphics, span.ctx(), span.start(), span.end(),
                            x + span.x() + 0.2F, spanY, textColor == null ? color : textColor.getValue());
                } else {
                    renderer.drawText(graphics, span.ctx(), span.start(), span.end(),
                            x + span.x(), spanY, textColor == null ? color : textColor.getValue());
                }
            }

            for (ImageSpan image : line.images()) {
                int imageY = lineY - image.height() / 2;
                renderer.drawImage(graphics, image.ctx(), x + image.x(), imageY,
                        image.width(), image.height());
            }
        }
    }

    @Nullable
    public Span spanAt(int mouseX, int mouseY) {
        int x = mouseX - getX() - settings.getIndent();
        int y = mouseY - getY() - settings.getMarginTop();

        for (Line line : layout.getLines()) {
            int top = line.top();
            int height = line.height();

            if (!line.images().isEmpty()) {
                int lineHeight = ((TextMetrics) renderer).lineHeight();
                top += (line.height() / 2) - lineHeight / 2;
                height = lineHeight;
            }

            if (y < top || y >= top + height) continue;

            for (Span span : line.texts()) {
                TextContext ctx = span.ctx();
                String string = ctx.getText().substring(span.start(), span.end());
                int w = ((TextMetrics) renderer).width(string, ctx.getStyle());
                if (x >= span.x() && x < span.x() + w) return span;
            }

            return null;
        }

        return null;
    }

    @Nullable
    public ImageSpan imageAt(int mouseX, int mouseY) {
        int x = mouseX - getX() - settings.getIndent();
        int y = mouseY - getY() - settings.getMarginTop();

        for (Line line : layout.getLines()) {
            if (y < line.top() || y >= line.top() + line.height()) continue;

            for (ImageSpan image : line.images()) {
                int imageY = line.top() + line.height() / 2 - image.height() / 2;

                if (y < imageY || y >= imageY + image.height()) continue;
                if (x >= image.x() && x < image.x() + image.width()) return image;
            }

            return null;
        }

        return null;
    }

    @Override
    public boolean mouseDoubleClicked(MouseButton button) {
        if (!button.isLeft() || !isMouseOver()) return false;

        ImageSpan image = imageAt(getMouseX(), getMouseY());
        if (image != null && image.ctx().getLink() == null) {
            BaseScreen gui = this.getGui();
            gui.pushModalPanel(new MarkdownImageOverlay(gui, image.ctx(), (MarkdownRenderer) renderer));
            return true;
        }

        return false;
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (!button.isLeft() || !isMouseOver()) return false;

        Span span = spanAt(getMouseX(), getMouseY());
        if (span != null) {
            KeyboardHandler keyboardHandler = Minecraft.getInstance().keyboardHandler;
            if (span.ctx().getLink() != null) {
                if (!span.ctx().getStyle().isKeyBind())
                    return linkHandler != null && linkHandler.onClick(span.ctx().getLink());
                keyboardHandler.setClipboard(span.ctx().getLink().getUrl());
                return true;
            } else if (span.ctx().getStyle().isCode()) {
                keyboardHandler.setClipboard(span.ctx().getText());
                return true;
            }
        }

        ImageSpan image = imageAt(getMouseX(), getMouseY());
        if (image != null && image.ctx().getLink() != null)
            return linkHandler != null && linkHandler.onClick(image.ctx().getLink());

        return false;
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        if (!isMouseOver()) return;

        Span span = spanAt(getMouseX(), getMouseY());
        if (span != null) {
            LinkContext link = span.ctx().getLink();
            if (link != null) {
                if (span.ctx().getStyle().isKeyBind()) {
                    list.add(Component.literal(!link.getTitle().isEmpty() ? link.getTitle() : link.getUrl()));
                    return;
                }
                addLinkTooltip(list, link);
                return;
            } else if (span.ctx().getStyle().isCode()) {
                list.add(Component.translatable("ftbquests.gui.copy"));
                return;
            }
        }

        ImageSpan image = imageAt(getMouseX(), getMouseY());
        if (image == null) return;

        if (image.ctx().getLink() != null && !image.ctx().getLink().getTitle().isEmpty()) {
            addLinkTooltip(list, image.ctx().getLink());
        } else {
            String title = image.ctx().getImage().getTitle();
            if (title != null && !title.isEmpty()) {
                list.add(Component.literal(title));
            }
        }
    }

    private void addLinkTooltip(TooltipList list, LinkContext link) {
        if (linkHandler != null) {
            Component hover = linkHandler.onHover(link);
            if (hover != null) {
                list.add(hover);
            }
        } else if (!link.getTitle().isEmpty()) {
            list.add(Component.literal(link.getTitle()));
        }
    }

    @Override
    @Nullable
    public CursorType getCursor() {
        Span span = spanAt(getMouseX(), getMouseY());
        if (span != null && (span.ctx().getLink() != null || span.ctx().getStyle().isCode()))
            return CursorType.HAND;

        ImageSpan image = imageAt(getMouseX(), getMouseY());
        if (image != null && image.ctx().getLink() != null) return CursorType.HAND;

        return super.getCursor();
    }
}