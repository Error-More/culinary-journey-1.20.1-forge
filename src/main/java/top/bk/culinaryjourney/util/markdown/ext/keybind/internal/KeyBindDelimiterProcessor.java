package top.bk.culinaryjourney.util.markdown.ext.keybind.internal;

import top.bk.culinaryjourney.util.markdown.ext.keybind.KeyBind;
import org.commonmark.node.Node;
import org.commonmark.node.SourceSpans;
import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

public class KeyBindDelimiterProcessor implements DelimiterProcessor {

    @Override
    public char getOpeningCharacter() {
        return ':';
    }

    @Override
    public char getClosingCharacter() {
        return ':';
    }

    @Override
    public int getMinLength() {
        return 2;
    }

    @Override
    public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
        if (openingRun.length() < 2 || closingRun.length() < 2) {
            return 0;
        }

        Text opener = openingRun.getOpener();
        Text closer = closingRun.getCloser();

        StringBuilder builder = new StringBuilder();
        SourceSpans sourceSpans = new SourceSpans();
        sourceSpans.addAllFrom(openingRun.getOpeners(2));

        Node current = opener.getNext();
        while (current != null && current != closer) {
            Node next = current.getNext();
            if (current instanceof Text text) {
                builder.append(text.getLiteral());
            }
            sourceSpans.addAll(current.getSourceSpans());
            current.unlink();
            current = next;
        }

        sourceSpans.addAllFrom(closingRun.getClosers(2));

        KeyBind keyBind = new KeyBind(builder.toString());
        keyBind.setSourceSpans(sourceSpans.getSourceSpans());
        opener.insertAfter(keyBind);

        return 2;
    }
}
