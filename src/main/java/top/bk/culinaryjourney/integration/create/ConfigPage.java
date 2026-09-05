package top.bk.culinaryjourney.integration.create;

import com.electronwill.nightconfig.core.Config;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.bk.culinaryjourney.CulinaryJourney;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 机械动力（Create）配置页面注释汉化
 *
 * 原理：Create 的配置注释写死在代码里、随 jar 分发，且没有开放本地化接口
 * 运行时它表现为 {@code ForgeConfigSpec.ValueSpec} 上私有的 {@code comment} 字段
 * 因此这里在配置界面打开之前，用反射把该字段逐个替换成中文
 *
 * 汉化只影响配置 GUI 的显示，不会写回 Create 的配置文件
 */
@Mod.EventBusSubscriber(modid = CulinaryJourney.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ConfigPage {

    /** 日志前缀 */
    private static final String MARKER = "[I18nCreate]";

    /**
     * 是否已应用过汉化
     *
     * 保证幂等
     */
    private static volatile boolean APPLIED = false;

    /**
     * 客户端初始化入口
     *
     * @param event 客户端初始化事件
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        loadTable();
        // enqueueWork：反射要触碰 Create 的配置对象，必须等所有模组初始化完毕，且在主线程执行
        event.enqueueWork(ConfigPage::applyChinese);
    }

    /** 配置项路径到中文注释的映射表 */
    private static final Map<String, String> COMMENT_TABLE = new LinkedHashMap<>();

    /**
     * 加载汉化表
     */
    private static void loadTable() {
        try {
            java.net.URL url = ConfigPage.class.getClassLoader()
                    .getResource("assets/culinary_journey/integration/create/configpage/zh_cn.json");
            if (url != null) {
                try (InputStream in = url.openStream()) {
                    String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    com.google.gson.JsonElement root = com.google.gson.JsonParser.parseString(json);
                    if (root.isJsonObject()) {
                        flatten(root.getAsJsonObject(), "", COMMENT_TABLE);
                    }
                    CulinaryJourney.LOGGER.info("{} loaded {} comment entries from jar resource", MARKER, COMMENT_TABLE.size());
                }
            } else {
                CulinaryJourney.LOGGER.info("{} comment table not found in jar resource", MARKER);
            }
        } catch (Throwable t) {
            CulinaryJourney.LOGGER.info("{} loadTable failed: {}", MARKER, t.toString());
        }
    }

    /**
     * 把嵌套的 JSON 递归摊平成"点分路径 -> 中文注释"的扁平表。
     *
     * 约定：若某个节点自身既有子项、又需要一条注释，用 {@code "_self"} 键表示"这一层自己的注释"。
     * 该键不计入路径，也不会被当作子配置项使用
     *
     * 路径统一转小写（用 {@link Locale#ROOT} 以避免土耳其语等 locale 下的大小写转换歧义），
     * 因为匹配环节对大小写不敏感
     *
     * @param obj    当前层的 JSON 对象
     * @param prefix 父路径前缀，顶层传空串
     * @param out    结果容器
     */
    private static void flatten(com.google.gson.JsonObject obj, String prefix, Map<String, String> out) {
        for (Map.Entry<String, com.google.gson.JsonElement> e : obj.entrySet()) {
            String rawKey = e.getKey();
            com.google.gson.JsonElement val = e.getValue();
            String key = prefix.isEmpty() ? rawKey : prefix + "." + rawKey;

            if (val.isJsonObject()) {
                com.google.gson.JsonObject child = val.getAsJsonObject();
                if (child.has("_self") && child.get("_self").isJsonPrimitive()) {
                    out.put(key.toLowerCase(Locale.ROOT), child.get("_self").getAsString());
                }
                flatten(child, key, out);
            } else if (val.isJsonPrimitive()) {
                out.put(key.toLowerCase(Locale.ROOT), val.getAsString());
            }
        }
    }

    /**
     * 遍历 Create 的全部配置项，把注释替换为中文。
     *
     * 幂等，重复调用无副作用
     */
    private static void applyChinese() {
        if (APPLIED) {
            return;
        }
        int applied = 0;
        int matched = 0;
        int total = 0;
        // 诊断用
        StringBuilder diag = new StringBuilder("[I18nCreate] unmatched sample: ");
        for (ModConfig config : findCreateConfigs()) {
            ForgeConfigSpec spec = (ForgeConfigSpec) config.getSpec();
            if (spec == null) continue;
            Map<String, ForgeConfigSpec.ValueSpec> specs = findValueSpecs(spec);
            for (Map.Entry<String, ForgeConfigSpec.ValueSpec> e : specs.entrySet()) {
                ForgeConfigSpec.ValueSpec vs = e.getValue();
                if (vs == null) continue;
                total++;
                String specPath = e.getKey();
                String key = specPath.toLowerCase(Locale.ROOT);
                String zh = COMMENT_TABLE.get(key);
                if (zh == null) {
                    if (diag.length() < 600) diag.append(key).append(" | ");
                    continue;
                }
                matched++;
                // matched 与 applied 分开统计
                if (setComment(vs, zh)) applied++;
            }
        }
        APPLIED = true;
        CulinaryJourney.LOGGER.info("{} applyChinese: total={} matched={} applied={}", MARKER, total, matched, applied);
        CulinaryJourney.LOGGER.info("{}", diag.toString());
    }

    /**
     * 反射写入单条注释。
     *
     * @param vs      目标配置项
     * @param comment 中文注释
     * @return 是否写入成功；失败时调用方应继续处理其余配置项，而不是中断
     */
    private static boolean setComment(ForgeConfigSpec.ValueSpec vs, String comment) {
        try {
            Field f = findCommentField(vs.getClass());
            if (f == null) return false;
            f.setAccessible(true);
            // comment 字段是 final 的，不清掉 final 修饰符就无法用反射赋值
            removeFinal(f);
            f.set(vs, comment);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * 沿类继承链向上查找名为 {@code comment}、类型为 {@code String} 的字段。
     *
     * 按"名称 + 类型"而非写死声明类来查找，是为了容忍 Forge 在不同小版本里调整
     * {@code ValueSpec} 的继承结构。
     *
     * @param type 起始类
     * @return 找到的字段，未找到返回 {@code null}
     */
    private static Field findCommentField(Class<?> type) {
        Class<?> c = type;
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getType() == String.class && "comment".equals(f.getName())) {
                    return f;
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }

    /**
     * 清除字段的 {@code final} 修饰符。
     *
     * @param f 目标字段
     */
    private static void removeFinal(Field f) {
        try {
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
        } catch (Throwable ignored) {
            // 失败不影响流程
        }
    }

    /**
     * 取出一个 Forge 配置规格下的全部配置项，键为点分路径。
     *
     * @param spec 配置规格
     * @return 路径到配置项的映射；反射失败时返回空表而非 {@code null}
     */
    private static Map<String, ForgeConfigSpec.ValueSpec> findValueSpecs(ForgeConfigSpec spec) {
        Map<String, ForgeConfigSpec.ValueSpec> result = new LinkedHashMap<>();
        try {
            Object storage = spec.getSpec();
            if (storage == null) return result;
            collectValueSpecs(storage, "", result);
        } catch (Throwable t) {
            CulinaryJourney.LOGGER.info("{} findValueSpecs failed: {}", MARKER, t.toString());
        }
        return result;
    }

    /**
     * 递归遍历配置树，收集所有叶子配置项。
     *
     * 判断规则：值是 {@code ValueSpec} 即为叶子，值是 {@code Config} 即为中间节点、继续下钻。
     * 这条规则决定了路径的拼法，也就决定了汉化表里键名该怎么写。
     *
     * @param config 当前层配置节点
     * @param prefix 父路径前缀，顶层传空串
     * @param out    结果容器
     */
    @SuppressWarnings("unchecked")
    private static void collectValueSpecs(Object config, String prefix, Map<String, ForgeConfigSpec.ValueSpec> out) {
        try {
            // valueMap 是 Forge 内部类 Config 的成员，不在公开 API 里，只能反射调用
            Method valueMap = config.getClass().getMethod("valueMap");
            Object raw = valueMap.invoke(config);
            if (!(raw instanceof Map)) return;
            Map<Object, Object> map = (Map<Object, Object>) raw;
            for (Map.Entry<Object, Object> e : map.entrySet()) {
                String key = String.valueOf(e.getKey());
                Object val = e.getValue();
                String full = prefix.isEmpty() ? key : prefix + "." + key;
                if (val instanceof ForgeConfigSpec.ValueSpec) {
                    out.put(full, (ForgeConfigSpec.ValueSpec) val);
                } else if (val instanceof Config) {
                    collectValueSpecs(val, full, out);
                }
            }
        } catch (Throwable t) {
            CulinaryJourney.LOGGER.info("{} collectValueSpecs failed: {}", MARKER, t.toString());
        }
    }

    /**
     * 找出 Create 模组已注册的全部配置对象。
     *
     * @return Create 的配置列表，未找到或反射失败时返回空列表
     */
    private static java.util.List<ModConfig> findCreateConfigs() {
        java.util.List<ModConfig> result = new java.util.ArrayList<>();
        try {
            // ConfigTracker#configSets 是包私有成员，需反射取用；它持有所有模组的全部配置
            Object tracker = ConfigTracker.INSTANCE;
            Method configSets = tracker.getClass().getMethod("configSets");
            @SuppressWarnings("unchecked")
            Map<ModConfig.Type, ?> sets = (Map<ModConfig.Type, ?>) configSets.invoke(tracker);
            if (sets != null) {
                for (Object set : sets.values()) {
                    if (set instanceof Iterable) {
                        for (Object o : (Iterable<?>) set) {
                            // 按 modId 精确匹配，避免误改其他模组的配置注释
                            if (o instanceof ModConfig mc && "create".equals(mc.getModId())) {
                                result.add(mc);
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            CulinaryJourney.LOGGER.info("{} findCreateConfigs failed: {}", MARKER, t.toString());
        }
        return result;
    }
}
