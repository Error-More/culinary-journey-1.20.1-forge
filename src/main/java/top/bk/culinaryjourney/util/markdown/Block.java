package top.bk.culinaryjourney.util.markdown;

import java.util.List;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 所有的 Block 级节点, 用于对应 Markdown AST 的节点.
 */
public abstract class Block {

    protected Block() { }

    /**
     * 段落
     * 构造传入 {@link Inline} 列表.
     */
    public static final class Paragraph extends Block {
        private final List<Inline> inlines;
        public Paragraph(List<Inline> inlines) {
            this.inlines = inlines;
        }
        public List<Inline> getInlines() {
            return inlines;
        }
    }

    /**
     * 标题
     * 构造传入标题等级与 {@link Inline} 列表.
     */
    public static final class Heading extends Block {
        private final int level;
        private final List<Inline> inlines;
        public Heading(int level, List<Inline> inlines) {
            this.level = level;
            this.inlines = inlines;
        }
        public int getLevel() {
            return level;
        }
        public List<Inline> getInlines() {
            return inlines;
        }
    }

    /**
     * 分割线
     */
    public static final class ThematicBreak extends Block {
        public ThematicBreak() { }
    }

    /**
     * 代码块
     * 传入语言信息与 literal 原始代码字符串.
     */
    public static final class FencedCode extends Block {
        private final String info;
        private final String literal;
        public FencedCode(String info, String literal) {
            this.info = info == null ? "" : info;
            this.literal = literal;
        }
        public String getInfo() {
            return info;
        }
        public String getLiteral() {
            return literal;
        }
    }

    /**
     * 引用块
     * 传入 Block 列表.
     */
    public static final class Quote extends Block {
        private final List<Block> blocks;
        public Quote(List<Block> blocks) {
            this.blocks = blocks;
        }
        public List<Block> getBlocks() {
            return blocks;
        }
    }

    /**
     * 无序列表
     * 传入 Block 列表.
     */
    public static final class BulletList extends Block {
        private final List<Block> blocks;
        public BulletList(List<Block> blocks) {
            this.blocks = blocks;
        }
        public List<Block> getBlocks() {
            return blocks;
        }
    }

    /**
     * 有序列表
     * 传入初始索引与 Block 列表.
     */
    public static final class OrderedList extends Block {
        private final Integer startNumber;
        private final List<Block> blocks;
        public OrderedList(Integer startNumber, List<Block> blocks) {
            this.startNumber = startNumber;
            this.blocks = blocks;
        }
        public Integer getStartNumber() {
            return startNumber;
        }
        public List<Block> getBlocks() {
            return blocks;
        }
    }
}