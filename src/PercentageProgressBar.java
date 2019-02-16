import javax.swing.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Marvin on 29.12.2014.
 */
public class PercentageProgressBar extends JProgressBar {
    private long longMax;
    private long renderStarted;

    public PercentageProgressBar() {
        this(0);
    }

    public PercentageProgressBar(long longMax) {
        this.longMax = longMax;

        setMinimum(0);
        setMaximum(100);
    }

    public void setValue(long n) {
        if (n == longMax) {
            setString("Finished");
            setValue(getMaximum());
            return;
        }

        setValue((int) (((double) n / (double) longMax) * 100));

        long millis = (System.nanoTime() - renderStarted) / 1000000L;

        String time = "";

        if (longMax > 0 && millis > 0) {
            millis = (long) (((double) (longMax - n) / (double) longMax) * millis);

            millis = (long) (millis * (1 / ((double) n / (double) longMax)));

            time = String.format("%dm, %ds remaining",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
            );
        }

        setString(getValue() + "% " + time);
    }

    public void setLongMax(long longMax) {
        this.longMax = longMax;
    }

    public void start() {
        renderStarted = System.nanoTime();
    }
}
