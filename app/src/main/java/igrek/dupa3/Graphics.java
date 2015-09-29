package igrek.dupa3;

import android.content.Context;
import android.hardware.Sensor;

public class Graphics extends CanvasView {
    App app;
    Config config;

    public Graphics(Context context, TouchPanel engine) {
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
            float ypos = 0;
            setColor("20ff50");
            Buttons.Button last;
            for (Sensor sensor : engine.sensormaster.msensorList) {
                //nazwa sensora
                int name_w = getTextWidth(sensor.getName());
                drawText(sensor.getName(), 0, ypos);
                ypos += config.lineheight * 1.1f;
                if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    last = engine.buttons.find("acc_x").setPos(0, ypos, 0, 0, Buttons.Align.HADJUST);
                    last = engine.buttons.find("acc_y").setPos(last.x + last.w + config.button_space, ypos, 0, 0, Buttons.Align.HADJUST);
                    last = engine.buttons.find("acc_z").setPos(last.x + last.w + config.button_space, ypos, 0, 0, Buttons.Align.HADJUST);
                    last = engine.buttons.find("acc_w").setPos(last.x + last.w + config.button_space, ypos, 0, 0, Buttons.Align.HADJUST);
                    last = engine.buttons.find("spirit_level").setPos(last.x + last.w + config.button_space, ypos, 0, 0, Buttons.Align.HADJUST);
                } else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    last = engine.buttons.find("mag_x").setPos(0, ypos, 0, 0, Buttons.Align.HADJUST);
                    last = engine.buttons.find("mag_y").setPos(last.x + last.w + config.button_space, ypos, 0, 0, Buttons.Align.HADJUST);
                    last = engine.buttons.find("mag_z").setPos(last.x + last.w + config.button_space, ypos, 0, 0, Buttons.Align.HADJUST);
                    last = engine.buttons.find("mag_w").setPos(last.x + last.w + config.button_space, ypos, 0, 0, Buttons.Align.HADJUST);
                } else if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                    last = engine.buttons.find("rot_x").setPos(0, ypos, 0, 0, Buttons.Align.HADJUST);
                    last = engine.buttons.find("rot_y").setPos(last.x + last.w + config.button_space, ypos, 0, 0, Buttons.Align.HADJUST);
                    last = engine.buttons.find("rot_z").setPos(last.x + last.w + config.button_space, ypos, 0, 0, Buttons.Align.HADJUST);
                    last = engine.buttons.find("compass").setPos(last.x + last.w + config.button_space, ypos, 0, 0, Buttons.Align.HADJUST);
                }
                ypos += config.lineheight * 1.6f;
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
                setColor("005000");
                drawText(engine.sensormaster.get_units(), 0, 0);
                for (int i = 0; i < config.plot_sections + 2; i++) {
                    //linia pomocnicza
                    setColor("002000");
                    float ypos = (1 - (float) i / 8) * plot_offset;
                    if (i > 0) drawLine(0, ypos, w, ypos);
                    //wartość pomocnicza
                    setColor("00ff00");
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
                float ypos = plot_offset;
                drawText(engine.sensormaster.get_name() + " = " + engine.sensormaster.get_value() + engine.sensormaster.get_units(), 0, ypos);
                ypos += config.lineheight;
                drawText("Średnia: " + App.round(engine.srednia(tab), 5) + engine.sensormaster.get_units(), 0, ypos);
                ypos += config.lineheight;
                drawText("Odch. std.: " + App.round(engine.odchylenie(tab), 5) + engine.sensormaster.get_units(), 0, ypos);
                ypos += config.lineheight;
                drawText("Maximum: " + max + engine.sensormaster.get_units(), 0, ypos);
                ypos += config.lineheight;
                drawText("Minimum: " + min + engine.sensormaster.get_units(), 0, ypos);
            }
        } else if (app.mode == App.Mode.COMPASS) {
            setColor("006000");
            drawLine(0, h / 2, w, h / 2);
            drawLine(w / 2, 0, w / 2, h);
            //azymut
            setColor("909090");
            float theta = (float) Math.asin(engine.sensormaster.get_value(2)) * 2;
            drawText("Azymut: " + theta * 180 / Math.PI, 0, 0);
            //wskazówka
            setColor("00a0f0");
            float scale = config.compass_scale * h / 2;
            float cx = (float) (w / 2 - scale * Math.cos(theta));
            float cy = (float) (h / 2 - scale * Math.sin(theta));
            drawLine(w / 2, h / 2, cx, cy);
            outlineCircle(cx, cy, 10, 1);
            drawText("N", cx, cy, Align.CENTER);

        } else if (app.mode == App.Mode.SPIRIT_LEVEL) {
            setColor("006000");
            drawLine(0, h / 2, w, h / 2);
            drawLine(w / 2, 0, w / 2, h);
            drawText("Y", 0, h / 2, Align.BOTTOM | Align.LEFT);
            drawText("X", w / 2, 0, Align.TOP | Align.RIGHT);
            //wskazówka
            setColor("00a0f0");
            float scale = config.spirit_level_scale * w / 2;
            float cx = w / 2 + engine.sensormaster.get_value(1) * scale;
            float cy = h / 2 + engine.sensormaster.get_value(0) * scale;
            drawLine(w / 2, h / 2, cx, cy);
            outlineCircle(cx, cy, 10, 1);
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
        paint.setTextSize(config.buttons_fontsize);
        drawText(b.text, b.x + b.w / 2, b.y + b.h / 2, Align.CENTER);
        paint.setTextSize(config.fontsize);
    }
}