package igrek.findme.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import igrek.findme.logic.Types;
import igrek.findme.logic.Engine;
import igrek.findme.settings.Config;
import igrek.findme.system.Output;

public class CanvasView extends View {
    public int w, h;
    Paint paint;
    private Canvas canvas = null;
    Engine engine;

    public CanvasView(Context context, Engine engine) {
        super(context);
        this.engine = engine;
        paint = new Paint();
    }

    //metoda odrysowująca ekran do nadpisania
    public void repaint() { }

    //obsługa zdarzeń ekranu dotykowego do zaimplementowania
    public interface TouchPanel {
        void touch_down(float touch_x, float touch_y);

        void touch_move(float touch_x, float touch_y);

        void touch_up(float touch_x, float touch_y);

        void resize_event();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = getWidth();
        this.h = getHeight();
        engine.resize_event();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        repaint();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touch_x = event.getX();
        float touch_y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                engine.touch_down(touch_x, touch_y);
                break;
            case MotionEvent.ACTION_MOVE:
                engine.touch_move(touch_x, touch_y);
                break;
            case MotionEvent.ACTION_UP:
                engine.touch_up(touch_x, touch_y);
                break;
        }
        return true;
    }

    //pomocnicze funkcje rysujące
    private Rect textBounds = new Rect();

    public void drawText(String text, float cx, float cy, int align) {
        //domyślne wartości
        if ((align & 0x0f) == 0) align |= Types.Align.LEFT;
        if ((align & 0xf0) == 0) align |= Types.Align.TOP;
        if (Types.isFlagSet(align, Types.Align.LEFT)) {
            paint.setTextAlign(Paint.Align.LEFT);
        } else if (Types.isFlagSet(align, Types.Align.HCENTER)) {
            paint.setTextAlign(Paint.Align.CENTER);
        } else { //right
            paint.setTextAlign(Paint.Align.RIGHT);
        }
        paint.getTextBounds(text, 0, text.length(), textBounds);
        float y_pos = cy - (paint.descent() + paint.ascent()) / 2;
        if (Types.isFlagSet(align, Types.Align.TOP)) {
            y_pos += textBounds.height() / 2;
        } else if (Types.isFlagSet(align, Types.Align.BOTTOM)) {
            y_pos -= textBounds.height() / 2;
        }
        canvas.drawText(text, cx, y_pos, paint);
    }

    public void drawText(String text, float cx, float cy) {
        drawText(text, cx, cy, 0);
    }

    public void drawTextMultiline(String text, float cx, float cy, float lineheight, int align) {
        //wyznaczenie ilości linii
        int lines = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') lines++;
        }
        //domyślne wartości
        if ((align & 0x0f) == 0) align |= Types.Align.LEFT;
        if ((align & 0xf0) == 0) align |= Types.Align.TOP;
        //przesunięcie w osi y
        float offset_y;
        if (Types.isFlagSet(align, Types.Align.TOP)) {
            offset_y = cy;
        } else if (Types.isFlagSet(align, Types.Align.VCENTER)) {
            offset_y = cy - (lines - 1) * lineheight / 2;
        } else { //bottom
            offset_y = cy - (lines - 1) * lineheight;
        }
        //dla każdego wiersza
        for (int i = 0; i < lines; i++) {
            //szukanie \n
            int indexn = text.indexOf("\n");
            if (indexn == -1) indexn = text.length(); //nie było już \n
            //wycięcie wiersza
            String row_text = text.substring(0, indexn);
            if (indexn + 1 < text.length()) {
                text = text.substring(indexn + 1); //usunięcie wyciętego wiersza i \n
            }
            //narysowanie 1 wiersza
            drawText(row_text, cx, offset_y, align);
            offset_y += lineheight;
        }
    }

    public int getTextWidth(String text) {
        paint.getTextBounds(text, 0, text.length(), textBounds);
        return textBounds.width();
    }

    public void setFontSize(int textsize) {
        paint.setTextSize(textsize);
    }

    public void setFont(int fontface) {
        //domyślna rodzina
        if ((fontface & 0x0f) == 0) fontface |= Types.Font.FONT_DEFAULT;
        //domyślny styl
        if ((fontface & 0xf0) == 0) fontface |= Types.Font.FONT_NORMAL;
        Typeface family;
        if (Types.isFlagSet(fontface, Types.Font.FONT_MONOSPACE)) {
            family = Typeface.MONOSPACE;
        } else {
            family = Typeface.DEFAULT;
        }
        int style;
        if (Types.isFlagSet(fontface, Types.Font.FONT_BOLD)) {
            style = Typeface.BOLD;
        } else {
            style = Typeface.NORMAL;
        }
        paint.setTypeface(Typeface.create(family, style));
    }

    public void setFont() {
        setFont(0); //reset czcionki na zwykłą
    }

    public void setColor(String color) {
        if (color.length() > 0 && color.charAt(0) != '#') {
            color = "#" + color;
        }
        paint.setColor(Color.parseColor(color));
    }

    public void setColor(int color) {
        //jeśli kanał alpha jest zerowy (nie ustawiony) - ustaw na max
        if ((color & 0xff000000) == 0) color |= 0xff000000;
        paint.setColor(color);
    }

    public void setColor(int rgb, int alpha) {
        paint.setColor(rgb | (alpha << 24));
    }

    public void clearScreen() {
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);
    }

    public void clearScreen(String color) {
        setColor(color);
        clearScreen();
    }

    public void drawLine(float startx, float starty, float stopx, float stopy) {
        canvas.drawLine(startx, starty, stopx, stopy, paint);
    }

    public void fillCircle(float cx, float cy, float radius) {
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, radius, paint);
    }

    public void outlineCircle(float cx, float cy, float radius, float thickness) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(thickness);
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setStrokeWidth(0);
    }

    public void fillRect(float left, float top, float right, float bottom) {
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(left, top, right, bottom, paint);
    }

    public void fillRoundRect(float left, float top, float right, float bottom, float radius) {
        paint.setStyle(Paint.Style.FILL);
        RectF rectf = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rectf, radius, radius, paint);
    }

    public void fillRoundRectWH(float left, float top, float width, float height, float radius) {
        fillRoundRect(left, top, left + width, top + height, radius);
    }

    public void outlineRect(float left, float top, float right, float bottom, float thickness) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(thickness);
        canvas.drawRect(left, top, right, bottom, paint);
        paint.setStrokeWidth(0);
    }
}