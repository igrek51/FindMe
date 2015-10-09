package igrek.findme.graphics;

import android.content.Context;

import igrek.findme.logic.Engine;
import igrek.findme.logic.Types;
import igrek.findme.settings.App;
import igrek.findme.settings.Config;
import igrek.findme.modules.Output;

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
        drawButtons();
        drawEcho();
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
