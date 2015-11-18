package igrek.findme.graphics;

import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

import igrek.findme.logic.Types;
import igrek.findme.settings.Config;
import igrek.findme.system.Output;

public class Buttons {
    public List<Button> buttons = new ArrayList<>();

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
            if (h == 0) h = Config.Buttons.height; //domyślna wysokość
            this.h = h;
            if ((align & 0xf00) != 0) { //coś jest na pozycji adjust
                Paint paint2 = new Paint();
                paint2.setTextSize(Config.Buttons.fontsize);
                Rect textBounds = new Rect();
                paint2.getTextBounds(text, 0, text.length(), textBounds);
                if (Types.isFlagSet(align,  Types.Align.HADJUST)) {
                    this.w = textBounds.width() + 2 * Config.Buttons.padding_h;
                }
                if (Types.isFlagSet(align, Types.Align.VADJUST)) {
                    this.h = textBounds.height() + 2 * Config.Buttons.padding_v;
                }
            }
            //domyślne wartości
            if ((align & 0x0f) == 0) align |=  Types.Align.LEFT;
            if ((align & 0xf0) == 0) align |=  Types.Align.TOP;
            if (Types.isFlagSet(align,  Types.Align.LEFT)) {
                this.x = x;
            } else if (Types.isFlagSet(align,  Types.Align.HCENTER)) {
                this.x = x - this.w / 2;
            } else { //right
                this.x = x - this.w;
            }
            if (Types.isFlagSet(align,  Types.Align.TOP)) {
                this.y = y;
            } else if (Types.isFlagSet(align,  Types.Align.VCENTER)) {
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
            return !(touch_x > x + w || touch_y > y + h);
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

    public Button find(String id) throws Exception {
        for (Button b : buttons) {
            if (b.id.equals(id)) return b;
        }
        Output.errorthrow("Nie znaleziono przycisku o nazwie: " + id);
        return null;
    }

    public void setActive(String id, boolean set) throws Exception {
        Button b = find(id);
        if (b == null) return;
        b.active = set;
    }

    public void disableAllButtons() {
        for (Button b : buttons) {
            b.active = false;
        }
    }

    public boolean checkClicked(float touch_x, float touch_y) {
        for (Button b : buttons) {
            if (b.active) {
                if (b.isInRect(touch_x, touch_y)) {
                    b.clicked = true;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkClickedNoAction(float touch_x, float touch_y) {
        for (Button b : buttons) {
            if (b.active) {
                if (b.isInRect(touch_x, touch_y)) {
                    return true;
                }
            }
        }
        return false;
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
