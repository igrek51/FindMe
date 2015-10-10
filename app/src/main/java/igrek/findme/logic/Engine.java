package igrek.findme.logic;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import igrek.findme.graphics.Buttons;
import igrek.findme.graphics.CanvasView;
import igrek.findme.graphics.Graphics;
import igrek.findme.managers.Files;
import igrek.findme.managers.InternetManager;
import igrek.findme.managers.InternetManager.*;
import igrek.findme.managers.InputManager;
import igrek.findme.managers.GPSManager;
import igrek.findme.managers.Sensors;
import igrek.findme.managers.TimerManager;
import igrek.findme.settings.App;
import igrek.findme.settings.Config;
import igrek.findme.settings.Preferences;
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
    public InternetManager.InternetTask internetTask1;
    public Preferences preferences;
    public InputManager inputmanager;
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
        sensors = new Sensors(activity);
        gps = new GPSManager(activity);
        internetmanager = new InternetManager(activity);
        files = new Files(activity);
        Output.log("Utworzenie aplikacji.");
    }

    @Override
    public void timer_run() {
        if (!running) return;
        update();
        if (!inputmanager.visible) {
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
        //obsługa przycisków
        if (buttons.isClicked()) {
            buttonsExecute(buttons.clickedId());
        }
        /*
        if (internetTask1 != null) {
            if (internetTask1.isReady()) {
                if (internetTask1.isCorrect()) {
                    Output.info("Kod odpowiedzi: " + internetTask1.getResponseCode());
                    Output.info("Odpowiedź: " + internetTask1.getResponse());
                } else {
                    Output.info("Błąd odbierania");
                }
                internetTask1 = null;
            }
        }
        */
    }

    public void buttonsExecute(String bid) {
        if (bid.equals("exit")) {
            control.executeEvent(Types.ControlEvent.BACK);
        } else if (bid.equals("clear")) {
            Output.echos = "";
        } else if (bid.equals("get")) {
            internetmanager.download("http://igrek.cba.pl/findme/get.php?name=dupa", new InternetManager.ResponseHandler() {
                @Override
                public void onResponse(InternetManager.InternetTask internetTask) {
                    if (internetTask.isCorrect()) {
                        Output.info("Kod odpowiedzi: " + internetTask.getResponseCode());
                        Output.info("Odpowiedź: " + internetTask.getResponse());
                    } else {
                        Output.info("Błąd odbierania");
                    }
                }
            });
            Output.info("Wysłano żądanie.");
        } else if (bid.equals("sql_get")) {
            internetTask1 = internetmanager.download("http://igrek.cba.pl/findme/getsql.php");
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
                    App.geti().login = inputText;
                    Output.info("Wpisany login: " + inputText);
                    inputmanager.inputScreenShow("Hasło:", new InputManager.InputHandler() {
                        @Override
                        public void onInput(String inputText) {
                            App.geti().pass = inputText;
                            Output.info("Wpisane hasło: " + inputText);
                            login();
                        }
                    });
                }
            });
        } else {
            Output.error("Nie obsłużono zdarzenia dla przycisku: " + bid);
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
        List<InternetManager.Variable> data = new ArrayList<>();
        data.add(new Variable("login", App.geti().login));
        data.add(new Variable("pass", App.geti().pass));
        internetmanager.downloadPOST("http://igrek.cba.pl/findme/login.php", data, new ResponseHandler() {
            @Override
            public void onResponse(InternetTask internetTask) {
                if (internetTask.isCorrect()) {
                    Output.info("Kod odpowiedzi: " + internetTask.getResponseCode());
                    Output.info("Odpowiedź: " + internetTask.getResponse());
                } else {
                    Output.info("Błąd odbierania");
                }
            }
        });
        Output.info("Próba logowania...");
    }
}
