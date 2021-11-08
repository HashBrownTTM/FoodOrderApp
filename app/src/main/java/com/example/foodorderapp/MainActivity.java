package com.example.foodorderapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    EditText txtAddress, txtCall;
    Button btnAdd, btnCall, btnCurrent, btnDirections, btnEmail, btnMessage;
    LocationManager locationManager;
    FusedLocationProviderClient fusedLocationProviderClient;
    String latitude, longitude;
    public int type;

    public static final int REQUEST_CHECK_SETTING = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtAddress = findViewById(R.id.txtAddress);
        txtCall = findViewById(R.id.txtCall);
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);
        btnCall = findViewById(R.id.btnCall);
        btnCall.setOnClickListener(this);
        btnCurrent = findViewById(R.id.btnCurrent);
        btnCurrent.setOnClickListener(this);
        btnDirections = findViewById(R.id.btnDirections);
        btnDirections.setOnClickListener(this);
        btnEmail = findViewById(R.id.btnEmail);
        btnEmail.setOnClickListener(this);
        btnMessage = findViewById(R.id.btnMessage);
        btnMessage.setOnClickListener(this);

        //initialize fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);


    }

    @Override
    public void onClick(View v) {
        Intent intent;
        String address;
        switch (v.getId())
        {
            case R.id.btnAdd:
                type = 0;
                address = txtAddress.getText().toString();
                if(address.equals(""))
                {
                    Toast.makeText(MainActivity.this, "Please enter the address", Toast.LENGTH_SHORT).show();
                }
                else{
                    GeoLocation geoLocation = new GeoLocation();
                    geoLocation.getAddress(address, getApplicationContext(), new GeoHandler());
                }
                break;
            case R.id.btnDirections: //opens the route part of google maps directly
                type = 1;
                address = txtAddress.getText().toString();
                if(address.equals(""))
                {
                    Toast.makeText(MainActivity.this, "Please enter the address", Toast.LENGTH_SHORT).show();
                }
                else{

                    GeoLocation geoLocation = new GeoLocation();
                    geoLocation.getAddress(address, getApplicationContext(), new GeoHandler());
                }
                break;
            case R.id.btnCall:
                /*n
                without needing permission
                https://stackoverflow.com/questions/4275678/how-to-make-a-phone-call-using-intent-in-android

                String number = txtCall.getText().toString();
                intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null));
                startActivity(intent);*/

                /*with permission
                https://stackoverflow.com/questions/4275678/how-to-make-a-phone-call-using-intent-in-android
                */
                if(ContextCompat.checkSelfPermission(
                        MainActivity.this,android.Manifest.permission.CALL_PHONE) !=
                        PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions((Activity) MainActivity.this, new
                            String[]{android.Manifest.permission.CALL_PHONE}, 0);
                }
                else
                {
                    String number = txtCall.getText().toString();
                    intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + number));
                    startActivity(intent);
                }
                break;
            case R.id.btnCurrent:
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    getCurrentLocation();
                }
                else{
                    showGpsDisabledAlert();
                }
                break;
            case R.id.btnEmail:
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"tiego202@gmail.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Testing");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hope this works....");

                emailIntent.setType("message/rfc822");

                startActivity(Intent.createChooser(emailIntent, "Choose an Email Client:"));
                break;
            case R.id.btnMessage:
                //checks condition
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED){
                    //when permission is granted
                    //Create method
                    sendMethod();
                }
                else{
                    //when permisson is denied
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.SEND_SMS}
                            , 100);
                }
                break;
        }
    }

    private void sendMethod() {
        String sPhone = txtCall.getText().toString();
        String message = "bwtenebtrwgbtetwrgqerwbt";

        if(!sPhone.equals("")){
            SmsManager smsManager = SmsManager.getDefault();
            //send text message
            smsManager.sendTextMessage(sPhone, null, message, null, null);

            Toast.makeText(MainActivity.this, "SMS sent successfully", Toast.LENGTH_SHORT).show();
        }
        else{
            //when edit test value is blank
            Toast.makeText(MainActivity.this, "Enter a phone number", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //check condition
        if(requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //When permission is granted
            //call method
            sendMethod();
        }
        else{
            //when permission is denied
            //Display toast
            Toast.makeText(MainActivity.this, "Permission Denied...", Toast.LENGTH_SHORT).show();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }*/

    private void showGpsDisabledAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Go to setting page to enable GPS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CHECK_SETTING){
            switch (resultCode){
                case Activity.RESULT_OK:
                    Toast.makeText(MainActivity.this, "GPS is on", Toast.LENGTH_SHORT).show();
                    getCurrentLocation();
                    break;

                case Activity.RESULT_CANCELED:
                    Toast.makeText(MainActivity.this, "GPS needs to be turned on", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private void getCurrentLocation() {
        /* https://www.youtube.com/watch?v=Ak1O9Gip-pg&t=735s */
        //check permissions
        if(ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            //when permission granted
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    //initialise location
                    Location location = task.getResult();
                    if(location != null)
                    {
                        try{
                            //initialise geoCoder
                            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                            //initialise address list
                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(), location.getLongitude(), 1);
                            latitude = Double.toString(location.getLatitude());
                            longitude = Double.toString(location.getLongitude());

                            String address = addresses.get(0).getAddressLine(0);
                            txtAddress.setText(address);
                        }
                        catch (IOException e){e.printStackTrace();}
                    }
                }
            });


        }
        else
        {
            //when permission denied
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    private class GeoHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String address;
            switch(msg.what){
                case 1:
                    Bundle bundle = msg.getData();
                    address = bundle.getString("address");
                    break;
                default:
                    address = null;
            }
            /*
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("geo:0,0?q=" + address + " (name)"));*/

            Intent intent = new Intent(Intent.ACTION_VIEW);

            if(type == 0)
            {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("geo:0,0?q=" + address + " (name)"));
            }
            else if(type == 1)
            {
                intent.setData(Uri.parse("google.navigation:q=" + address));
                intent.setPackage("com.google.android.apps.maps");
            }

            startActivity(intent);
        }
    }
}

/*
* original code
String address = txtAddress.getText().toString();
intent = new Intent(Intent.ACTION_VIEW);
intent.setData(Uri.parse("http://maps.google.co.in/maps?q=" +
        address));

startActivity(intent);

* new code
https://javapapers.com/android/android-geocoding-to-get-latitude-longitude-for-an-address/
*/