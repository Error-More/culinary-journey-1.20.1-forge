package top.bk.culinaryjourney.client.ui.markdown;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Theme;
import top.bk.culinaryjourney.CulinaryJourney;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineContext.*;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineLayout.*;
import top.bk.culinaryjourney.util.markdown.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 我懒 TAT
 * TODO : 注释
 */
public class MarkdownRenderer implements TextMetrics, Renderer {

    public static final class Settings {
        public static final Settings EMPTY = new Settings(0xFF2D2D2D,
                0xFF303030, 0xFF282828, 0xFF4493F8, false);

        public static final float BACKGROUND_SCALE = 0.15F;

        private final int codeColor;
        private final int keyBindColor;
        private final int keyBindShadowColor;
        private final int linkColor;

        private final boolean shadow;

        public Settings(int codeColor, int keyBindColor, int keyBindShadowColor, int linkColor, boolean shadow) {
            this.codeColor = codeColor;
            this.keyBindColor = keyBindColor;
            this.keyBindShadowColor = keyBindShadowColor;
            this.linkColor = linkColor;
            this.shadow = shadow;
        }

        public int getCodeColor() {
            return codeColor;
        }
        public int getKeyBindColor() {
            return keyBindColor;
        }
        public int getKeyBindShadowColor() {
            return keyBindShadowColor;
        }
        public int getLinkColor() {
            return linkColor;
        }
        public boolean isShadow() {
            return shadow;
        }
    }

    private final Theme theme;
    private final Settings settings;
    private final float scale;
    private final Map<String, Icon> icons = new ConcurrentHashMap<>();

    public MarkdownRenderer(Theme theme) {
        this(theme, Settings.EMPTY, 1.0F);
    }

    public MarkdownRenderer(Theme theme, Settings settings) {
        this(theme, settings, 1.0F);
    }

    private MarkdownRenderer(Theme theme, Settings settings, float scale) {
        this.theme = theme;
        this.settings = settings;
        this.scale = scale;
    }

    public MarkdownRenderer scaled(float scale) {
        return scale == this.scale ? this : new MarkdownRenderer(theme, settings, scale);
    }

    public Map<String, Icon> getIcons() {
        return icons;
    }
    public float getScale() {
        return scale;
    }
    public Settings getSettings() {
        return settings;
    }

    private int getWidth(String text, Style style) {
        if (text.isEmpty()) return 0;
        return theme.getFont().width(Component.literal(text).withStyle(style.toStyle()));
    }

    @Override
    public int width(String text, Style style) {
        return Math.round(getWidth(text, style) * (style.isCode() || style.isKeyBind()
                ? scale - Settings.BACKGROUND_SCALE : scale));
    }

    @Override
    public int rawWidth(String text, Style style) {
        return Math.round(getWidth(text, style) * scale);
    }

    @Override
    public int lineHeight() {
        return Math.round(theme.getFontHeight() * scale);
    }

    @Override
    public void drawText(GuiGraphics graphics, TextContext ctx, int start, int end,
                         float x, int y, int color) {
        String text = ctx.getText().substring(start, end);
        if (text.isEmpty()) return;

        int drawColor = color;
        if (ctx.getLink() != null && ctx.getStyle().getColor() == null)
            drawColor = settings.getLinkColor();

        net.minecraft.network.chat.Style style = ctx.getStyle().toStyle();

        if (ctx.getLink() != null)
            style = style.applyFormat(ChatFormatting.UNDERLINE);

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        float scale = this.scale - (ctx.getStyle().isCode() || ctx.getStyle().isKeyBind() ?
                Settings.BACKGROUND_SCALE : 0);
        pose.scale(scale, scale, 1.0F);
        graphics.drawString(theme.getFont(), Component.literal(text).withStyle(style),
                0, 0, drawColor, settings.isShadow());
        pose.popPose();
    }

    @Override
    public void drawBackground(GuiGraphics graphics, TextContext ctx, int x, int y,
                               int width, int lineHeight) {
        if (width <= 0) return;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        float scale = this.scale - Settings.BACKGROUND_SCALE;
        pose.scale(scale, scale, 1.0F);
        int i = -2;
        int j = -2;
        if (ctx.getStyle().isCode()) {
            graphics.fill(i, j, width + 2, lineHeight + 2, settings.getCodeColor());
        } else if (ctx.getStyle().isKeyBind()) {
            pose.pushPose();
            pose.translate(0, 0.8F, 0);
            int shadowY = lineHeight + 1;
            graphics.fill(i, shadowY, width + 2, shadowY + 1,
                    settings.getKeyBindShadowColor());
            pose.popPose();

            graphics.fill(i, j, width + 2, lineHeight + 2, settings.getKeyBindColor());

            graphics.renderOutline(i, j, width + 4, lineHeight + 4,
                    settings.getKeyBindShadowColor());
        }
        pose.popPose();
    }

    @Override
    public void drawImage(GuiGraphics graphics, ImageContext ctx, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;

        Icon icon = getIcon(ctx);

        if (icon != null && !icon.isEmpty())
            icon.draw(graphics, x, y, width, height);
    }

    public Icon getIcon(ImageContext ctx) {
        try {
            return icons.computeIfAbsent(ctx.getImage().getImage(), Icon::getIcon);
        } catch (Exception e) {
            CulinaryJourney.LOGGER.error("Failed get resource {}", ctx.getImage().getImage(), e);
        }

        return null;
    }

    @Override
    public void drawPrefix(GuiGraphics graphics, String prefix, int x, int y, int color) {
        if (prefix.isEmpty()) return;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, 1.0F);
        graphics.drawString(theme.getFont(), Component.literal(prefix),
                0, 0, color, settings.isShadow());
        pose.popPose();
    }

}
