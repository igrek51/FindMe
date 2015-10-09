package igrek.findme.modules;

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
    float values[] = new float[3];
    List<Sensor> msensorList;
    public final int sensor_type = Sensor.TYPE_ACCELEROMETER;

    public SensorMaster(Activity activity) {
        this.activity = activity;
        sensorManager = (SensorManager) activity.getSystemService(this.activity.SENSOR_SERVICE);
        if (sensorManager == null) {
            Output.errorCritical("Błąd usługi sensorów");
            return;
        }
        msensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        list_sensors();
        //szukanie wybranego sensora
        sensor = sensorManager.getDefaultSensor(sensor_type);
        if (sensor == null) {
            Output.error("Nie znaleziono sensora");
            return;
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
        if (event.sensor.getType() == sensor_type) {
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
