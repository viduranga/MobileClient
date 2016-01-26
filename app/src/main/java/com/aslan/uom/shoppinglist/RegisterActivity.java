package com.aslan.uom.shoppinglist;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by King on 21-Jan-16.
 */
public class RegisterActivity extends Activity {
    // UI elements
    EditText txtName, txtMobileNo;

    // Register button
    Button btnRegister;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        // Check server configuration is set

        /***

        if (SERVER_URL == null || SENDER_ID == null || SERVER_URL.length() == 0
                || SENDER_ID.length() == 0) {

            return;
        }
         ***/

        txtName = (EditText) findViewById(R.id.txtName);
        txtMobileNo = (EditText) findViewById(R.id.txtMobileNo);
        btnRegister = (Button) findViewById(R.id.btnRegister);

		/*
		 * Click event on Register button
		 */
        btnRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Read EditText dat

                String mobileNo = txtMobileNo.getText().toString();

                int userID = Integer.parseInt(mobileNo);
                MainActivity.userID = userID;

            }
        });
    }

}
