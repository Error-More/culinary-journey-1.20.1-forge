package top.bk.culinaryjourney.mixin.accessor;

import fi.dy.masa.malilib.hotkeys.KeybindCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = KeybindCategory.class, remap = false)
public interface KeybindCategoryAccessor {

    @Accessor
    String getCategoryName();

}