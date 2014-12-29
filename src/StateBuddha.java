import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
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
    private int exposure;
    private int[] pixels;

    public StateBuddha(Controller controller) {
        super(controller);
        iterations = 100;
        numPoints = 100000000;
        colorCap = 10;
        minIterations = 3;
        exposure = 10;
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
    public JPanel getControlPanel() {
        JPanel panel = new JPanel();

        JPanel spinnerPanel = new JPanel(new GridLayout(0, 2));
        spinnerPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Values"));

        JSpinner iterationSpinner = new JSpinner(new SpinnerNumberModel(iterations, 50, 6553600, 50));
        iterationSpinner.addChangeListener(e -> {
            iterations = (int) iterationSpinner.getValue();
        });

        JSpinner minItSpinner = new JSpinner(new SpinnerNumberModel(minIterations, 0, 6553600, 1));
        minItSpinner.addChangeListener(e -> {
            minIterations = (int) minItSpinner.getValue();
        });

        JSpinner renderScaleSpinner = new JSpinner(new SpinnerNumberModel(renderScale, 1, 8, 1));
        renderScaleSpinner.addChangeListener(e -> {
            renderScale = (int) renderScaleSpinner.getValue();
        });

        JSpinner numPointsSpinner = new JSpinner(new SpinnerNumberModel(numPoints, 1, Integer.MAX_VALUE, 50000));
        numPointsSpinner.addChangeListener(e -> {
            numPoints = (int) numPointsSpinner.getValue();
        });

        JSpinner exposureSpinner = new JSpinner(new SpinnerNumberModel(exposure, 1, 1000, 1));
        exposureSpinner.addChangeListener(e -> {
            exposure = (int) exposureSpinner.getValue();
        });

        JButton rerenderButton = new JButton("Rerender");
        rerenderButton.addActionListener(e -> {
            terminate();
            render();
        });

        JButton redrawButton = new JButton("Redraw");
        redrawButton.addActionListener(e -> {

        });

        spinnerPanel.add(new JLabel("Iterations"));
        spinnerPanel.add(iterationSpinner);
        spinnerPanel.add(new JLabel("Minimum Iterations"));
        spinnerPanel.add(minItSpinner);
        spinnerPanel.add(new JLabel("Render Scale"));
        spinnerPanel.add(renderScaleSpinner);
        spinnerPanel.add(new JLabel("Points"));
        spinnerPanel.add(numPointsSpinner);
        spinnerPanel.add(new JLabel("Exposure"));
        spinnerPanel.add(exposureSpinner);
        spinnerPanel.add(rerenderButton);
        spinnerPanel.add(redrawButton);

        panel.add(spinnerPanel);
        panel.revalidate();
        return panel;
    }

    @Override
    protected void render() {
        numHits = new int[controller.width * controller.height];

        this.threads = new ArrayList<>();

        long time = System.nanoTime();
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        Timer timer = new Timer(1000, e -> {
            draw(pixels);
            controller.repaint();
        });
        timer.setInitialDelay(0);
        timer.start();

        for (int i = 0; i < controller.processors; i++) {
            RenderThreadBuddha t = new RenderThreadBuddha(iterations, minIterations, numPoints / controller.processors);
            threads.add(t);
            t.start();
        }

        new Thread(() -> {
            try {
                for (Thread t : threads)
                    t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Done");
                timer.stop();
                System.out.println(((System.nanoTime() - time) / 1000000f));

                draw(pixels);
                controller.repaint();

                for (RenderThread t : threads)
                    t.setTerminate(true);
                threads.clear();
            }
        }).start();
    }

    protected void draw(int[] pixels) {
        System.out.println("ayy" + exposure);
        int maxCount = 0;

        for (int i : numHits) maxCount = Math.max(i, maxCount);

        for (int i = 0; i < controller.width * controller.height; i++) {
            int c = (int)(((float) numHits[i] / ((float) maxCount / exposure))*255f);
            pixels[i] = c << 16 | c << 8 | c;
        }
    }

    class RenderThreadBuddha extends RenderThread {
        private int iterations;
        private int numPoints;
        private int minIterations;

        public RenderThreadBuddha(int iterations, int minIterations, int numPoints) {
            this.iterations = iterations;
            this.minIterations = minIterations;
            this.numPoints = numPoints;
        }

        @Override
        public void run() {
            Random random = new Random(0);
            double cR, cI;

            for (int i = 0; i < numPoints; i++) {
                if (terminate) return;
                cR = (random.nextFloat() * 3.0f) - 2.0f;
                cI = (random.nextFloat() * 3.0f) - 1.5f;

                //Check if point is within the cardiodic
                double q = ((cR - (1f / 4f)) * (cR - (1f / 4f))) + (cI * cI);
                if (((q * (q + (cR - (1f/4f))))<(1f/4f)*(cI*cI)))
                    continue;

                //Check if point is within the period-2 bulb
                if (((cR+1)*(cR+1)+(cI*cI) < (1f/16f)))
                    continue;

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
