package igrek.dupa3;

import android.util.Log;

public class App {
    //singleton
    private App() {
        instance = this; //dla pewności
    }

    private static App instance = null;

    public static App geti() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    //funkcje pomocnicze
    public static void log(String l) {
        Log.i(Config.geti().logTag, l);
    }

    public static void log(String l, int i) {
        log(l + " = " + i);
    }

    public static void log(String l, float f) {
        log(l + " = " + f);
    }

    public static void log() {
        log("Dupa");
    }

    //zmienne aplikacji
    public boolean size_changed = false;
    public Plot plot = new Plot();
    public class Plot {
        double ax[], ay[], az[], aw[];
        int recorded = 0;
        boolean recording = true;
    }
    int state = 0;
}
