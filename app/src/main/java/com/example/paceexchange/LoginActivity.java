package com.example.paceexchange;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmailEditText, mPasswordEditText;
    private TextView mRegistrationLink;
    private Button mLoginButton;
    private ProgressDialog mProgressUpdate;
    private String mEmailInput, mPasswordInput;

    private FirebaseAuth mFirebaseAuthorization;
    private FirebaseFirestore mFirebaseDatabase;
    private CollectionReference mFirebaseProfileCollection;

    public static final String USER_IDENTIFICATION_PROFILE_MESSAGE = "com.example.paceexchange.USERID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailEditText = findViewById(R.id.emailInput);
        mPasswordEditText = findViewById(R.id.passwordInput);
        mRegistrationLink = findViewById(R.id.registerLink);
        mLoginButton = findViewById(R.id.loginButton);

        mPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        mFirebaseAuthorization = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseFirestore.getInstance();
        mFirebaseProfileCollection = mFirebaseDatabase.collection("profiles");

        mProgressUpdate = new ProgressDialog(this);
        setOnClickListener();
    }

    /**
     * This method sets click listener on login button and new user registration link; method calls to login and access registration page
     **/
    public void setOnClickListener() {

        mLoginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                loginClient();
            }

        });

        mRegistrationLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
            }

        });

    }

    /**
     * This method validates user email and password, which is stored in Firebase. If login accepted, redirect to User Profile and pass user ID with intent; notify user
     * if login fails
     **/
    private void loginClient() {

        if (mEmailEditText.getText().toString().isEmpty() || !mEmailEditText.getText().toString().contains("@") && ((!mEmailEditText.getText().toString().contains(".com")) ||
                (!mEmailEditText.getText().toString().contains(".edu")))) {

            Toast.makeText(LoginActivity.this, R.string.empty_login, Toast.LENGTH_LONG).show();

        } else if (mPasswordEditText.getText().toString().isEmpty()) {

            Toast.makeText(LoginActivity.this, R.string.empty_password, Toast.LENGTH_LONG).show();

        } else {

            mEmailInput = mEmailEditText.getText().toString().trim();
            mPasswordInput = mPasswordEditText.getText().toString().trim();

            mProgressUpdate.setMessage("Logging In...");
            mProgressUpdate.show();

            mFirebaseAuthorization.signInWithEmailAndPassword(mEmailInput, mPasswordInput).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        mProgressUpdate.dismiss();
                        Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
                        intent.putExtra(USER_IDENTIFICATION_PROFILE_MESSAGE, mEmailInput);
                        LoggedInUser.getInstance().setmLoogedInUser(mEmailInput);
                        startActivity(intent);
                    } else {
                        mProgressUpdate.dismiss();
                        Toast.makeText(LoginActivity.this, R.string.login_fail, Toast.LENGTH_LONG).show();
                    }
                }
            });

        }
    }

}