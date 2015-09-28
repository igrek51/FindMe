package igrek.dupa3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

    public static Engine engine = null;
    public static Activity activity = null;

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

    private static void echo(String e) {
        if (echos.length() == 0) {
            for (int i = 0; i < Config.geti().echo_spaces; i++) {
                echos += ' ';
            }
            echos += e;
        } else {
            echos = echos + " ::: " + e;
        }
    }

    public static void error(String e) {
        echo("[ERROR] " + e);
        log("[ERROR] " + e);
    }

    public static void errorCritical(String e) {
        if (activity == null) {
            error("errorCritical: Brak activity");
            return;
        }
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
        dlgAlert.setMessage(e);
        dlgAlert.setTitle("Błąd krytyczny");
        dlgAlert.setPositiveButton("Zamknij", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (engine != null) {
                    engine.quit();
                }
            }
        });
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
        log("[CRITICAL ERROR] " + e);
    }

    public static void info(String e) {
        echo(e);
        log("[INFO] " + e);
    }

    public static void infoMessage(String e) {
        if (activity == null) {
            error("infoMessage: Brak activity");
            return;
        }
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
        dlgAlert.setMessage(e);
        dlgAlert.setTitle("Info");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
        log("[INFO Message] " + e);
    }

    public static String echo_show() {
        String old_echos = echos;
        if (echos.length() > 0) {
            echos = echos.substring(1);
        }
        return old_echos;
    }

    //zmienne aplikacji
    public boolean size_changed = false;

    public static String echos = "";
    public Plot plot = new Plot();

    public class Plot {
        double buffer[];
        int recorded = 0;
        boolean recording = true;
    }
    int sensor_type = 0;
    int sensor_axis = 0;
}
