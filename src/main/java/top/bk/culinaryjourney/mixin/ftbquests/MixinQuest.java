package top.bk.culinaryjourney.mixin.ftbquests;

import com.mojang.datafixers.util.Pair;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.bk.culinaryjourney.client.MarkdownComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 通过 Mixin Redirect 替换 {@code getDescription} 中对于 {@code cachedDescription} 的赋值操作.
 * 将所有被 Markdown 标识符包裹的 Component 替换为 literal Component, 并在标识符处创建 MarkdownComponent.
 */
@Mixin(value = Quest.class, remap = false)
public class MixinQuest {

    /**
     * Markdown 的标识符常量, 用于标记 Markdown 的开始与结束.
     */
    @Unique private static final String culinary_journey$MARKDOWN_BEGIN = "{@MarkdownBegin}";
    @Unique private static final String culinary_journey$MARKDOWN_END = "{@MarkdownEnd}";

    /**
     * 未经 parse 的原始描述字符串列表.
     *
     * @see Quest#rawDescription
     */
    @Shadow @Final private List<String> rawDescription;

    /**
     * 经过 parse 后缓存的描述 Component 列表.
     *
     * @see Quest#cachedDescription
     */
    @Shadow private List<Component> cachedDescription;

    /**
     * Mixin Redirect 重定向 {@link Quest#getDescription()} 中对于 {@link Quest#cachedDescription} 的赋值操作, 添加对于 Markdown 的额外处理逻辑.
     * Origin {@code this.cachedDescription = this.rawDescription.stream().map(TextUtils::parseRawText).toList();}
     *
     * @see Quest#cachedDescription
     * @param instance 赋值字段的实例
     * @param value    原赋值值
     */
    @Redirect(method = "getDescription",
            at = @At(value = "FIELD",
                    target = "Ldev/ftb/mods/ftbquests/quest/Quest;cachedDescription:Ljava/util/List;",
                    opcode = Opcodes.PUTFIELD))
    public void getDescription(Quest instance, List<Component> value) {
        cachedDescription = value;

        List<Pair<Integer, Integer>> markdownIndices = culinary_journey$buildDescriptionMarkdown();
        if (!markdownIndices.isEmpty()) {
            // 使 unmodifiableList 可更改
            cachedDescription = new ArrayList<>(cachedDescription);

            for (Pair<Integer, Integer> pair : markdownIndices) {
                List<String> raw = rawDescription.subList(pair.getFirst(), pair.getSecond() + 1);
                List<Component> components = cachedDescription.subList(pair.getFirst(),
                        pair.getSecond() + 1);

                // 将 parse 后的 Component 转换为 literal Component.
                for (int i = 0; i < components.size(); i++)
                    components.set(i, Component.literal(raw.get(i)));

                // 创建 MarkdownComponent
                cachedDescription.set(pair.getFirst() - 1,
                        MutableComponent.create(new MarkdownComponent(true)));
                cachedDescription.set(pair.getSecond() + 1,
                        MutableComponent.create(new MarkdownComponent(false)));
            }
        }
    }

    /**
     * 根据原始描述获取 Markdown 标识符在描述中的索引.
     *
     * @return 所有的 Markdown 标识符对
     */
    @Unique
    public List<Pair<Integer, Integer>> culinary_journey$buildDescriptionMarkdown() {
        List<Pair<Integer, Integer>> markdown = new ArrayList<>();

        Integer markdownBegin = null;
        for (int i = 0; i < rawDescription.size(); i++) {
            String string = rawDescription.get(i);
            switch (string) {
                case Quest.PAGEBREAK_CODE -> {
                    if (markdownBegin != null && markdownBegin != i) {
                        markdown.add(Pair.of(markdownBegin, i - 1));
                    }
                    markdownBegin = null;
                }
                case culinary_journey$MARKDOWN_BEGIN -> markdownBegin = i + 1;
                case culinary_journey$MARKDOWN_END -> {
                    if (markdownBegin != null) {
                        if (markdownBegin == i || markdownBegin == i - 1) {
                            markdownBegin = null;
                            continue;
                        }
                        markdown.add(Pair.of(markdownBegin, i - 1));
                        markdownBegin = null;
                    }
                }
            }
        }
        if (markdownBegin != null && markdownBegin != rawDescription.size() - 1)
            markdown.add(Pair.of(markdownBegin, rawDescription.size() - 1));

        return markdown;
    }

}
