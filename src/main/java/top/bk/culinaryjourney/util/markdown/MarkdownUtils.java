package top.bk.culinaryjourney.util.markdown;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.image.attributes.ImageAttributes;
import org.commonmark.ext.ins.Ins;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import top.bk.culinaryjourney.util.markdown.ext.image.ImageAttributesExtension;
import top.bk.culinaryjourney.util.markdown.ext.keybind.KeyBind;
import top.bk.culinaryjourney.util.markdown.ext.keybind.KeyBindExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 使用 commonmark 解析 Markdown.
 * 注册 Extensions, 创建 Parser, 提供 Markdown AST 的解析并转换为自定义结构.
 */
public class MarkdownUtils {

    /**
     * commonmark Extensions, 创建 commonmark 的扩展.
     */
    private static final List<Extension> EXTENSIONS = List.of(
            StrikethroughExtension.create(),
            ImageAttributesExtension.create(),
            InsExtension.create(),
            KeyBindExtension.create());

    /**
     * commonmark Parser, 创建 commonmark 的解析器并注册所有扩展.
     */
    public static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();

    /**
     * commonmark 的基础解析, 将 Markdown 字符串转换为 Markdown AST.
     *
     * @param markdown 原始 Markdown 字符串
     * @return Markdown AST 抽象语法树
     */
    public static Node parse(String markdown) {
        return PARSER.parse(markdown);
    }

    /**
     * 将 Markdown AST 转换为自定义结构的方法.
     */
    public static List<Block> parseToBlocks(String markdown) {
        return toBlocks(parse(markdown));
    }
    public static List<Block> toBlocks(Node root) {
        return parseBlocks(root);
    }

    /**
     * 遍历访问 Markdown AST 的节点并且进行解析, 独立的 inline 节点会自动包装在 {@link Block.Paragraph} 内.
     *
     * @param node 根节点
     * @return 自定义 Block 列表
     */
    private static List<Block> parseBlocks(Node node) {
        List<Block> blocks = new ArrayList<>();
        List<Inline> inlines = new ArrayList<>();

        visitChildren(node, child -> {
            Block block = parseBlock(child);
            if (block != null) {
                flush(inlines, blocks);
                blocks.add(block);
            } else if (isInline(child)) {
                parseInline(child, Style.EMPTY, inlines);
            }

            return false;
        });
        flush(inlines, blocks);

        return blocks;
    }

    /**
     * 将累计的没有 Block 作为父节点的 inlines 存入 {@link Block.Paragraph}, 添加进 blocks 列表.
     *
     * @param inlines 独立 Inline 列表.
     * @param blocks  目标 Block 列表.
     */
    private static void flush(List<Inline> inlines, List<Block> blocks) {
        if (!inlines.isEmpty()) {
            blocks.add(new Block.Paragraph(new ArrayList<>(inlines)));
            inlines.clear();
        }
    }

    /**
     * 对 List 的所有 ListItem 进行扁平化, 通过 {@link Inline.HardBreak} 进行分隔.
     *
     * @param listItems 拥有 ListItem 的根节点
     * @return 扁平化的结果
     */
    private static List<Block> parseListItems(Node listItems) {
        List<Block> items = new ArrayList<>();

        visitChildren(listItems, item -> {
            List<Block> blocks = parseBlocks(item);

            if (blocks.size() <= 1) {
                items.addAll(blocks);
            } else if (blocks.stream().allMatch(block -> block instanceof Block.Paragraph)) {
                List<Inline> inlines = new ArrayList<>();
                for (int i = 0; i < blocks.size(); i++) {
                    if (i > 0) inlines.add(new Inline.HardBreak());
                    inlines.addAll(((Block.Paragraph) blocks.get(i)).getInlines());
                }
                items.add(new Block.Paragraph(inlines));
            } else {
                items.addAll(blocks);
            }

            return false;
        });

        return items;
    }


    /**
     * 解析所有属于 {@link Block} 的单个节点.
     *
     * @param node 原始 AST 节点
     * @return 解析后的 Block
     */
    private static Block parseBlock(Node node) {
        if (node instanceof Paragraph paragraph) {
            return new Block.Paragraph(parseInlines(paragraph, Style.EMPTY));
        } else if (node instanceof Heading heading) {
            return new Block.Heading(heading.getLevel(), parseInlines(heading, Style.EMPTY));
        } else if (node instanceof ThematicBreak) {
            return new Block.ThematicBreak();
        } else if (node instanceof FencedCodeBlock code) {
            return new Block.FencedCode(code.getInfo(), code.getLiteral());
        } else if (node instanceof IndentedCodeBlock code) {
            return new Block.FencedCode("", code.getLiteral());
        } else if (node instanceof BlockQuote quote) {
            return new Block.Quote(parseBlocks(quote));
        } else if (node instanceof BulletList bulletList) {
            return new Block.BulletList(parseListItems(bulletList));
        } else if (node instanceof OrderedList o) {
            return new Block.OrderedList(o.getMarkerStartNumber(), parseListItems(o));
        }
        return null;
    }

