package igrek.findme.graphics;

import android.content.Context;
import android.location.Location;

import igrek.findme.logic.Engine;
import igrek.findme.logic.Types;
import igrek.findme.settings.App;
import igrek.findme.settings.Config;
import igrek.findme.system.Output;

public class Graphics extends CanvasView {
    private App app;
    public boolean init = false;

    public Graphics(Context context, Engine engine) {
        super(context, engine);
        app = App.geti();
    }

    @Override
    public void repaint() {
        drawBackground();
        if (!init) return;
        setFontSize(Config.Fonts.fontsize);
        drawSignature();
        if (app.mode == Types.AppMode.MENU) {
            drawMenu();
        } else if (app.mode == Types.AppMode.COMPASS) {
            drawCompass();
        }
        drawButtons();
        drawEcho();
    }

    public void drawMenu() {
        drawStats();
    }

    public void drawCompass() {
        setColor("006000");
        drawLine(0, h / 2, w, h / 2);
        drawLine(w / 2, 0, w / 2, h);
        if (engine.sensors == null) return;
        //azymut
        setColor("909090");
        float theta = (float) Math.asin(engine.sensors.getValue(2)) * 2;
        drawText("Azymut: " + -theta * 180 / Math.PI, 0, 0);
        //wskazówka
        setColor("00a0f0");
        float scale = Config.compass_scale * h / 2;
        float cx = (float) (w / 2 + scale * Math.sin(theta));
        float cy = (float) (h / 2 - scale * Math.cos(theta));
        drawLine(w / 2, h / 2, cx, cy);
        outlineCircle(cx, cy, 10, 1);
        drawText("N", cx, cy, Types.Align.CENTER);
    }

    void drawBackground() {
        setColor(Config.Colors.background);
        clearScreen();
    }

    public void drawSignature() {
        setColor(Config.Colors.signature);
        setFont(Types.Font.FONT_MONOSPACE);
        drawText("Igrek", w, h, Types.Align.BOTTOM_RIGHT);
    }

    public void drawEcho() {
        setColor(Config.Colors.text);
        setFont();
        Output.echoTryClear();
        drawTextMultiline(Output.echos, 0, 5 * Config.Buttons.height + 6 * Config.Fonts.lineheight, Config.Fonts.lineheight);
    }

    public void drawButtons() {
        for (Buttons.Button b : engine.buttons.buttons) {
            drawButton(b);
        }
    }

    public void drawButton(Buttons.Button b) {
        if (!b.visible) return;
        if (b.clicked > 0) {
            setColor(Config.Colors.Buttons.background_clicked, Config.Colors.Buttons.alpha_clicked);
        } else {
            setColor(Config.Colors.Buttons.background, Config.Colors.Buttons.alpha);
        }
        fillRect(b.x, b.y, b.x + b.w, b.y + b.h);
        if (b.clicked > 0) {
            setColor(Config.Colors.Buttons.outline_clicked, Config.Colors.Buttons.alpha_clicked);
        } else {
            setColor(Config.Colors.Buttons.outline, Config.Colors.Buttons.alpha);
        }
        outlineRect(b.x, b.y, b.x + b.w, b.y + b.h, 2);
        setColor(Config.Colors.Buttons.text, Config.Colors.Buttons.alpha);
        setFontSize(Config.Buttons.fontsize);
        setFont();
        drawText(b.text, b.x + b.w / 2, b.y + b.h / 2, Types.Align.CENTER);
        setFontSize(Config.Fonts.fontsize);
    }

    public void drawStats() {
        setFont(Types.Font.FONT_BOLD);
        String message;
        //Internet
        if (engine.internetmanager != null) {
            if (engine.internetmanager.isConnected()) {
                setColor(Config.Colors.Stats.success);
                message = "OK";
                if (engine.internetmanager.isWifiEnabled()) {
                    message += " (Wifirifi)";
                } else if (engine.internetmanager.isMobileEnabled()) {
                    message += " (dane pakietowe)";
                }
            } else {
                setColor(Config.Colors.Stats.error);
                if (engine.internetmanager.isAvailable()) {
                    message = "włączony, brak połączenia";
                } else {
                    message = "wyłączony";
                }
            }
        } else {
            setColor(Config.Colors.Stats.error);
            message = "błąd krytyczny";
        }
        drawText("Internet: " + message, w / 2, 5 * Config.Buttons.height + 2, Types.Align.HCENTER);
        //status gps
        if (engine.gps != null) {
            if (engine.gps.isGPSEnabled()) {
                if (engine.gps.isGPSAvailable()) {
                    setColor(Config.Colors.Stats.success);
                    message = "OK";
                } else {
                    setColor(Config.Colors.Stats.error);
                    message = "brak sygnału";
                }
                message += " (satelity: " + engine.gps.getGPSSatellites() + ")";
            } else {
                setColor(Config.Colors.Stats.error);
                message = "wyłączony";
            }
        } else {
            setColor(Config.Colors.Stats.error);
            message = "błąd krytyczny";
        }
        drawText("GPS: " + message, w / 2, 5 * Config.Buttons.height + Config.Fonts.lineheight, Types.Align.HCENTER);
        //Lokalizacja
        if (engine.gps != null) {
            if (engine.gps.isLocationAvailable()) {
                setColor(Config.Colors.Stats.success);
                message = "OK";
                if (engine.gps.isGPSAvailable()) {
                    message += " (GPS)";
                } else if (engine.gps.isInternetAvailable()) {
                    message += " (Internet)";
                }
                //ostatni czas
                message += ", "+engine.getElapsedTimeFormatted(engine.gps.getLastLocationTime())+" temu";
            } else {
                setColor(Config.Colors.Stats.error);
                message = "brak sygnału";
            }
        } else {
            setColor(Config.Colors.Stats.error);
            message = "błąd krytyczny";
        }
        drawText("Lokalizacja: " + message, w / 2, 5 * Config.Buttons.height + 2 * Config.Fonts.lineheight, Types.Align.HCENTER);
        //szczegółowe info lokalizacji
        if (engine.gps.isLocationAvailable()) {
            setColor(Config.Colors.Stats.info);
            try {
                Location location = engine.gps.getLocation();
                message = location.getLongitude() + ", " + location.getLatitude();
                drawText(message, w / 2, 5 * Config.Buttons.height + 3 * Config.Fonts.lineheight, Types.Align.HCENTER);
            } catch (Exception e) { }
        }
        //stan zalogowania
        if (app.id_user > 0) {
            setColor(Config.Colors.Stats.success);
            message = "Zalogowany jako: " + app.login + " (ID: " + app.id_user + ")";
        }else{
            setColor(Config.Colors.Stats.error);
            message = "Niezalogowany";
        }
        drawText(message, w / 2, 5 * Config.Buttons.height + 4 * Config.Fonts.lineheight, Types.Align.HCENTER);
        //ostatnio wysłana pozycja
        if (app.last_position_update > 0) {
            setColor(Config.Colors.Stats.info);
            message = "Ostatnio wysłane położenie: "+engine.getElapsedTimeFormatted(app.last_position_update)+" temu";
            drawText(message, w / 2, 5 * Config.Buttons.height + 5 * Config.Fonts.lineheight, Types.Align.HCENTER);
        }
    }
}
