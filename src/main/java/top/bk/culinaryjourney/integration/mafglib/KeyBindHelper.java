package top.bk.culinaryjourney.integration.mafglib;

import com.mojang.blaze3d.platform.InputConstants;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.KeybindCategory;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.settings.KeyModifier;

import java.util.ArrayList;
import java.util.List;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * Helper class : 获取 masa's MOD 的 KeyBind.
 */
public class KeyBindHelper {

    public static IKeybindManager getManager() {
        return InputEventHandler.getKeybindManager();
    }

    public static List<KeybindCategory> getCategories() {
        return getManager().getKeybindCategories();
    }

    public static List<String> getCategoryNames() {
        return getManager().getKeybindCategories().stream()
                .map(KeybindCategory::getCategory)
                .toList();
    }

    public static List<IHotkey> getHotkeys() {
        return getCategories().stream()
                .<IHotkey>flatMap(category -> category.getHotkeys().stream())
                .toList();
    }

    public static IHotkey getHotkey(String name) {
        return getHotkeys().stream()
                .filter(hotkey -> hotkey.getName().equals(name))
                .findFirst().orElse(null);
    }

    public static String getTranslatedKey(IHotkey hotkey) {
        if (hotkey == null) return InputConstants.UNKNOWN.getDisplayName().getString();
        IKeybind keybind = hotkey.getKeybind();
        if (keybind == null) return InputConstants.UNKNOWN.getDisplayName().getString();
        List<Integer> keys = keybind.getKeys();
        if (keys.isEmpty()) return InputConstants.UNKNOWN.getDisplayName().getString();

        StringBuilder builder = new StringBuilder();

        int size = keys.size();
        for (int i = 0; i < size; i++) {
            Integer code = keys.get(i);
            if (code == -1) return InputConstants.UNKNOWN.getDisplayName().getString();
            if (code < -1) {
                builder.append(InputConstants.Type.MOUSE.getOrCreate(code + 100)
                        .getDisplayName().getString());
                if (i < size - 1) builder.append(" + ");
                continue;
            }
            InputConstants.Key key = InputConstants.getKey(code, 0);
            if (size != 1 && KeyModifier.isKeyCodeModifier(key)) {
                KeyModifier modifier = KeyModifier.getModifier(key);
                if (modifier != null) {
                    Component displayName = modifier.getCombinedName(key, () -> Component.literal(""));
                    builder.append(displayName.getString().replace("+", "").trim());
                    if (i < size - 1) builder.append(" + ");
                    continue;
                }
            }
            builder.append(key.getDisplayName().getString());
        }

        return builder.toString();
    }

    public static List<String> getTranslatedKeyList(IHotkey hotkey) {
        List<String> unknown = List.of(InputConstants.UNKNOWN.getDisplayName().getString());
        if (hotkey == null) return unknown;
        IKeybind keybind = hotkey.getKeybind();
        if (keybind == null) return unknown;
        List<Integer> keys = keybind.getKeys();
        if (keys.isEmpty()) return unknown;

        List<String> out = new ArrayList<>();

        int size = keys.size();
        for (int code : keys) {
            if (code == -1) return unknown;
            if (code < -1) {
                out.add(InputConstants.Type.MOUSE.getOrCreate(code + 100)
                        .getDisplayName().getString());
                continue;
            }
            InputConstants.Key key = InputConstants.getKey(code, 0);
            if (size != 1 && KeyModifier.isKeyCodeModifier(key)) {
                KeyModifier modifier = KeyModifier.getModifier(key);
                if (modifier != null) {
                    Component displayName = modifier.getCombinedName(key, Component::empty);
                    out.add(displayName.getString().replace("+", "").trim());
                    continue;
                }
            }
            out.add(key.getDisplayName().getString());
        }

        return out;
    }

}