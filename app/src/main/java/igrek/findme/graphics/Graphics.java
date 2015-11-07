package igrek.findme.graphics;

import android.content.Context;
import igrek.findme.logic.Engine;
import igrek.findme.logic.Types;
import igrek.findme.settings.App;
import igrek.findme.settings.Config;
import igrek.findme.system.Output;

public class Graphics extends CanvasView {
    private App app;
    private Config config;

    public Graphics(Context context, Engine engine) {
        super(context, engine);
        app = App.geti();
        config = Config.geti();
    }

    @Override
    public void repaint() {
        drawBackground();
        setFontSize(config.fontsize);
        drawSignature();
        if(app.mode == Types.AppMode.MENU){
            drawMenu();
        }else if(app.mode == Types.AppMode.COMPASS){
            drawCompass();
        }
        drawButtons();
        drawEcho();
    }

    public void drawMenu(){


    }

    public void drawCompass(){
        setColor("006000");
        drawLine(0, h / 2, w, h / 2);
        drawLine(w / 2, 0, w / 2, h);
        //azymut
        setColor("909090");
        float theta = (float) Math.asin(engine.sensors.getValue(2)) * 2;
        drawText("Azymut: " + theta * 180 / Math.PI, 0, 0);
        //wskazówka
        setColor("00a0f0");
        float scale = config.compass_scale * h / 2;
        float cx = (float) (w / 2 + scale * Math.sin(theta));
        float cy = (float) (h / 2 - scale * Math.cos(theta));
        drawLine(w / 2, h / 2, cx, cy);
        outlineCircle(cx, cy, 10, 1);
        drawText("N", cx, cy, Types.Align.CENTER);
    }

    void drawBackground() {
        setColor(config.colors.background);
        clearScreen();
    }

    public void drawSignature() {
        setColor(config.colors.signature);
        setFont(Types.Font.FONT_MONOSPACE);
        drawText("Igrek", w, h, Types.Align.BOTTOM_RIGHT);
    }

    public void drawEcho() {
        setColor(Config.geti().colors.text);
        setFont();
        Output.echoTryClear();
        drawTextMultiline(Output.echos, 0, 0, Config.geti().lineheight);
    }

    public void drawButtons() {
        for (Buttons.Button b : engine.buttons.buttons) {
            draw(b);
        }
    }

    public void draw(Buttons.Button b) {
        if (!b.active) return;
        setColor(config.colors.buttons_background, config.colors.buttons_alpha);
        fillRect(b.x, b.y, b.x + b.w, b.y + b.h);
        setColor(config.colors.buttons_outline, config.colors.buttons_alpha);
        outlineRect(b.x, b.y, b.x + b.w, b.y + b.h, 2);
        setColor(config.colors.buttons_text, config.colors.buttons_alpha);
        paint.setTextSize(config.buttons_fontsize);
        setFont();
        drawText(b.text, b.x + b.w / 2, b.y + b.h / 2, Types.Align.CENTER);
        paint.setTextSize(config.fontsize);
    }
}
