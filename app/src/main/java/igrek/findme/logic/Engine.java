package igrek.findme.logic;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import igrek.findme.graphics.*;
import igrek.findme.managers.*;
import igrek.findme.managers.InternetManager.*;
import igrek.findme.settings.*;
import igrek.findme.system.Output;

public class Engine implements TimerManager.MasterOfTime, CanvasView.TouchPanel {
    boolean init = false;
    boolean running = true;
    public Activity activity;
    App app;
    Config config;
    TimerManager timer;
    Random random;
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
        config = Config.geti();
        preferences = new Preferences(activity);
        graphics = new Graphics(activity, this);
        buttons = new Buttons();
        timer = new TimerManager(this, Config.geti().timer_interval0);
        random = new Random();
        try {
            sensors = new Sensors(activity);
        } catch (Exception e) {
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
            Output.error(e);
        }
        //files = new Files(activity);
        Output.log("Utworzenie aplikacji.");
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

    public void init() {
        //po inicjalizacji grafiki - po ustaleniu rozmiarów
        Output.log("Inicjalizacja (po starcie grafiki).");
        touchpanel = new TouchPanel(this, graphics.w, graphics.h);
        control = new Control(this);
        inputmanager = new InputManager(activity, graphics);
        //przyciski
        Buttons.Button b;
        b = buttons.add("Znajdź mnie, Iro!", "login", 0, 0, graphics.w, 120);
        b = buttons.add("Ustawienia", "preferences", 0, b.y + b.h, graphics.w, 0);
        b = buttons.add("Kompas", "compass", 0, b.y + b.h, graphics.w, 0);
        b = buttons.add("Czyść konsolę", "clear", 0, b.y + b.h, graphics.w, 0);
        b = buttons.add("Minimalizuj", "minimize", 0, b.y + b.h, graphics.w, 0);
        buttons.add("Zakończ", "exit", 0, b.y + b.h, graphics.w, 0);
        buttons.add("Powrót", "back", 0, graphics.h, graphics.w, 0, Types.Align.BOTTOM);
        preferencesLoad();
        try {
            setAppMode(Types.AppMode.MENU);
        } catch (Exception e) {
            Output.error(e);
        }
        //odczekanie przed czyszczeniem konsoli
        Output.lastEcho = System.currentTimeMillis() + 4000;
        init = true;
    }

    public void pause() {
        sensors.unregister();
    }

    public void resume() {
        sensors.register();
    }

    public void quit() {
        if (!running) { //próba ponownego zamknięcia
            Output.log("Anulowanie próby ponownego zamknięcia aplikacji.");
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
            if (buttons.isClicked()) {
                buttonsExecute(buttons.clickedId());
            }
            if (app.id_user > 0 && gps.isGPSAvailable()) { //zalogowany i ma sygnał gps
                if (System.currentTimeMillis() > app.last_position_update + config.location.position_update_period) {
                    app.last_position_update = System.currentTimeMillis();
                    sendGPSPosition();
                }
            }
        } catch (Exception e) {
            Output.error(e);
        }
    }

    public void buttonsExecute(String bid) throws Exception {
        if (bid.equals("exit")) {
            control.executeEvent(Types.ControlEvent.BACK);
        } else if (bid.equals("clear")) {
            Output.echos = "";
        } else if (bid.equals("preferences")) {
            inputmanager.inputScreenShow("Login:", new InputManager.InputHandler() {
                @Override
                public void onInput(String inputText) {
                    app.login = inputText;
                    inputmanager.inputScreenShow("Hasło:", new InputManager.InputHandler() {
                        @Override
                        public void onInput(String inputText) {
                            app.pass = inputText;
                            preferencesSave();
                        }
                    });
                }
            });
        } else if (bid.equals("login")) {
            login();
        } else if (bid.equals("compass")) {
            setAppMode(Types.AppMode.COMPASS);
        } else if (bid.equals("back")) {
            setAppMode(Types.AppMode.MENU);
        } else if (bid.equals("minimize")) {
            minimize();
        } else {
            Output.errorthrow("Nie obsłużono zdarzenia dla przycisku: " + bid);
        }
    }

    @Override
    public void touch_down(float touch_x, float touch_y) {
        if (buttons.checkClickedNoAction(touch_x, touch_y)) return;
        if (touchpanel != null) {
            touchpanel.touch_down(touch_x, touch_y);
        }
    }

    @Override
    public void touch_move(float touch_x, float touch_y) {
        if (touchpanel != null) {
            touchpanel.touch_move(touch_x, touch_y);
        }
    }

    @Override
    public void touch_up(float touch_x, float touch_y) {
        if (buttons.checkClicked(touch_x, touch_y)) return;
        if (touchpanel != null) {
            touchpanel.touch_up(touch_x, touch_y);
        }
    }

    @Override
    public void resize_event() {
        app.w = graphics.w;
        app.h = graphics.h;
        if (!init) init();
        Output.log("Rozmiar ekranu zmieniony na: " + graphics.w + "px x " + graphics.h + "px");
    }

    public void keycode_back() {
        if (inputmanager.visible) {
            inputmanager.inputScreenHide();
            return;
        }
        try{
            control.executeEvent(Types.ControlEvent.BACK);
        }catch (Exception e) {
            Output.error(e);
        }
    }

    public void keycode_menu() {
        try{
            control.executeEvent(Types.ControlEvent.MENU);
        }catch (Exception e) {
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
                Output.info("Zalogowano, ID: " + app.id_user);
                Output.info("Oczekiwanie na sygnał GPS...");
            }
        });
    }

    public void sendGPSPosition() throws Exception {
        Output.info("Wysyłanie położenia...");
        Location location = gps.getGPSLocation();
        if (location == null) {
            Output.errorthrow("Błąd location = null");
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
        buttons.disableAllButtons();
        if (app.mode == Types.AppMode.MENU) {
            buttons.setActive("login", true);
            buttons.setActive("preferences", true);
            buttons.setActive("compass", true);
            buttons.setActive("clear", true);
            buttons.setActive("minimize", true);
            buttons.setActive("exit", true);
            sensors.unregister();
        } else if (app.mode == Types.AppMode.COMPASS) {
            buttons.setActive("back", true);
            sensors.register();
        }
    }
}
