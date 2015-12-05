package igrek.findme.managers;

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

public class InputManager {
    Graphics graphics;
    Activity activity;
    InputMethodManager imm;
    public boolean visible = false;
    EditText editText;
    TextView textViewLabel;
    View layoutView;
    InputHandler inputHandler = null;
    Button button_ok;
    Button button_cancel;

    public InputManager(Activity activity, Graphics graphics) {
        this.activity = activity;
        this.graphics = graphics;
        imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        //monitorowanie stanu ekranu - czy doklejona jest klawiatura ekranowa
        //graphics.getViewTreeObserver().addOnGlobalLayoutListener(new GlobalLayoutListener());
        //inicjalizacja layoutu
        LayoutInflater inflater = activity.getLayoutInflater();
        layoutView = inflater.inflate(R.layout.keyboardinput, null);
        //akcja dla przycisku OK
        button_ok = (Button) layoutView.findViewById(R.id.button_ok);
        button_ok.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                inputScreenAccept();
            }
        });
        button_cancel = (Button) layoutView.findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                inputScreenCancel();
            }
        });
        editText = (EditText) layoutView.findViewById(R.id.inputKeyboardText);
        textViewLabel = (TextView) layoutView.findViewById(R.id.label_text);
    }

    public abstract static class InputHandler {
        public void onAccept(String inputText){ }
    }

    public abstract static class InputHandlerCancellable extends InputHandler {
        public void onCancel(String inputText){ }
    }

    public void inputScreenShow(String label, String value, InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        activity.setContentView(layoutView);
        //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
        textViewLabel.setText(label);
        editText.setText(value);
        editText.requestFocus();
        //button cancel
        if(inputHandler instanceof InputHandlerCancellable){
            button_cancel.setVisibility(View.VISIBLE);
        }else{
            button_cancel.setVisibility(View.INVISIBLE);
        }
        imm.showSoftInput(editText, 0);
        visible = true;
    }

    public void inputScreenShow(String label, InputHandler inputHandler){
        inputScreenShow(label, "", inputHandler); //domyślna wartość pusta
    }

    public void inputScreenHide(){
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        Output.echoWait(0);
        activity.setContentView(graphics);
        visible = false;
    }

    public void inputScreenAccept(){
        inputScreenHide();
        if(inputHandler!=null){
            inputHandler.onAccept(editText.getText().toString()); //wywołanie zdarzenia
        }
    }

    public boolean isCancellable() {
        return inputHandler != null && inputHandler instanceof InputHandlerCancellable;
    }

    public void inputScreenCancel(){
        inputScreenHide();
        if(inputHandler!=null){
            if(inputHandler instanceof InputHandlerCancellable) {
                InputHandlerCancellable inputHandlerCancellable = (InputHandlerCancellable) inputHandler;
                inputHandlerCancellable.onCancel(editText.getText().toString()); //wywołanie zdarzenia
            }
        }
    }
}
