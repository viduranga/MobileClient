package com.aslan.uom.shoppinglist;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.os.Handler;

import com.aslan.uom.shoppinglist.db.Item;
import com.aslan.uom.shoppinglist.db.ItemDBHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends ListActivity {

    private ItemDBHelper helper;
    private ListAdapter listAdapter;

    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private int UIInterval = 500; // 5 seconds by default, can be changed later
    private Handler mHandler = new Handler();

    static int userID;
    private int groupID = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//         Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
//         startActivity(i);

        userID = 122;
        sendNewUserMessage();
        sendConnectToGroupMessage();

        updateUI();

        startRepeatingTask();
//        mHandler.postDelayed(mStatusChecker, mInterval);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            sync(); //this function can change value of mInterval.
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    Runnable UIUpdater = new Runnable() {
        @Override
        public void run() {
            updateUI();
            mHandler.postDelayed(UIUpdater, UIInterval);
        }
    };
    void startRepeatingTask() {
        Thread t1 = new Thread(mStatusChecker);
        Thread t2 = new Thread(UIUpdater);
        t1.start();
        t2.start();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
        mHandler.removeCallbacks(UIUpdater);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_item:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Add an item");
                builder.setMessage("What do you want to buy?");
                final EditText inputField = new EditText(this);
                builder.setView(inputField);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Log.d("MainActivity",inputField.getText().toString());
                        String item = inputField.getText().toString();
                        Log.d("MainActivity", item);

                        //function to add item
                        addItem(item);

                    }

                });


                builder.setNegativeButton("Cancel",null);

                builder.create().show();
                return true;

            default:
                return false;
        }
    }

    // method to add item
    private void addItem(String item){
        sendAddMessage(item);

        helper = new ItemDBHelper(MainActivity.this);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.clear();
        values.put(Item.Columns.ITEM, item);
        values.put(Item.Columns.done, "false");


        db.insertWithOnConflict(Item.TABLE, null, values,
                SQLiteDatabase.CONFLICT_IGNORE);

        updateUI();
    }

    private void updateUI() {
        helper = new ItemDBHelper(MainActivity.this);
        SQLiteDatabase sqlDB = helper.getReadableDatabase();
        Cursor cursor = sqlDB.query(Item.TABLE,
                new String[]{Item.Columns._ID, Item.Columns.ITEM},
                null, null, null, null, null);

        listAdapter = new SimpleCursorAdapter(
                this,
                R.layout.item_view,
                cursor,
                new String[] { Item.Columns.ITEM},
                new int[] { R.id.itemTextView},
                0
        );
        this.setListAdapter(listAdapter);

//        for(int i=0;i<listAdapter.getCount();i++){
//            String item = listAdapter.getItem(i).toString();
//            Log.d("++++ " , item );
//            Cursor cur = getData(item);
//            cur.moveToFirst();
//            String done = cur.getString(cur.getColumnIndex(Item.Columns.done));
//            if (!cur.isClosed())
//            {
//                cur.close();
//            }
//
//            if (done.equals("true")){
////                TextView itemTextView = (TextView) findViewById(R.id.itemTextView);
////                Button doneButton = (Button) findViewById(R.id.doneButton);
////                itemTextView.setTextColor(Color.RED);
////                doneButton.setEnabled(false);
//            }
//        }
    }

    public void onDeleteButtonClick(View view) {
        View v = (View) view.getParent();
        TextView itemTextView = (TextView) v.findViewById(R.id.itemTextView);
        String item = itemTextView.getText().toString();

        // function to delete item
        deleteItem(item);
    }

    // method to delete item
    private void deleteItem(String item){
        sendRemoveMessage(item);

        String sql = String.format("DELETE FROM %s WHERE %s = '%s'",
                Item.TABLE,
                Item.Columns.ITEM,
                item);


        helper = new ItemDBHelper(MainActivity.this);
        SQLiteDatabase sqlDB = helper.getWritableDatabase();
        sqlDB.execSQL(sql);
        updateUI();
    }

    public void onDoneButtonClick(View view) {
        View v = (View) view.getParent();
        TextView itemTextView = (TextView) v.findViewById(R.id.itemTextView);
        String item = itemTextView.getText().toString();

        Button doneButton = (Button) v.findViewById(R.id.doneButton);
        itemTextView.setTextColor(Color.RED);
        doneButton.setEnabled(false);

        //String sql = "UPDATE "+TABLE_NAME +" SET " + ColumnName+ " = '"+newValue+"' WHERE "+Column+ " = "+rowId;

//        String TRUE = "true";
//        String sql = "UPDATE "+Item.TABLE +" SET " + Item.Columns.done + " = '"+ TRUE +"' WHERE "+Item.Columns.ITEM+ " = "+item;
//        helper = new ItemDBHelper(MainActivity.this);
//        SQLiteDatabase sqlDB = helper.getWritableDatabase();
//        sqlDB.execSQL(sql);

        updateItem(item, "true");


        //updateUI();
    }

    public boolean updateItem (String item, String done)
    {
        sendMarkMessage(item);

        SQLiteDatabase sqlDB = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //contentValues.put(Item.Columns.ITEM, item);
        contentValues.put(Item.Columns.done, done);

        sqlDB.update(Item.TABLE, contentValues, "item = ? ", new String[]{item});
        return true;
    }

    public Cursor getData(String item){
        SQLiteDatabase sqlDB = helper.getReadableDatabase();
        Cursor res =  sqlDB.rawQuery( "select done from items where item="+item+"", null );
        return res;
    }

    private void sync(){
        String message = Commands._sync + ":";
        message += (userID + ":");
        message += (groupID);

        RecieveMessage recieveMessage = new RecieveMessage(message);
        recieveMessage.execute();

    }



    private void sendNewUserMessage(){

        String message = Commands._newUser + ":";
        message += (userID);

        SendMessage sendMessageTask = new SendMessage(message);
        sendMessageTask.execute();

    }

    private void sendNewGroupMessage(){

        String message = Commands._newGroup + ":";
        message += (userID + ":");
        message += (groupID);

        SendMessage sendMessageTask = new SendMessage(message);
        sendMessageTask.execute();
    }

    private void sendConnectToGroupMessage(){
        String message = Commands._connectToGroup + ":";
        message += (userID + ":");
        message += (groupID);


        SendMessage sendMessageTask = new SendMessage(message);
        sendMessageTask.execute();
    }

    private void sendAddMessage(String item){
        sendMessage(Commands._add, item);
    }

    private void sendRemoveMessage(String item){
        sendMessage(Commands._remove, item);
    }

    private void sendMarkMessage(String item){
        sendMessage(Commands._mark, item);
    }

    private void sendMessage(String command, String item){

        String message = command + ":";
        message += (userID + ":");
        message += (groupID + ":");
        message += item;

        SendMessage sendMessageTask = new SendMessage(message);
        sendMessageTask.execute();
    }

    private class SendMessage extends AsyncTask<Void, Void, Void> {

        private String ServerIP = "52.89.223.19";

        String message;

        public SendMessage(String message){
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                Socket client = new Socket(ServerIP, 4444); // connect to the server
                PrintWriter printwriter = new PrintWriter(client.getOutputStream(), true);
                printwriter.println(message); // write the message to output stream


                printwriter.flush();
                printwriter.close();
                client.close(); // closing the connection

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private class RecieveMessage extends AsyncTask<Void, Void, Void> {

        String message;

        public RecieveMessage(String message){
            this.message = message;
        }

        private String ServerIP = "52.89.223.19";
        @Override
        protected Void doInBackground(Void... params) {
            try {

                Socket client = new Socket(ServerIP, 4444); // connect to the server
                PrintWriter printwriter = new PrintWriter(client.getOutputStream(), true);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                printwriter.println(message); // write the message to output stream

                printwriter.flush();
                try {
                    String response = bufferedReader.readLine();
                    if(response!=null && !response.equals("null")) {
                        while (!response.equals("e")) {
                            Log.d("response ", response);
                            processMessage(response);
                            response = bufferedReader.readLine();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                printwriter.close();
                bufferedReader.close();
                client.close(); // closing the connection

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void processMessage(String message){
            Log.d("message", message);
            String[] lines = message.split(":");
            String command = lines[0];

            int userID = Integer.parseInt(lines[1]);
            int groupID = Integer.parseInt(lines[2]);

            String item = lines[3];
            if(command.equals(Commands._add)){
                helper = new ItemDBHelper(MainActivity.this);
                SQLiteDatabase db = helper.getWritableDatabase();
                ContentValues values = new ContentValues();

                values.clear();
                values.put(Item.Columns.ITEM, item);
                values.put(Item.Columns.done, "false");


                db.insertWithOnConflict(Item.TABLE, null, values,
                        SQLiteDatabase.CONFLICT_IGNORE);

                //updateUI();
            }else if(command.equals(Commands._remove)){
                String sql = String.format("DELETE FROM %s WHERE %s = '%s'",
                        Item.TABLE,
                        Item.Columns.ITEM,
                        item);


                helper = new ItemDBHelper(MainActivity.this);
                SQLiteDatabase sqlDB = helper.getWritableDatabase();
                sqlDB.execSQL(sql);
                //updateUI();
            }else if(command.equals(Commands._mark)){
                String done = "true";
                SQLiteDatabase sqlDB = helper.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                //contentValues.put(Item.Columns.ITEM, item);
                contentValues.put(Item.Columns.done, done);

                sqlDB.update(Item.TABLE, contentValues, "item = ? ", new String[]{item});
            }
        }
    }
}
