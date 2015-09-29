package igrek.dupa3;

import android.app.Activity;
import android.hardware.Sensor;

public class Engine implements TimeMaster.MasterOfTime, CanvasView.TouchPanel {
    Graphics graphics = null;
    Activity activity = null;
    TimeMaster timer = null;
    SensorMaster sensormaster = null;
    App app;
    Config config;
    Buttons buttons;
    boolean init = false;

    public Engine(Activity activity) {
        app = App.reset();
        App.engine = this;
        this.activity = activity;
        graphics = new Graphics(activity, this);
        sensormaster = new SensorMaster(activity);
        buttons = new Buttons();
        config = Config.geti();
        App.log("Utworzenie aplikacji.");
    }

    @Override
    public void timer_run() {
        if (!app.running) return;
        update();
        graphics.invalidate();
    }

    public void start() {
        timer = new TimeMaster(this);
        App.log("Start aplikacji.");
    }

    public void init() {
        init = true;
        //po inicjalizacji grafiki
        App.log("Inicjalizacja.");
        app.plot.buffer = new float[config.plot_buffer_size];
        for (int i = 0; i < config.plot_buffer_size; i++) {
            app.plot.buffer[i] = 0;
        }
        Buttons.Button last;
        //akcelerometr
        last = buttons.add("Wykres X", "acc_x", 0, 0, 0, 0, Buttons.Align.HADJUST);
        last = buttons.add("Wykres Y", "acc_y", 0, 0, 0, 0, Buttons.Align.HADJUST);
        last = buttons.add("Wykres Z", "acc_z", 0, 0, 0, 0, Buttons.Align.HADJUST);
        last = buttons.add("Wypadkowe", "acc_w", 0, 0, 0, 0, Buttons.Align.HADJUST);
        last = buttons.add("Poziomica", "spirit_level", 0, 0, 0, 0, Buttons.Align.HADJUST);
        //sensor magnetyczny
        last = buttons.add("Wykres X", "mag_x", 0, 0, 0, 0, Buttons.Align.HADJUST);
        last = buttons.add("Wykres Y", "mag_y", 0, 0, 0, 0, Buttons.Align.HADJUST);
        last = buttons.add("Wykres Z", "mag_z", 0, 0, 0, 0, Buttons.Align.HADJUST);
        last = buttons.add("Wypadkowe", "mag_w", 0, 0, 0, 0, Buttons.Align.HADJUST);
        //rotation vector sensor
        last = buttons.add("x*sin(th/2)", "rot_x", 0, 0, 0, 0, Buttons.Align.HADJUST);
        last = buttons.add("y*sin(th/2)", "rot_y", 0, 0, 0, 0, Buttons.Align.HADJUST);
        last = buttons.add("z*sin(th/2)", "rot_z", 0, 0, 0, 0, Buttons.Align.HADJUST);
        last = buttons.add("Kompas", "compass", 0, 0, 0, 0, Buttons.Align.HADJUST);
        //nawigacja po ekranach
        last = buttons.add("Wyjście", "exit", graphics.w, graphics.h, 0, 0, Buttons.Align.HADJUST | Buttons.Align.RIGHT | Buttons.Align.BOTTOM);
        last = buttons.add("Powrót", "back", graphics.w, graphics.h, 0, 0, Buttons.Align.HADJUST | Buttons.Align.RIGHT | Buttons.Align.BOTTOM);
        last = buttons.add("Wyczyść", "clear", graphics.w, last.y, 0, 0, Buttons.Align.HADJUST | Buttons.Align.RIGHT | Buttons.Align.BOTTOM);
        last = buttons.add("Start/Stop", "startstop", graphics.w, last.y, 0, 0, Buttons.Align.HADJUST | Buttons.Align.RIGHT | Buttons.Align.BOTTOM);
        set_mode(App.Mode.MENU);
    }

    public void pause() {
        sensormaster.unregister();
    }

    public void resume() {
        sensormaster.register();
    }

    public void quit() {
        app.running = false;
        timer.stop();
        sensormaster.unregister();
        App.log("Zamykanie aplikacji");
        activity.finish();
    }

