package igrek.dupa3;

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
    public final int timer_fps0 = 6;
    //TYPE_ACCELEROMETER, TYPE_MAGNETIC_FIELD, TYPE_ROTATION_VECTOR
    public final float spirit_level_scale = 0.085f;
    public final float compass_scale = 0.9f;
    public final int plot_buffer_size = 256;
    public final int fontsize = 15;
    public final int buttons_fontsize = 12;
    public final int lineheight = 15;
    public final int echo_spaces = 40;
    public final int button_padding_h = 5;
    public final int button_padding_v = 2;
    public final int button_space = 0;
    public final int button_height = 21;
    public final float plot_part = 0.6f;
    public final int plot_sections = 6;
}
