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

public class InternetMaster {
    public boolean received = false;
    public boolean send = false;
    public String response = "";

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
        Output.log("Łączę...");
        new DownloadWebpageTask().execute(stringUrl);
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
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
            String contentAsString = readIt(is, len);
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
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        char[] buffer = new char[len];
        if (reader != null) {
            reader.read(buffer);
        }
        return new String(buffer);
    }

    //TODO: funkcja wysyłania pakietów do podaneg URL

    //TODO: funkcja odbierania danych z URL
}
