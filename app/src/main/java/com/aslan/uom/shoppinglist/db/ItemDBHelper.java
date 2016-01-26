package com.aslan.uom.shoppinglist.db;

/**
 * Created by King on 21-Jan-16.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ItemDBHelper extends SQLiteOpenHelper {

    public ItemDBHelper(Context context) {
        super(context, Item.DB_NAME, null, Item.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqlDB) {
        String sqlQuery =
                String.format("CREATE TABLE %s (" +
                                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s TEXT, " + "%s TEXT)" , Item.TABLE,
                        Item.Columns.ITEM, Item.Columns.done);


        Log.d("ItemDBHelper","Query to form table: "+sqlQuery);
        sqlDB.execSQL(sqlQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqlDB, int i, int i2) {
        sqlDB.execSQL("DROP TABLE IF EXISTS "+Item.TABLE);
        onCreate(sqlDB);
    }
}
