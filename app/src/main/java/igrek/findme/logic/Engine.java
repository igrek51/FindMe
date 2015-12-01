package igrek.findme.logic;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import igrek.findme.graphics.*;
import igrek.findme.graphics.Buttons.*;
import igrek.findme.managers.*;
import igrek.findme.managers.InputManager.*;
import igrek.findme.managers.InternetManager.*;
import igrek.findme.settings.*;
import igrek.findme.system.Output;

public class Engine implements TimerManager.MasterOfTime, CanvasView.TouchPanel {
    boolean init = false;
    boolean running = true;
    public Activity activity;
    App app;
    TimerManager timer;
    public Graphics graphics;
    public Buttons buttons;
    TouchPanel touchpanel = null;
    Control control = null;
    public Sensors sensors;
    public GPSManager gps;
    public InternetManager internetmanager;
    public Preferences preferences;
    public InputManager inputmanager = null;

    public Engine(Activity activity) {
        this.activity = activity;
        Output.reset();
        app = App.reset(this);
        preferences = new Preferences(activity);
        graphics = new Graphics(activity, this);
        buttons = new Buttons();
        timer = new TimerManager(this, Config.timer_interval0);
        try {
            sensors = new Sensors(activity);
        } catch (Exception e) {
            sensors = null;
            Output.error(e);
        }
        try {
            gps = new GPSManager(activity);
        } catch (Exception e) {
            Output.error(e);
        }
        try {
            internetmanager = new InternetManager(activity);
        } catch (Exception e) {
            internetmanager = null;
            Output.error(e);
        }
        activity.setContentView(graphics);
        //files = new Files(activity);
        Output.log("Utworzenie aplikacji.");
    }

    public void init() {
        //po inicjalizacji grafiki - po ustaleniu rozmiarów
        Output.log("Inicjalizacja (po starcie grafiki).");
        touchpanel = new TouchPanel(this, graphics.w, graphics.h);
        control = new Control(this);
        inputmanager = new InputManager(activity, graphics);
        //przyciski
        //TODO: rozmiary buttonów w module grafiki
        buttons.add("Znajdź mnie, Iro!", "login", 0, 0, graphics.w, Config.Buttons.height * 2, new ButtonActionListener() {
            public void clicked() throws Exception {
                login();
            }
        });
        buttons.add("Ustawienia", "preferences", 0, buttons.lastYBottom(), graphics.w / 2, 0, new ButtonActionListener() {
            public void clicked() throws Exception {
                clickedPreferences();
            }
        });
        buttons.add("Wyloguj", "logout", graphics.w / 2, buttons.lastYTop(), graphics.w / 2, 0, new ButtonActionListener() {
            public void clicked() throws Exception {
                logout();
            }
        });
        buttons.add("Kompas", "compass", 0, buttons.lastYBottom(), graphics.w / 2, 0, new ButtonActionListener() {
            public void clicked() throws Exception {
                setAppMode(Types.AppMode.COMPASS);
            }
        });
        buttons.add("Czyść konsolę", "clear", graphics.w / 2, buttons.lastYTop(), graphics.w / 2, 0, new ButtonActionListener() {
            public void clicked() throws Exception {
                Output.reset();
            }
        });
        buttons.add("Minimalizuj", "minimize", 0, buttons.lastYBottom(), graphics.w / 2, 0, new ButtonActionListener() {
            public void clicked() throws Exception {
                minimize();
            }
        });
        buttons.add("Zakończ", "exit", graphics.w / 2, buttons.lastYTop(), graphics.w / 2, 0, new ButtonActionListener() {
            public void clicked() throws Exception {
                Output.info("Zamykanie...");
                control.executeEvent(Types.ControlEvent.BACK);
            }
        });
        buttons.add("Powrót", "back", 0, graphics.h, graphics.w, 0, Types.Align.BOTTOM, new ButtonActionListener() {
            public void clicked() throws Exception {
                setAppMode(Types.AppMode.MENU);
            }
        });
        preferencesLoad();
        try {
            setAppMode(Types.AppMode.MENU);
        } catch (Exception e) {
            Output.error(e);
        }
        //odczekanie przed czyszczeniem konsoli
        Output.echoWait(4000);
        graphics.init = true;
        init = true;
    }

    @Override
    public void timer_run() {
        if (!running) return;
        if (!init) return;
        update();
        if (inputmanager != null && !inputmanager.visible) {
            graphics.invalidate();
        }
    }

    public void pause() {
        if (sensors != null) {
            sensors.unregister();
        }
    }

    public void resume() {
        if (sensors != null) {
            sensors.register();
        }
    }

    public void quit() {
        if (!running) { //próba ponownego zamknięcia
            Output.log("Zamykanie aplikacji (2) - anulowanie");
            return;
        }
        running = false;
        timer.stop();
        Output.log("Zamykanie aplikacji");
        activity.finish();
    }

    public void update() {
        try {
            //obsługa przycisków
            buttons.executeClicked();
            if (app.id_user > 0 && gps.isLocationAvailable()) { //zalogowany i ma sygnał gps (lub internet)
                if (System.currentTimeMillis() > app.last_position_update + Config.Location.position_update_period) {
                    app.last_position_update = System.currentTimeMillis();
                    sendGPSPosition();
                }
            }
        } catch (Exception e) {
            Output.error(e);
        }
    }

