package igrek.findme.managers;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import igrek.findme.settings.Config;
import igrek.findme.system.Output;

public class InternetManager {
    NetworkInfo networkInfo;

    public InternetManager(Activity activity) throws Exception {
        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) {
            Output.errorCritical("Błąd usługi połączenia");
        }
        networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            Output.errorthrow("Błąd połączenia z internetem");
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            Output.info("Wifirifi jest dostępne.");
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            Output.info("Dane pakietowe są dostępne.");
        }
        if(!networkInfo.isAvailable()){
            Output.errorthrow("Brak dostępnych połączeń internetowych.");
        }
        if(!networkInfo.isConnected()){
            Output.errorthrow("Brak połączenia internetowego.");
        }
        Output.info("Połączenie internetowe jest dostępne.");
    }

    public abstract static class ResponseHandler {
        public void onResponse(InternetTask internetTask) throws Exception {
            //domyślna obsługa odpowiedzi
            if (internetTask.isCorrect()) {
                if (internetTask.getResponse1Int() == Config.Connection.success_code) {
                    onSuccess(internetTask);
                } else {
                    Output.errorthrow(internetTask.getResponse2String());
                }
            } else {
                Output.errorthrow("Błąd odbierania pakietu odpowiedzi");
            }
        }

        public void onSuccess(InternetTask internetTask) throws Exception {
            //...
        }
    }

    public class InternetTask {
        String url;
        String response = "";
        int response_code = 0;
        String method;
        public boolean error = false; //wystąpił błąd
        List<Variable> data = null; //dane POST
        public ResponseHandler responseHandler = null;

        public InternetTask(String url, String method) {
            this.url = url;
            this.method = method;
        }

        public boolean isCorrect() {
            return !error;
        }

        public int getResponseCode() {
            return response_code;
        }

        public String getResponse() throws Exception {
            if (error) {
                Output.errorthrow("getResponse: Błąd podczas pobierania odpowiedzi");
            }
            return response;
        }

        public List<String> getResponseStrings() throws Exception {
            String resp = getResponse();
            //podział znakami \n
            return split(resp, '\n');
        }

        public int getResponse1Int() throws Exception {
            String resp = getResponse();
            //obcięcie numeru odpowiedzi i pozostałych danych
            int first_space = resp.indexOf("\n");
            if (first_space != -1) {
                resp = resp.substring(0, first_space);
            }
            try {
                return Integer.parseInt(resp);
            } catch (NumberFormatException e) {
                Output.errorthrow("getResponse1Int: Nieprawidłowy format liczby: " + resp);
                return 0;
            }
        }

        public String getResponse2String() throws Exception {
            List<String> lista = getResponseStrings();
            if (lista.size() < 2) {
                Output.errorthrow("getResponse2String: Brak drugiego Stringa w odpowiedzi");
                return "";
            } else {
                return lista.get(1);
            }
        }

        public int getResponse2Int() throws Exception {
            try {
                return Integer.parseInt(getResponse2String());
            } catch (NumberFormatException e) {
                Output.errorthrow("getResponse2Int: Nieprawidłowy format liczby");
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
            try {
                executeTask(its[0]);
            }catch(Exception e){
                Output.error(e);
            }
            return null;
        }
    }

    public static class Variable {
        public String name;
        public String value;

        public Variable(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public Variable(String name, double value) {
            this.name = name;
            this.value = String.valueOf(value);
        }

        public Variable(String name, int value) {
            this.name = name;
            this.value = String.valueOf(value);
        }


        public String toURLString() throws Exception {
            return URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
        }
    }

    private String getDataQuery(List<Variable> data) throws Exception {
        if (data == null) return "";
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Variable var : data) {
            if (first) first = false;
            else result.append("&");
            result.append(var.toURLString());
        }
        return result.toString();
    }

    private void methodPOST(HttpURLConnection conn, List<Variable> data) throws Exception {
        conn.setRequestMethod("POST");
        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(getDataQuery(data));
        writer.flush();
        writer.close();
        os.close();
    }

    private void executeTask(InternetTask internetTask) throws Exception {
        InputStream is = null;
        try {
            URL url = new URL(internetTask.url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(Config.Connection.read_timeout);
            conn.setConnectTimeout(Config.Connection.connect_timeout);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            if (internetTask.method.equals("POST")) {
                methodPOST(conn, internetTask.data);
            } else {
                conn.setRequestMethod("GET");
            }
            conn.connect();
            internetTask.response_code = conn.getResponseCode();
            is = conn.getInputStream();
            // konwersja na String
            internetTask.response = readInputStream(is, Config.Connection.max_response_size);
        } catch (Exception ex) {
            internetTask.error = true; //wystąpił błąd
            throw ex;
        } finally {
            if (is != null) {
                is.close();
            }
            if (internetTask.responseHandler != null) { //wywołanie zdarzenia
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

    public void GET(String url, ResponseHandler responseHandler) {
        InternetTask internetTask = new InternetTask(url, "GET");
        internetTask.responseHandler = responseHandler;
        new DownloadTask().execute(internetTask);
    }

    public void POST(String url, List<Variable> data, ResponseHandler responseHandler) {
        InternetTask internetTask = new InternetTask(url, "POST");
        internetTask.data = data;
        internetTask.responseHandler = responseHandler;
        new DownloadTask().execute(internetTask);
    }

    public boolean isConnected() {
        if (networkInfo == null) return false;
        return networkInfo.isConnected();
    }

    public boolean isAvailable() {
        if (networkInfo == null) return false;
        return networkInfo.isAvailable();
    }

    public boolean isWifiEnabled(){
        if (networkInfo == null) return false;
        return networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public boolean isMobileEnabled(){
        if (networkInfo == null) return false;
        return networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }
}
