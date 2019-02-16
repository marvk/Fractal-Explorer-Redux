import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Marvin on 23.12.2014.
 */
public class StateBuddha extends State {
    private int numPoints;
    private int[] numHits;
    private int minIterations;
    private int exposure;
    private int[] pixels;
    private PercentageProgressBar progressBar;
    private AtomicLong counter;
    private boolean anti;

    public StateBuddha(Controller controller, boolean anti) {
        super(controller);
        iterations = 100;
        numPoints = 100000000;
        minIterations = 3;
        renderScale = 1;
        exposure = 1;
        this.anti = anti;

        progressBar = new PercentageProgressBar();
        progressBar.setEnabled(true);
        progressBar.setStringPainted(true);
        counter = new AtomicLong();

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
        GridBagConstraints gc = new GridBagConstraints();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(300, controller.height));

        JPanel spinnerPanel = new JPanel(new GridLayout(0, 2));
        spinnerPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Buddha Settings"));

        JSpinner iterationSpinner = new JSpinner(new SpinnerNumberModel(iterations, 1, 6553600, 50));
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

        JSpinner exposureSpinner = new JSpinner(new SpinnerNumberModel(exposure, 1, 100000, 1));
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
            draw(pixels);
            controller.repaint();
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

        gc.gridy = 0;
        panel.add(spinnerPanel, gc);
        gc.gridy = 1;
        panel.add(progressBar, gc);
        panel.revalidate();
        return panel;
    }

    @Override
    protected void render() {
        progressBar.start();
        progressBar.setLongMax(numPoints);
        counter.set(0);
        image = getNewBufferedImage();
        numHits = new int[controller.width * renderScale * controller.height * renderScale];

        this.threads = new ArrayList<>();

        long time = System.nanoTime();
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        Timer timer = new Timer(1000 / 10, e -> {
            draw(pixels);
            controller.repaint();
            progressBar.setValue(counter.get());
        });
        timer.setInitialDelay(0);
        timer.start();

        startThreads();

        new Thread(() -> {
            try {
                for (Thread t : threads)
                    t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                timer.stop();

                System.out.println("Rendered in " + ((System.nanoTime() - time) / 1000000f) + "ms");

                draw(pixels);
                controller.repaint();

                for (RenderThread t : threads)
                    t.setTerminate(true);
                threads.clear();

                progressBar.setValue((long) numPoints);
            }
        }).start();
    }

    private void startThreads() {
        for (int i = 0; i < controller.processors; i++) {
            RenderThreadBuddha t = new RenderThreadBuddha(iterations, minIterations, numPoints / controller.processors, anti);
            threads.add(t);
            t.start();
        }
    }

    protected void draw(int[] pixels) {
        int maxCount = 0;

        for (int i : numHits) maxCount = Math.max(i, maxCount);
        Random r = new Random();
        for (int i = 0; i < pixels.length; i++) {
            int c = Math.min((int) (((float) numHits[i] / ((float) maxCount / ((float) exposure))) * 255f), 255);
            pixels[i] = c << 16 | c << 8 | c;
        }
    }

    class RenderThreadBuddha extends RenderThread {
        private int iterations;
        private int numPoints;
        private int minIterations;
        private int w, h;
        private boolean anti;

        public RenderThreadBuddha(int iterations, int minIterations, int numPoints, boolean anti) {
            this.iterations = iterations;
            this.minIterations = minIterations;
            this.numPoints = numPoints;
            this.w = controller.width * renderScale;
            this.h = controller.height * renderScale;
            this.anti = anti;
        }

        @Override
        public void run() {
            Random random = new Random(0);
            double cR, cI;

            for (int i = 0; i < numPoints; ) {
                if (terminate) return;
                cR = (random.nextFloat() * 3.0f) - 2.0f;
                cI = (random.nextFloat() * 3.0f) - 1.5f;

                if (!anti) {
                    //Check if point is within the cardiodic
                    double q = ((cR - (1f / 4f)) * (cR - (1f / 4f))) + (cI * cI);
                    if (((q * (q + (cR - (1f / 4f)))) < (1f / 4f) * (cI * cI)))
                        continue;

                    //Check if point is within the period-2 bulb
                    if (((cR + 1) * (cR + 1) + (cI * cI) < (1f / 16f)))
                        continue;
                }

                if (iterate(cR, cI, false)) {
                    iterate(cR, cI, true);
                    counter.incrementAndGet();
                    i++;
                }
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
                    pX = (int) (w * (x + 2f) / 4f);
                    pY = (int) (h * (y + 2f) / 4f);

                    if (pX >= 0 && pX < w && pY >= 0 && pY < h) {
                        numHits[pX * h + pY]++;
                    }
                }

                if (xNew * xNew + yNew * yNew > 4) return !anti;

                x = xNew;
                y = yNew;
            }

            return anti;
        }
    }
}
