package top.bk.culinaryjourney.util.markdown;

import java.util.List;
import java.util.Objects;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 所有的 Inline 级节点, 用于对应 Markdown AST 的节点.
 */
public abstract class Inline {

    /**
     * Style 样式, 所有 Inline 必须拥有.
     */
    private final Style style;

    protected Inline(Style style) {
        this.style = style;
    }

    public Style getStyle() {
        return style;
    }

    /**
     * 我懒 TAT
     * TODO : 注释
     */
    public static final class Text extends Inline {
        private final String text;

        public Text(String text, Style style) {
            super(style);
            this.text = text;
        }

        public String getText() {
            return text;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Text text1)) return false;
            return Objects.equals(getText(), text1.getText());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getText());
        }
    }

    public static final class Link extends Inline {
        private final String url;
        private final List<Inline> text;
        private final String title;

        public Link(String url, List<Inline> text, String title, Style style) {
            super(style);
            this.url = url;
            this.text = text;
            this.title = title == null ? "" : title;
        }

        public String getUrl() {
            return url;
        }
        public List<Inline> getText() {
            return text;
        }
        public String getTitle() {
            return title;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Link link)) return false;
            return Objects.equals(getUrl(), link.getUrl()) && Objects.equals(getText(), link.getText()) && Objects.equals(getTitle(), link.getTitle());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getUrl(), getText(), getTitle());
        }
    }

    public static final class Image extends Inline {
        private final String image;
        private final String title;
        private final String align;
        private final String fit;
        private final String width, height;

        public Image(String image, String title, String align, String fit, String width, String height, Style style) {
            super(style);
            this.image = image;
            this.title = title;
            this.align = align == null ? "center" : align;
            this.fit = fit == null ? "false" : fit;
            this.width = width;
            this.height = height;
        }

        public String getImage() {
            return image;
        }
        public String getTitle() {
            return title;
        }
        public String getAlign() {
            return align;
        }
        public String getFit() {
            return fit;
        }
        public String getWidth() {
            return width;
        }
        public String getHeight() {
            return height;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Image image1)) return false;
            return Objects.equals(getImage(), image1.getImage()) && Objects.equals(getTitle(), image1.getTitle()) && Objects.equals(getAlign(), image1.getAlign()) && Objects.equals(getFit(), image1.getFit()) && Objects.equals(getWidth(), image1.getWidth()) && Objects.equals(getHeight(), image1.getHeight());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getImage(), getTitle(), getAlign(), getFit(), getWidth(), getHeight());
        }
    }

    public static final class KeyBind extends Inline {
        private final String key;

        public KeyBind(String key) {
            super(Style.EMPTY);
            this.key = key == null ? "" : key;
        }

        public String getKey() {
            return key;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof KeyBind keyBind)) return false;
            return Objects.equals(getKey(), keyBind.getKey());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getKey());
        }
    }

    public static final class SoftBreak extends Inline {
        public SoftBreak() {
            super(Style.EMPTY);
        }
    }

    public static final class HardBreak extends Inline {
        public HardBreak() {
            super(Style.EMPTY);
        }
    }

}