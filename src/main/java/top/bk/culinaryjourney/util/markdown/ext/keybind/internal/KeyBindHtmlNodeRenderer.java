package top.bk.culinaryjourney.util.markdown.ext.keybind.internal;

import org.commonmark.node.Node;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;

import java.util.Map;

public class KeyBindHtmlNodeRenderer extends KeyBindNodeRenderer {

    private final HtmlNodeRendererContext context;
    private final HtmlWriter html;

    public KeyBindHtmlNodeRenderer(HtmlNodeRendererContext context) {
        this.context = context;
        this.html = context.getWriter();
    }

    @Override
    public void render(Node node) {
        Map<String, String> attributes = context.extendAttributes(node, "kbd", Map.of());
        html.tag("kbd", attributes);
        renderChildren(node);
        html.tag("/kbd");
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
