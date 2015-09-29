package igrek.dupa3;

import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

public class Buttons {
    List<Button> buttons = new ArrayList<Button>();

    public class Align {
        //pozycja
        static final int LEFT = 0x001;
        static final int RIGHT = 0x002;
        static final int HCENTER = 0x004;
        static final int TOP = 0x010;
        static final int BOTTOM = 0x020;
        static final int VCENTER = 0x040;
        static final int CENTER = HCENTER | VCENTER;
        //rozmiar
        static final int HADJUST = 0x100;
        static final int VADJUST = 0x200;
        static final int ADJUST = HADJUST | VADJUST;
    }

    public class Button {
        public Button(String text, String id, float x, float y, float w, float h) {
            this.text = text;
            this.id = id;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public Button setPos(float x, float y, float w, float h, int align) {
            this.w = w;
            if (h == 0) h = Config.geti().button_height; //domyślna wysokość
            this.h = h;
            if ((align & 0xf00) != 0) { //coś jest na pozycji adjust
                Paint paint2 = new Paint();
                paint2.setTextSize(Config.geti().buttons_fontsize);
                Rect textBounds = new Rect();
                paint2.getTextBounds(text, 0, text.length(), textBounds);
                if (App.isFlagSet(align, Align.HADJUST)) {
                    this.w = textBounds.width() + 2 * Config.geti().button_padding_h;
                }
                if (App.isFlagSet(align, Align.VADJUST)) {
                    this.h = textBounds.height() + 2 * Config.geti().button_padding_v;
                }
            }
            //domyślne wartości
            if ((align & 0x0f) == 0) align |= Align.LEFT;
            if ((align & 0xf0) == 0) align |= Align.TOP;
            if (App.isFlagSet(align, Align.LEFT)) {
                this.x = x;
            } else if (App.isFlagSet(align, Align.HCENTER)) {
                this.x = x - this.w / 2;
            } else { //right
                this.x = x - this.w;
            }
            if (App.isFlagSet(align, Align.TOP)) {
                this.y = y;
            } else if (App.isFlagSet(align, Align.VCENTER)) {
                this.y = y - this.h / 2;
            } else { //bottom
                this.y = y - this.h;
            }
            return this;
        }

        public Button setPos(float x, float y, float w, float h) {
            return setPos(x, y, w, h, 0);
        }

        public boolean isInRect(float touch_x, float touch_y) {
            if (touch_x < x || touch_y < y) return false;
            if (touch_x > x + w || touch_y > y + h) return false;
            return true;
        }

        public float x, y;
        public float w, h;
        public String text;
        public String id;
        public boolean clicked = false;
        public boolean active = true;
    }

    public Button add(String text, String id, float x, float y, float w, float h, int align) {
        Button b = new Button(text, id, x, y, w, h);
        b.setPos(x, y, w, h, align);
        buttons.add(b);
        return b;
    }

    public Button add(String text, String id, float x, float y, float w, float h) {
        return add(text, id, x, y, w, h, 0);
    }

    public void clear() {
        buttons.clear();
    }

    public Button find(String id) {
        for (Button b : buttons) {
            if (b.id.equals(id)) return b;
        }
        App.geti().error("Nie znaleziono przycisku o nazwie: " + id);
        return null;
    }

    public void setActive(String id, boolean set) {
        Button b = find(id);
        if (b == null) return;
        b.active = set;
    }

    public void disableAllButtons() {
        for (Button b : buttons) {
            b.active = false;
        }
    }

    public void checkClicked(float touch_x, float touch_y) {
        for (Button b : buttons) {
            if (b.active) {
                if (b.isInRect(touch_x, touch_y)) {
                    b.clicked = true;
                }
            }
        }
    }

    public boolean isClicked() {
        for (Button b : buttons) {
            if (b.clicked) return true;
        }
        return false;
    }

    public String clickedId() {
        for (Button b : buttons) {
            if (b.clicked) {
                b.clicked = false;
                return b.id;
            }
        }
        return "";
    }
}
