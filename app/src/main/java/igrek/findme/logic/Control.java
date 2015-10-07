package igrek.findme.logic;

import igrek.findme.settings.App;
import igrek.findme.settings.Config;

public class Control {
    App app;
    Config config;
    Engine engine;

    public Control(Engine engine) {
        this.engine = engine;
        app = App.geti();
        config = Config.geti();
    }

    public static boolean isPointInCircle(float px, float py, float circlex, float circley, float radius) {
        float square_sum = (px - circlex) * (px - circlex) + (py - circley) * (py - circley);
        return square_sum <= radius * radius;
    }

    public static boolean isPointAboveLine(float px, float py, float line_a, float line_b) {
        //dla współrzędnej x punktu punkt na prostej ma współrzędną
        float line_y = line_a * px + line_b;
        return py > line_y;
    }

    public static boolean isAngleInRange(float angle, float range1, float range2) {
        //normalizacja zakresów do liczb: [0, 360)
        while (range1 < 0) range1 += 360;
        while (range2 < 0) range2 += 360;
        while (range1 >= 360) range1 -= 360;
        while (range2 >= 360) range2 -= 360;
        //normalizacja sprawdzanego kąta
        while (angle < 0) angle += 360;
        while (angle >= 360) angle -= 360;
        if (range1 <= range2) { //przedział nie przechodzi przez zero
            return range1 <= angle && angle <= range2;
        } else {
            //zakres przechodzi przez zero (range2 < range1)
            return angle >= range1 || angle <= range2;
        }
    }

    public void executeEvent(Types.ControlEvent event) {
        if (event == Types.ControlEvent.BACK) engine.quit();

    }
}
