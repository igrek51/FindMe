package igrek.dupa3;

import java.util.Timer;
import java.util.TimerTask;

public class TimeMaster {
    //metoda run do zaimplementowania
    public interface MasterOfTime {
        void timer_run();
    }

    MasterOfTime master_of_time = null;
    Timer timer = null;
    TimerTask timerTask = null;
    int interval; //ms

    public TimeMaster(MasterOfTime master_of_time, int interval) {
        this.master_of_time = master_of_time;
        if (interval == 0) interval = 1000 / Config.geti().timer_fps0; //odczytanie z fpsów
        this.interval = interval;
        start();
    }

    public TimeMaster(MasterOfTime master_of_time) {
        this(master_of_time, Config.geti().timer_interval0);
    }

    public void setInterval(int interval) {
        if (interval <= 0) interval = 1;
        this.interval = interval;
        reset();
    }

    public void setFPS(int fps) {
        setInterval(1000 / fps);
    }

    public void start() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Engine gameengine = (Engine) master_of_time;
                //Only the original thread that created a view hierarchy can touch its views.
                gameengine.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        master_of_time.timer_run();
                    }
                });
            }
        };
        timer.schedule(timerTask, interval, interval);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void reset() {
        stop();
        start();
    }
}
