package com.aslan.uom.shoppinglist.db;

/**
 * Created by King on 21-Jan-16.
 */

import android.provider.BaseColumns;

public class Item {

    public static final String DB_NAME = "com.aslan.uom.shoppinglist.db.items";
    public static final int DB_VERSION = 1;
    public static final String TABLE = "items";

    public class Columns {
        public static final String ITEM = "item";
        public static final String _ID = BaseColumns._ID;
        public static final String done = "done";
    }

}
