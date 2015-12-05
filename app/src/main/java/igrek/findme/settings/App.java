package igrek.findme.settings;

import igrek.findme.logic.Engine;
import igrek.findme.logic.Types;
import igrek.findme.system.Output;

public class App {
    //singleton
    private App() {
        instance = this; //dla pewności
    }

    private static App instance = null;

    public static App geti() {
        if (instance == null) {
            Output.error("Utworzona instancja App bez resetowania i przekazania engine");
            instance = new App();
        }
        return instance;
    }

    public static App reset(Engine engine) {
        instance = new App();
        instance.engine = engine;
        return instance;
    }

    public Engine engine;

    //  ZMIENNE APLIKACJI
    public Types.AppMode mode = Types.AppMode.MENU;
    //rozmiar ekranu
    public int w = 0;
    public int h = 0;
    //  LOGOWANIE
    public int id_user = 0;
    public String login;
    public String pass;
    //  GPS
    public long last_position_update = 0; //czas wysłania ostatniej pozycji
}
