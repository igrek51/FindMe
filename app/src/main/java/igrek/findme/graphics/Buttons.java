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

    public interface ButtonActionListener {
        void clicked() throws Exception;
    }

    public class Button {
        public float x, y;
        public float w, h;
        public String text, id;
        public int clicked = 0; //0 - nie wciśnięty, 1 - przyciśnięty (nie puszczony), 2 - wciśnięty i puszczony (kliknięty)
        public boolean visible = true;
        public ButtonActionListener actionListener;

        public Button(String text, String id, float x, float y, float w, float h, ButtonActionListener actionListener) {
            this.text = text;
            this.id = id;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.actionListener = actionListener;
        }

        public Button setPos(float x, float y, float w, float h, int align) {
            //rozmiar
            this.w = w;
            if (h == 0) h = Config.Buttons.height; //domyślna wysokość
            this.h = h;
            if ((align & 0xf00) != 0) { //coś jest na pozycji adjust
                Paint paint2 = new Paint();
                paint2.setTextSize(Config.Buttons.fontsize);
                Rect textBounds = new Rect();
                paint2.getTextBounds(text, 0, text.length(), textBounds);
                if (Types.isFlagSet(align, Types.Align.HADJUST)) { //automatyczne dobranie szerokości
                    this.w = textBounds.width() + 2 * Config.Buttons.padding_h;
                }
                if (Types.isFlagSet(align, Types.Align.VADJUST)) { //automatyczne dobranie wysokości
                    this.h = textBounds.height() + 2 * Config.Buttons.padding_v;
                }
            }
            //pozycja
            //domyślne wartości
            if ((align & 0x0f) == 0) align |= Types.Align.LEFT;
            if ((align & 0xf0) == 0) align |= Types.Align.TOP;
            if (Types.isFlagSet(align, Types.Align.LEFT)) {
                this.x = x;
            } else if (Types.isFlagSet(align, Types.Align.HCENTER)) {
                this.x = x - this.w / 2;
            } else { //right
                this.x = x - this.w;
            }
            if (Types.isFlagSet(align, Types.Align.TOP)) {
                this.y = y;
            } else if (Types.isFlagSet(align, Types.Align.VCENTER)) {
                this.y = y - this.h / 2;
            } else { //bottom
                this.y = y - this.h;
            }
            return this;
        }

        public boolean isInRect(float touch_x, float touch_y) {
            return touch_x >= x && touch_x <= x + w && touch_y >= y && touch_y <= y + h;
        }
    }

    public Button add(String text, String id, float x, float y, float w, float h, int align, ButtonActionListener actionListener) {
        Button b = new Button(text, id, x, y, w, h, actionListener);
        b.setPos(x, y, w, h, align);
        buttons.add(b);
        return b;
    }

    public Button add(String text, String id, float x, float y, float w, float h, ButtonActionListener actionListener) {
        return add(text, id, x, y, w, h, Types.Align.DEFAULT, actionListener);
    }

    public Button find(String id) throws Exception {
        for (Button b : buttons) {
            if (b.id.equals(id)) return b;
        }
        Output.errorthrow("Nie znaleziono przycisku o nazwie: " + id);
        return null;
    }

    public void setVisible(String id, boolean set) throws Exception {
        Button b = find(id);
        b.visible = set;
    }

    public void setVisible(String id) throws Exception {
        setVisible(id, true);
    }

    public void hideAll() {
        for (Button b : buttons) {
            b.visible = false;
        }
    }

    public boolean checkPressed(float touch_x, float touch_y) {
        for (Button b : buttons) {
            if (b.visible) {
                if (b.isInRect(touch_x, touch_y)) {
                    b.clicked = 1;
                    return true; //przechwycenie
                }
            }
        }
        return false;
    }

    public boolean checkReleased(float touch_x, float touch_y) {
        for (Button b : buttons) {
            if (b.clicked == 1) { //jeśli był wciśnięty
                if (b.isInRect(touch_x, touch_y)) {
                    b.clicked = 2;
                    return true; //przechwycenie
                } else {
                    b.clicked = 0; //reset stanu
                }
            }
        }
        return false;
    }

    public boolean checkMoved(float touch_x, float touch_y) {
        for (Button b : buttons) {
            if (b.visible) {
                if (b.isInRect(touch_x, touch_y)) {
                    return true; //przechwycenie
                }
            }
        }
        return false;
    }

    public void executeClicked() throws Exception{
        for (Button b : buttons) {
            if (b.clicked == 2) { //został kliknięty
                b.clicked = 0; //zresetowanie udanego kliknięcia
                b.actionListener.clicked(); //wykonanie akcji
            }
        }
    }

    public float lastYTop(){
        if(buttons.size()==0){
            Output.error("Brak buttonów na liście.");
            return 0;
        }
        return buttons.get(buttons.size()-1).y;
    }

    public float lastYBottom(){
        if(buttons.size()==0){
            Output.error("Brak buttonów na liście.");
            return 0;
        }
        Button last = buttons.get(buttons.size()-1);
        return last.y + last.h;
    }

    public float lastXLeft(){
        if(buttons.size()==0){
            Output.error("Brak buttonów na liście.");
            return 0;
        }
        return buttons.get(buttons.size()-1).x;
    }

    public float lastXRight(){
        if(buttons.size()==0){
            Output.error("Brak buttonów na liście.");
            return 0;
        }
        Button last = buttons.get(buttons.size()-1);
        return last.x + last.w;
    }
}
