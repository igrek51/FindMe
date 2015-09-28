package igrek.dupa3;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.List;

public class SensorMaster implements SensorEventListener {
    Activity activity = null;
    SensorManager sensorManager = null;
    Sensor sensor = null;
    float values[] = new float[6];
    List<Sensor> msensorList;

    public SensorMaster(Activity activity) {
        this.activity = activity;
        sensorManager = (SensorManager) activity.getSystemService(activity.SENSOR_SERVICE);
        if (sensorManager == null) {
            App.errorCritical("Błąd usługi sensorów");
            return;
        }
        msensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        list_sensors();
    }

    public void select_sensor() {
        if (App.geti().sensor_type == 0) {
            App.error("Brak wybranego sensora");
            return;
        }
        //szukanie wybranego sensora
        sensor = sensorManager.getDefaultSensor(App.geti().sensor_type);
        if (sensor == null) {
            App.error("Nie znaleziono sensora");
            return;
        }
        register();
    }

    public void list_sensors() {
        App.log("Dostępne sensory: " + msensorList.size());
        // Print each Sensor available using sSensList as the String to be printed
        for (Sensor sensorZListy : msensorList) {
            if (sensorZListy.getType() == Sensor.TYPE_ACCELEROMETER) {
                App.geti().sensor_accelerometer_enabled = true;
            }else if (sensorZListy.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                App.geti().sensor_magnetic_enabled = true;
            }
            App.log("Sensor: " + sensorZListy.getName() + ", typ: " + sensorZListy.getType() + ", moc: " + sensorZListy.getPower() + ", rozdzielczosc: " + sensorZListy.getResolution());
        }
    }

    public void register() {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == App.geti().sensor_type) {
            for (int i = 0; i < event.values.length; i++) {
                values[i] = event.values[i];
            }
        }
    }

    public float get_value(int number) {
        return values[number];
    }

    public float get_value() {
        if (App.geti().sensor_axis >= 7) {
            return get_w();
        } else {
            return get_value(App.geti().sensor_axis - 1);
        }
    }

    public float get_w() {
        return (float) Math.sqrt((double) (values[0] * values[0] + values[1] * values[1] + values[2] * values[2]));
    }

    public String get_units() {
        return " dupa";
    }

    public String get_name() {
        return " dupa";
    }
}
