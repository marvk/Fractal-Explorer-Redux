import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
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
    private int colorRepetitions;
    private int[] colors;
    private boolean hasChanged;
    private int red, green, blue;

    public StateMandel(Controller controller) {
        super(controller);
        red = this.green = this.blue = 255;
        iterations = 200;
        colorRepetitions = 1;
        xOff = 0;
        yOff = 0;
        scale = 4;
        hasChanged = true;
        render();
    }

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

    @Override
    public JPanel getControlPanel() {
        GridBagConstraints gc = new GridBagConstraints();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(300, controller.height));

        JPanel spinnerPanel = new JPanel(new GridLayout(0, 2));
        spinnerPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Mandel Settings"));

        JSpinner iterationSpinner = new JSpinner(new SpinnerNumberModel(iterations, 50, 6553600, 50));
        iterationSpinner.addChangeListener(e -> {
            iterations = (int) iterationSpinner.getValue();
            hasChanged = true;
            render();
        });

        JSpinner renderScaleSpinner = new JSpinner(new SpinnerNumberModel(renderScale, 1, 8, 1));
        renderScaleSpinner.addChangeListener(e -> {
            renderScale = (int) renderScaleSpinner.getValue();
            hasChanged = true;
            render();
        });

        JSpinner colorRepSpinner = new JSpinner(new SpinnerNumberModel(colorRepetitions, 1, 10, 1));
        colorRepSpinner.addChangeListener(e -> {
            colorRepetitions = (int) colorRepSpinner.getValue();
            hasChanged = true;
            render();
        });

        JSpinner redSpinner = new JSpinner(new SpinnerNumberModel(red, 0, 255, 1));
        redSpinner.addChangeListener(e -> {
            red = (int) redSpinner.getValue();
            hasChanged = true;
            render();
        });

        JSpinner greenSpinner = new JSpinner(new SpinnerNumberModel(green, 0, 255, 1));
        greenSpinner.addChangeListener(e -> {
            green = (int) greenSpinner.getValue();
            hasChanged = true;
            render();
        });

        JSpinner blueSpinner = new JSpinner(new SpinnerNumberModel(blue, 0, 255, 1));
        blueSpinner.addChangeListener(e -> {
            blue = (int) blueSpinner.getValue();
            hasChanged = true;
            render();
        });

        spinnerPanel.add(new JLabel("Iterations"));
        spinnerPanel.add(iterationSpinner);
        spinnerPanel.add(new JLabel("Render Scale"));
        spinnerPanel.add(renderScaleSpinner);
        spinnerPanel.add(new JLabel("Color repititions"));
        spinnerPanel.add(colorRepSpinner);
        spinnerPanel.add(new JLabel("Red"));
        spinnerPanel.add(redSpinner);
        spinnerPanel.add(new JLabel("Green"));
        spinnerPanel.add(greenSpinner);
        spinnerPanel.add(new JLabel("Blue"));
        spinnerPanel.add(blueSpinner);

        panel.add(spinnerPanel, gc);
        panel.revalidate();
        return panel;
    }

    private int[] generateColors() {
        int[] result = new int[iterations];

        for (int i = 0; i < iterations; i++) {
            double f = ((float) i / (float) iterations) * Math.PI * 2f * colorRepetitions;

            int r = (int) (Math.sin(f + 2f) * (float)(red/2) + (float)(1+(red/2)));
            int g = (int) (Math.sin(f) * (float)(green/2) + (float)(1+(green/2)));
            int b = (int) (Math.sin(f + 4f) * (float)(blue/2) + (float)(1+(blue/2)));

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
            int w = controller.width*renderScale;
            int h = controller.height*renderScale;

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

