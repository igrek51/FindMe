package igrek.dupa3;

import android.app.Activity;

public class Engine implements TimeMaster.MasterOfTime, CanvasView.TouchPanel {
    GraphicsEngine graphics = null;
    Activity activity = null;
    TimeMaster timer = null;
    SensorMaster sensormaster = null;
    App app;
    Config config;
    Buttons buttons;
    boolean running = true;

    public Engine(Activity activity) {
        this.activity = activity;
        graphics = new GraphicsEngine(activity, this);
        sensormaster = new SensorMaster(activity);
        buttons = new Buttons();
        app = App.geti();
        App.engine = this;
        App.activity = activity;
        config = Config.geti();
        //init
        app.plot.buffer = new double[config.plot_buffer_size];
        for (int i = 0; i < config.plot_buffer_size; i++) {
            app.plot.buffer[i] = 0;
        }
        buttons.add("dupa", "dupa", 10, 50, 0, 0, buttons.ADJUST);
        App.log("Utworzenie aplikacji.");
    }

    @Override
    public void timer_run() {
        if (!running) return;
        if (App.geti().size_changed) {
            size_changed();
        }
        update();
        graphics.invalidate();
    }

    public void size_changed() {
        App.geti().size_changed = false;
        App.log("Rozmiar ekranu zmieniony.");
    }

    public void start() {
        timer = new TimeMaster(this);
        App.log("Start aplikacji.");
    }

    public void pause() {
        sensormaster.unregister();
    }

    public void resume() {
        sensormaster.register();
    }

    public void quit() {
        running = false;
        timer.stop();
        sensormaster.unregister();
        App.log("Zamykanie aplikacji");
        activity.finish();
    }

    public void update() {
        if(buttons.isClicked()){
            String bid = buttons.clickedId();
            if(bid.equals("dupa")){
                App.info("Wciśnięto Dupę!");
            }
        }
        if (app.plot.recording) add_record();
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

    public boolean options_select(int id) {
        if (id == R.id.action_exit) {
            quit();
            return true;
        } else if (id == R.id.action_clear) {
            clear_records();
            app.plot.recording = false;
            return true;
        } else if (id == R.id.action_startstop) {
            app.plot.recording ^= true;
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
        if(app.sensor_axis == 7){
            app.plot.buffer[config.plot_buffer_size - 1] = sensormaster.get_w();
        }else {
            app.plot.buffer[config.plot_buffer_size - 1] = sensormaster.get_value(app.sensor_axis);
        }
    }

    void clear_records() {
        app.plot.recorded = 0;
    }

    double srednia(double tab[]) {
        double suma = 0;
        for (int i = config.plot_buffer_size - app.plot.recorded; i < config.plot_buffer_size; i++) {
            suma += tab[i];
        }
        return suma / app.plot.recorded;
    }

    double odchylenie(double tab[]) {
        double srednia = srednia(tab);
        double suma = 0;
        for (int i = config.plot_buffer_size - app.plot.recorded; i < config.plot_buffer_size; i++) {
            suma += (tab[i] - srednia) * (tab[i] - srednia);
        }
        return Math.sqrt(suma / app.plot.recorded);
    }

    double max(double tab[]) {
        double max = tab[0];
        for (int i = config.plot_buffer_size - app.plot.recorded; i < config.plot_buffer_size; i++) {
            if (tab[i] > max) max = tab[i];
        }
        return max;
    }

    double min(double tab[]) {
        double min = tab[0];
        for (int i = config.plot_buffer_size - app.plot.recorded; i < config.plot_buffer_size; i++) {
            if (tab[i] < min) min = tab[i];
        }
        return min;
    }


}
