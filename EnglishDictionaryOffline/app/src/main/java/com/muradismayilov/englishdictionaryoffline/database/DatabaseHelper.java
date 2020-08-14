package com.muradismayilov.englishdictionaryoffline.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final String DB_PATH;
    private static final String DB_NAME = "eng_dictionary.db";
    private SQLiteDatabase myDataBase;
    private final Context myContext;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
        @SuppressLint("SdCardPath") String path = "/data/data/";
        this.DB_PATH = path + context.getPackageName() + "/" + "databases/";
    }

    public void createDataBase() {
        boolean dbExist = checkDataBase();
        if (!dbExist) {

            this.getReadableDatabase();
            try {
                myContext.deleteDatabase(DB_NAME);
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        if (checkDB != null) {
            checkDB.close();
        }

        return checkDB != null;
    }

    private void copyDataBase() throws IOException {
        InputStream myInput = myContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        try {
            this.getReadableDatabase();
            myContext.deleteDatabase(DB_NAME);
            copyDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Cursor getMeaning(String text) {
        try {
            return myDataBase.rawQuery("SELECT en_definition,example,synonyms,antonyms FROM words WHERE en_word==UPPER('" + text + "')", null);
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getSuggestions(String text) {
        return myDataBase.rawQuery("SELECT _id, en_word FROM words WHERE en_word LIKE '" + text + "%' LIMIT 40", null);
    }

    public Cursor getHistory() {
        return myDataBase.rawQuery("select distinct  word, en_definition from history h join words w on h.word==w.en_word order by h._id desc", null);
    }

    public void deleteHistory() {
        myDataBase.execSQL("DELETE  FROM history");
    }

    public void insertHistory(String text) {
        myDataBase.execSQL("INSERT INTO history(word) VALUES(UPPER('" + text + "'))");

    }
}