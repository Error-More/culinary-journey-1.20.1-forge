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

// 机械动力配置页面注释汉化
@Mod.EventBusSubscriber(modid = CulinaryJourney.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ConfigPage {

    private static final String MARKER = "[I18nCreate]";
    private static volatile boolean APPLIED = false;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        loadTable();
        event.enqueueWork(ConfigPage::applyChinese);
    }

    private static final Map<String, String> COMMENT_TABLE = new LinkedHashMap<>();

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

    private static void applyChinese() {
        if (APPLIED) {
            return;
        }
        int applied = 0;
        int matched = 0;
        int total = 0;
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
                if (setComment(vs, zh)) applied++;
            }
        }
        APPLIED = true;
        CulinaryJourney.LOGGER.info("{} applyChinese: total={} matched={} applied={}", MARKER, total, matched, applied);
        CulinaryJourney.LOGGER.info("{}", diag.toString());
    }

    private static boolean setComment(ForgeConfigSpec.ValueSpec vs, String comment) {
        try {
            Field f = findCommentField(vs.getClass());
            if (f == null) return false;
            f.setAccessible(true);
            removeFinal(f);
            f.set(vs, comment);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

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

    private static void removeFinal(Field f) {
        try {
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
        } catch (Throwable ignored) {
        }
    }

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

    @SuppressWarnings("unchecked")
    private static void collectValueSpecs(Object config, String prefix, Map<String, ForgeConfigSpec.ValueSpec> out) {
        try {
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

    private static java.util.List<ModConfig> findCreateConfigs() {
        java.util.List<ModConfig> result = new java.util.ArrayList<>();
        try {
            Object tracker = ConfigTracker.INSTANCE;
            Method configSets = tracker.getClass().getMethod("configSets");
            @SuppressWarnings("unchecked")
            Map<ModConfig.Type, ?> sets = (Map<ModConfig.Type, ?>) configSets.invoke(tracker);
            if (sets != null) {
                for (Object set : sets.values()) {
                    if (set instanceof Iterable) {
                        for (Object o : (Iterable<?>) set) {
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
