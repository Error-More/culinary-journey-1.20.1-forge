package top.bk.culinaryjourney.client;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import top.bk.culinaryjourney.CulinaryJourney;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 动态窗口标题
 *
 * 仅负责计算标题，写入系统窗口由 {@link top.bk.culinaryjourney.mixin.MixinWindowTitle} 完成。
 *
 * 事件里只置标记，刷新统一推迟到下一个 tick
 *
 * @since 1.0.0
 */
@Mod.EventBusSubscriber(modid = CulinaryJourney.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class WindowTitle {

    /** 日志前缀 */
    private static final String MARKER = "[WindowTitle]";

    /** 标题无版本基础部分的国际化键 */
    private static final String I18N_TITLE_BASE = "culinary_journey.windowtitle.base";

    /** 多人服务器后缀国际化键 */
    private static final String I18N_SUFFIX_MULTIPLAYER = "culinary_journey.windowtitle.multiplayer";

    /** 局域网开放后缀国际化键 */
    private static final String I18N_SUFFIX_LAN = "culinary_journey.windowtitle.lan";

    /** 单人世界后缀国际化键 */
    private static final String I18N_SUFFIX_SOLO = "culinary_journey.windowtitle.solo";

    /** 版本号文件 */
    private static final Path VERSION_PATH =
            FMLPaths.CONFIGDIR.get().resolve("fancymenu/assets/version.txt");

    /** 组合后的基础标题（本地化基础文本 + 可选的版本后缀） */
    private static volatile String baseTitle = "";

    /** 本地化基础文本（无版本），随语言切换更新 */
    private static volatile String baseText = "";

    /** 版本号文本，仅首次进入主菜单时读取一次，之后不再变化 */
    private static volatile String versionText = "";

    /** 状态已变化，需在下一个 tick 刷新标题 */
    private static volatile boolean pendingStateRefresh = false;

    /** 已首次进入主菜单，需在下一个 tick 读取版本文件 */
    private static volatile boolean pendingVersionRead = false;

    /** 是否已完全进入主菜单并完成初始化，保证仅首次进入主菜单时初始化一次 */
    private static final AtomicBoolean versionRead = new AtomicBoolean(false);

    /** 上次记录的语言代码，用于探测语言切换以重读基础标题 */
    private static volatile String lastLocale = null;

    /** 服务器名提取策略 */
    private static volatile ServerNameGetter serverNameGetter = ServerNameGetter.FALLBACK;

    /** 当前是否处于游戏中 */
    private static volatile boolean inGame = false;

    /** 当前是否为多人服务器 */
    private static volatile boolean isMultiplayer = false;

    /** 当前单人世界是否已开放到局域网 */
    private static volatile boolean isLan = false;

    /** 世界名或服务器显示名，用于标题后缀 */
    private static volatile String locationName = "";

    /** 服务器名提取策略 */
    @FunctionalInterface
    private interface ServerNameGetter {

        /** 兜底策略：直接读 {@code ip} 字段 */
        ServerNameGetter FALLBACK = server -> {
            try {
                Field ip = ServerData.class.getField("ip");
                Object v = ip.get(server);
                return v instanceof String s ? s : "";
            } catch (Exception ignored) {
                return "";
            }
        };

        /**
         * 提取服务器显示名
         *
         * @param server 当前连接的服务器数据
         * @return 显示名，失败时返回空串
         */
        String apply(ServerData server);
    }

    /**
     * 是否已完全进入主菜单并完成初始化
     *
     * 供 {@link top.bk.culinaryjourney.mixin.MixinWindowTitle} 判断是否接管 {@code setTitle}：
     * 初始化前不接管，保留原版窗口标题，同时避免启动阶段无谓的标题计算。复用 {@link #versionRead} 的语义。
     *
     * @return 已初始化返回 {@code true}
     */
    public static boolean isInitialized() {
        return versionRead.get();
    }

    /**
     * 计算当前应当显示的完整窗口标题
     *
     * 只读事件维护好的状态字段，不主动采样游戏会话。
     *
     * @return 完整标题
     */
    public static String computeCurrentTitle() {
        if (!inGame) {
            return baseTitle;
        }
        String name = locationName == null ? "" : locationName;
        String suffix;
        if (isMultiplayer) {
            suffix = I18n.get(I18N_SUFFIX_MULTIPLAYER);
        } else if (isLan) {
            suffix = I18n.get(I18N_SUFFIX_LAN);
        } else {
            suffix = I18n.get(I18N_SUFFIX_SOLO);
        }
        return baseTitle + " | " + suffix + ": " + name;
    }

    /**
     * 进入世界时排布一次标题刷新
     *
     * @param event 客户端登录事件
     */
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        captureState();
        pendingStateRefresh = true;
    }

    /**
     * 离开世界时排布一次标题刷新
     *
     * @param event 客户端登出事件
     */
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        inGame = false;
        isMultiplayer = false;
        isLan = false;
        locationName = "";
        pendingStateRefresh = true;
    }

    /**
     * 单人世界开放到局域网时排布一次标题刷新
     *
     * 由 {@link top.bk.culinaryjourney.mixin.MixinIntegratedServerLan} 回调
     */
    public static void onLanOpened() {
        isLan = true;
        pendingStateRefresh = true;
    }

    /**
     * 单人世界关闭局域网时排布一次标题刷新
     *
     * 由 {@link top.bk.culinaryjourney.mixin.MixinIntegratedServerLan} 回调
     */
    public static void onLanClosed() {
        isLan = false;
        pendingStateRefresh = true;
    }

    /**
     * 首次进入主菜单时排布一次版本文件读取
     *
     * @param event 界面初始化完成事件
     */
    @SubscribeEvent
    public static void onMainMenuInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen)) return;
        if (versionRead.compareAndSet(false, true)) {
            // 完全进入主菜单时进行一次性初始化
            baseText = I18n.get(I18N_TITLE_BASE);
            serverNameGetter = resolveServerNameGetter();
            pendingVersionRead = true;
            pendingStateRefresh = true;
            rebuildBaseTitle();
        }
    }

    /**
     * 消费脏标记，执行版本读取与标题刷新
     *
     * @param event 客户端 tick 事件
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        boolean dirty = false;

        // 语言切换后，仅本地化基础文本需要重设
        if (versionRead.get()) {
            String locale = currentLocale();
            if (lastLocale != null && !lastLocale.equals(locale)) {
                refreshBaseText();
                pendingStateRefresh = true;
            }
            lastLocale = locale;
        }

        if (pendingVersionRead) {
            pendingVersionRead = false;
            refreshVersionText();   // 仅首次：读取版本文件
            dirty = true;
        }
        if (pendingStateRefresh) {
            pendingStateRefresh = false;
            dirty = true;
        }
        if (dirty) {
            trigger();
        }
    }

    /**
     * 读取当前语言代码
     *
     * @return 语言代码，无法获取时返回空串
     */
    private static String currentLocale() {
        String sel = Minecraft.getInstance().getLanguageManager().getSelected();
        return sel == null ? "" : sel;
    }

    /**
     * 首次进入主菜单时读取版本文件并更新基础标题
     *
     * 仅在 {@code pendingVersionRead} 消费时调用一次，语言切换不会再触发。
     */
    private static void refreshVersionText() {
        versionText = readVersionFile();
        rebuildBaseTitle();
    }

    /**
     * 仅刷新本地化基础文本（语言切换时调用），不读取版本文件
     */
    private static void refreshBaseText() {
        baseText = I18n.get(I18N_TITLE_BASE);
        rebuildBaseTitle();
    }

    /**
     * 用本地化基础文本与版本号组合出最终基础标题
     */
    private static void rebuildBaseTitle() {
        baseTitle = versionText.isEmpty() ? baseText : baseText + " - " + versionText;
    }

    /**
     * 触发一次标题写入
     */
    private static void trigger() {
        var window = Minecraft.getInstance().getWindow();
        if (window != null) {
            // 空串是刻意设计：setTitle 已被 Mixin 接管并取消原实现，参数值本身无意义
            window.setTitle("");
        }
    }

    /** 从当前游戏会话读取并缓存状态，供标题计算使用 */
    private static void captureState() {
        Minecraft mc = Minecraft.getInstance();
        ServerData server = mc.getCurrentServer();
        if (server != null) {
            inGame = true;
            isMultiplayer = true;
            isLan = false;
            locationName = serverNameGetter.apply(server);
            return;
        }
        IntegratedServer integrated = mc.getSingleplayerServer();
        if (integrated != null) {
            inGame = true;
            isMultiplayer = false;
            isLan = integrated.isPublished();
            String name = integrated.getWorldData().getLevelName();
            locationName = name == null ? "" : name;
            return;
        }
        inGame = false;
        isMultiplayer = false;
        isLan = false;
        locationName = "";
    }

    /**
     * 探测可用的服务器名提取方式
     *
     * @return 探测到的实现，永不返回 {@code null}
     */
    @SuppressWarnings("null")
    private static ServerNameGetter resolveServerNameGetter() {
        // 依次尝试"方法"和"public 字段"两种形式，兼容不同映射下的命名
        String[] candidates = {"getMotd", "motd", "serverMOTD"};
        for (String name : candidates) {
            try {
                Method m = ServerData.class.getMethod(name);
                if (m.getReturnType() == String.class) {
                    return server -> {
                        try {
                            Object r = m.invoke(server);
                            return r instanceof String s ? s : "";
                        } catch (Exception ignored) {
                            return ServerNameGetter.FALLBACK.apply(server);
                        }
                    };
                }
            } catch (Exception ignored) {
                try {
                    Field f = ServerData.class.getField(name);
                    if (f.getType() == String.class) {
                        return server -> {
                            try {
                                Object r = f.get(server);
                                return r instanceof String s ? s : "";
                            } catch (Exception ignored2) {
                                return ServerNameGetter.FALLBACK.apply(server);
                            }
                        };
                    }
                } catch (Exception ignored2) {
                    // 字段也不存在，尝试下一个候选名
                }
            }
        }
        return ServerNameGetter.FALLBACK;
    }

    /**
     * 读取版本号文件，仅返回版本号文本
     *
     * @return 版本号，缺失或为空时返回空串
     */
    private static String readVersionFile() {
        if (!Files.exists(VERSION_PATH)) {
            return "";
        }
        try {
            List<String> lines = Files.readAllLines(VERSION_PATH, StandardCharsets.UTF_8);
            // 只取第一个非空行
            return lines.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .findFirst()
                    .orElse("");
        } catch (IOException e) {
            CulinaryJourney.LOGGER.info("{} failed to read version file: {}", MARKER, e.toString());
            return "";
        }
    }
}
