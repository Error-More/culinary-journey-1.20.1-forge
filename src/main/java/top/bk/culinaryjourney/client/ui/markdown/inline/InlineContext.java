package top.bk.culinaryjourney.client.ui.markdown.inline;

import dev.ftb.mods.ftblibrary.util.client.ImageComponent;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindCategory;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.settings.KeyModifier;
import top.bk.culinaryjourney.integration.mafglib.KeyBindHelper;
import top.bk.culinaryjourney.mixin.accessor.KeybindCategoryAccessor;
import top.bk.culinaryjourney.util.markdown.Inline;
import top.bk.culinaryjourney.util.markdown.MarkdownUtils;
import top.bk.culinaryjourney.util.markdown.Style;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.mojang.blaze3d.platform.InputConstants.UNKNOWN;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 我懒 TAT
 * TODO : 注释
 */
public class InlineContext {

    public static final int DEFAULT_IMAGE_SIZE = 100;

    public interface Context { }

    public static final class LinkContext implements Context {
        private final String url;
        private final String title;

        public LinkContext(String url, String title) {
            this.url = url;
            this.title = title == null ? "" : title;
        }

        public String getUrl() {
            return url;
        }
        public String getTitle() {
            return title;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof LinkContext that)) return false;
            return Objects.equals(getUrl(), that.getUrl()) && Objects.equals(getTitle(), that.getTitle());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getUrl(), getTitle());
        }
    }

    public static final class TextContext implements Context {
        private final String text;
        private final Style style;
        @Nullable
        private final LinkContext link;

        public TextContext(String text, Style style, @Nullable LinkContext link) {
            this.text = text;
            this.style = style;
            this.link = link;
        }

        public String getText() {
            return text;
        }
        public Style getStyle() {
            return style;
        }
        public @Nullable LinkContext getLink() {
            return link;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof TextContext that)) return false;
            return Objects.equals(getText(), that.getText()) && Objects.equals(getStyle(), that.getStyle()) && Objects.equals(getLink(), that.getLink());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getText(), getStyle(), getLink());
        }
    }

    public static final class ImageContext implements Context {
        private final Inline.Image image;
        private final boolean fit;
        private final ImageComponent.ImageAlign align;
        private final int width, height;
        @Nullable
        private final LinkContext link;

        public ImageContext(Inline.Image image, int width, int height, @Nullable LinkContext link) {
            this.image = image;
            this.width = width;
            this.height = height;
            this.fit = image.getFit().equals("true");
            this.align = ImageComponent.ImageAlign.fromString(image.getAlign());
            this.link = link;
        }

        public Inline.Image getImage() {
            return image;
        }
        public boolean isFit() {
            return fit;
        }
        public ImageComponent.ImageAlign getAlign() {
            return align;
        }
        public int getWidth() {
            return width;
        }
        public int getHeight() {
            return height;
        }
        public @Nullable LinkContext getLink() {
            return link;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ImageContext that)) return false;
            return getWidth() == that.getWidth() && getHeight() == that.getHeight() && Objects.equals(getImage(), that.getImage()) && Objects.equals(getLink(), that.getLink());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getImage(), getWidth(), getHeight(), getLink());
        }
    }

    public static final class Break implements Context {
        public Break() { }
    }

    public static final class Soft implements Context {
        public Soft() { }
    }

    public static List<Context> parse(List<Inline> inlines) {
        ArrayList<Context> contexts = new ArrayList<>(inlines.size());

        parse(inlines, null, contexts);
        resolveSoftBreaks(contexts);

        return contexts;
    }

    private static void parse(List<Inline> inlines, @Nullable LinkContext ctx, List<Context> contexts) {
        for (Inline inline : inlines) {
            if (inline instanceof Inline.Text text) {
                if (!text.getText().isEmpty()) {
                    String string = text.getStyle().isCode() ? text.getText() : resolveSpace(text.getText());
                    contexts.add(new TextContext(string, text.getStyle(), ctx));
                }
            } else if (inline instanceof Inline.Link link) {
                parse(link.getText(), new LinkContext(link.getUrl(), link.getTitle()), contexts);
            } else if (inline instanceof Inline.Image image) {
                contexts.add(new ImageContext(image, parseSize(image.getWidth()), parseSize(image.getHeight()), ctx));
            } else if (inline instanceof Inline.KeyBind keyBind) {
                KeyMapping keyMapping = Arrays.stream(Minecraft.getInstance().options.keyMappings)
                        .filter(mapping -> mapping.getName().equals(keyBind.getKey()))
                        .findFirst().orElse(null);

                List<String> keys = Arrays.stream(keyBind.getKey().split(":", 2))
                        .filter(s -> !s.isEmpty()).toList();
                KeybindCategory keybindCategory = null;
                if (keys.size() == 2) {
                    keybindCategory = KeyBindHelper.getCategories().stream()
                            .filter(c -> ((KeybindCategoryAccessor) c)
                                    .getCategoryName().equals(keys.get(0)))
                            .findFirst().orElse(null);
                }

                LinkContext link = null;

                IHotkey hotkey;
                if (keybindCategory != null) {
                    hotkey = keybindCategory.getHotkeys().stream()
                            .filter(key -> key.getName().equals(keys.get(1)))
                            .findFirst().orElse(null);
                    if (hotkey != null) link = new LinkContext(keys.get(1),
                            Component.translatable(hotkey.getName()).getString());
                } else {
                    hotkey = KeyBindHelper.getHotkey(keyBind.getKey());
                    if (hotkey != null) link = new LinkContext(keyBind.getKey(),
                            Component.translatable(hotkey.getName()).getString());
                }

                if (keyMapping != null) {
                    String key = keyMapping.getKey().getDisplayName().getString();
                    KeyModifier keyModifier = keyMapping.getKeyModifier();
                    if (key.isEmpty()) key = UNKNOWN.getDisplayName().getString();

                    String category = keyMapping.getCategory();
                    String name = keyMapping.getName();

                    StringBuilder search = new StringBuilder();
                    if (!category.isEmpty() && !name.isEmpty())
                        search.append("category:")
                                .append("\"").append(Component.translatable(category).getString()).append("\"")
                                .append(" name:")
                                .append("\"").append(Component.translatable(name).getString()).append("\"");

                    link = search.toString().isEmpty() ?
                            null : new LinkContext(search.toString(), null);

                    if (keyModifier != KeyModifier.NONE) {
                        String modifier = keyModifier.getCombinedName(keyMapping.getKey(), Component::empty)
                                .getString().replace("+", "").trim();
                        if (!modifier.isEmpty()) {
                            contexts.add(new TextContext(modifier, Style.EMPTY.withKeyBind(true), link));
                            contexts.add(new TextContext(" + ", Style.EMPTY, null));
                        }
                    }
                    contexts.add(new TextContext(key, Style.EMPTY.withKeyBind(true), link));
                } else if (hotkey != null) {
                    List<String> keyList = KeyBindHelper.getTranslatedKeyList(hotkey);
                    for (int i = 0; i < keyList.size(); i++) {
                        String s = keyList.get(i);
                        contexts.add(new TextContext(s, Style.EMPTY.withKeyBind(true), link));
                        if (i < keyList.size() - 1)
                            contexts.add(new TextContext(" + ", Style.EMPTY, null));
                    }
                } else {
                    contexts.add(new TextContext(keyBind.getKey(), Style.EMPTY.withKeyBind(true), null));
                }
            } else if (inline instanceof Inline.HardBreak) {
                contexts.add(new Break());
            } else if (inline instanceof Inline.SoftBreak) {
                contexts.add(new Soft());
            }
        }
    }

    private static int parseSize(String size) {
        if (size == null || size.isEmpty())
            return DEFAULT_IMAGE_SIZE;

        try {
            int value = Integer.parseInt(size);
            return value > 0 ? value : DEFAULT_IMAGE_SIZE;
        } catch (NumberFormatException e) {
            return DEFAULT_IMAGE_SIZE;
        }
    }

    private static void resolveSoftBreaks(List<Context> contexts) {
        for (int i = 0; i < contexts.size(); i++) {
            if (!(contexts.get(i) instanceof Soft))
                continue;

            TextContext before = findText(contexts, i, -1);
            TextContext after = findText(contexts, i, 1);

            if (before == null || after == null
                    || before.getText().isEmpty() || after.getText().isEmpty()) {
                contexts.remove(i--);
                continue;
            }

            String beforeText = before.getText();
            String afterText = after.getText();

            if (MarkdownUtils.isCJK(beforeText.codePointBefore(beforeText.length()))
                    && MarkdownUtils.isCJK(afterText.codePointAt(0))) {
                contexts.remove(i--);
            } else {
                contexts.set(i, new TextContext(" ", Style.EMPTY, null));
            }
        }
    }

    private static TextContext findText(List<Context> contexts, int from, int step) {
        for (int i = from + step; i >= 0 && i < contexts.size(); i += step) {
            Context ctx = contexts.get(i);
            if (ctx instanceof TextContext text) {
                return text;
            } else if (ctx instanceof Break) {
                return null;
            }
        }
        return null;
    }

    private static String resolveSpace(String text) {
        int length = text.length();
        StringBuilder builder = new StringBuilder(length);

        boolean space = false;
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            if (c == ' ' || c == '\t') {
                space = true;
            } else {
                if (space) {
                    builder.append(' ');
                    space = false;
                }
                builder.append(c);
            }
        }

        if (space) builder.append(' ');

        return builder.toString();
    }

}
