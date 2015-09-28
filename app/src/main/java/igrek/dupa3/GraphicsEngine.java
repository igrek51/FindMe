package igrek.dupa3;

import android.content.Context;
import android.hardware.Sensor;

public class GraphicsEngine extends CanvasView {
    App app;
    Config config;

    public GraphicsEngine(Context context, TouchPanel engine) {
        super(context, engine);
        app = App.geti();
        config = Config.geti();
    }

    @Override
    public void repaint() {
        clearCanvas("000000");
        //podpis
        paint.setTextSize(config.fontsize);
        setColor("252525");
        drawText("Igrek", w, 0, Align.RIGHT);
        //menu
        if (app.mode == App.Mode.MENU) {
            for (Sensor sensor : engine.sensormaster.msensorList) {
                if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                }else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

                }

            }
        } else if (app.mode == App.Mode.PLOT) {
            //układ współrzędnych
            setColor("006000");
            float plot_offset = config.plot_part * h;
            drawLine(0, plot_offset, w, plot_offset);
            drawLine(1, 0, 1, plot_offset);
            if (app.plot.recorded > 1) {
                //skala
                float tab[] = app.plot.buffer;
                //wyznaczenie minimum i maksimum - prawdziwe
                float min = tab[config.plot_buffer_size - 1];
                float max = tab[config.plot_buffer_size - 1];
                for (int i = config.plot_buffer_size - app.plot.recorded; i < config.plot_buffer_size; i++) {
                    if (tab[i] > max) max = tab[i];
                    if (tab[i] < min) min = tab[i];
                }
                float roznica = max - min;
                if (roznica < 0.5) roznica = 0.5f;
                float max2 = max + roznica / config.plot_sections; //minimum i maksimum na wykresie
                float min2 = min - roznica / config.plot_sections;
                float skala = (plot_offset) / (max2 - min2);
                //os pionowa
                setColor("006000");
                drawText(engine.sensormaster.get_units(), 0, 0);
                for (int i = 0; i < config.plot_sections + 2; i++) {
                    //linia pomocnicza
                    setColor("002000");
                    float ypos = (1 - (float) i / 8) * plot_offset;
                    if (i > 0) drawLine(0, ypos, w, ypos);
                    //wartość pomocnicza
                    setColor("009000");
                    float numer_pomocniczy = App.round((float) i / (config.plot_sections + 2) * (max2 - min2) + min2, 3);
                    drawText("" + numer_pomocniczy, 2, ypos, Align.VCENTER);
                }
                //punkty
                setColor("00c000");
                for (int i = config.plot_buffer_size - app.plot.recorded; i < config.plot_buffer_size - 1; i++) {
                    float x1 = (float) (i * w) / config.plot_buffer_size; //skalowanie wzdłuż osi X
                    float x2 = (float) (i + 1) * w / config.plot_buffer_size;
                    float y1 = (float) ((tab[i] - min2) * skala); //skalowanie wzdłuż osi Y
                    float y2 = (float) ((tab[i + 1] - min2) * skala);
                    drawLine(x1, plot_offset - y1, x2, plot_offset - y2);
                }
                setColor("407040");
                float ypos = plot_offset + 25;
                drawText("Wartość " + engine.sensormaster.get_name() + ": " + engine.sensormaster.get_value() + engine.sensormaster.get_units(), 0, ypos);
                ypos += config.lineheight;
                drawText("Średnia arytmetyczna: " + App.round(engine.srednia(tab), 5) + engine.sensormaster.get_units(), 0, ypos);
                ypos += config.lineheight;
                drawText("Odchylenie standardowe: " + App.round(engine.odchylenie(tab), 5) + engine.sensormaster.get_units(), 0, ypos);
                ypos += config.lineheight;
                drawText("Maximum: " + max + engine.sensormaster.get_units(), 0, ypos);
                ypos += config.lineheight;
                drawText("Minimum: " + min + engine.sensormaster.get_units(), 0, ypos);
            }
        } else if (app.mode == App.Mode.COMPASS) {

        } else if (app.mode == App.Mode.SPIRIT_LEVEL) {
            /*
            setColor("ffffff");
            drawText("X: " + axisx + config.sensor_units, 0, 0);
            drawText("Y: " + axisy + config.sensor_units, 0, 15);
            drawText("Z: " + axisz + config.sensor_units, 0, 30);
            drawText("W: " + a_wyp + config.sensor_units, 0, 45);
            drawText("Czas: " + System.currentTimeMillis(), 0, 60);
            drawText("Liczba punktow: " + app.plot.recorded, 0, 75);
            drawText("Rejestrowanie: " + (app.plot.recording ? "tak" : "nie"), 0, 90);
            //wektory
            setColor("006000");
            drawLine(0, h / 2, w, h / 2);
            drawLine(w / 2, 0, w / 2, h);
            drawText("X", 0, h / 2, Align.BOTTOM | Align.LEFT);
            drawText("Y", w / 2, 0, Align.BOTTOM | Align.RIGHT);
            setColor("00c0c0");
            float scale = config.indicator_scale * w / 2;
            drawLine(w / 2, h / 2, w / 2 + axisy * scale, h / 2 + axisx * scale);
            */
        }
        drawButtons();
        drawEcho();
    }

    public void drawEcho() {
        setColor("00ff00");
        drawText(app.echo_show(), 0, h, Align.BOTTOM);
    }

    public void drawButtons() {
        for (Buttons.Button b : engine.buttons.buttons) {
            drawButton(b);
        }
    }

    public void drawButton(Buttons.Button b) {
        if (!b.active) return;
        setColor("303030");
        fillRect(b.x, b.y, b.x + b.w, b.y + b.h);
        setColor("606060");
        outlineRect(b.x, b.y, b.x + b.w, b.y + b.h, 2);
        setColor("f0f0f0");
        drawText(b.text, b.x + b.w / 2, b.y + b.h / 2, Align.CENTER);
    }
}
