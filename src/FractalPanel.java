import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Marvin on 22.12.2014.
 */
public class FractalPanel extends JPanel {
    private Controller controller;

    public FractalPanel(Controller controller) {
        this.controller = controller;

        setPreferredSize(new Dimension(controller.width, controller.height));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.PINK);
        g2.drawRect(0, 0, getWidth(), getHeight());

        try {
            BufferedImage image = controller.getState().getResizedImage();
            g2.drawImage(image, 0, 0, null);
        } catch (NullPointerException ignored) {
        }
    }
}