    /**
     * 解析一个父节点下的所有属于 {@link Inline} 的节点.
     *
     * @param node  原始父节点
     * @param style 递归传递的 Style 样式
     * @return 解析后的 Inline
     */
    private static List<Inline> parseInlines(Node node, Style style) {
        List<Inline> inlines = new ArrayList<>();
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            parseInline(child, style, inlines);
        }
        return inlines;
    }

    /**
     * 解析所有属于 {@link Inline} 的单个节点
     *
     * @param node    原始 AST 节点
     * @param style   递归传递的 Style 样式
     * @param inlines 外部传递的解析结果 List.
     */
    private static void parseInline(Node node, Style style, List<Inline> inlines) {
        if (node instanceof Text text) {
            inlines.add(new Inline.Text(text.getLiteral(), style));
        } else if (node instanceof Code code) {
            inlines.add(new Inline.Text(code.getLiteral(), style.withCode(true)));
        } else if (node instanceof Emphasis emphasis) {
            inlines.addAll(parseInlines(emphasis, style.withItalic(true)));
        } else if (node instanceof StrongEmphasis strongEmphasis) {
            inlines.addAll(parseInlines(strongEmphasis, style.withBold(true)));
        } else if (node instanceof Strikethrough strikethrough) {
            inlines.addAll(parseInlines(strikethrough, style.withStrikethrough(true)));
        } else if (node instanceof Ins ins) {
            inlines.addAll(parseInlines(ins, style.withUnderline(true)));
        } else if (node instanceof KeyBind keyBind) {
            inlines.add(new Inline.KeyBind(keyBind.getKey()));
        } else if (node instanceof SoftLineBreak) {
            inlines.add(new Inline.SoftBreak());
        } else if (node instanceof HardLineBreak) {
            inlines.add(new Inline.HardBreak());
        } else if (node instanceof Link link) {
            inlines.add(new Inline.Link(
                    link.getDestination(),
                    parseInlines(link, style), link.getTitle(), style));
        } else if (node instanceof Image image) {
            for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
                if (child instanceof ImageAttributes attributes) {
                    Map<String, String> map = attributes.getAttributes();

                    inlines.add(new Inline.Image(
                            image.getDestination(),
                            image.getTitle(),
                            map.get("align"),
                            map.get("fit"),
                            map.get("width"),
                            map.get("height"),
                            style));

                    return;
                }
            }

            inlines.add(new Inline.Image(
                    image.getDestination(), image.getTitle(),
                    "", "", "", "", style));
        } else if (node instanceof HtmlInline htmlInline) {
            inlines.add(new Inline.Text(htmlInline.getLiteral(), style));
        }
    }

    /**
     * 根据 AST 节点返回该节点是否属于 {@link Inline}
     *
     * @param node AST 节点
     * @return 是否属于 Inline
     */
    private static boolean isInline(Node node) {
        return node instanceof Text
                || node instanceof Code
                || node instanceof Emphasis || node instanceof StrongEmphasis
                || node instanceof Link || node instanceof Image
                || node instanceof SoftLineBreak || node instanceof HardLineBreak
                || node instanceof HtmlInline
                || node instanceof Strikethrough || node instanceof Ins || node instanceof KeyBind;
    }

    /**
     * 遍历访问 AST 节点的所有子节点.
     *
     * @param node AST 根节点
     * @param stop lambda 表达式, 返回值为是否停止遍历
     */
    public static void visitChildren(Node node, Predicate<Node> stop) {
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (stop.test(child)) break;
        }
    }

    /**
     * 判断是否为 CJK 字符.
     *
     * @param code 码点
     * @return 是否为 CJK
     */
    public static boolean isCJK(int code) {
        Character.UnicodeScript script = Character.UnicodeScript.of(code);
        return script == Character.UnicodeScript.HAN
                || script == Character.UnicodeScript.HIRAGANA
                || script == Character.UnicodeScript.KATAKANA
                || script == Character.UnicodeScript.HANGUL;
    }

}