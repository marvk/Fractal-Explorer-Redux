import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by Marvin on 22.12.2014.
 */
public class FractalMenuBar extends JMenuBar {

    public FractalMenuBar(Controller controller) {

        JMenu menuMenu = new JMenu("Menu");
        JMenu fractalMenu = new JMenu("Fractals");

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> {
            try {
                ImageIO.write(controller.getState().getImage(), "png", new File("B:\\Marvin\\Desktop\\image.png"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        menuMenu.add(saveItem);

        ButtonGroup fractalGroup = new ButtonGroup();

        JRadioButtonMenuItem mandelItem = new JRadioButtonMenuItem("Mandelbrot");
        mandelItem.addActionListener(e -> {
            if (!(controller.getState() instanceof StateMandel)) {
                controller.getState().terminate();
                controller.setState(new StateMandel(controller));
            }
        });
        mandelItem.setSelected(true);
        fractalGroup.add(mandelItem);
        fractalMenu.add(mandelItem);

        JRadioButtonMenuItem buddhaItem = new JRadioButtonMenuItem("Buddhabrot");
        buddhaItem.addActionListener(e -> {
            if (!(controller.getState() instanceof StateBuddha)) {
                controller.getState().terminate();
                controller.setState(new StateBuddha(controller, false));
            }
        });
        fractalGroup.add(buddhaItem);
        fractalMenu.add(buddhaItem);

        JRadioButtonMenuItem antiBuddhaItem = new JRadioButtonMenuItem("Anti-Buddhabrot");
        antiBuddhaItem.addActionListener(e -> {
            if (!(controller.getState() instanceof StateBuddha)) {
                controller.getState().terminate();
                controller.setState(new StateBuddha(controller, true));
            }
        });
        fractalGroup.add(antiBuddhaItem);
        fractalMenu.add(antiBuddhaItem);

        add(menuMenu);
        add(fractalMenu);

        setVisible(true);
    }
}
