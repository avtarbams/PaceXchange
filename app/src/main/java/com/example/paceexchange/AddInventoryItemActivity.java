package com.example.paceexchange;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddInventoryItemActivity extends AppCompatActivity {


    private EditText mNewItemInput, mNewItemTagInput;
    private Button mSubmitItemButton, mUploadInventoryPicture;
    private Spinner mUserItemSpinner, mReturnItemSpinner;
    private ArrayAdapter<CharSequence> mItemAdapter;
    private String mNewItemName, mNewImageTag, mNewItemCategory, mReturnItemCategory, mUserIdentification, mNewItemImage;
    private ImageView mImageThumbnail;
    private FirebaseFirestore mFirebaseDatabase;
    private CollectionReference mFirebaseInventoryCollection;
    private Map<String, Object> mFirebaseInventoryMap;
    private ProgressBar  mProgressBar;

    String currentPhotoPath;

    private static final String CHANNEL_ID = "com.example.keithinacio.NOTIFICATION";
    private static final String CHANNEL_NAME = "com.example.keithinacio.DICTIONARY_NOTIFICATION";
    private static final String CHANNEL_DESC = "com.example.keithinacio.NEW_WORD_NOTIFICATION";
    private static final int NOTIFICATION_ID = 001;

    //Added by Avtar
    static final int REQUEST_IMAGE_CAPTURE = 100;
    private StorageReference mStorage;

    private ArrayList<InventoryData> mCurrentInventorylist;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_inventory);

        Intent intent = getIntent();
        mUserIdentification = intent.getStringExtra(CurrentInventoryActivity.USER_IDENTIFICATION_ADD_ITEM_MESSAGE);

        mNewItemInput = findViewById(R.id.itemNameInput);
        mNewItemTagInput = findViewById(R.id.itemTagEditText);
        mSubmitItemButton = findViewById(R.id.submitNewItemButton);
        mUserItemSpinner=findViewById(R.id.userItemSpinner);
        mReturnItemSpinner=findViewById(R.id.returnItemSpinner);
        mUploadInventoryPicture = findViewById(R.id.uploadInventoryPicture);
        mImageThumbnail = findViewById(R.id.uploadImageThumbnail);
        mFirebaseDatabase = FirebaseFirestore.getInstance();
        mFirebaseInventoryCollection = mFirebaseDatabase.collection("inventory");
        mFirebaseInventoryMap = new HashMap<>();
        mNewItemImage = "https://drff4istxjic.cloudfront.net/no_photo_640_640_pad.jpg";
        mCurrentInventorylist = new ArrayList<>();

        mStorage = FirebaseStorage.getInstance().getReference();

        mProgressBar = (ProgressBar)findViewById(R.id.progress);
        Sprite wave = new Wave();
        mProgressBar.setIndeterminateDrawable(wave);


        setOnItemMenuClickListener();
        setOnButtonClickListener();

        mUploadInventoryPicture.setOnClickListener(V -> {
            dispatchTakePictureIntent();
        });

        //establish a notification channel
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);

            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    }

//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
//    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.paceexchange.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Uri uri = Uri.parse(currentPhotoPath);
            mImageThumbnail.setImageURI(uri);
            mProgressBar.setVisibility(View.VISIBLE);

            // Get the data from an ImageView as bytes
            mImageThumbnail.setDrawingCacheEnabled(true);
            mImageThumbnail.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) mImageThumbnail.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();
            StorageReference filepath = mStorage.child("InventoryPhotos").child(uri.getLastPathSegment());
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
                        mProgressBar.setVisibility(View.GONE);
                        mImageThumbnail.setVisibility(View.VISIBLE);
                        Uri downloadUri = task.getResult();
                        mNewItemImage = downloadUri.toString();
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        }
    }


    public void getNewItemData() {


        if (mNewItemInput.getText().toString().isEmpty() || mNewItemTagInput.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.error_new_item_name, Toast.LENGTH_LONG).show();
        } else {
            mNewItemName = mNewItemInput.getText().toString().trim();
            mNewImageTag = mNewItemTagInput.getText().toString().trim();
        }
    }

    public void addItemToFirebaseInventory(){
        mFirebaseInventoryCollection.document(mUserIdentification).update("Items", FieldValue.arrayUnion(new InventoryData(mNewItemCategory, mNewItemName, mReturnItemCategory, mNewItemImage, mNewImageTag)));
    }

    public void setOnButtonClickListener() {

        mSubmitItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNewItemData();
                addItemToFirebaseInventory();
                displayNotification();
            }
        });
    }

    public void setOnItemMenuClickListener() {

        mItemAdapter = ArrayAdapter.createFromResource(this, R.array.item_array, android.R.layout.simple_spinner_item);
        mItemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mUserItemSpinner.setAdapter(mItemAdapter);
        mReturnItemSpinner.setAdapter(mItemAdapter);

        mUserItemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mNewItemCategory = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mReturnItemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mReturnItemCategory= (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void displayNotification() {


        //build the notification message for the addition of a new word
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_thumb_up_black_24dp).setContentTitle(getResources().getString(R.string.added_item_notification_title)).setContentText(getResources().getString(R.string.new_item_added_notification, mNewItemName));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //build notification manager to display notification
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

}
