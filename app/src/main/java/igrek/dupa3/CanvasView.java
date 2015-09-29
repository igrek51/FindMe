package igrek.dupa3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

public class CanvasView extends View {
    public int w, h;
    Paint paint;
    Canvas canvas = null;
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
    Rect textBounds = new Rect();

    public class Align {
        static final int LEFT = 0x01;
        static final int RIGHT = 0x02;
        static final int HCENTER = 0x04;
        static final int TOP = 0x10;
        static final int BOTTOM = 0x20;
        static final int VCENTER = 0x40;
        static final int CENTER = HCENTER | VCENTER;
    }

    public void drawText(String text, float cx, float cy, int align) {
        //domyślne wartości
        if ((align & 0x0f) == 0) align |= Align.LEFT;
        if ((align & 0xf0) == 0) align |= Align.TOP;
        if (App.isFlagSet(align, Align.LEFT)) {
            paint.setTextAlign(Paint.Align.LEFT);
        } else if (App.isFlagSet(align, Align.HCENTER)) {
            paint.setTextAlign(Paint.Align.CENTER);
        } else { //right
            paint.setTextAlign(Paint.Align.RIGHT);
        }
        paint.getTextBounds(text, 0, text.length(), textBounds);
        float y_pos = cy - (paint.descent() + paint.ascent()) / 2;
        if (App.isFlagSet(align, Align.TOP)) {
            y_pos += textBounds.height() / 2;
        } else if (App.isFlagSet(align, Align.BOTTOM)) {
            y_pos -= textBounds.height() / 2;
        }
        canvas.drawText(text, cx, y_pos, paint);
    }

    public void drawText(String text, float cx, float cy) {
        drawText(text, cx, cy, 0);
    }

    public int getTextWidth(String text){
        paint.getTextBounds(text, 0, text.length(), textBounds);
        return textBounds.width();
    }

    public void setColor(String color) {
        if (color.length() > 0 && color.charAt(0) != '#') {
            color = "#" + color;
        }
        paint.setColor(Color.parseColor(color));
    }

    public void clearCanvas() {
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);
    }

    public void clearCanvas(String color) {
        setColor(color);
        clearCanvas();
    }

    public void drawLine(float startx, float starty, float stopx, float stopy){
        canvas.drawLine(startx, starty, stopx, stopy, paint);
    }

    public void fillCircle(float cx, float cy, float radius){
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, radius, paint);
    }

    public void outlineCircle(float cx, float cy, float radius, float thickness){
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(thickness);
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setStrokeWidth(0);
    }

    public void fillRect(float left, float top, float right, float bottom){
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(left, top, right, bottom, paint);
    }

    public void outlineRect(float left, float top, float right, float bottom, float thickness){
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(thickness);
        canvas.drawRect(left, top, right, bottom, paint);
        paint.setStrokeWidth(0);
    }
}