    public void clickedPreferences() {
        inputmanager.inputScreenShow("Login:", new InputHandlerCancellable() {
            @Override
            public void onAccept(String inputText) {
                app.login = inputText;
                inputmanager.inputScreenShow("Hasło:", new InputHandlerCancellable() {
                    @Override
                    public void onAccept(String inputText) {
                        app.pass = inputText;
                        preferencesSave();
                    }
                });
            }
        });
    }

    @Override
    public void touchDown(float touch_x, float touch_y) {
        if (buttons.checkPressed(touch_x, touch_y)) return;
        if (touchpanel != null) {
            touchpanel.touch_down(touch_x, touch_y);
        }
    }

    @Override
    public void touchMove(float touch_x, float touch_y) {
        if (buttons.checkMoved(touch_x, touch_y)) return;
        if (touchpanel != null) {
            touchpanel.touch_move(touch_x, touch_y);
        }
    }

    @Override
    public void touchUp(float touch_x, float touch_y) {
        if (buttons.checkReleased(touch_x, touch_y)) return;
        if (touchpanel != null) {
            touchpanel.touch_up(touch_x, touch_y);
        }
    }

    @Override
    public void resizeEvent() {
        app.w = graphics.w;
        app.h = graphics.h;
        if (!init) init();
        Output.log("Rozmiar ekranu zmieniony na: " + graphics.w + "px x " + graphics.h + "px");
    }

    public void keycode_back() {
        if (inputmanager.visible) {
            if (inputmanager.isCancellable()) {
                inputmanager.inputScreenCancel();
            }
            return;
        }
        try {
            control.executeEvent(Types.ControlEvent.BACK);
        } catch (Exception e) {
            Output.error(e);
        }
    }

    public void keycode_menu() {
        try {
            control.executeEvent(Types.ControlEvent.MENU);
        } catch (Exception e) {
            Output.error(e);
        }
    }

    public boolean options_select(int id) {
        return false;
    }

    public void minimize() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(startMain);
    }


    public void login() throws Exception {
        if (app.login.length() == 0) {
            Output.errorthrow("Nie podano loginu.");
        }
        if (app.pass.length() == 0) {
            Output.errorthrow("Nie podano hasła.");
        }
        List<Variable> data = new ArrayList<>();
        data.add(new Variable("login", app.login));
        data.add(new Variable("pass", app.pass));
        Output.info("Próba logowania...");
        internetmanager.POST("http://igrek.cba.pl/findme/login.php", data, new ResponseHandler() {
            @Override
            public void onSuccess(InternetTask internetTask) throws Exception {
                app.id_user = internetTask.getResponse2Int();
                Output.info("Zalogowano jako: " + app.login + " (ID: " + app.id_user + ")");
            }
        });
    }

    public void logout() throws Exception {
        if (app.id_user == 0) {
            Output.errorthrow("Nie zalogowano.");
        }
        app.id_user = 0;
        Output.info("Wylogowano.");
    }

    public void sendGPSPosition() throws Exception {
        Output.info("Wysyłanie położenia...");
        Location location = gps.getLocation();
        if (location == null) {
            Output.errorthrow("Brak lokalizacji (location = null)");
        } else {
            List<Variable> data = new ArrayList<>();
            data.add(new Variable("id_user", app.id_user));
            data.add(new Variable("longitude", location.getLongitude()));
            data.add(new Variable("latitude", location.getLatitude()));
            if (location.hasAltitude()) {
                data.add(new Variable("altitude", location.getAltitude()));
            }
            if (location.hasAccuracy()) {
                data.add(new Variable("accuracy", location.getAccuracy()));
            }
            if (location.hasBearing()) {
                data.add(new Variable("direction", location.getBearing()));
            }
            if (location.hasSpeed()) {
                data.add(new Variable("speed", location.getSpeed()));
            }
            data.add(new Variable("provider", gps.getProviderCode(location)));
            internetmanager.POST("http://igrek.cba.pl/findme/send_location.php", data, new ResponseHandler() {
                @Override
                public void onSuccess(InternetTask internetTask) throws Exception {
                    if (internetTask.isCorrect()) {
                        Output.info("Wysłano położenie.");
                    }
                }
            });
        }
    }

    public void preferencesSave() {
        //zapisanie do shared preferences
        preferences.setBoolean("login_set", true);
        preferences.setString("login_login", app.login);
        preferences.setString("login_pass", app.pass);
        Output.info("Zapisano preferencje logowania.");
    }

    public void preferencesLoad() {
        //wczytanie z shared preferences
        if (preferences.exists("login_set")) {
            app.login = preferences.getString("login_login");
            app.pass = preferences.getString("login_pass");
            Output.info("Wczytano preferencje logowania.");
        } else {
            app.login = "";
            app.pass = "";
        }
    }

    public void setAppMode(Types.AppMode mode) throws Exception {
        app.mode = mode;
        buttons.hideAll();
        if (app.mode == Types.AppMode.MENU) {
            buttons.setVisible("login");
            buttons.setVisible("preferences");
            buttons.setVisible("logout");
            buttons.setVisible("compass");
            buttons.setVisible("clear");
            buttons.setVisible("minimize");
            buttons.setVisible("exit");
            if (sensors != null) {
                sensors.unregister();
            }
        } else if (app.mode == Types.AppMode.COMPASS) {
            buttons.setVisible("back");
            if (sensors != null) {
                sensors.register();
            }
        }
    }

    public String getElapsedTimeFormatted(long time2) {
        long ms = System.currentTimeMillis() - time2;
        int min = (int) (ms / 1000 / 60);
        int s = (int) (ms / 1000);
        String output = "";
        if (min > 0) {
            output += min + " min ";
        }
        output += s + " s";
        return output;
    }
}
