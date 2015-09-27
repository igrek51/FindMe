package igrek.dupa3;

import android.app.Activity;

public class Engine implements TimeMaster.MasterOfTime, CanvasView.TouchPanel {
    GraphicsEngine graphics = null;
    Activity activity = null;
    TimeMaster timer = null;
    SensorMaster sensormaster = null;
    App app;
    Config config;

    public Engine(Activity activity) {
        this.activity = activity;
        graphics = new GraphicsEngine(activity, this);
        sensormaster = new SensorMaster(activity);
        app = App.geti();
        config = Config.geti();
        //init
        app.plot.ax = new double[config.plot_buffer_size];
        app.plot.ay = new double[config.plot_buffer_size];
        app.plot.az = new double[config.plot_buffer_size];
        app.plot.aw = new double[config.plot_buffer_size];
        for (int i = 0; i < config.plot_buffer_size; i++) {
            app.plot.ax[i] = 0;
            app.plot.ay[i] = 0;
            app.plot.az[i] = 0;
            app.plot.aw[i] = 0;
        }
        App.log("Utworzenie aplikacji.");
    }

    @Override
    public void timer_run() {
        if (App.geti().size_changed) {
            size_changed();
        }
        synchronized (graphics) {
            update();
            graphics.invalidate();
        }
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
        App.log("Zamykanie aplikacji");
        timer.stop();
        activity.finish();
    }

    public void update() {
        if (app.plot.recording) add_record();
    }

    @Override
    public void touch_down(float touch_x, float touch_y) {
        float rtx = touch_x / graphics.w;
        float rty = touch_y / graphics.h;
    }

    @Override
    public void touch_move(float touch_x, float touch_y) {
        float rtx = touch_x / graphics.w;
        float rty = touch_y / graphics.h;
    }

    @Override
    public void touch_up(float touch_x, float touch_y) {
        float rtx = touch_x / graphics.w;
        float rty = touch_y / graphics.h;

    }

    public boolean options_select(int id){
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
            app.plot.ax[i] = app.plot.ax[i + 1];
            app.plot.ay[i] = app.plot.ay[i + 1];
            app.plot.az[i] = app.plot.az[i + 1];
            app.plot.aw[i] = app.plot.aw[i + 1];
        }
        //nowy rekord
        app.plot.ax[config.plot_buffer_size - 1] = sensormaster.get_ax();
        app.plot.ay[config.plot_buffer_size - 1] = sensormaster.get_ay();
        app.plot.az[config.plot_buffer_size - 1] = sensormaster.get_az();
        app.plot.aw[config.plot_buffer_size - 1] = sensormaster.get_a_resultant();
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

}
