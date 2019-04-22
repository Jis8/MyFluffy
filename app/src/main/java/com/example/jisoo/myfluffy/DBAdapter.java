package com.example.jisoo.myfluffy;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DBAdapter {


    private static final String TAG = "DbAdapter";
    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDB; // 데이터베이스를 저장
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "myDB";
    // Table Names
    private static final String TABLE_INFO = "infoTBL";
    private static final String TABLE_WEIGHT = "weightTBL";
    private static final String TABLE_RECORD = "recordTBL";

    // Info Table Field Names
    private static final String KEY_IMG = "img";
    private static final String KEY_NAME = "name";
    private static final String KEY_DOB = "birthday";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_INITWEIGHT = "initWeight";
    private static final String KEY_INITDATE = "initDate";
    private static final String ROW_ID = "_id";
    // Weight Table Field Names
    private static final String KEY_WDATE = "wDate";
    private static final String KEY_WEIGHT = "weight";
    // Record Table Field Names
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DATE = "date";
    private static final String KEY_TIME1 = "time1";
    private static final String KEY_TIME2 = "time2";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_IMG1 = "img1";
    private static final String KEY_IMG2 = "img2";
    private static final String KEY_IMG3 = "img3";

    // Table Create Statements
    private static final String CREATE_TABLE_INFO = "CREATE TABLE " + TABLE_INFO + "("
            + KEY_IMG + " BLOB, " + KEY_NAME + " TEXT NOT NULL," + KEY_DOB + " TEXT NOT NULL,"
            + KEY_GENDER + " TEXT NOT NULL," + KEY_INITWEIGHT + " REAL NOT NULL," + KEY_INITDATE + " TEXT NOT NULL, " + ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL);";
    private static final String CREATE_TABLE_WEIGHT = "CREATE TABLE " + TABLE_WEIGHT + "("
            + KEY_WDATE + " TEXT PRIMARY KEY NOT NULL," + KEY_WEIGHT + " REAL NOT NULL);";
    private static final String CREATE_TABLE_RECORD = "CREATE TABLE " + TABLE_RECORD + "("
            + KEY_CATEGORY + " TEXT NOT NULL," + KEY_TITLE + " TEXT,"
            + KEY_DATE + " TEXT NOT NULL," + KEY_TIME1 + " TEXT NOT NULL," + KEY_TIME2 + " TEXT NOT NULL,"
            + KEY_CONTENT + " TEXT," + KEY_IMG1 + " BLOB," + KEY_IMG2 + " BLOB," + KEY_IMG3 + " BLOB, " + ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL);";


    private final Context context;

    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_INFO);
            db.execSQL(CREATE_TABLE_WEIGHT);
            db.execSQL(CREATE_TABLE_RECORD);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // on upgrade drop older tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_INFO);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORD);

            // create new tables
            onCreate(db);
        }
    }

    public DBAdapter(Context context) {
        this.context = context;
    }

    public DBAdapter open() { //throws SQLException
        mDBHelper = new DatabaseHelper(context);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDBHelper.close();
    }


    // Create new Info
    public long createInfo(byte[] img, String name, String birthday, String gender, float initWeight, String initDate) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_IMG, img);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_DOB, birthday);
        initialValues.put(KEY_GENDER, gender);
        initialValues.put(KEY_INITWEIGHT, initWeight);
        initialValues.put(KEY_INITDATE, initDate);
        mDB.insert(TABLE_INFO, null, initialValues);
        return 0;
    }

    public Cursor fetchInfo() { //throws SQLException
        Cursor mCursor =
                mDB.query(true, TABLE_INFO, new String[]{KEY_IMG, KEY_NAME, KEY_DOB, KEY_GENDER, KEY_INITWEIGHT, KEY_INITDATE}, null, null, null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();
        return mCursor;
    }

    public boolean deleteInfo(int rowID) {
        return mDB.delete(TABLE_INFO, ROW_ID + "= '" + rowID + "'", null) > 0;
    }

    public boolean updateInfo(int rowID, byte[] img, String name, String birthday, String gender, float initWeight, String initDate) {
        ContentValues args = new ContentValues();
        args.put(KEY_IMG, img);
        args.put(KEY_NAME, name);
        args.put(KEY_DOB, birthday);
        args.put(KEY_GENDER, gender);
        args.put(KEY_INITWEIGHT, initWeight);
        args.put(KEY_INITDATE, initDate);

        return mDB.update(TABLE_INFO, args, ROW_ID + "= '" + rowID + "'", null) > 0;
    }


    // Create or Update Weight
    public long createWeight(String wDate, float weight) {
        ContentValues values = new ContentValues();
        values.put(KEY_WDATE, wDate);
        values.put(KEY_WEIGHT, weight);
        Cursor mCursor = mDB.query(TABLE_WEIGHT, new String[]{KEY_WDATE}, KEY_WDATE + "= '" + wDate + "'", null, null, null, null);
        if (mCursor.getCount() > 0) {
            Log.v("DB WORK TEST", "createWeight UPDATE");
            return mDB.update(TABLE_WEIGHT, values, KEY_WDATE + "='" + wDate + "'", null);
        } else {
            Log.v("DB WORK TEST", "createWeight INSERT");
            return mDB.insert(TABLE_WEIGHT, null, values);
        }
    }

    public boolean deleteWeight(String wDate) {   // 삭제되면 true 안되면 false 반환함
        Log.v("DB WORK TEST", "deleteWeight DELETE");
        return mDB.delete(TABLE_WEIGHT, KEY_WDATE + "= '" + wDate + "'", null) > 0;
    }

    public Cursor fetchWeight() {
        Cursor mCursor =
                mDB.query(TABLE_WEIGHT, new String[]{KEY_WDATE, KEY_WEIGHT}, null, null, null, null, KEY_WDATE + " DESC");
        mCursor.moveToFirst();
        //"printf(\"%.2f\"," + KEY_WEIGHT +") AS " + KEY_WEIGHT2
        return mCursor;
    }


    // Create new Record
    public long createRecord(String category, String title, String date, String time1, String time2, String content, byte img1, byte img2, byte img3) {
        ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY, category);
        values.put(KEY_TITLE, title);
        values.put(KEY_DATE, date);
        values.put(KEY_TIME1, time1);
        values.put(KEY_TIME2, time2);
        values.put(KEY_CONTENT, content);
        values.put(KEY_IMG1, img1);
        values.put(KEY_IMG2, img2);
        values.put(KEY_IMG3, img3);
        return mDB.insert(TABLE_RECORD, null, values);
    }

    public boolean deleteRecord(String category, String title, String date, String time1) {
        return mDB.delete(TABLE_RECORD, KEY_DATE + "= '" + date + "' AND " + KEY_CATEGORY + "= '" + category + "' AND " + KEY_TITLE + "= '" + title + "' AND " + KEY_TIME1 + "= '" + time1 + "'", null) > 0;
    }

    public Cursor fetchTitles(String category) {
        Cursor mCursor = mDB.query(true, TABLE_RECORD, new String[]{KEY_TITLE}, KEY_CATEGORY + "= '" + category + "'", null, null, null, null, null);
        if (mCursor != null)
            mCursor.moveToNext();
        return mCursor;
    }

    // Log_Monthly 에서 선택한 카테고리 추출
    public Cursor fetchCategory(ArrayList category, String date, int cOrder) { //throws SQLException
        String selection = "";
        if (category.size() == 0) {
            selection = "";
        }else{
            selection = " AND " + KEY_CATEGORY + " IN('" + category.get(0);
            if (category.size() == 1) {
                selection += "')";
            } else {
                for (int i = 1; i < category.size(); i++) {
                    selection += "', '" + category.get(i);
                }
                selection += "')";
            }
        }
        Log.v("fetchDailyRecord TEST", "selection : " + selection);
        String order = (cOrder == 0) ? KEY_CATEGORY + " ASC" : KEY_CATEGORY + " DESC";
        Cursor mCursor =
                mDB.query(true, TABLE_RECORD, new String[]{KEY_CATEGORY}, KEY_DATE + "= '" + date + "'" +  selection, null, null, null, order, null);
        if (mCursor != null)
            mCursor.moveToFirst();
        return mCursor;
    }

    public Cursor fetchThisRecord(String category, String title, String date, String time1) {
        Cursor mCursor =
                mDB.query(TABLE_RECORD, new String[]{KEY_CATEGORY, KEY_TITLE, KEY_DATE, KEY_TIME1, KEY_TIME2, KEY_CONTENT, KEY_IMG1, KEY_IMG2, KEY_IMG3, ROW_ID},
                        KEY_DATE + "= '" + date + "' AND " + KEY_CATEGORY + "= '" + category + "' AND " + KEY_TITLE + "= '" + title + "' AND " + KEY_TIME1 + "= '" + time1 + "'",
                        null, null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();
        return mCursor;
    }

    public Cursor fetchAllRecords() {
        return mDB.query(TABLE_RECORD, new String[]{KEY_CATEGORY, KEY_TITLE, KEY_DATE, KEY_TIME1, KEY_TIME2, KEY_CONTENT, KEY_IMG1, KEY_IMG2, KEY_IMG3}, null, null, null, null, null);
    }

    public Cursor fetchRecord(String date, int timeOrder) { //throws SQLException

        String order = (timeOrder == 0) ? KEY_TIME1 + " ASC" : KEY_TIME1 + " DESC";
        Cursor mCursor =
                mDB.query(true, TABLE_RECORD, new String[]{KEY_CATEGORY, KEY_TITLE, KEY_DATE, KEY_TIME1, KEY_TIME2, KEY_CONTENT, KEY_IMG1, KEY_IMG2, KEY_IMG3}, KEY_DATE + "= '" + date + "'", null, null, null, order, null);
        if (mCursor != null)
            mCursor.moveToFirst();
        return mCursor;
    }

    public Cursor fetchCategoryRecord(String category, String date, int timeOrder) { //throws SQLException

        String order = (timeOrder == 0) ? KEY_TIME1 + " ASC" : KEY_TIME1 + " DESC";
        Cursor mCursor =
                mDB.query(true, TABLE_RECORD, new String[]{KEY_CATEGORY, KEY_TITLE, KEY_DATE, KEY_TIME1, KEY_TIME2, KEY_CONTENT, KEY_IMG1, KEY_IMG2, KEY_IMG3}, KEY_DATE + "= '" + date + "' AND " + KEY_CATEGORY + "= '" + category + "'", null, null, null, order, null);
        if (mCursor != null)
            mCursor.moveToFirst();
        return mCursor;
    }

    public Cursor fetchDailyRecord(ArrayList category, String date, int timeOrder) { //throws SQLException
        String selection = "";
        if (category.size() == 0) {
            selection = "";
        }else{
            selection = " AND " + KEY_CATEGORY + " IN('" + category.get(0);
            if (category.size() == 1) {
                selection += "')";
            } else {
                for (int i = 1; i < category.size(); i++) {
                    selection += "', '" + category.get(i);
                }
                selection += "')";
            }
        }


        Log.v("fetchDailyRecord TEST", "selection : " + selection);
        String order = (timeOrder == 0) ? KEY_TIME1 + " ASC" : KEY_TIME1 + " DESC";
        Cursor mCursor =
                mDB.query(true, TABLE_RECORD, new String[]{KEY_CATEGORY, KEY_TITLE, KEY_DATE, KEY_TIME1, KEY_TIME2, KEY_CONTENT, KEY_IMG1, KEY_IMG2, KEY_IMG3}, KEY_DATE + "= '" + date + "'" +  selection, null, null, null, order, null);
        if (mCursor != null)
            mCursor.moveToFirst();
        return mCursor;
    }

    public Cursor fetchToiletRecord(String title, String date, int timeOrder) { //throws SQLException

        String order = (timeOrder == 0) ? KEY_TIME1 + " ASC" : KEY_TIME1 + " DESC";
        Cursor mCursor =
                mDB.query(true, TABLE_RECORD, new String[]{KEY_CATEGORY, KEY_TITLE, KEY_DATE, KEY_TIME1, KEY_TIME2, KEY_CONTENT, KEY_IMG1, KEY_IMG2, KEY_IMG3}, KEY_DATE + "= '" + date + "' AND " + KEY_TITLE + "= '" + title + "'", null, null, null, order, null);
        if (mCursor != null)
            mCursor.moveToFirst();
        return mCursor;
    }

    public boolean updateRecord(int rowID, String category, String title, String date, String time1, String time2, String content, byte img1, byte img2, byte img3) {
        ContentValues args = new ContentValues();
        args.put(KEY_CATEGORY, category);
        args.put(KEY_TITLE, title);
        args.put(KEY_DATE, date);
        args.put(KEY_TIME1, time1);
        args.put(KEY_TIME2, time2);
        args.put(KEY_CONTENT, content);
        args.put(KEY_IMG1, img1);
        args.put(KEY_IMG2, img2);
        args.put(KEY_IMG3, img3);

        return mDB.update(TABLE_RECORD, args, ROW_ID + "= '" + rowID + "'", null) > 0;
    }

boolean update;
    public boolean askUpdate() {
        AlertDialog.Builder dlg = new AlertDialog.Builder(context);
        dlg.setMessage("같은 기록이 있어요. 덮어쓸까요?");
        dlg.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               update = true;
             }
        });
        dlg.setNegativeButton("아니요.새로 쓸래요.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                update = false;
            }
        });
        dlg.show();

        return update;

    }
}


