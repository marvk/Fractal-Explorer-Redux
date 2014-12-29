import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Observable;

/**
 * Created by Marvin on 22.12.2014.
 */
public class Controller extends Observable {
    private FractalFrame frame;
    private State state;

    public final int height;
    public final int width;
    public final int processors = Runtime.getRuntime().availableProcessors();

    public Controller() {
        width = 800;
        height = 800;

        frame = new FractalFrame(this);

        MouseAdapter a = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                state.mouseClicked(e);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                state.mouseWheelMoved(e);
            }
        };

        frame.addMouseListener(a);
        frame.addMouseWheelListener(a);

        addObserver(frame);

        setState(new StateMandel(this));
    }

    public void repaint() {
        frame.repaint();
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
        setChanged();
        notifyObservers();
    }
}
