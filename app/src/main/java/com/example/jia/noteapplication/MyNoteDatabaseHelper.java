package com.example.jia.noteapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by jia on 2017/5/6.
 */

public class MyNoteDatabaseHelper extends SQLiteOpenHelper {
    public  static  final  String CREATE_NOTE=
            "create table Note(Time Date, Title varchar(20) , Picture BLOB ,Kind varchar(4),Plane varchar(100),ChangedTime Date )";

    private Context mcContext;
    public MyNoteDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mcContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_NOTE);
        Log.i("==========","表note 创建成功");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
