package c4q.nyc.notesapp.models;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import c4q.nyc.notesapp.R;

/**
 * Implements IDataSource using HashMap as underlying storage
 */

public class DataSource extends ArrayList<Note> implements IDataSource {
    private static final String NOTES_FILE = "notes.json";
    private static final String TAG = "DataSource";
    private static DataSource dataSource;

    private DataSource() {
    }

    public static IDataSource getInstance(Context context) {
        if (dataSource != null) {
            return dataSource;
        }

        Type collectionType = new TypeToken<Collection<Note>>() {
        }.getType();
        Gson gs = new Gson();
        Collection<Note> notes = null;

        // try to load the data file (where we save to)
        try {
            FileInputStream fis = context.openFileInput(NOTES_FILE);
            notes = gs.fromJson(new InputStreamReader(fis), collectionType);
        } catch (FileNotFoundException e) {
            Log.d(TAG, e.getMessage());
        }

        // above failed, use initializer file
        if (notes == null) {
            Log.d(TAG, "Using initializer file resource");
            InputStream is = context.getResources().openRawResource(R.raw.golden);
            InputStreamReader isr = new InputStreamReader(is);
            notes = gs.fromJson(isr, collectionType);
        }

        // above two failed, use empty list
        if (notes == null) {
            Log.d(TAG, "No data was loaded from any storage, initializing empty notes manager");
            notes = new ArrayList<>();
        }

        dataSource = new DataSource();
        dataSource.addAll(notes);
        return dataSource;
    }

    /**
     * Serializes a notes to a json file
     *
     * @Param context
     */
    public void persist(Context context, IDataSource dataSource) {
        try {
            FileOutputStream fos = context.openFileOutput(NOTES_FILE, Context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            new Gson().toJson(dataSource, writer);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public Note addNote(String title, String body) {
        final Note n = new Note();
        n.id = getNewId();
        n.title = title;
        n.body = body;
        n.dateCreated = 0l;
        n.lastModified = n.dateCreated;
        add(n);
        return n;
    }

    @Override
    public Note updateNote(String noteId, String title, String body) {
        for (Note n : this) {
            if (n.id.equals(noteId)) {
                n.title = title;
                n.body = body;
                n.lastModified++;
                return n;
            }
        }
        return null;
    }


    /**
     * getNewId returns a short string that is unique in notes keyset
     */
    private String getNewId() {
        final String alphaNum = "abcdefghijklmnopqrstuvwzyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            int index = (int) (Math.random() * alphaNum.length());
            sb.append(alphaNum.charAt(index));
        }
        return sb.toString();
    }
}
