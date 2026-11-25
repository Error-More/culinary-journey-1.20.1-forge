package top.bk.culinaryjourney.client.ui.markdown;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineContext.*;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineLayout.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 我懒 TAT
 * TODO : 注释
 */
public class MarkdownLink implements LinkHandler {

    private static final Pattern STRING_TEXT_PATTERN = Pattern.compile("\"[\\s\\S]*\"");

    @Override
    public boolean onClick(LinkContext link) {
        String url = link.getUrl();
        URI uri;

        Matcher matcher = STRING_TEXT_PATTERN.matcher(url);
        if (matcher.find()) {
            Minecraft.getInstance().keyboardHandler.setClipboard(matcher.group().replace("\"", ""));
            return true;
        }

        try {
            uri = new URI(url);

            String scheme = uri.getScheme();
            if (scheme == null)
                throw new URISyntaxException(url, "Missing protocol");
            if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))
                throw new URISyntaxException(url, "Unsupported protocol: " + scheme.toLowerCase(Locale.ROOT));
        } catch (URISyntaxException e) {
            File file = new File(url.replace("%20", " "));
            if (file.exists()) {
                openLink(url.replace("%20", " "), file.toURI());
                return true;
            } else {
                Optional<Long> questId = QuestObjectBase.parseHexId(url);
                if (questId.isPresent()) {
                    ClientQuestFile clientQuestFile = ClientQuestFile.INSTANCE;
                    if (clientQuestFile == null) return false;

                    QuestObject quest = clientQuestFile.getQuest(questId.get());
                    if (quest != null) {
                        clientQuestFile.getQuestScreen()
                                .ifPresent(screen -> screen.open(quest, false));

                        return true;
                    }
                }

                return false;
            }
        }

        openLink(url, uri);

        return false;
    }

    @Override
    public Component onHover(LinkContext link) {
        String title = link.getTitle();
        if (title.isEmpty()) return null;
        return Component.literal(title);
    }

    private void openLink(String string, URI uri) {
        Screen screen = Minecraft.getInstance().screen;
        Minecraft.getInstance().setScreen(new ConfirmLinkScreen(accepted -> {
            if (accepted) Util.getPlatform().openUri(uri);
            Minecraft.getInstance().setScreen(screen);
        }, string, false));
    }

}
