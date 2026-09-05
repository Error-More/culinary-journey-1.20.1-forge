package top.bk.culinaryjourney;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.bk.culinaryjourney.integration.thirst.ItemStackModifier;

/**
 * 模组主入口类。
 *
 * @since 1.0.0
 */
@Mod(CulinaryJourney.MOD_ID)
public class CulinaryJourney {

    /** 模组 ID，同时用作资源命名空间 */
    public static final String MOD_ID = "culinary_journey";

    /** 模组日志器，日志前缀为模组 ID */
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    /**
     * 模组构造入口，由 FML 在模组加载阶段调用。
     *
     * @param context 模组加载上下文，提供本模组专属的事件总线
     */
    public CulinaryJourney(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::compatSetup);
    }

    /**
     * 执行与其他模组的兼容补丁。
     *
     * 为何必须用 {@code enqueueWork} 包装：{@link FMLCommonSetupEvent} 会与其他模组并行派发，
     * 而这里的补丁要读写别的模组的类。直接执行会抢在目标模组初始化完成之前，
     * 排入队列后才会串行执行，从而保证依赖已就绪。
     *
     * @param event 公共初始化事件
     */
    private void compatSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ItemStackModifier::init);
    }
}