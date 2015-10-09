package igrek.findme.modules;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import igrek.findme.R;
import igrek.findme.graphics.Graphics;
import igrek.findme.system.Output;

public class KeyboardManager {
    Graphics graphics;
    Activity activity;
    InputMethodManager imm;
    public boolean visible = false;
    EditText editText;
    TextView textViewLabel;
    View layoutView;

    public KeyboardManager(Activity activity, Graphics graphics) {
        this.activity = activity;
        this.graphics = graphics;
        imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        //monitorowanie stanu ekranu - czy doklejona jest klawiatura ekranowa
        //graphics.getViewTreeObserver().addOnGlobalLayoutListener(new GlobalLayoutListener());
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

    /*
    public class GlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            graphics.getWindowVisibleDisplayFrame(r);
            int heightDiff = graphics.getRootView().getHeight() - (r.bottom - r.top);
            keyboard_actual_shown = heightDiff >= Config.geti().keyboard_min_height;
        }
    }
    */

    public void inputScreenShow(String label, String value) {
        activity.setContentView(layoutView);
        //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
        textViewLabel.setText(label);
        editText.setText(value);
        editText.requestFocus();
        imm.showSoftInput(editText, 0);
        visible = true;
    }

    public void inputScreenShow(String label){
        inputScreenShow(label, "");
    }

    public void inputScreenHide(){
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        activity.setContentView(graphics);
        visible = false;
        Output.info("Wpisany tekst: " + getInputText());
    }

    public String getInputText(){
        return editText.getText().toString();
    }
}
