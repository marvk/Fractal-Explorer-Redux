import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Marvin on 22.12.2014.
 */
public class FractalFrame extends JFrame implements Observer {
    private FractalPanel contentPanel;
    private FractalMenuBar bar;
    private Controller controller;
    private JPanel controlPanel;
    private JPanel superPanel;

    public FractalFrame(Controller controller) throws HeadlessException {
        this.controller = controller;
        this.contentPanel = new FractalPanel(controller);
        this.bar = new FractalMenuBar(controller);
        this.superPanel = new JPanel();

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.getState().terminate();
            }
        });

        setJMenuBar(bar);
        superPanel.add(contentPanel);

        controlPanel = new JPanel();
        superPanel.add(controlPanel);

        add(superPanel);

        contentPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void addMouseWheelListener(MouseWheelListener listener) {
        contentPanel.addMouseWheelListener(listener);
    }

    @Override
    public void addMouseListener(MouseListener listener) {
        contentPanel.addMouseListener(listener);
    }

    @Override
    public void update(Observable o, Object arg) {
        superPanel.remove(controlPanel);
        controlPanel = controller.getState().getControlPanel();
        controlPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        superPanel.add(controlPanel);
        revalidate();
        pack();
    }
}
