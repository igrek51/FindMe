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

    public CanvasView(Context context) {
        super(context);
        paint = new Paint();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = getWidth();
        this.h = getHeight();
        App.geti().size_changed = true;
        App.log("Rozmiar ekranu zmieniony.");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawPaint(paint);
        float x = App.geti().smyradlo.x;
        float y = App.geti().smyradlo.y;
        // Use Color.parseColor to define HTML colors
        paint.setColor(Color.parseColor("#8cc24d"));
        //tułów
        canvas.drawRect(x + w * 1 / 4, y + h * 1 / 4, x + w * 3 / 4, y + h * 3 / 4, paint);
        //jajka
        canvas.drawCircle(x + w / 4, y + h * 3 / 4, w / 4, paint);
        canvas.drawCircle(x + w * 3 / 4, y + h * 3 / 4, w / 4, paint);
        //głowa
        canvas.drawCircle(x + w * 2 / 4, y + h * 1 / 4, w / 4, paint);
        //czułki
        canvas.drawLine(x + w * 2 / 4, y + h * 1 / 4, x + w * 2 / 4 - w * 3 / 8, y + h * 1 / 4 - w * 3 / 8, paint);
        canvas.drawLine(x + w * 2 / 4, y + h * 1 / 4, x + w * 2 / 4 + w * 3 / 8, y + h * 1 / 4 - w * 3 / 8, paint);
        //oczka
        paint.setColor(Color.parseColor("#e0e0e0"));
        canvas.drawCircle(x + w * 2 / 4 - w / 10, y + h * 7 / 32, w / 25, paint);
        canvas.drawCircle(x + w * 2 / 4 + w / 10, y + h * 7 / 32, w / 25, paint);
        //buzia
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawLine(x + 0, y + h * 5 / 16, x + w, y + h * 5 / 16, paint);
        //teskt
        paint.setTextSize(30);
        paint.setColor(Color.parseColor("#006000"));
        drawTextTopCenter("Posmyraj go!", x + w / 2, y);
    }


    //override the onTouchEvent
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touch_x = event.getX();
        float touch_y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                App.geti().smyradlo.start_x = touch_x;
                App.geti().smyradlo.start_y = touch_y;
                App.geti().smyradlo.start_pos_x = App.geti().smyradlo.x;
                App.geti().smyradlo.start_pos_y = App.geti().smyradlo.y;
                App.geti().smyradlo.Vx = 0;
                App.geti().smyradlo.Vy = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                App.geti().smyradlo.x = App.geti().smyradlo.start_pos_x + touch_x - App.geti().smyradlo.start_x;
                App.geti().smyradlo.y = App.geti().smyradlo.start_pos_y + touch_y - App.geti().smyradlo.start_y;
                App.geti().smyradlo.Vx = 0;
                App.geti().smyradlo.Vy = 0;
                break;
            case MotionEvent.ACTION_UP:
                App.geti().smyradlo.x = App.geti().smyradlo.start_pos_x + touch_x - App.geti().smyradlo.start_x;
                App.geti().smyradlo.y = App.geti().smyradlo.start_pos_y + touch_y - App.geti().smyradlo.start_y;
                App.geti().smyradlo.Vx = (touch_x - App.geti().smyradlo.start_x) * App.geti().smyradlo.speed_factor;
                App.geti().smyradlo.Vy = (touch_y - App.geti().smyradlo.start_y) * App.geti().smyradlo.speed_factor;
                break;
        }
        return true;
    }

    //pomocnicze funkcje rysujące
    Rect textBounds = new Rect();

    public void drawTextTopCenter(String text, float cx, float cy) {
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, cx - textBounds.width() / 2, cy + textBounds.height() / 2, paint);
    }
}