package top.bk.culinaryjourney.util.markdown.ext.keybind.internal;

import org.commonmark.node.Node;
import org.commonmark.renderer.text.TextContentNodeRendererContext;

public class KeyBindTextContentNodeRenderer extends KeyBindNodeRenderer {

    private final TextContentNodeRendererContext context;

    public KeyBindTextContentNodeRenderer(TextContentNodeRendererContext context) {
        this.context = context;
    }

    @Override
    public void render(Node node) {
        renderChildren(node);
    }

    private void renderChildren(Node parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();
            context.render(node);
            node = next;
        }
    }
}
