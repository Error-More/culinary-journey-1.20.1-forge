package top.bk.culinaryjourney.mixin.ftbquests;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.BlankPanel;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.util.client.ImageComponent;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.bk.culinaryjourney.client.MarkdownComponent;
import top.bk.culinaryjourney.client.ui.markdown.MarkdownLink;
import top.bk.culinaryjourney.client.ui.markdown.MarkdownRenderer;
import top.bk.culinaryjourney.client.ui.markdown.MarkdownView;
import top.bk.culinaryjourney.util.markdown.Block;
import top.bk.culinaryjourney.util.markdown.MarkdownUtils;

import java.util.List;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 通过 Mixin Inject, WrapOperation 的分层 Mixin 与 flag 实现对于 MarkdownWidget 的创建.
 */
@Mixin(value = ViewQuestPanel.class, remap = false)
public abstract class MixinViewQuestPanel {

    /**
     * 由于分层 Mixin 所以将局部变量转换为 class field.
     */
    @Unique private MarkdownRenderer culinary_journey$renderer;
    @Unique private MarkdownLink culinary_journey$link;
    @Unique private boolean culinary_journey$markdown;
    @Unique private boolean culinary_journey$continue;
    @Unique private StringBuilder culinary_journey$markdownText;

    /**
     * 所有描述的父面板.
     */
    @Shadow private BlankPanel panelText;

    /**
     * 任务界面实例, 用于 getTheme.
     *
     * @see QuestScreen#getTheme()
     */
    @Shadow @Final private QuestScreen questScreen;

    /**
     * 当前正在显示的任务实例.
     */
    @Shadow private Quest quest;

    /**
     * 用于获取 Component 与其的所有子 Component 是否包含 ImageComponent.
     *
     * @param c Component 实例
     * @return  ImageComponent 实例 (可为 null)
     */
    @Shadow protected abstract ImageComponent findImageComponent(Component c);

    /**
     * Mixin Inject {@link ViewQuestPanel#addDescriptionText(boolean, Component)} HEAD 对本应为局部变量的字段进行初始化.
     *
     * @see ViewQuestPanel#addDescriptionText(boolean, Component)
     * @param canEdit  描述文本是否能够编辑
     * @param subtitle 任务副标题
     * @param ci       注入回调
     */
    @Inject(method = "addDescriptionText", at = @At(value = "HEAD"))
    private void createRenderer(boolean canEdit, Component subtitle, CallbackInfo ci) {
        culinary_journey$renderer = new MarkdownRenderer(questScreen.getTheme());
        culinary_journey$link = new MarkdownLink();

        culinary_journey$markdown = false;
        culinary_journey$markdownText = new StringBuilder();
    }

    /**
     * Mixin Inject {@link ViewQuestPanel#addDescriptionText(boolean, Component)} 在每次 getDescription 时 (即 for 的第一次操作) 添加 Markdown 逻辑.
     *
     * @see ViewQuestPanel#addDescriptionText(boolean, Component)
     * @param canEdit  描述文本是否能够编辑
     * @param subtitle 任务副标题
     * @param ci       注入回调
     * @param i        通过 @Local 捕获的 for 循环 index
     */
    @Inject(method = "addDescriptionText", at = @At(value = "INVOKE",
            target = "Ldev/ftb/mods/ftbquests/quest/Quest;getDescription()Ljava/util/List;", ordinal = 1))
    private void addMarkdownDescription(boolean canEdit, Component subtitle, CallbackInfo ci,
                                        @Local(name = "i") int i) {
        // 是否跳过添加 QuestDescriptionField.
        culinary_journey$continue = false;

        Component component = quest.getDescription().get(i);

        ImageComponent img = findImageComponent(component);
        MarkdownComponent md = culinary_journey$findMarkdownComponent(component);
        if (md != null) {
            culinary_journey$markdown = md.isBegin();
            if (culinary_journey$markdown) {
                culinary_journey$markdownText.setLength(0);
            } else {
                List<Block> blocks = MarkdownUtils.parseToBlocks(culinary_journey$markdownText.toString());
                MarkdownView.addBlocks(panelText, blocks, panelText.getWidth(),
                        culinary_journey$renderer, culinary_journey$link);
            }
            culinary_journey$continue = true;
        } else if (img == null && culinary_journey$markdown) {
            String string = component.getString();
            culinary_journey$markdownText.append(string);
            culinary_journey$markdownText.append("\n");
            culinary_journey$continue = true;
        }
    }

    /**
     * Mixin WrapOperation 重定向 {@link ViewQuestPanel#addDescriptionText(boolean, Component)} 中的 {@link Panel#add(Widget)} 根据 flag 决定是否允许调用.
     * Origin {@code this.panelText.add(field);}
     *
     * @see ViewQuestPanel#addDescriptionText(boolean, Component)
     * @param instance 原调用方法所属实例
     * @param widget   原调用方法实参
     * @param original 原操作
     */
    @WrapOperation(method = "addDescriptionText",
            at = @At(value = "INVOKE",
                    target = "Ldev/ftb/mods/ftblibrary/ui/BlankPanel;add(Ldev/ftb/mods/ftblibrary/ui/Widget;)V",
                    ordinal = 2))
    private void addMarkdownDescriptionContinue(BlankPanel instance, Widget widget,
                                                Operation<Void> original) {
        if (culinary_journey$continue) return;
        original.call(instance, widget);
    }

    /**
     * 用于获取 Component 与其的所有子 Component 是否包含 MarkdownComponent.
     *
     * @param c Component 实例
     * @return  MarkdownComponent 实例
     */
    @Unique
    private MarkdownComponent culinary_journey$findMarkdownComponent(Component c) {
        for (Component c1 : c.getSiblings()) {
            if (c1.getContents() instanceof MarkdownComponent markdown) {
                return markdown;
            } else {
                return culinary_journey$findMarkdownComponent(c1);
            }
        }
        if (c.getContents() instanceof MarkdownComponent markdown) {
            return markdown;
        }
        return null;
    }

}
