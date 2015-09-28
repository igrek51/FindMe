package igrek.dupa3;

import android.content.Context;

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

        if (app.state == 0) {
            float axisx = engine.sensormaster.get_x();
            float axisy = engine.sensormaster.get_y();
            float axisz = engine.sensormaster.get_z();
            float a_wyp = engine.sensormaster.get_w();
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
        } else if (app.state <= 4) {
            setColor("006000");
            drawLine(0, h / 2, w, h / 2);
            drawLine(1, 0, 1, h / 2);
            double number = 0;
            if (app.plot.recorded > 1) {
                //skala
                double tab[] = null;
                if (app.state == 1) tab = app.plot.ax;
                if (app.state == 2) tab = app.plot.ay;
                if (app.state == 3) tab = app.plot.az;
                if (app.state == 4) tab = app.plot.aw;
                double min = tab[config.plot_buffer_size - 1], max = tab[config.plot_buffer_size - 1];
                for (int i = config.plot_buffer_size - app.plot.recorded; i < config.plot_buffer_size; i++) {
                    if (tab[i] > max) max = tab[i];
                    if (tab[i] < min) min = tab[i];
                }
                double roznica = max - min;
                if (roznica < 0.8) roznica = 0.8;
                max += roznica / 6;
                min -= roznica / 6;
                double skala = (h / 2) / (max - min);
                //os pionowa
                setColor("006000");
                drawText(config.sensor_units, 0, 0);
                for (int i = 0; i < 8; i++) {
                    setColor("002000");
                    if (i > 0)
                        drawLine(0, (int) ((1 - ((double) i) / 8) * h / 2), w, (int) ((1 - ((double) i) / 8) * h / 2));
                    setColor("009000");
                    number = (float) Math.floor((((double) i) * h / 16 / skala + min) * 1000) / 1000;
                    drawText("" + number, 2, (int) ((1 - ((double) i) / 8) * h / 2) - 8);
                }
                //punkty
                setColor("00c000");
                for (int i = config.plot_buffer_size - app.plot.recorded; i < config.plot_buffer_size - 1; i++) {
                    int x1 = (i * w / config.plot_buffer_size); //skalowanie wzdłuż osi X
                    if (x1 > w) break;
                    int x2 = ((i + 1) * w / config.plot_buffer_size);
                    int y1 = (int) ((tab[i] - min) * skala); //skalowanie wzdłuż osi Y
                    int y2 = (int) ((tab[i + 1] - min) * skala);
                    drawLine(x1, h / 2 - y1, x2, h / 2 - y2);
                }
                setColor("407040");
                number = Math.floor(engine.srednia(tab) * 1000) / 1000;
                drawText("Średnia arytmetyczna: " + number + config.sensor_units, 0, h / 2 + 25);
                number = Math.floor(engine.odchylenie(tab) * 100000) / 100000;
                drawText("Odchylenie standardowe: " + number + config.sensor_units, 0, h / 2 + 40);
                drawText("Maximum: " + engine.max(tab) + config.sensor_units, 0, h / 2 + 55);
                drawText("Minimum: " + engine.min(tab) + config.sensor_units, 0, h / 2 + 70);
            }
            setColor("409090");
            String txt = "";
            if (app.state == 1) {
                txt = "x";
                number = Math.floor(engine.sensormaster.get_x() * 100000) / 100000;
            }
            if (app.state == 2) {
                txt = "y";
                number = Math.floor(engine.sensormaster.get_y() * 100000) / 100000;
            }
            if (app.state == 3) {
                txt = "z";
                number = Math.floor(engine.sensormaster.get_z() * 100000) / 100000;
            }
            if (app.state == 4) {
                txt = "w";
                number = Math.floor(engine.sensormaster.get_w() * 100000) / 100000;
            }
            drawText(txt + ": " + number + config.sensor_units, 0, h / 2 + 10);
        }
        drawButtons();
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
        setColor("404040");
        fillRect(b.x, b.y, b.x + b.w, b.y + b.h);
        setColor("808080");
        outlineRect(b.x, b.y, b.x + b.w, b.y + b.h, 2);
        setColor("f0f0f0");
        drawText(b.text, b.x + b.w / 2, b.y + b.h / 2, Align.CENTER);
    }
}
