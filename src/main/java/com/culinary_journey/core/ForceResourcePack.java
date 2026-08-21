package com.culinary_journey.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// 强制加载
public final class ForceResourcePack {

    public static final String TCJ_FILE_NAME = "TCJ补充汉化资源包.zip";
    public static final String TCJ_PACK_ID = "file/" + TCJ_FILE_NAME;

    public static final String LANG_FILE_NAME = "Minecraft-Mod-Language-Modpack-Converted-1.20.1.zip";
    public static final String LANG_PACK_ID = "file/" + LANG_FILE_NAME;

    private ForceResourcePack() {
    }

    public static List<String> enforce(Collection<String> ids) {
        List<String> order = new ArrayList<>(ids);

        if (!order.contains(LANG_PACK_ID)) {
            order.add(LANG_PACK_ID);
        }
        order.remove(TCJ_PACK_ID);
        if (!order.contains(TCJ_PACK_ID)) {
            int idx = order.indexOf(LANG_PACK_ID);
            order.add(idx + 1, TCJ_PACK_ID);
        }
        return order;
    }
}
