package igrek.findme.modules;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import igrek.findme.R;
import igrek.findme.graphics.Graphics;
import igrek.findme.settings.Config;
import igrek.findme.system.Output;

public class KeyboardManager {
    Graphics graphics;
    Activity activity;
    InputMethodManager imm;
    public boolean keyboard_actual_shown = false;
    public boolean keyboard_shown = false;
    public boolean visible = false;
    EditText editText;
    TextView textViewLabel;
    View layoutView;

    public KeyboardManager(Activity activity, Graphics graphics) {
        this.activity = activity;
        this.graphics = graphics;
        imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        //monitorowanie stanu ekranu - czy doklejona jest klawiatura ekranowa
        graphics.getViewTreeObserver().addOnGlobalLayoutListener(new GlobalLayoutListener());
        //inicjalizacja layoutu
        LayoutInflater inflater = activity.getLayoutInflater();
        layoutView = inflater.inflate(R.layout.keyboardinput, null);
        //akcja dla przycisku OK
        Button button_ok = (Button) layoutView.findViewById(R.id.button_ok);
        button_ok.setOnClickListener(new ButtonOK());
        editText = (EditText) layoutView.findViewById(R.id.inputKeyboardText);
        textViewLabel = (TextView) layoutView.findViewById(R.id.label_text);
    }

    public class ButtonOK implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            inputScreenHide();
        }
    }

    public class GlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            graphics.getWindowVisibleDisplayFrame(r);
            int heightDiff = graphics.getRootView().getHeight() - (r.bottom - r.top);
            keyboard_actual_shown = heightDiff >= Config.geti().keyboard_min_height;
        }
    }

    public void validateKeyboardVisible() {
        setKeyboardVisible(keyboard_shown);
    }

    public void setKeyboardVisible(boolean keyboard_shown) {
        this.keyboard_shown = keyboard_shown;
        //Output.log("keyboard_shown: "+ keyboard_shown);
        //Output.log("keyboard_actual_shown: "+ keyboard_actual_shown);
        if (keyboard_shown == keyboard_actual_shown) return;
        if (keyboard_shown) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            imm.toggleSoftInput(0, 0);
            //imm.hideSoftInputFromWindow(graphics.getWindowToken(), 0);
        }
    }

    public void inputScreenShow(String label, String value) {
        activity.setContentView(layoutView);
        textViewLabel.setText(label);
        editText.setText(value);
        editText.requestFocus();
        setKeyboardVisible(true);
        visible = true;
    }

    public void inputScreenShow(String label){
        inputScreenShow(label, "");
    }

    public void inputScreenHide(){
        activity.setContentView(graphics);
        setKeyboardVisible(false);
        visible = false;
    }

    public String getInputText(){
        return editText.getText().toString();
    }
}
