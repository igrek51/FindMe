package igrek.findme.logic;

import android.app.Activity;

import java.util.Random;

import igrek.findme.settings.Preferences;
import igrek.findme.system.LocationMaster;
import igrek.findme.system.Output;
import igrek.findme.system.SensorMaster;
import igrek.findme.graphics.Buttons;
import igrek.findme.graphics.CanvasView;
import igrek.findme.graphics.Graphics;
import igrek.findme.system.TimeMaster;
import igrek.findme.settings.App;
import igrek.findme.settings.Config;

public class Engine implements TimeMaster.MasterOfTime, CanvasView.TouchPanel {
    public Graphics graphics;
    public Activity activity;
    TimeMaster timer = null;
    App app;
    Config config;
    public Buttons buttons;
    Random random;
    TouchPanel touchpanel = null;
    Control control = null;
    public SensorMaster sensormaster;
    public LocationMaster locationmaster;
    public Preferences preferences;
    boolean init = false;
    boolean running = true;

    public Engine(Activity activity) {
        this.activity = activity;
        Output.reset();
        app = App.reset(this);
        config = Config.geti();
        preferences = new Preferences(activity);
        graphics = new Graphics(activity, this);
        buttons = new Buttons();
        timer = new TimeMaster(this, Config.geti().timer_interval0);
        random = new Random();
        sensormaster = new SensorMaster(activity);
        locationmaster = new LocationMaster(activity);
        Output.log("Utworzenie aplikacji.");
    }

    @Override
    public void timer_run() {
        if (!running) return;
        update();
        graphics.invalidate();
    }

    public void init() {
        init = true;
        //po inicjalizacji grafiki - po ustaleniu rozmiarów
        Output.log("Inicjalizacja (po starcie grafiki).");
        touchpanel = new TouchPanel(this, graphics.w, graphics.h);
        control = new Control(this);
        //przyciski do nawigacji po ekranach
        //buttons.add("Wyjdź", "exit", graphics.w, graphics.h, 0, 0, Buttons.HADJUST | Buttons.RIGHT | Buttons.BOTTOM);
    }

    public void pause() {
        sensormaster.unregister();
    }

    public void resume() {
        sensormaster.register();
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
    }

    public void buttonsExecute(String bid) {
        if (bid.equals("exit")) {
            control.executeEvent(Types.ControlEvent.BACK);
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
        control.executeEvent(Types.ControlEvent.BACK);
    }

    public void keycode_menu() {
        control.executeEvent(Types.ControlEvent.MENU);
    }

    public boolean options_select(int id) {
        return false;
    }
}
