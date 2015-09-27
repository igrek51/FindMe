package igrek.dupa3;

import android.app.Activity;

public class Engine implements TimeMaster.MasterOfTime {
    CanvasView canvas = null;
    Activity activity = null;
    TimeMaster timer = null;
    SensorMaster sensormaster = null;

    public Engine(Activity activity) {
        this.activity = activity;
        canvas = new CanvasView(activity);
        sensormaster = new SensorMaster(activity);
        App.log("Utworzenie aplikacji.");
    }

    @Override
    public void timer_run() {
        if (App.geti().size_changed) {
            App.geti().size_changed = false;
            //restart po zmianie romziaru lub orientacji ekranu
            App.geti().smyradlo.x = 0;
            App.geti().smyradlo.y = canvas.h * 7 / 12;
        }
        App.Smyradlo smyradlo = App.geti().smyradlo;
        synchronized (canvas) {
            //ustalenie prędkości na podstawie akcelerometru
            smyradlo.Vx += -sensormaster.get_ax() * smyradlo.speed_acc_factor;
            smyradlo.Vy += sensormaster.get_ay() * smyradlo.speed_acc_factor;
            //prędkość maksymalna
            if(smyradlo.Vx > smyradlo.V_max) smyradlo.Vx = smyradlo.V_max;
            if(smyradlo.Vx < -smyradlo.V_max) smyradlo.Vx = -smyradlo.V_max;
            if(smyradlo.Vy > smyradlo.V_max) smyradlo.Vy = smyradlo.V_max;
            if(smyradlo.Vy < -smyradlo.V_max) smyradlo.Vy = -smyradlo.V_max;
            //przesunięcie obrazka
            smyradlo.x += smyradlo.Vx;
            smyradlo.y += smyradlo.Vy;
            if (smyradlo.x < -canvas.w) smyradlo.x += 2 * canvas.w;
            if (smyradlo.x > canvas.w) smyradlo.x -= 2 * canvas.w;
            if (smyradlo.y < -canvas.h) smyradlo.y += 2 * canvas.h;
            if (smyradlo.y > canvas.h) smyradlo.y -= 2 * canvas.h;
            canvas.invalidate();
        }
    }

    public void quit() {
        App.log("Zamykanie aplikacji");
        timer.stop();
        activity.finish();
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

}
