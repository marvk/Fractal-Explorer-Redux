import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Created by Marvin on 22.12.2014.
 */
public abstract class State {
    protected BufferedImage image;
    protected Controller controller;
    protected int iterations;
    protected ArrayList<RenderThread> threads;
    protected int renderScale;

    protected static GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

    public State(Controller controller) {
        this.controller = controller;
        this.iterations = 0;
        this.renderScale = 1;
        this.threads = new ArrayList<>();
        this.image = getNewBufferedImage();
    }

    public void redraw() {
        this.image = getNewBufferedImage();
        render();
    }

    public void terminate() {
        for (RenderThread t : threads)
            t.setTerminate(true);
    }

    public JPanel getControlPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(400, 800));
        panel.setBackground(Color.PINK);
        return panel;
    }

    protected abstract void mouseClicked(MouseEvent e);

    protected abstract void mouseWheelMoved(MouseWheelEvent e);

    protected abstract void render();

    protected BufferedImage getNewBufferedImage() {
        return gc.createCompatibleImage(controller.width*renderScale, controller.height*renderScale);
    }

    protected BufferedImage resizeImage(BufferedImage image) {
        BufferedImage resized = gc.createCompatibleImage(controller.width, controller.height);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, controller.width, controller.height, null);
        g.dispose();
        return resized;
    }

    protected BufferedImage getImage() {
        return image;
    }

    protected BufferedImage getResizedImage() {
        if (renderScale != 1)
            return resizeImage(image);
        return image;
    }

    abstract class RenderThread extends Thread {
        protected boolean terminate = false;

        public void setTerminate(boolean terminate) {
            this.terminate = terminate;
        }
    }
}
