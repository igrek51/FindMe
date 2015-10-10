package igrek.findme.managers;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import igrek.findme.settings.Config;
import igrek.findme.system.Output;

public class InternetManager {
    public InternetManager(Activity activity) {
        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) {
            Output.errorCritical("Błąd usługi połączenia");
            return;
        }
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            Output.error("Błąd połączenia z internetem");
            return;
        }
        Output.log("networkInfo.isAvailable() = " + networkInfo.isAvailable());
        Output.log("networkInfo.isConnected() = " + networkInfo.isConnected());
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            Output.info("Wifirifi dostępne.");
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            Output.info("Dane pakietowe dostępne.");
        }
        Output.info("Moduł połączenia internetowego uruchomiony.");
    }

    public interface ResponseHandler {
        void onResponse(InternetTask internetTask);
    }

    public class InternetTask {
        String url;
        String response = "";
        int response_code = 0;
        String method;
        public boolean ready = false; //zakończenie powodzeniem lub niepowodzeniem
        public boolean error = false; //wystąpił błąd
        public ResponseHandler responseHandler = null;

        public InternetTask(String url, String method) {
            this.url = url;
            this.method = method;
        }

        public boolean isReady() {
            return ready;
        }

        public boolean isCorrect() {
            return ready && !error;
        }

        public int getResponseCode() {
            if (!ready) {
                Output.error("getResponseCode: Zadanie nie zostało ukończone");
                return 0;
            }
            return response_code;
        }

        public String getResponse() {
            if (!ready) {
                Output.error("getResponse: Zadanie nie zostało ukończone");
                return "";
            }
            if (error) {
                Output.error("getResponse: Błąd podczas pobierania odpowiedzi");
                return "";
            }
            return response;
        }

        public List<String> getResponseStrings(){
            String resp = getResponse();
            //podział znakami \n
            return split(resp, '\n');
        }

        public int getResponse1Int() {
            String resp = getResponse();
            //obcięcie numeru odpowiedzi i pozostałych danych
            int first_space = resp.indexOf("\n");
            if(first_space != -1){
                resp = resp.substring(0, first_space);
            }
            try {
                return Integer.parseInt(resp);
            }catch(NumberFormatException e){
                Output.error("getResponse1Int: Nieprawidłowy format liczby");
                return 0;
            }
        }

        public String getResponse2String() {
            List<String> lista = getResponseStrings();
            if(lista.size() < 2){
                Output.error("getResponse2String: Brak drugiego Stringa w odpowiedzi");
                return "";
            }else{
                return lista.get(1);
            }
        }

        public int getResponse2Int() {
            try {
                return Integer.parseInt(getResponse2String());
            }catch(NumberFormatException e){
                Output.error("getResponse2Int: Nieprawidłowy format liczby");
                return 0;
            }
        }
    }

    public static List<String> split(String strToSplit, char delimiter) {
        List<String> arr = new ArrayList<>();
        int foundPosition;
        int startIndex = 0;
        while ((foundPosition = strToSplit.indexOf(delimiter, startIndex)) > -1) {
            arr.add(strToSplit.substring(startIndex, foundPosition));
            startIndex = foundPosition + 1;
        }
        arr.add(strToSplit.substring(startIndex));
        return arr;
    }

    private class DownloadTask extends AsyncTask<InternetTask, Void, Void> {
        @Override
        protected Void doInBackground(InternetTask... its) {
            downloadUrl(its[0]);
            return null;
        }
    }

    private void downloadUrl(InternetTask internetTask) {
        InputStream is = null;
        try {
            URL url = new URL(internetTask.url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(Config.geti().connection.read_timeout);
            conn.setConnectTimeout(Config.geti().connection.connect_timeout);
            conn.setRequestMethod(internetTask.method);
            conn.setDoInput(true);
            conn.connect();
            internetTask.response_code = conn.getResponseCode();
            is = conn.getInputStream();
            // konwersja na String
            internetTask.response = readInputStream(is, Config.geti().connection.max_response_size);
        } catch (Exception ex) {
            Output.error(ex);
            internetTask.error = true; //wystąpił błąd
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Output.error("Błąd zamykania strumienia danych");
                }
            }
            internetTask.ready = true; //zakończono
            if(internetTask.responseHandler != null){ //wywołanie zdarzenia
                internetTask.responseHandler.onResponse(internetTask);
            }
        }
    }

    public String readInputStream(InputStream stream, int maxlen) throws IOException, UnsupportedEncodingException {
        Reader reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[maxlen];
        int characters = reader.read(buffer);
        return (new String(buffer)).substring(0, characters);
    }

    public InternetTask download(String url) {
        InternetTask internetTask = new InternetTask(url, "GET");
        new DownloadTask().execute(internetTask);
        return internetTask;
    }

    public InternetTask download(String url, ResponseHandler responseHandler) {
        InternetTask internetTask = new InternetTask(url, "GET");
        internetTask.responseHandler = responseHandler;
        new DownloadTask().execute(internetTask);
        return internetTask;
    }
}
