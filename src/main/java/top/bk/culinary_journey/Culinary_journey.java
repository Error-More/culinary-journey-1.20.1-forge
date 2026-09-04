package top.bk.culinary_journey;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.bk.culinary_journey.client.WindowTitle;
import top.bk.culinary_journey.integration.thirst.ItemStackModifier;

@Mod(Culinary_journey.MODID)
public class Culinary_journey {

    public static final String MODID = "culinary_journey";

    public Culinary_journey(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(WindowTitle::onClientSetup);

        modEventBus.addListener(this::compatSetup);
    }

    // 各兼容逻辑内部自行检测 ModList，未安装对应模组时自动跳过
    private void compatSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ItemStackModifier::init);
    }
}