    public void update() {
        //obsługa przycisków
        if (buttons.isClicked()) {
            String bid = buttons.clickedId();
            if (bid.equals("acc_x")) {
                App.geti().sensor_type = Sensor.TYPE_ACCELEROMETER;
                App.geti().sensor_axis = 1;
                set_mode(App.Mode.PLOT);
            } else if (bid.equals("acc_y")) {
                App.geti().sensor_type = Sensor.TYPE_ACCELEROMETER;
                App.geti().sensor_axis = 2;
                set_mode(App.Mode.PLOT);
            } else if (bid.equals("acc_z")) {
                App.geti().sensor_type = Sensor.TYPE_ACCELEROMETER;
                App.geti().sensor_axis = 3;
                set_mode(App.Mode.PLOT);
            } else if (bid.equals("acc_w")) {
                App.geti().sensor_type = Sensor.TYPE_ACCELEROMETER;
                App.geti().sensor_axis = 7;
                set_mode(App.Mode.PLOT);
            } else if (bid.equals("spirit_level")) {
                set_mode(App.Mode.SPIRIT_LEVEL);
            } else if (bid.equals("mag_x")) {
                App.geti().sensor_type = Sensor.TYPE_MAGNETIC_FIELD;
                App.geti().sensor_axis = 1;
                set_mode(App.Mode.PLOT);
            } else if (bid.equals("mag_y")) {
                App.geti().sensor_type = Sensor.TYPE_MAGNETIC_FIELD;
                App.geti().sensor_axis = 2;
                set_mode(App.Mode.PLOT);
            } else if (bid.equals("mag_z")) {
                App.geti().sensor_type = Sensor.TYPE_MAGNETIC_FIELD;
                App.geti().sensor_axis = 3;
                set_mode(App.Mode.PLOT);
            } else if (bid.equals("mag_w")) {
                App.geti().sensor_type = Sensor.TYPE_MAGNETIC_FIELD;
                App.geti().sensor_axis = 7;
                set_mode(App.Mode.PLOT);
            } else if (bid.equals("rot_x")) {
                App.geti().sensor_type = Sensor.TYPE_ROTATION_VECTOR;
                App.geti().sensor_axis = 1;
                set_mode(App.Mode.PLOT);
            } else if (bid.equals("rot_y")) {
                App.geti().sensor_type = Sensor.TYPE_ROTATION_VECTOR;
                App.geti().sensor_axis = 2;
                set_mode(App.Mode.PLOT);
            } else if (bid.equals("rot_z")) {
                App.geti().sensor_type = Sensor.TYPE_ROTATION_VECTOR;
                App.geti().sensor_axis = 3;
                set_mode(App.Mode.PLOT);
            } else if (bid.equals("compass")) {
                set_mode(App.Mode.COMPASS);
            } else if (bid.equals("back")) {
                set_mode(App.Mode.MENU);
            } else if (bid.equals("clear")) {
                clear_records();
                app.plot.recording = false;
            } else if (bid.equals("startstop")) {
                app.plot.recording ^= true;
            } else if (bid.equals("exit")) {
                quit();
            } else {
                App.error("Nie obsłużono zdarzenia dla przycisku: " + bid);
            }
        }
        if (app.plot.recording && app.mode == App.Mode.PLOT) add_record();
    }

    @Override
    public void touch_down(float touch_x, float touch_y) {

    }

    @Override
    public void touch_move(float touch_x, float touch_y) {

    }

    @Override
    public void touch_up(float touch_x, float touch_y) {
        buttons.checkClicked(touch_x, touch_y);
    }

    @Override
    public void resize_event() {
        if (!init) init();
        App.log("Rozmiar ekranu zmieniony.");
    }

    public void keycode_back() {
        if(app.mode==App.Mode.MENU){
            quit();
        }else{
            set_mode(App.Mode.MENU);
        }
    }

    public boolean options_select(int id) {
        if (id == R.id.action_exit) {
            quit();
            return true;
        }
        return false;
    }

    public void add_record() {
        if (app.plot.recorded < config.plot_buffer_size) {
            app.plot.recorded++;
        }
        //przesunięcie starych rekordów
        for (int i = config.plot_buffer_size - app.plot.recorded; i < config.plot_buffer_size - 1; i++) {
            app.plot.buffer[i] = app.plot.buffer[i + 1];
        }
        //nowy rekord
        app.plot.buffer[config.plot_buffer_size - 1] = sensormaster.get_value();
    }

    void clear_records() {
        app.plot.recorded = 0;
    }

    float srednia(float tab[]) {
        float suma = 0;
        for (int i = config.plot_buffer_size - app.plot.recorded; i < config.plot_buffer_size; i++) {
            suma += tab[i];
        }
        return suma / app.plot.recorded;
    }

    float odchylenie(float tab[]) {
        float srednia = srednia(tab);
        float suma = 0;
        for (int i = config.plot_buffer_size - app.plot.recorded; i < config.plot_buffer_size; i++) {
            suma += (tab[i] - srednia) * (tab[i] - srednia);
        }
        return (float) Math.sqrt(suma / app.plot.recorded);
    }

    void set_mode(App.Mode mode) {
        app.mode = mode;
        buttons.disableAllButtons();
        if (app.mode == App.Mode.MENU) {
            buttons.setActive("exit", true);
            buttons.setActive("acc_x", true);
            buttons.setActive("acc_y", true);
            buttons.setActive("acc_z", true);
            buttons.setActive("acc_w", true);
            buttons.setActive("spirit_level", true);
            buttons.setActive("mag_x", true);
            buttons.setActive("mag_y", true);
            buttons.setActive("mag_z", true);
            buttons.setActive("mag_w", true);
            buttons.setActive("rot_x", true);
            buttons.setActive("rot_y", true);
            buttons.setActive("rot_z", true);
            buttons.setActive("compass", true);
            sensormaster.unregister();
            clear_records();
        } else if (app.mode == App.Mode.PLOT) {
            buttons.setActive("back", true);
            buttons.setActive("clear", true);
            buttons.setActive("startstop", true);
            sensormaster.select_sensor();
        } else if (app.mode == App.Mode.SPIRIT_LEVEL) {
            buttons.setActive("back", true);
            App.geti().sensor_type = Sensor.TYPE_ACCELEROMETER;
            sensormaster.select_sensor();
        } else if (app.mode == App.Mode.COMPASS) {
            buttons.setActive("back", true);
            App.geti().sensor_type = Sensor.TYPE_ROTATION_VECTOR;
            sensormaster.select_sensor();
        }
    }


}
