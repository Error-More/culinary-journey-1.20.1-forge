package top.bk.culinaryjourney.util.markdown.ext.keybind;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;

public class KeyBind extends CustomNode implements Delimited {

    private static final String DELIMITER = "::";

    private final String key;

    public KeyBind(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getOpeningDelimiter() {
        return DELIMITER;
    }

    @Override
    public String getClosingDelimiter() {
        return DELIMITER;
    }
}
