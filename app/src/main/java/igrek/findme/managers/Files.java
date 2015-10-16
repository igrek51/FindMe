package igrek.findme.managers;

import android.app.Activity;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import igrek.findme.system.Output;

public class Files {
    Activity activity;

    public Files(Activity activity) {
        this.activity = activity;
    }

    public String pathSD(String path) {
        if (path.length() == 0 || path.charAt(0) != '/') {
            path = "/" + path;
        }
        return Environment.getExternalStorageDirectory().toString() + path;
    }

    public String path(String dir, String filename) {
        if (dir.length() == 0 || dir.charAt(dir.length() - 1) != '/') {
            dir += "/";
        }
        return dir + filename;
    }

    public String internalAppDirectory() {
        return activity.getFilesDir().toString();
    }

    public List<String> listDir(String path) {
        List<String> lista = new ArrayList<>();
        File f = new File(path);
        File file[] = f.listFiles();
        for (File aFile : file) {
            lista.add(aFile.getName());
        }
        return lista;
    }

    public byte[] openFile(String filename) throws IOException {
        RandomAccessFile f = null;
        f = new RandomAccessFile(new File(filename), "r");
        int length = (int) f.length();
        byte[] data = new byte[length];
        f.readFully(data);
        f.close();
        return data;
    }

    public String openFileString(String filename) throws Exception {
        byte[] bytes = openFile(filename);
        return new String(bytes, "UTF-8");
    }

    public void saveFile(String filename, byte[] data) throws IOException, FileNotFoundException {
        File file = new File(filename);
        FileOutputStream fos;
        fos = new FileOutputStream(file);
        fos.write(data);
        fos.flush();
        fos.close();
    }

    public void saveFile(String filename, String str) throws IOException, FileNotFoundException {
        saveFile(filename, str.getBytes());
    }

    public boolean isDirectory(String path) {
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }

    public boolean isFile(String path) {
        File f = new File(path);
        return f.exists() && f.isFile();
    }

    public boolean exists(String path) {
        File f = new File(path);
        return f.exists();
    }
}
