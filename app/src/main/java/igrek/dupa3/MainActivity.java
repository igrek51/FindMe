package igrek.dupa3;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    Engine engine = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //schowanie paska tytułu
        if (Config.geti().hide_taskbar) {
            getSupportActionBar().hide();
        }
        //fullscreen
        if (Config.geti().fullscreen) {
            getWindow().setFlags(Config.geti().fullscreen_flag, Config.geti().fullscreen_flag);
        }
        engine = new Engine(this);
        setContentView(engine.graphics);
        engine.start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            App.log("orientation: landscape");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            App.log("orientation: portrait");
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
        super.onDestroy();
        engine.quit();
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
        if(engine.options_select(id)){
            return true; //event obsłużony
        }
        return super.onOptionsItemSelected(item);
    }
}