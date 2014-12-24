import javax.swing.*;

/**
 * Created by Marvin on 22.12.2014.
 */
public class FractalMenuBar extends JMenuBar {
    private Controller controller;

    public FractalMenuBar(Controller controller) {
        this.controller = controller;

        JMenu menuMenu = new JMenu("Menu");
        JMenu fractalMenu = new JMenu("Fractals");

        ButtonGroup fractalGroup = new ButtonGroup();

        JRadioButtonMenuItem mandelItem = new JRadioButtonMenuItem("Mandelbrot");
        mandelItem.addActionListener(e -> {
            if (!(controller.getState() instanceof StateMandel)) {
                controller.setState(new StateMandel(controller));
            }
        });
        mandelItem.setSelected(true);
        fractalGroup.add(mandelItem);
        fractalMenu.add(mandelItem);

        JRadioButtonMenuItem buddhaItem = new JRadioButtonMenuItem("Buddhabrot");
        buddhaItem.addActionListener(e -> {
            if (!(controller.getState() instanceof StateBuddha)) {
                controller.setState(new StateBuddha(controller));
            }
        });
        fractalGroup.add(buddhaItem);
        fractalMenu.add(buddhaItem);

        add(menuMenu);
        add(fractalMenu);

        setVisible(true);
    }
}
