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
        sensorManager = (SensorManager) activity.getSystemService(this.activity.SENSOR_SERVICE);
        if (sensorManager == null) {
            App.geti().errorCritical("Błąd usługi sensorów");
            return;
        }
        msensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        list_sensors();
    }

    public void select_sensor() {
        if (App.geti().sensor_type == 0) {
            App.geti().error("Brak wybranego sensora");
            return;
        }
        //szukanie wybranego sensora
        sensor = sensorManager.getDefaultSensor(App.geti().sensor_type);
        if (sensor == null) {
            App.geti().error("Nie znaleziono sensora");
            return;
        }
        register();
    }

    public void list_sensors() {
        App.log("Dostępne sensory: " + msensorList.size());
        // Print each Sensor available using sSensList as the String to be printed
        for (Sensor sensorZListy : msensorList) {
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

    public float get_resolution(){
        return sensor.getResolution();
    }

    public String get_units() {
        if (App.geti().sensor_type == Sensor.TYPE_ACCELEROMETER) {
            return " m/s^2";
        } else if (App.geti().sensor_type == Sensor.TYPE_MAGNETIC_FIELD) {
            return " uT";
        } else if (App.geti().sensor_type == Sensor.TYPE_ROTATION_VECTOR) {
            return "";
        } else if (App.geti().sensor_type == Sensor.TYPE_LIGHT) {
            return " lux";
        } else if (App.geti().sensor_type == Sensor.TYPE_PROXIMITY) {
            return " cm";
        }else if (App.geti().sensor_type == Sensor.TYPE_ORIENTATION) {
            return " degrees";
        }
        return "";
    }

    public String get_name() {
        String name1 = "", name2 = "";
        if (App.geti().sensor_type == Sensor.TYPE_ACCELEROMETER) {
            name1 = "Przyspieszenie ";
            if (App.geti().sensor_axis == 1) {
                name2 = "X";
            } else if (App.geti().sensor_axis == 2) {
                name2 = "Y";
            } else if (App.geti().sensor_axis == 3) {
                name2 = "Z";
            } else if (App.geti().sensor_axis == 7) {
                name2 = "wypadkowe";
            }
        } else if (App.geti().sensor_type == Sensor.TYPE_MAGNETIC_FIELD) {
            name1 = "Pole magnetyczne ";
            if (App.geti().sensor_axis == 1) {
                name2 = "X";
            } else if (App.geti().sensor_axis == 2) {
                name2 = "Y";
            } else if (App.geti().sensor_axis == 3) {
                name2 = "Z";
            } else if (App.geti().sensor_axis == 7) {
                name2 = "wypadkowe";
            }
        } else if (App.geti().sensor_type == Sensor.TYPE_ROTATION_VECTOR) {
            name1 = "Wektor rotacji ";
            if (App.geti().sensor_axis == 1) {
                name2 = "x*sin(th/2)";
            } else if (App.geti().sensor_axis == 2) {
                name2 = "y*sin(th/2)";
            } else if (App.geti().sensor_axis == 3) {
                name2 = "z*sin(th/2)";
            }
        } else if (App.geti().sensor_type == Sensor.TYPE_LIGHT) {
            name1 = "Oświetlenie otoczenia";
        } else if (App.geti().sensor_type == Sensor.TYPE_PROXIMITY) {
            name1 = "Zbliżenie";
        }else if (App.geti().sensor_type == Sensor.TYPE_ORIENTATION) {
            name1 = "Orientacja: ";
            if (App.geti().sensor_axis == 1) {
                name2 = "Azimuth";
            } else if (App.geti().sensor_axis == 2) {
                name2 = "Pitch";
            } else if (App.geti().sensor_axis == 3) {
                name2 = "Roll";
            }
        }
        return name1 + name2;
    }
}
