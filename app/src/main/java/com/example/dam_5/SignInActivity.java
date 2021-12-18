package com.example.dam_5;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {


    //firebase instantiation
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    //regex to verify if username is valid
    private static final String USERNAME_REGEX = "^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]$";
    private static final Pattern pattern = Pattern.compile(USERNAME_REGEX);

    public static boolean isValid(final String username) {
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        EditText email_input = findViewById(R.id.email_input);
        EditText username_input = findViewById(R.id.input_username);
        EditText psw_input = findViewById(R.id.password_input);
        EditText confirm_psw = findViewById(R.id.confirm_psw_input);
        Button btn_signin = findViewById(R.id.btn_signin);
        ProgressDialog progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();


        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }

            private void registerUser() {
                String email = email_input.getText().toString();
                String psw = psw_input.getText().toString();
                String conf_psw = confirm_psw.getText().toString();
                String username = username_input.getText().toString();

                //check if passwords are equal
                if (!psw.equals(conf_psw)) {
                    confirm_psw.setError("Passwords dont match");
                    return;
                }
                if (username.length() < 4) {
                    username_input.setError("Username must be at least 5 characters long");
                    return;
                }
                if (!isValid(username)) {
                    username_input.setError("The inserted username is not valid");
                    return;
                }
                //check if another user with the same username exists
                db.collection("users")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        //Log.d("DEBUG", document.getId() + " => " + document.getData());
                                        if (document.get("username").equals(username)) {
                                            username_input.setError("There is already a profile with the same username registered");
                                            return;
                                        }
                                    }
                                } else {
                                    Log.w("DEBUG", "Error getting documents.", task.getException());
                                }
                            }
                        });

                if (email.isEmpty() || psw.isEmpty() || conf_psw.isEmpty()) {
                    Toast tst_empty_input = Toast.makeText(getApplicationContext(), "One of the fields has been left empty", Toast.LENGTH_LONG);
                    tst_empty_input.show();
                    return;
                }

                if (psw.length() < 6) {
                    psw_input.setError("Password length must be over 6 characters");
                    return;
                } else {
                    progressDialog.setIndeterminate(true);
                    progressDialog.setTitle("Registration in process");


                    progressDialog.setMessage("Please wait");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    Map<String, Object> data = new HashMap<>();
                    data.put("email", email);
                    data.put("username", username);
                    data.put("pending_received_requests", new LinkedList<String>());
                    data.put("pending_sent_requests", new LinkedList<String>());
                    data.put("friends", new LinkedList<String>());
                    data.put("points", 0);
                    //array with the id of previous hunts
                    data.put("past_hunts", new LinkedList<Object>());
                    //if the user is currently on a hunt, initialized to false
                    data.put("isOnHunt", false);
                    data.put("lastHunt", "");
                    data.put("hasProfilePicture", false);
                    data.put("profilePictureURL", "");

                    CollectionReference users = db.collection("users");

                    users.document(email).set(data);


/*                    db.collection("users").document(email)
                            .set(data);*/


                    mAuth.createUserWithEmailAndPassword(email, psw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                Toast.makeText(SignInActivity.this, "Registration completed", Toast.LENGTH_LONG).show();
                                //move user to home activity
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                SignInActivity.this.startActivity(intent);

                            }
                            //if registration not succesful
                            else {
                                progressDialog.dismiss();
                                Toast.makeText(SignInActivity.this, "Error in registration : " + task.getException().toString(),
                                        Toast.LENGTH_LONG).show();

                            }
                        }
                    });

                }

            }
        });


    }


}