package com.example.paceexchange;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText mEmailEditText, mPasswordEditText, mFirstNameEditText, mLastNameEditText, mGradDateEditText;
    private Button mRegisterButton;
    private ProgressDialog mProgressUpdate;

    private FirebaseAuth mFirebaseAuthorization;
    private FirebaseFirestore mFirebaseDatabase;
    private CollectionReference mFirebaseInventoryCollection;
    private CollectionReference mFirebaseProfileCollection;
    private CollectionReference ref;
    private Map<String, Object> mFirebaseProfileMap;
    private Map<String, Object> mFirebaseInventoryMap;

    /*** Default Image Initialized ***/
    private final String PROFILE_URL = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        mEmailEditText = findViewById(R.id.email);
        mPasswordEditText = findViewById(R.id.password);
        mFirstNameEditText = findViewById(R.id.firstname);
        mLastNameEditText = findViewById(R.id.lastname);
        mGradDateEditText = findViewById(R.id.graduation);
        mRegisterButton = findViewById(R.id.registerButton);

        mFirebaseAuthorization = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseFirestore.getInstance();
        mFirebaseProfileCollection = mFirebaseDatabase.collection("profiles");
        mFirebaseInventoryCollection = mFirebaseDatabase.collection("inventory");
        mFirebaseInventoryMap = new HashMap<>();
        mFirebaseProfileMap = new HashMap<>();

        mProgressUpdate = new ProgressDialog(this);
        setOnClickListener();
    }

    /**
     * This method sets a click listener to the registration button
     **/

    public void setOnClickListener() {

        mRegisterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                registerClient();
            }

        });

    }

    /**
     * This method validates user input and assigns same to String variables. The supplied email and password are sent to firebase for authorization.
     **/

    private void registerClient() {

        if ((mEmailEditText.getText().toString().isEmpty()) || (!mEmailEditText.getText().toString().contains("@")) && (!mEmailEditText.getText().toString().contains(".com") ||
                !mEmailEditText.getText().toString().contains(".edu"))) {

            Toast.makeText(RegistrationActivity.this, R.string.empty_login, Toast.LENGTH_LONG).show();

        } else if (mPasswordEditText.getText().toString().isEmpty()) {

            Toast.makeText(RegistrationActivity.this, R.string.empty_password, Toast.LENGTH_LONG).show();

        } else {

            /**get registration data from user input and send to firebase database**/

            String firstName = mFirstNameEditText.getText().toString().trim();
            String lastName = mLastNameEditText.getText().toString().trim();
            int gradYear = Integer.parseInt(mGradDateEditText.getText().toString().trim());
            String email = mEmailEditText.getText().toString().trim();
            String password = mPasswordEditText.getText().toString().trim();
            int defaultReputation = 70;

            addToFirebaseProfileDatabase(firstName, lastName, gradYear, email, defaultReputation);
            ref = mFirebaseDatabase.collection(email);
            establishFirebaseInventoryDatabase(email);

            mProgressUpdate.setMessage("Completing Registration...");
            mProgressUpdate.show();

            mFirebaseAuthorization.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mProgressUpdate.dismiss();
                        Toast.makeText(RegistrationActivity.this, R.string.register_success, Toast.LENGTH_LONG).show();
                        finish();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    } else {
                        mProgressUpdate.dismiss();
                        Toast.makeText(RegistrationActivity.this, R.string.register_fail, Toast.LENGTH_LONG).show();
                    }
                }
            });

        }
    }

    //add data registration data to firebase
    private void addToFirebaseProfileDatabase(String firstName, String lastName, int gradYear, String email, int reputation) {

        mFirebaseProfileMap.put("First Name", firstName);
        mFirebaseProfileMap.put("Last Name", lastName);
        mFirebaseProfileMap.put("Graduation", gradYear);
        mFirebaseProfileMap.put("Email", email);
        mFirebaseProfileMap.put("Reputation", reputation);
        mFirebaseProfileMap.put("profileUrl", PROFILE_URL);

        mFirebaseProfileCollection.document(email).set(mFirebaseProfileMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        });
    }

    //establish registration data to firebase
    private void establishFirebaseInventoryDatabase(String email) {

        mFirebaseInventoryCollection.document(email).set(mFirebaseInventoryMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        });
    }


}

