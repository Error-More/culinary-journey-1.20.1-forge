package com.culinary_journey.core;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// 入口
@Mod(CoreMod.MODID)
public class CoreMod {

    public static final String MODID = "culinary_journey";

    public CoreMod(FMLJavaModLoadingContext context) {
        var modEventBus = context.getModEventBus();
        modEventBus.addListener(WindowTitle::onClientSetup);
    }
}
