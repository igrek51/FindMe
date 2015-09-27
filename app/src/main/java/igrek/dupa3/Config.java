package igrek.dupa3;

import android.hardware.Sensor;
import android.view.WindowManager;

public class Config {
    //singleton
    private Config() {
        instance = this; //dla pewności
    }


    private static Config instance = null;

    public static Config geti() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    //stałe
    public final String logTag = "DupaLog";
    public final int fullscreen_flag = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
    public final boolean hide_taskbar = true;
    public final boolean fullscreen = true;
    public final int timer_interval0 = 0;
    public final int timer_fps0 = 25;
    public final int sensor_type = Sensor.TYPE_ACCELEROMETER; // Sensor.TYPE_ROTATION_VECTOR
    public final int plot_buffer_size = 256;
    public final int fontsize = 15;
}
