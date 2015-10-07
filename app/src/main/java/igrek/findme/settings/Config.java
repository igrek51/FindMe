package igrek.findme.settings;

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

    //STAŁE
    //  OUTPUT
    public final String logTag = "AppLog";
    public final int echo_spaces = 40;
    public final int echo_showtime = 1800; //[ms]
    //  SCREEN
    public final int fullscreen_flag = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
    public final boolean fullscreen = true;
    public final boolean hide_taskbar = true;
    public final boolean keep_screen_on = true;
    //  TIMER
    public final int timer_interval0 = 0;
    public final int timer_fps0 = 10;
    //  BUTTONY
    public final int buttons_fontsize = 12;
    public final int button_padding_h = 10;
    public final int button_padding_v = 5;
    public final int button_height = 25;
    //  CZCIONKI
    public final int fontsize = 12;
    public final int lineheight = 13;
    //  KOLORY
    public final Colors colors = new Colors();
    public class Colors {
        public final int background = 0x000000;
        public final int text = 0x00f000;
        public final int signature = 0x009000;
        public final int buttons_background = 0x303030;
        public final int buttons_outline = 0x606060;
        public final int buttons_text = 0xf0f0f0;
        public final int buttons_alpha = 0xb0;
    }
    //  PANEL DOTYKOWY
    public final Touch touch = new Touch();
    public class Touch {
        //  Sterowanie względne
        //maksymalna odległość między początkiem a końcem w przypadku zatwierdzania (przycisk OK) [cm]
        public final float max_assert_distance = 0.7f;
        //maksymalna dopuszczalna odchyłka kąta od osi układu współrzędnych (z jednej strony) [stopnie]
        public final float max_angle_bias = 38f;
        //minimalna odległość między punktem początkowym a końcowym [cm]
        public final float min_relative_distance = 1.0f;
        //  Sterowanie bezwzględne
        //promień środkowego koła (sterowanie bezwzględne) jako część szerokości ekranu
        public final float center_radius = 0.167f;
    }
    //  USTAWIENIA UŻYTKOWNIKA
    public final String shared_preferences_name = "userpreferences";
    //  INTERNET
    public Connection connection = new Connection();
    public class Connection {
        public final int max_response_size = 1024;
        public final int read_timeout = 10000; //[ms]
        public final int connect_timeout = 15000; //[ms]
    }
    //  LOKALIZACJA
    public Location location = new Location();
    public class Location {
        public final int min_updates_time = 5000; //[ms]
        public final int min_updates_distance = 0; //[m]
    }
}
