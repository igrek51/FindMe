package igrek.findme.system;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import igrek.findme.settings.App;
import igrek.findme.settings.Config;

public class Output {
    public static class SoftErrorException extends Exception {
        public SoftErrorException(String e) {
            super(e);
        }
    }

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

    //  OUTPUT na ekran w jednej linii
    private static void echoOneline(String e) {
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
        echoMultiline(e);
        log("[info] " + e);
    }

    public static void error(String e) {
        echoMultiline("[ERROR] " + e);
        log("[ERROR] " + e);
    }

    public static void error(Exception ex) {
        if (ex instanceof SoftErrorException) {
            echoMultiline("[BŁĄD] " + ex.getMessage());
        } else {
            echoMultiline("[" + ex.getClass().getName() + "] " + ex.getMessage());
        }
        log("[EXCEPTION - " + ex.getClass().getName() + "] " + ex.getMessage());
    }

    public static void errorthrow(String e) throws Exception {
        throw new SoftErrorException(e);
    }

    public static void errorCritical(String e) throws Exception {
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
        throw new Exception(e);
    }

    public static String echoShow() {
        String old_echos = echos;
        if (echos.length() > 0) {
            echos = echos.substring(1);
        }
        return old_echos;
    }

    private static void echoMultiline(String e) {
        String next = "";
        //podział komunikatu, jeśli nie mieści się w jednej linii
        if (e.length() > Config.geti().echo_line_max) {
            next = e.substring(Config.geti().echo_line_max);
            e = e.substring(0, Config.geti().echo_line_max);
        }
        if (echos.length() == 0) {
            echos = e;
            lastEcho = System.currentTimeMillis();
        } else {
            echos += "\n" + e;
        }
        if (next.length() > 0) {
            echoMultiline(next);
        }
    }

    public static void echoTryClear() {
        if (System.currentTimeMillis() > lastEcho + Config.geti().echo_showtime) {
            echoClear1();
            lastEcho += Config.geti().echo_showtime;
        }
    }

    public static void echoClear1() {
        if (echos.length() == 0) return;
        //usuwa 1 wpis z echo
        int firstIndex = echos.indexOf("\n");
        if (firstIndex == -1) {
            echos = "";
        } else {
            echos = echos.substring(firstIndex + 1);
        }
    }

    public static String echos = "";
    public static long lastEcho = 0;
}
