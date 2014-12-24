import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;

/**
 * Created by Marvin on 22.12.2014.
 */
public class StateMandel extends State {
    private double xOff, yOff, scale;
    private final int escapeRadius = 4;
    private int[] colors;
    private boolean hasChanged;

    public StateMandel(Controller controller) {
        super(controller);
        this.iterations = 200;
        this.xOff = 0;
        this.yOff = 0;
        this.scale = 4;
        this.hasChanged = true;
        render();
    }


    //TODO doesn't center properly
    @Override
    protected void mouseClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) return;

        xOff = Math.max(Math.min(xOff + ((e.getX() - (controller.width / 2.0f)) * scale / controller.width), 2), -2);
        yOff = Math.max(Math.min(yOff + ((e.getY() - (controller.height / 2.0f)) * scale / controller.height), 2), -2);
        hasChanged = true;
        render();
    }

    @Override
    protected void mouseWheelMoved(MouseWheelEvent e) {
        scale = scale + ((scale * Math.max(Math.min(e.getPreciseWheelRotation(), 2), -2)) / 10.0f);
        hasChanged = true;
        render();
    }

    private int[] generateColors() {
        int[] result = new int[iterations];

        for (int i = 0; i < iterations; i++) {
            double f = ((float) i / (float) iterations) * Math.PI * 2f * 3f;

            int r = (int) (Math.sin(f + 2f) * 127f + 128f);
            int g = (int) (Math.sin(f) * 127f + 128f);
            int b = (int) (Math.sin(f + 4f) * 127f + 128f);

            result[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
        }

        return result;
    }

    @Override
    protected void render() {
        if (threads.size() > 0 || !hasChanged) return;
        hasChanged = false;

        BufferedImage newImage = getNewBufferedImage();

        this.threads = new ArrayList<>();
        this.colors = generateColors();

        long time = System.nanoTime();

        int[] pixels = ((DataBufferInt) (newImage.getRaster().getDataBuffer())).getData();

        for (int i = 0; i < controller.processors; i++) {
            RenderThreadMandel t = new RenderThreadMandel(pixels, i, iterations, xOff, yOff, scale);
            threads.add(t);
            t.start();
        }

        new Thread(() -> {
            try {
                for (Thread t : threads) {
                    t.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(((System.nanoTime() - time) / 1000000f));

                this.image = newImage;
                controller.repaint();

                for (RenderThread t : threads)
                    t.setTerminate(true);
                threads.clear();

                if (hasChanged)
                    render();
            }
        }).start();
    }

    class RenderThreadMandel extends RenderThread {
        private int[] pixels;
        private int threadID;
        private int iterations;
        private double xOff;
        private double yOff;
        private double scale;

        RenderThreadMandel(int[] pixels, int threadID, int iterations, double xOff, double yOff, double scale) {
            this.pixels = pixels;
            this.threadID = threadID;
            this.iterations = iterations;
            this.xOff = xOff;
            this.yOff = yOff;
            this.scale = scale;
        }

        @Override
        public void run() {
            int w = controller.width;
            int h = controller.height;

            double cR, cI, x, y, xNew;
            int k;

            int yStart = (int) ((threadID / (float) controller.processors) * h);
            int yEnd = (int) (((threadID + 1) / (float) controller.processors) * h);

            for (int i = yStart; i < yEnd; i++) {
                if (terminate) return;
                cI = this.yOff + (i - (h / 2f)) * (this.scale / h);

                for (int j = 0; j < w; j++) {
                    cR = this.xOff + (j - (w / 2f)) * (this.scale / w);

                    x = y = k = 0;

                    while ((x * x) + (y * y) < escapeRadius && k < this.iterations) {
                        xNew = (x * x) - (y * y) + cR;
                        y = (2 * x * y) + cI;
                        x = xNew;
                        k++;
                    }

                    pixels[i * w + j] = (k < this.iterations) ? colors[k] : 0x000000;
                }
            }
        }
    }
}

