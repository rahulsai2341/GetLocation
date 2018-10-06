package com.example.rahulk.mapsupdated;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.Location;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, OnMapReadyCallback {

    GoogleApiClient mGoogleApiClient;
    Location myLocation;
    TextView mAddressOutput;
    Button GetLocation;
    private static final String TAG = "MainActivity";
    public AddressResultReceiver mResultReceiver;
    public String mAddresstring;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAddressOutput = (TextView)findViewById(R.id.AddrE);
        mAddressOutput.setMovementMethod(ScrollingMovementMethod.getInstance());

        mResultReceiver = new AddressResultReceiver(new Handler());
        GetLocation = (Button) findViewById(R.id.GetLocation);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(MainActivity.this)
                .addOnConnectionFailedListener(MainActivity.this)
                .build();


        /*GetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               //these methods can be called here also.This makes location update when the button is pressed.
                //mGoogleApiClient.connect();
                *//*if (ActivityCompat.checkSelfPermission(HomePage.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomePage.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                myLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                    if (myLocation != null) {
                    mLatitudeText.setText(String.valueOf(myLocation.getLatitude()));
                    mLongitudeText.setText(String.valueOf(myLocation.getLongitude()));
                    }*//*

            }
        });*/
    }


    @Override
    public void onMapReady(final GoogleMap map) {
        GetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    map.setMyLocationEnabled(true);
                } else {
                    // Show rationale and request permission.
                }
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                        .title("Current Location"));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(),myLocation.getLongitude()),18));
                Log.i(TAG,"Location found and camera updated");

                if (mGoogleApiClient.isConnected() && myLocation != null) {

                    startIntentService();
                }

            }
        });
    }


    protected void onStart() {
        //calling this method here updates the location only when the app refreshes and then clicking the button.By clicking the button alone the location is not updated.
        Log.i(TAG, "Google Client Connected.");
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    protected void onPause(){

        super.onPause();
    }

    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        /*GetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                }



            }
        });*/
    }
    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, myLocation);
        Log.i(TAG, "Intent service started");
        startService(intent);

    }




    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.

        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i(TAG,"Result recieved");

            // Display the address string
            // or an error message sent from the intent service.
            mAddresstring = resultData.getString(Constants.RESULT_DATA_KEY);

            mAddressOutput.setText(mAddresstring);


            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.i(TAG,"Address found");
            }

        }
    }
}
