package com.example.dam_5;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dam_5.utilities.GlobalVariables;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    EditText email_input;
    EditText psw_input;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //firebaseauth instance created
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        if(mUser!=null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            LoginActivity.this.startActivity(intent);
        }
        //progressdialog
        progressDialog = new ProgressDialog(this);


        //credentials
        email_input = findViewById(R.id.email_login_input);
        psw_input = findViewById(R.id.password_login_input);
        Button btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logInUser();
            }
        });


        Button btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.dismiss();
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                LoginActivity.this.startActivity(intent);

            }
        });
    }

    private void logInUser() {
        String email = email_input.getText().toString();
        String psw = psw_input.getText().toString();
        if(email.isEmpty() || psw.isEmpty()){
            Toast.makeText(getApplicationContext(), "One or more fields are empty", Toast.LENGTH_LONG).show();
        }
        else{
            progressDialog.setTitle("Logging in");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            //firebase login flow
            mAuth.signInWithEmailAndPassword(email, psw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        LoginActivity.this.startActivity(intent);
                    }
                    //in case of unsuccessful login
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Error while logging in: " + task.getException(), Toast.LENGTH_LONG).show();

                    }
                }
            });

        }
    }

    //snippet to add to all the other sections to verify if user is
    //authorized to view this
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        /*FirebaseUser currentUser = mAuth.getCurrentUser();*/


        //updateUI(currentUser);
    }
}