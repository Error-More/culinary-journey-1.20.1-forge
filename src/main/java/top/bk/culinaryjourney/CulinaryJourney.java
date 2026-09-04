package top.bk.culinaryjourney;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.bk.culinaryjourney.client.WindowTitle;
import top.bk.culinaryjourney.integration.thirst.ItemStackModifier;

@Mod(CulinaryJourney.MOD_ID)
public class CulinaryJourney {

    public static final String MOD_ID = "culinary_journey";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public CulinaryJourney(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(WindowTitle::onClientSetup);

        modEventBus.addListener(this::compatSetup);
    }

    private void compatSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ItemStackModifier::init);
    }
}