package jp.ac.chiba_fjb.x14b_b.daichan;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import to.pns.lib.AppDB;

/**
 * Created by oikawa on 2016/11/02.
 */

public class CameraDB extends AppDB {
    public CameraDB(Context context) {
        super(context, "app.db", 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
