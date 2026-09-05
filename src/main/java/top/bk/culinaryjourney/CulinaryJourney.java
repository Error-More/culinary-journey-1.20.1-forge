package top.bk.culinaryjourney;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.bk.culinaryjourney.integration.thirst.ItemStackModifier;

/**
 * 模组入口主类.
 */
@Mod(CulinaryJourney.MOD_ID)
public class CulinaryJourney {

    /**
     * 静态 Mod ID 常量.
     */
    public static final String MOD_ID = "culinary_journey";

    /**
     * Mod Logger, 注册模组专属 Logger, 名称使用 Mod ID.
     */
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    /**
     * 入口主类的构造函数: 由 FML 在模组加载阶段调用.
     *
     * @param context 模组加载上下文, 包含模组专属的事件总线
     */
    public CulinaryJourney(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::compatSetup);
    }

    /**
     * 兼容性修复补丁初始化事件.
     *
     * {@link FMLCommonSetupEvent} 属于 ForgeMod 的生命周期事件之一.
     * 绝大多数生命周期时间都是并行的, 因此 {@link FMLCommonSetupEvent} 会被多个 Mod 同时接收.
     * 为了保证线程安全, 需要使用 {@link FMLCommonSetupEvent#enqueueWork(Runnable)} 来调用.
     *
     * @param e 事件实例
     */
    private void compatSetup(FMLCommonSetupEvent e) {
        e.enqueueWork(ItemStackModifier::init);
    }
}