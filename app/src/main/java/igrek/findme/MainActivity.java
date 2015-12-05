package igrek.findme;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import igrek.findme.logic.Engine;
import igrek.findme.settings.Config;
import igrek.findme.system.Output;

public class MainActivity extends AppCompatActivity {
    private Engine engine = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //schowanie paska tytułu
        if (Config.hide_taskbar) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
        }
        //fullscreen
        if (Config.fullscreen) {
            getWindow().setFlags(Config.fullscreen_flag, Config.fullscreen_flag);
        }
        if (Config.keep_screen_on) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        engine = new Engine(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Output.log("orientation changed: landscape");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Output.log("orientation changed: portrait");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        engine.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        engine.resume();
    }

    @Override
    protected void onDestroy() {
        engine.quit();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //event obsłużony lub przekazany dalej
        return engine.options_select(id) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            engine.keycode_back();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            engine.keycode_menu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true; //przechwycenie klawisza menu
        }
        return super.onKeyDown(keyCode, event);
    }
}


