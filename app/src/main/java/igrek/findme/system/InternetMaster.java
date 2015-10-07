package igrek.findme.system;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class InternetMaster {
    private List<InternetTask> tasks;

    public InternetMaster(Activity activity) {
        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) {
            Output.errorCritical("Błąd usługi połączenia");
            return;
        }
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        Output.log("networkInfo.isAvailable() = " + networkInfo.isAvailable());
        Output.log("networkInfo.isConnected() = " + networkInfo.isConnected());
        if (networkInfo == null || !networkInfo.isConnected()) {
            Output.errorCritical("Błąd połączenia z internetem");
            return;
        }
        Output.log("Możliwe połączenie z internetem.");
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            Output.log("Wifirifi dostępne.");
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            Output.log("dane pakietowe dostępne.");
        }
        tasks = new ArrayList<>();
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                Output.error(e);
                return "";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Output.log("Odpowiedź: " + result);

        }
    }

    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Output.log("The response is: " + response);
            is = conn.getInputStream();
            // Convert the InputStream into a string
            String contentAsString = readInputStream(is, len);
            return contentAsString;
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readInputStream(InputStream stream, int maxlen) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        char[] buffer = new char[maxlen];
        if (reader != null) {
            reader.read(buffer);
        }
        return new String(buffer);
    }

    //TODO: funkcja wysyłania pakietów do podaneg URL

    //TODO: funkcja odbierania danych z URL
    public void download(String url) {
        Output.log("Łączę...");
        new DownloadTask().execute(url);
    }

    private class InternetTask {
        public InternetTask(String name, String url){
            this.name = name;
            this.url = url;
        }
        String name;
        String url;
        String response = "";
        boolean ready = false;
        boolean error = false;
    }

    public void addTask(String name, String url) {

    }

    public boolean isReady(String name) {

    }

    public String getResponse(String name) {

    }
}
