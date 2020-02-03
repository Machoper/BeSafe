package edu.virginia.cs4720.besafe;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportActivity extends AppCompatActivity {

    static final int TAKE_PHOTO_PERMISSION = 1;
    static final int REQUEST_TAKE_PHOTO = 2;
    static final int PICK_IMAGE_REQUEST = 3;

    ImageView imageView;
    Button takePictureButton;
    Button reportButton;

    Uri file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        takePictureButton = (Button) findViewById(R.id.takePictureButton);
        imageView = (ImageView) findViewById(R.id.imageView);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            takePictureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE }, TAKE_PHOTO_PERMISSION);
        }

        reportButton = (Button) findViewById(R.id.report_button);
        reportButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent save = new Intent();
                EditText tmp = (EditText) findViewById(R.id.edit_title);
                save.putExtra("name", tmp.getText().toString());
                tmp = (EditText) findViewById(R.id.edit_time);
                save.putExtra("desc", tmp.getText().toString());
                tmp = (EditText) findViewById(R.id.edit_location);
                save.putExtra("lat", tmp.getText().toString());
                tmp = (EditText) findViewById(R.id.edit_desc);
                save.putExtra("lon", tmp.getText().toString());

                setResult(Activity.RESULT_OK, save);
                finishAfterTransition();
            }
        });
    }

    public void takePicture(View view) {
        // Add code here to start the process of taking a picture
        // Note you can send an intent to the camera to take a picture...
        // So start by considering that!
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = Uri.fromFile(getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file);

        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    public void getImageFromLibrary(View view) {
        // Add code here to start the process of getting a picture from the library
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Add code here to handle results from both taking a picture or pulling
        // from the image gallery.

        if (requestCode == REQUEST_TAKE_PHOTO) {
            // Add here.
            imageView.setImageURI(file);
        }
        else if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            //Add here.
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    // Add other methods as necessary here
    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraDemo");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent goBack = new Intent();
            setResult(Activity.RESULT_CANCELED, goBack);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
