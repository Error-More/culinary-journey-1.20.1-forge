package com.culinary_journey.core;

import com.culinary_journey.compat.ItemStackModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// 入口
@Mod(CoreMod.MODID)
public class CoreMod {

    public static final String MODID = "culinary_journey";

    public CoreMod(FMLJavaModLoadingContext context) {
        var modEventBus = context.getModEventBus();
        modEventBus.addListener(WindowTitle::onClientSetup);
        modEventBus.addListener(this::compatSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void compatSetup(FMLCommonSetupEvent e) {
        e.enqueueWork(ItemStackModifier::init);
    }
}
