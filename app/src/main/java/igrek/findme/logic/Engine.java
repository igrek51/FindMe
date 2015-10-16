package igrek.findme.logic;

import android.app.Activity;
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
    public Files files;


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
        files = new Files(activity);
        Output.log("Utworzenie aplikacji.");
    }

    @Override
    public void timer_run() {
        if (!running) return;
        update();
        if (inputmanager != null && !inputmanager.visible) {
            graphics.invalidate();
        }
    }

    public void init() {
        init = true;
        //po inicjalizacji grafiki - po ustaleniu rozmiarów
        Output.log("Inicjalizacja (po starcie grafiki).");
        touchpanel = new TouchPanel(this, graphics.w, graphics.h);
        control = new Control(this);
        inputmanager = new InputManager(activity, graphics);
        //przyciski
        Buttons.Button b;
        buttons.add("Wyjdź", "exit", graphics.w, graphics.h, 0, 0, Types.Align.HADJUST | Types.Align.RIGHT | Types.Align.BOTTOM);
        b = buttons.add("Czyść", "clear", graphics.w / 2, graphics.h, 0, 0, Types.Align.HADJUST | Types.Align.HCENTER | Types.Align.BOTTOM);
        buttons.add("Zaloguj", "login", graphics.w / 2, b.y - b.h, 0, 0, Types.Align.HADJUST | Types.Align.HCENTER | Types.Align.TOP);
        buttons.add("Klawiatura", "keyboard_show", graphics.w / 2, 0, 0, 0, Types.Align.HADJUST | Types.Align.HCENTER | Types.Align.TOP);
        b = buttons.add("SQL GET", "sql_get", 0, graphics.h, 0, 0, Types.Align.HADJUST | Types.Align.LEFT | Types.Align.BOTTOM);
        buttons.add("GET", "get", 0, graphics.h - b.h, 0, 0, Types.Align.HADJUST | Types.Align.LEFT | Types.Align.BOTTOM);
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
        if (!init) return;
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
        } else if (bid.equals("get")) {
            internetmanager.GET("http://igrek.cba.pl/findme/get.php?name=dupa", new InternetManager.ResponseHandler() {
                @Override
                public void onResponse(InternetManager.InternetTask internetTask) throws Exception {
                    if (internetTask.isCorrect()) {
                        Output.info("Kod odpowiedzi: " + internetTask.getResponseCode());
                        Output.info("Odpowiedź: " + internetTask.getResponse());
                    } else {
                        Output.info("Błąd odbierania");
                    }
                }
            });
            Output.info("Wysłano żądanie.");
        } else if (bid.equals("keyboard_show")) {
            inputmanager.inputScreenShow("Podaj nazwę:", new InputManager.InputHandler() {
                @Override
                public void onInput(String inputText) {
                    Output.info("Wpisany tekst: " + inputText);
                }
            });
        } else if (bid.equals("login")) {
            inputmanager.inputScreenShow("Login:", new InputManager.InputHandler() {
                @Override
                public void onInput(String inputText) {
                    app.login = inputText;
                    Output.info("Wpisany login: " + inputText);
                    inputmanager.inputScreenShow("Hasło:", new InputManager.InputHandler() {
                        @Override
                        public void onInput(String inputText) {
                            app.pass = inputText;
                            Output.info("Wpisane hasło: " + inputText);
                            login();
                        }
                    });
                }
            });
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
        control.executeEvent(Types.ControlEvent.BACK);
    }

    public void keycode_menu() {
        control.executeEvent(Types.ControlEvent.MENU);
    }

    public boolean options_select(int id) {
        return false;
    }


    public void login() {
        List<Variable> data = new ArrayList<>();
        data.add(new Variable("login", app.login));
        data.add(new Variable("pass", app.pass));
        Output.info("Próba logowania...");
        internetmanager.POST("http://igrek.cba.pl/findme/login.php", data, new ResponseHandler() {
            @Override
            public void onSuccess(InternetTask internetTask) throws Exception {
                app.id_user = internetTask.getResponse2Int();
                Output.info("Zalogowano, ID: " + app.id_user);
            }
        });
    }

    public void sendGPSPosition() throws Exception {
        Output.info("Wysyłanie położenia...");
        Location location = gps.getGPSLocation();
        if (location == null) {
            Output.errorthrow("Błąd location");
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
                        Output.info("Wysłanie położenia powiodło się.");
                    }
                }
            });
        }
    }
}
