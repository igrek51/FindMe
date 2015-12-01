package igrek.findme.managers;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.List;

import igrek.findme.system.Output;

public class Sensors implements SensorEventListener {
    Activity activity = null;
    SensorManager sensorManager = null;
    Sensor sensor = null;
    float values[] = new float[3];
    List<Sensor> msensorList;
    //TODO: odczyt azymutu (i siły sygnału - nachylenia) z magnetometru
    public final int sensor_type = Sensor.TYPE_ROTATION_VECTOR;
    public final int sensor_type2 = Sensor.TYPE_GAME_ROTATION_VECTOR;

    public Sensors(Activity activity) throws Exception {
        this.activity = activity;
        sensorManager = (SensorManager) activity.getSystemService(this.activity.SENSOR_SERVICE);
        if (sensorManager == null) {
            Output.errorCritical("Błąd usługi sensorów");
        }
        msensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        list_sensors();
        //szukanie wybranego sensora
        sensor = sensorManager.getDefaultSensor(sensor_type);
        if (sensor == null) {
            sensor = sensorManager.getDefaultSensor(sensor_type2);
            if (sensor == null) {
                Output.errorthrow("Nie znaleziono sensora");
            }
        }
    }

    public void list_sensors() {
        Output.log("Dostępne sensory: " + msensorList.size());
        // Print each Sensor available using sSensList as the String to be printed
        for (Sensor sensorZListy : msensorList) {
            Output.log("Sensor: " + sensorZListy.getName() + ", typ: " + sensorZListy.getType() + ", moc: " + sensorZListy.getPower() + ", rozdzielczosc: " + sensorZListy.getResolution());
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
        if (event.sensor.getType() == sensor_type || event.sensor.getType() == sensor_type2) {
            for (int i = 0; i < event.values.length; i++) {
                values[i] = event.values[i];
            }
        }
    }

    public float getValue(int number) {
        return values[number];
    }

    public float getW() {
        return (float) Math.sqrt((double) (values[0] * values[0] + values[1] * values[1] + values[2] * values[2]));
    }

    public float getResolution() {
        return sensor.getResolution();
    }
}
