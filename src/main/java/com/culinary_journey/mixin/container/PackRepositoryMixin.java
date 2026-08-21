package com.culinary_journey.mixin.container;

import com.culinary_journey.core.ForceResourcePack;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Collection;
import java.util.List;

// 强制资源包
@Mixin(PackRepository.class)
public class PackRepositoryMixin {

    @ModifyArg(
            method = "setSelected",
            at = @At("HEAD"),
            index = 0)
    private Collection<String> onSetSelected(Collection<String> ids) {
        List<String> enforced = ForceResourcePack.enforce(ids);
        return enforced;
    }
}
