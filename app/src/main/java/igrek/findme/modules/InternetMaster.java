package igrek.findme.modules;

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

import igrek.findme.settings.Config;

public class InternetMaster {
    public InternetMaster(Activity activity) {
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

    public class InternetTask {
        String url;
        String response = "";
        int response_code = 0;
        String method;
        public boolean ready = false; //zakończenie powodzeniem lub niepowodzeniem
        public boolean error = false; //wystąpił błąd

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

        public int getResponseCode() {
            if (!ready) {
                Output.error("getResponse: Zadanie nie zostało ukończone");
                return 0;
            }
            if (error) {
                Output.error("getResponse: Błąd podczas pobierania odpowiedzi");
                return 0;
            }
            return response_code;
        }
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
}
