import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Marvin on 23.12.2014.
 */
public class StateBuddha extends State {
    private int numPoints;
    private int[] numHits;
    private int colorCap;
    private int minIterations;

    public StateBuddha(Controller controller) {
        super(controller);
        iterations = 10000;
        numPoints = 100000000;
        colorCap = 10;
        minIterations = 3;
        render();
    }

    @Override
    protected void mouseClicked(MouseEvent e) {
        //Auto generated method stub
    }

    @Override
    protected void mouseWheelMoved(MouseWheelEvent e) {
        //Auto generated method stub
    }

    @Override
    protected void render() {
        if (threads.size() > 0) return;

        numHits = new int[controller.width * controller.height];

        BufferedImage newImage = getNewBufferedImage();

        this.threads = new ArrayList<>();

        long time = System.nanoTime();
        int[] pixels = ((DataBufferInt) newImage.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < controller.processors; i++) {
            RenderThreadBuddha t = new RenderThreadBuddha(iterations, numPoints / controller.processors);
            threads.add(t);
            t.start();
        }

        Timer timer = new Timer(1000, e -> {
            draw(pixels);
            this.image = newImage;
            controller.repaint();
        });
        timer.start();

        new Thread(() -> {
            try {
                for (Thread t : threads)
                    t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                timer.stop();
                System.out.println(((System.nanoTime() - time) / 1000000f));

                draw(pixels);
                this.image = newImage;
                controller.repaint();

                for (RenderThread t : threads)
                    t.setTerminate(true);
                threads.clear();
            }
        }).start();
    }

    protected void draw(int[] pixels) {
        int maxCount = -1;

        for (int i : numHits) {
            maxCount = Math.max(i, maxCount);
        }

        for (int i = 0; i < controller.width * controller.height; i++) {
            float cRaw = ((float) numHits[i] / ((float) maxCount / ((float) colorCap / 10.0f)));
            int c = (int) Math.min(cRaw * 255f, 255);
            pixels[i] = c << 16 | c << 8 | c;
        }
    }

    class RenderThreadBuddha extends RenderThread {
        private int iterations;
        private int numPoints;

        RenderThreadBuddha(int iterations, int numPoints) {
            this.iterations = iterations;
            this.numPoints = numPoints;
        }

        @Override
        public void run() {
            Random random = new Random(0);
            double cR, cI;

            for (int i = 0; i < numPoints; i++) {
                cR = (random.nextFloat() * 3.0f) - 2.0f;
                cI = (random.nextFloat() * 3.0f) - 1.5f;

//                //Check if point is within the cardiodic
//                double q = ((cR - (1f / 4f)) * (cR - (1f / 4f))) + (cI * cI);
//                if (!((q * (q + (cR - (1f/4f))))<(1f/4f)*(cI*cI)))
//                    continue;
//
//                //Check if point is within the period-2 bulb
//                if (!((cR+1)*(cR+1)+(cI*cI) < (1f/16f)))
//                    continue;

                if (iterate(cR, cI, false))
                    iterate(cR, cI, true);
            }
        }

        private boolean iterate(double cR, double cI, boolean draw) {
            double x = 0;
            double y = 0;

            double xNew, yNew;

            int pX, pY;

            for (int i = 0; i < iterations; i++) {
                xNew = (x * x) - (y * y) + cR;
                yNew = (2 * x * y) + cI;

                if (draw && i > minIterations) {
                    pX = (int) (controller.width * (x + 2f) / 4f);
                    pY = (int) (controller.height * (y + 2f) / 4f);

                    if (pX >= 0 && pX < controller.width && pY >= 0 && pY < controller.height) {
                        numHits[pX * controller.height + pY]++;

                    }
                }


                if (xNew * xNew + yNew * yNew > 4) return true;

                x = xNew;
                y = yNew;
            }


            return false;
        }
    }
}
