package com.example.paceexchange;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private TextView mUserName, mEmail, mGraduationDate, mUserReputation;
    private LinearLayout mAuctionButton, mInventoryButton, mAvailableTradeItemsButton;
    private ImageView mLogoutButton;
    private FirebaseAuth mFirebaseAuthorization;
    private String mUserIdentification;

    private FirebaseFirestore mFirestoreInventoryDatabase;
    private CollectionReference mFirestoreInventoryCollection;

    private CircleImageView mCircleImageView;

    private StorageReference mStorage;
    private ProgressBar mProgressBar;

    private String loggedInUser;

    public static final String USER_IDENTIFICATION_INVENTORY_MESSAGE = "com.example.paceexchange.USERID";
    public static final int GALLERY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Intent intent = getIntent();
        mUserIdentification = intent.getStringExtra(LoginActivity.USER_IDENTIFICATION_PROFILE_MESSAGE);

        mFirebaseAuthorization = FirebaseAuth.getInstance();
        mFirestoreInventoryDatabase = FirebaseFirestore.getInstance();
        mFirestoreInventoryCollection = mFirestoreInventoryDatabase.collection("profiles");
        mStorage = FirebaseStorage.getInstance().getReference();
        mProgressBar = (ProgressBar)findViewById(R.id.profile_image_upload_progress);
        Sprite wave = new Wave();
        mProgressBar.setIndeterminateDrawable(wave);
        mUserName = findViewById(R.id.name);
        mGraduationDate = findViewById(R.id.graduation);
        mEmail = findViewById(R.id.email);
        mUserReputation = findViewById(R.id.rating);
        mLogoutButton = findViewById(R.id.logoutButton);
        mAuctionButton = findViewById(R.id.auctionButton);
        mInventoryButton = findViewById(R.id.inventoryButton);
        mAvailableTradeItemsButton = findViewById(R.id.availableItems);
        mCircleImageView = findViewById(R.id.profile_image);

        loggedInUser = LoggedInUser.getInstance().getmLoogedInUser();

        setButtonClickListener();
        setProfileDataFromFirebase();
        subscribeToMessageTopic();
    }

    private void subscribeToMessageTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("auction")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "subscription failed", Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(getApplicationContext(), "subscribed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void setProfileDataFromFirebase() {

        DocumentReference docRef = mFirestoreInventoryCollection.document(mUserIdentification);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        mEmail.setText(getResources().getString(R.string.profile_email, document.getData().get("Email").toString()));
                        mUserName.setText(getResources().getString(R.string.profile_name_display, document.getData().get("First Name"), document.getData().get("Last Name")).toUpperCase());
                        mGraduationDate.setText(getResources().getString(R.string.profile_grad_year, document.getData().get("Graduation").toString()));
                        mUserReputation.setText(getResources().getString(R.string.profile_rating, document.getData().get("Reputation").toString()));
                        if(document.getData().get("profileUrl")!=null){
                            Picasso.get().load(document.getData().get("profileUrl").toString()).into(mCircleImageView);
                        }else {

                        }
                        setUserReputation(Integer.parseInt(document.getData().get("Reputation").toString()));
                    } else {
                        Log.d("KLEITH", "No such document");
                    }
                } else {
                    Log.d("LEITH", "get failed with ", task.getException());
                }
            }
        });

    }


    /**
     * This method sets the color and reputation rating displayed in the user profile
     **/

    public void setUserReputation(int userRating) {

        if (userRating < 60) {
            mUserReputation.setText(R.string.poor_reputation);
            mUserReputation.setTextColor(Color.RED);
        } else if (userRating >= 60 && userRating < 80) {
            mUserReputation.setText(R.string.average_reputation);
            mUserReputation.setTextColor(Color.MAGENTA);
        } else if (userRating >= 80 && userRating < 90) {
            mUserReputation.setText(R.string.very_good_reputation);
            mUserReputation.setTextColor(Color.BLUE);
        } else if (userRating >= 90 && userRating <= 100) {
            mUserReputation.setText(R.string.excellent_reputation);
            mUserReputation.setTextColor(Color.GREEN);
        }


    }


    public void changeProfilePicture(View view){
        //Create an Intent with action as ACTION_PICK
        Intent intent=new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    //data.getData returns the content URI for the selected Image
                    Uri selectedImage = data.getData();
                    mCircleImageView.setImageURI(selectedImage);
                    mProgressBar.setVisibility(View.VISIBLE);
                    Bitmap bitmap = ((BitmapDrawable) mCircleImageView.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageData = baos.toByteArray();
                    StorageReference filepath = mStorage.child("profilePictures").child(selectedImage.getLastPathSegment());
                    UploadTask uploadTask = filepath.putBytes(imageData);
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return filepath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                mFirestoreInventoryCollection.document(mUserIdentification).update("profileUrl", downloadUri.toString());
                                mProgressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), getString(R.string.profile_image_uploaded), Toast.LENGTH_LONG).show();
                            } else {
                                // Handle failures
                                // ...
                            }
                        }
                    });

                    break;
            }

    }

    /**
     * This method sets a click listener to the logout, auction, and invetory buttons
     **/

    public void setButtonClickListener() {

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseAuthorization.signOut();
                finish();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));

            }
        });

        mAuctionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  Intent intent = new Intent(getApplicationContext(), AuctionActivity.class);
                intent.putExtra(USER_IDENTIFICATION_INVENTORY_MESSAGE, mUserIdentification);
                startActivity(intent);*/
                Intent intent = new Intent(getApplicationContext(), AddAuctionItemActivity.class);
                intent.putExtra(USER_IDENTIFICATION_INVENTORY_MESSAGE, mUserIdentification);
                startActivity(intent);
            }
        });

        mInventoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CurrentInventoryActivity.class);
                intent.putExtra(USER_IDENTIFICATION_INVENTORY_MESSAGE, mUserIdentification);
                startActivity(intent);

            }
        });

        mAvailableTradeItemsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ItemsForTradeActivity.class);
                intent.putExtra(USER_IDENTIFICATION_INVENTORY_MESSAGE, mUserIdentification);
                startActivity(intent);

            }
        });


    }

}


