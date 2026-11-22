package top.bk.culinaryjourney.util.markdown;

import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 我懒 TAT
 * TODO : 注释
 */
public final class Style {

    public static final Style EMPTY = new Style(false, false, false, false, false, false, null);

    private final boolean bold;
    private final boolean italic;

    private final boolean strikethrough;
    private final boolean underline;

    private final boolean code;
    private final boolean keyBind;

    @Nullable
    private final TextColor color;

    private Style(boolean bold, boolean italic, boolean strikethrough, boolean underline, boolean code, boolean keyBind, @Nullable TextColor color) {
        this.bold = bold;
        this.italic = italic;
        this.strikethrough = strikethrough;
        this.underline = underline;
        this.code = code;
        this.keyBind = keyBind;
        this.color = color;
    }

    @Nullable
    public TextColor getColor() {
        return color;
    }

    public boolean isBold() {
        return bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public boolean isStrikethrough() {
        return strikethrough;
    }

    public boolean isUnderline() {
        return underline;
    }

    public boolean isCode() {
        return code;
    }

    public boolean isKeyBind() {
        return keyBind;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public Style withBold(boolean bold) {
        return new Style(bold, italic, strikethrough, underline, code, keyBind, color);
    }

    public Style withItalic(boolean italic) {
        return new Style(bold, italic, strikethrough, underline, code, keyBind, color);
    }

    public Style withStrikethrough(boolean strikethrough) {
        return new Style(bold, italic, strikethrough, underline, code, keyBind, color);
    }

    public Style withUnderline(boolean underline) {
        return new Style(bold, italic, strikethrough, underline, code, keyBind, color);
    }

    public Style withCode(boolean code) {
        return new Style(bold, italic, strikethrough, underline, code, keyBind, color);
    }

    public Style withKeyBind(boolean keyBind) {
        return new Style(bold, italic, strikethrough, underline, code, keyBind, color);
    }

    public Style withColor(@Nullable TextColor color) {
        return new Style(bold, italic, strikethrough, underline, code, keyBind, color);
    }

    public net.minecraft.network.chat.Style toStyle() {
        return toStyle(this);
    }

    public static net.minecraft.network.chat.Style toStyle(Style style) {
        return net.minecraft.network.chat.Style.EMPTY
                .withBold(style.bold)
                .withItalic(style.italic)
                .withUnderlined(style.underline)
                .withStrikethrough(style.strikethrough)
                .withColor(style.color);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Style style)) return false;
        return isBold() == style.isBold() && isItalic() == style.isItalic() && isStrikethrough() == style.isStrikethrough() && isUnderline() == style.isUnderline() && isCode() == style.isCode() && Objects.equals(getColor(), style.getColor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isBold(), isItalic(), isStrikethrough(), isUnderline(), isCode(), getColor());
    }
}