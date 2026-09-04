package top.bk.culinary_journey.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import top.bk.culinary_journey.Culinary_journey;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// 窗口标题逻辑
@Mod.EventBusSubscriber(modid = Culinary_journey.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class WindowTitle {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MARKER = "[WindowTitle]";
    private static final String PREFIX = "美食的旅途 - ";
    private static final String FALLBACK = "美食的旅途";

    private static final Path VERSION_PATH =
            FMLPaths.CONFIGDIR.get().resolve("fancymenu/assets/version.txt");

    private static volatile String baseTitle = FALLBACK;

    private static volatile int titleEpoch = 0;

    private static volatile State lastState = State.MAIN_MENU;

    private static int pollCounter = 0;
    private static final int POLL_INTERVAL = 20;

    private static volatile ServerNameGetter serverNameGetter = ServerNameGetter.FALLBACK;

    private record State(boolean inGame, boolean isMultiplayer, String worldOrServer) {
        static final State MAIN_MENU = new State(false, false, "");
    }

    @FunctionalInterface
    private interface ServerNameGetter {
        ServerNameGetter FALLBACK = server -> {
            try {
                Field ip = ServerData.class.getField("ip");
                Object v = ip.get(server);
                return v instanceof String s ? s : "";
            } catch (Exception ignored) {
                return "";
            }
        };

        String apply(ServerData server);
    }

    public static String computeCurrentTitle() {
        return buildFullTitle(snapshotState());
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            refreshBaseTitle();
            serverNameGetter = resolveServerNameGetter();
            trigger();
        });
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener((ResourceManagerReloadListener) (manager) -> {
            refreshBaseTitle();
            trigger();
        });
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (Minecraft.getInstance().getWindow() == null) return;
        State now = snapshotState();
        if (!now.equals(lastState)) {
            lastState = now;
            trigger();
            return;
        }
        if ((pollCounter++ % POLL_INTERVAL) == 0) {
            refreshBaseTitle();
        }
    }

    private static void refreshBaseTitle() {
        String t = readBaseTitle();
        if (!t.equals(baseTitle)) {
            baseTitle = t;
        }
    }

    private static void trigger() {
        var window = Minecraft.getInstance().getWindow();
        if (window != null) {
            window.setTitle("");
        }
    }

    private static State snapshotState() {
        Minecraft mc = Minecraft.getInstance();
        ServerData server = mc.getCurrentServer();
        if (server != null) {
            return new State(true, true, serverNameGetter.apply(server));
        }
        IntegratedServer integrated = mc.getSingleplayerServer();
        if (integrated != null) {
            String name = integrated.getWorldData().getLevelName();
            return new State(true, false, name == null ? "" : name);
        }
        return State.MAIN_MENU;
    }

    private static String buildFullTitle(State state) {
        if (!state.inGame) {
            return baseTitle;
        }
        String suffix = state.isMultiplayer
                ? " | 众友盛宴: " + (state.worldOrServer == null ? "" : state.worldOrServer)
                : " | 孤独旅途: " + (state.worldOrServer == null ? "" : state.worldOrServer);
        return baseTitle + suffix;
    }

    @SuppressWarnings("null")
    private static ServerNameGetter resolveServerNameGetter() {
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
                }
            }
        }
        return ServerNameGetter.FALLBACK;
    }

    private static String readBaseTitle() {
        String title;
        if (!Files.exists(VERSION_PATH)) {
            title = FALLBACK;
        } else {
            try {
                List<String> lines = Files.readAllLines(VERSION_PATH, StandardCharsets.UTF_8);
                String version = lines.stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .findFirst()
                        .orElse("");
                title = version.isEmpty() ? FALLBACK : PREFIX + version;
            } catch (IOException e) {
                LOGGER.info("{} 读取版本文件失败：{}", MARKER, e.toString());
                title = FALLBACK;
            }
        }
        if (!title.equals(baseTitle)) {
            titleEpoch++;
        }
        return title;
    }

    public static int getTitleEpoch() {
        return titleEpoch;
    }
}
