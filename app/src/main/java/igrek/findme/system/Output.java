package igrek.findme.system;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import igrek.findme.settings.App;
import igrek.findme.settings.Config;

public class Output {
    public static void reset() {
        echos = "";
    }

    //  LOG
    public static void log(String l) {
        Log.i(Config.geti().logTag, l);
    }

    public static void log(String l, int i) {
        log(l + " = " + i);
    }

    public static void log(String l, float f) {
        log(l + " = " + f);
    }

    //  OUTPUT na ekran
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

    public static void info(String e) {
        echo(e);
        log("[INFO] " + e);
    }

    public static void error(String e) {
        echo("[ERROR] " + e);
        log("[ERROR] " + e);
    }

    public static void error(Exception ex) {
        error("[Exception - "+ex.getClass().getName()+"] " + ex.getMessage());
    }

    public static void errorCritical(String e) {
        if (App.geti().engine == null || App.geti().engine.activity == null) {
            error("errorCritical: Brak activity");
            return;
        }
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(App.geti().engine.activity);
        dlgAlert.setMessage(e);
        dlgAlert.setTitle("Błąd krytyczny");
        dlgAlert.setPositiveButton("Zamknij", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (App.geti().engine != null) {
                    App.geti().engine.quit();
                }
            }
        });
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
        log("[CRITICAL ERROR] " + e);
    }

    public static String echoShow() {
        String old_echos = echos;
        if (echos.length() > 0) {
            echos = echos.substring(1);
        }
        return old_echos;
    }

    public static String echos = "";
}
