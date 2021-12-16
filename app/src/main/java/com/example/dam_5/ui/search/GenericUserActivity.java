package com.example.dam_5.ui.search;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dam_5.R;
import com.example.dam_5.utilities.User;

public class
GenericUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        User currentUser = User.getUserFromEmail(email);
        Log.d("TESTUSER", email);

        ImageView proPic = (ImageView) findViewById(R.id.generic_user_profile_picture);
        TextView username = (TextView) findViewById(R.id.generic_user_username);
        username.setText(currentUser.getUsername());




        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_user);
    }
}