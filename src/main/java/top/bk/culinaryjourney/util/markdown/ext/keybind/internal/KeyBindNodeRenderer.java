package top.bk.culinaryjourney.util.markdown.ext.keybind.internal;

import top.bk.culinaryjourney.util.markdown.ext.keybind.KeyBind;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;

import java.util.Set;

abstract class KeyBindNodeRenderer implements NodeRenderer {

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(KeyBind.class);
    }
}
