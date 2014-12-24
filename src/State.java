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

    protected static GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

    public State(Controller controller) {
        this.controller = controller;
        this.iterations = 0;
        this.threads = new ArrayList<>();
        this.image = getNewBufferedImage();
    }

    public void redraw() {
        this.image = getNewBufferedImage();
        render();
    }

    protected abstract void mouseClicked(MouseEvent e);

    protected abstract void mouseWheelMoved(MouseWheelEvent e);

    protected abstract void render();

    protected BufferedImage getNewBufferedImage() {
        return gc.createCompatibleImage(controller.width, controller.height);
    }

    protected BufferedImage getImage() {
        return image;
    }

    abstract class RenderThread extends Thread {
        protected boolean terminate = false;

        public void setTerminate(boolean terminate) {
            this.terminate = terminate;
        }
    }
}
