package top.bk.culinaryjourney.client.ui.markdown;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.ModalPanel;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import top.bk.culinaryjourney.client.ui.markdown.inline.InlineContext.*;

/**
 * FTB Quests Markdown: 为 FTB Quests 的任务描述提供 Markdown 渲染.
 * <p>
 * 我懒 TAT
 * TODO : 注释
 */
public class MarkdownImageOverlay extends ModalPanel {

    private static final float STEP = 0.05F;

    private final ImageContext image;
    private final MarkdownRenderer renderer;

    private final Widget imageWidget;
    private float scale = 1.0F;

    public MarkdownImageOverlay(Panel panel, ImageContext image, MarkdownRenderer renderer) {
        super(panel);
        this.image = image;
        this.renderer = renderer;

        imageWidget = new ImageWidget();

        int guiWidth = this.getScreen().getGuiScaledWidth();
        int guiHeight = this.getScreen().getGuiScaledHeight();
        int w = guiWidth - (guiWidth / 6);
        int h = guiHeight - (guiHeight / 6);

        setPosAndSize((guiWidth - w) / 2, (guiHeight - h) / 2, w, h);
    }

    private float getImageScale() {
        return Math.min(Math.min((float) getWidth() / image.getWidth(),
                (float) getHeight() / image.getHeight()), 1.0F);
    }
    private int getImageWidth() {
        return Math.round(image.getWidth() * getImageScale() * scale);
    }
    private int getImageHeight() {
        return Math.round(image.getHeight() * getImageScale() * scale);
    }

    @Override
    public void addWidgets() {
        this.add(imageWidget);
    }

    @Override
    public void alignWidgets() {
        int w = getImageWidth();
        int h = getImageHeight();

        imageWidget.setPosAndSize((getWidth() - w) / 2, (getHeight() - h) / 2, w, h);
    }

    @Override
    public boolean scrollPanel(double scroll) {
        if (scale > 10.0F || scale < 0.1F) {
            scale = Mth.clamp(scale, 0.1F, 10.0F);
            return false;
        }
        float lastScale = scale;
        scale = Mth.clamp(scale + (float) (STEP * scroll), 0.1F, 10.0F);
        alignWidgets();
        setScrollX(getScrollX() * lastScale / scale);
        setScrollY(getScrollY() * lastScale / scale);
        return true;
    }

    @Override
    public boolean mouseDragged(int button, double dragX, double dragY) {
        if (MouseButton.get(button).isLeft()) {
            setOffset(true);

            double maxX = getWidth() - (imageWidget.getPosX() + Math.min(imageWidget.getWidth(), getWidth()));
            double maxY = getHeight() - (imageWidget.getPosY() + Math.min(imageWidget.getHeight(), getHeight()));
            setScrollX(Mth.clamp(-dragX + getScrollX(), -maxX, maxX));
            setScrollY(Mth.clamp(-dragY + getScrollY(), -maxY, maxY));

            setOffset(false);
            return true;
        }

        return super.mouseDragged(button, dragX, dragY);
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (button.isLeft()
                && ((getMouseX() < getPosX() || getMouseX() > getPosX() + getWidth())
                || (getMouseY() < getPosY() || getMouseY() > getPosY() + getHeight()))) {
            this.getGui().popModalPanel();
            return true;
        }

        return super.mousePressed(button);
    }

    @Override
    public boolean keyPressed(Key key) {
        if (key.esc()) {
            this.getGui().popModalPanel();
            return true;
        } else {
            return super.keyPressed(key);
        }
    }

    private class ImageWidget extends Widget {
        public ImageWidget() {
            super(MarkdownImageOverlay.this);
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            Icon icon = renderer.getIcon(image);
            if (icon != null && !icon.isEmpty()) icon.draw(graphics, x, y, w, h);
        }
    }
}
