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
    float ax = 0, ay = 0, az = 0;

    public SensorMaster(Activity activity){
        this.activity = activity;
        list_sensors();
        //szukanie akcelerometru
        sensor = sensorManager.getDefaultSensor(Config.geti().sensor_type);
    }

    public void list_sensors(){
        //Lista sensorów
        sensorManager = (SensorManager) activity.getSystemService(activity.SENSOR_SERVICE);
        // List of Sensors Available
        List<Sensor> msensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        // Print how may Sensors are there
        App.log("Dostępne sensory: " + msensorList.size());
        // Print each Sensor available using sSensList as the String to be printed
        for (Sensor sensorZListy : msensorList) {
            App.log("Sensor: " + sensorZListy.getName() + ", typ: " + sensorZListy.getType() + ", moc: " + sensorZListy.getPower() + ", rozdzielczosc: " + sensorZListy.getResolution());
        }
    }

    public void register(){
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister(){
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Config.geti().sensor_type) {
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];
        }
    }

    public float get_ax(){
        return ax;
    }
    public float get_ay(){
        return ay;
    }
    public float get_az(){
        return az;
    }
    public float get_a_resultant(){
        return (float)Math.sqrt((double)(ax*ax + ay*ay + az*az));
    }
}
