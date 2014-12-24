import javax.swing.*;
import java.awt.*;

/**
 * Created by Marvin on 22.12.2014.
 */
public class FractalFrame extends JFrame {
    private FractalPanel panel;
    private FractalMenuBar bar;
    private Controller controller;

    public FractalFrame(Controller controller) throws HeadlessException {
        this.controller = controller;
        this.panel = new FractalPanel(controller);
        this.bar = new FractalMenuBar(controller);

        setJMenuBar(bar);
        add(panel);

        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
