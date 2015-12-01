package igrek.findme.logic;

import igrek.findme.settings.App;
import igrek.findme.settings.Config;
import igrek.findme.system.Output;

public class TouchPanel {
    int w, h; //rozmiary ekranu dotykowego
    float start_x, start_y; //punkt początkowy smyrania
    int dpi;
    public static final float INCH = 2.54f; //1 cal [cm]
    App app;
    Engine engine;

    public TouchPanel(Engine engine, int w, int h) {
        this.engine = engine;
        this.w = w;
        this.h = h;
        app = App.geti();
        dpi = engine.activity.getResources().getDisplayMetrics().densityDpi;
        Output.log("DPI urządzenia: " + dpi);
    }

    public float pixels_to_cm(float pixels) {
        return pixels / dpi * INCH;
    }

    public float cm_to_pixels(float cm) {
        return cm / INCH * dpi;
    }

    public float partialW(float partOfWidth){
        return w * partOfWidth;
    }

    public boolean touch_down(float touch_x, float touch_y) {
        start_x = touch_x;
        start_y = touch_y;
        //controlAbsolute(touch_x, touch_y);
        return true;
        //return false;
    }

    public boolean touch_move(float touch_x, float touch_y) {
        return false;
    }

    public boolean touch_up(float touch_x, float touch_y) {
        float rtx = touch_x / w;
        float rty = touch_y / h;
//        control_realtive(touch_x, touch_y);
//        return true;
        return false;
    }

    public void controlAbsolute(float touch_x, float touch_y) throws Exception {
        //sprawdzenie kółka w środku
        if (Control.isPointInCircle(touch_x, touch_y, w / 2, h / 2, Config.Touch.center_radius * w)) {
            engine.control.executeEvent(Types.ControlEvent.OK);
            return;
        }
        float a = (float) h / w;
        //lewa ćwiartka
        if (!Control.isPointAboveLine(touch_x, touch_y, -a, h) && Control.isPointAboveLine(touch_x, touch_y, a, 0)) {
            engine.control.executeEvent(Types.ControlEvent.LEFT);
        }
        //prawa ćwiartka
        else if (Control.isPointAboveLine(touch_x, touch_y, -a, h) && !Control.isPointAboveLine(touch_x, touch_y, a, 0)) {
            engine.control.executeEvent(Types.ControlEvent.RIGHT);
        }
        //górna ćwiartka
        else if (!Control.isPointAboveLine(touch_x, touch_y, -a, h) && !Control.isPointAboveLine(touch_x, touch_y, a, 0)) {
            engine.control.executeEvent(Types.ControlEvent.UP);
        }
        //dolna ćwiartka
        else if (Control.isPointAboveLine(touch_x, touch_y, -a, h) && Control.isPointAboveLine(touch_x, touch_y, a, 0)) {
            engine.control.executeEvent(Types.ControlEvent.DOWN);
        }
    }

    public void controlRealtive(float touch_x, float touch_y) throws Exception {
        float distance = (float) Math.sqrt((touch_x - start_x) * (touch_x - start_x) + (touch_y - start_y) * (touch_y - start_y));
        if (distance <= cm_to_pixels(Config.Touch.max_assert_distance)) {
            engine.control.executeEvent(Types.ControlEvent.OK);
            return;
        }
        if (distance < cm_to_pixels(Config.Touch.min_relative_distance)) {
            Output.log("Touchpanel: Sterowanie względne: przemieszczenie za krótkie - brak akcji");
            return;
        }
        //obliczenie kąta względem osi X+
        float angle = (float) (Math.atan2(touch_y - start_y, touch_x - start_x) * 180 / Math.PI);
        //w prawo: 0
        if (Control.isAngleInRange(angle, 0 - Config.Touch.max_angle_bias, 0 + Config.Touch.max_angle_bias)) {
            engine.control.executeEvent(Types.ControlEvent.RIGHT);
        }
        //w dół: 90
        else if (Control.isAngleInRange(angle, 90 - Config.Touch.max_angle_bias, 90 + Config.Touch.max_angle_bias)) {
            engine.control.executeEvent(Types.ControlEvent.DOWN);
        }
        //w lewo: 180
        else if (Control.isAngleInRange(angle, 180 - Config.Touch.max_angle_bias, 180 + Config.Touch.max_angle_bias)) {
            engine.control.executeEvent(Types.ControlEvent.LEFT);
        }
        //w górę: 270
        else if (Control.isAngleInRange(angle, 270 - Config.Touch.max_angle_bias, 270 + Config.Touch.max_angle_bias)) {
            engine.control.executeEvent(Types.ControlEvent.UP);
        } else {
            Output.log("Touchpanel: Sterowanie względne: kąt zbyt odchylony - brak akcji");
        }
    }

}
