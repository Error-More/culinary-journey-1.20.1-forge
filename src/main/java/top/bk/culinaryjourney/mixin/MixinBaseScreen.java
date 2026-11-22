package top.bk.culinaryjourney.mixin;

import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.CursorType;
import dev.ftb.mods.ftblibrary.ui.ModalPanel;
import dev.ftb.mods.ftblibrary.ui.Panel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

/**
 * FTB Library 兼容性修复 : 修复 {@link BaseScreen} 没有重写 {@link Panel#getCursor()} 导致的 modalPanels 的 getCursor 全部失效.
 * <p>
 * 使用 Mixin 的机制, 直接 {@code @Override} 目标 Method.
 */
@Mixin(value = BaseScreen.class, remap = false)
public abstract class MixinBaseScreen extends Panel {

    /**
     * 模态面板队列.
     *
     * @see BaseScreen#modalPanels
     */
    @Shadow @Final private Deque<ModalPanel> modalPanels;

    /**
     * 为了保证编译通过的默认构造函数.
     *
     * @param panel 父面板
     */
    public MixinBaseScreen(Panel panel) {
        super(panel);
    }

    /**
     * 重写 getCursor 使其同时返回模态面板的 getCursor 结果.
     *
     * @see Panel#getCursor()
     * @return 光标类型
     */
    @Override
    public @Nullable CursorType getCursor() {
        Optional<CursorType> cursor = modalPanels.stream()
                .map(Panel::getCursor)
                .filter(Objects::nonNull).findFirst();
        return cursor.orElseGet(super::getCursor);
    }

}
