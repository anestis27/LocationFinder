package com.example.orion.locationfinder;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import org.json.JSONException;



public class MainActivity extends BaseActivity {

    private static final int PERMISSIONS_REQUEST_LOCATION = 122;
    private static final double GEOFENCE_1_LAT = 55.367844;
    private static final double GEOFENCE_1_LONG = 10.431250;
    private static final double GEOFENCE_2_LAT = 55.366874;
    private static final double GEOFENCE_2_LONG = 10.430515;

    private FusedLocationProviderClient fusedLocationProvider;
    private LocationCallback locationCallback;
    private ProximityManager proximityManager;
    private BeaconReader beaconReader;

    private String roomSharableString = "";

    private Location geoFP1 = new Location("DEV");
    private Location geoFP2 = new Location("DEV");



    private TextView location_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        KontaktSDK.initialize("AIzgCotlMgCdlfBqbblrveDKuZpCssPC");


        location_status = findViewById(R.id.tw_locationStatus);


        geoFP1.setLatitude(GEOFENCE_1_LAT);
        geoFP1.setLongitude(GEOFENCE_1_LONG);
        geoFP2.setLatitude(GEOFENCE_2_LAT);
        geoFP2.setLongitude(GEOFENCE_2_LONG);

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this);
        proximityManager = ProximityManagerFactory.create(this);
        proximityManager.setIBeaconListener(createIBeaconListener());

        try
        {
            beaconReader = new BeaconReader(this);
        }
        catch (JSONException ex)
        {
            Toast.makeText(this, "Failed to read JSON", Toast.LENGTH_LONG).show();
            finish();
        }
        getLocation();

        Button btn = findViewById(R.id.btn_shareRoomString);
        btn.setClickable(false); //Can't share lcoation before we have found it
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 16-03-2018 Implement sharing logic here
                System.out.println(roomSharableString);
            }
        });
    }

    @Override
    public void onStop() {
        if(proximityManager.isScanning()) {
            proximityManager.stopScanning();
        }
        if(locationCallback != null) {
            fusedLocationProvider.removeLocationUpdates(locationCallback);
        }
        super.onStop();
    }


    private void getLocation() {
        if(Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
            return;
        }
        Log.d("Request", "Requesting location...");
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location l = locationResult.getLastLocation();
                if(l != null)
                {
                    fusedLocationProvider.removeLocationUpdates(locationCallback);
                    if(checkGeoFence(l)) //Are we at building 44?
                    {
                        getBLELocation(); //Then we go to Bluetooth
                    }
                    else
                    {
                        location_status.setText("fail"); //Shit went wrong
                    }
                }
            }
        };
        fusedLocationProvider.requestLocationUpdates(createRequest(), locationCallback, null);
    }

    private LocationRequest createRequest() {
        return new LocationRequest()
                .setNumUpdates(1)
                .setInterval(1000)
                .setFastestInterval(500)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Need permission")
                        .setMessage("We need permission to get your location in order to make this app work.")
                        .setCancelable(false)
                        .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getLocation();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                builder.create().show();
            }
        }
    }

    private boolean checkGeoFence(Location location) {
        return geoFP1.getLatitude() >= location.getLatitude() && location.getLatitude() >= geoFP2.getLatitude()
                && geoFP1.getLongitude() >= location.getLongitude() && location.getLongitude() >= geoFP2.getLongitude();
    }

    /**
     *
     */
    private void getBLELocation()
    {
        System.out.println("Getting BLE Location");
        location_status.setText("Room");
        startScanning();
    }

    private void startScanning()
    {
        System.out.println("Starting Scanning");
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.startScanning();
            }
        });
    }

    private IBeaconListener createIBeaconListener() {
        return new SimpleIBeaconListener() {
            @Override
            public void onIBeaconDiscovered(final IBeaconDevice ibeacon, IBeaconRegion region) {
                RoomDetails r = beaconReader.getRoom(ibeacon.getUniqueId());
                try{
                    roomSharableString = r.getRoomName() + " : " +r.getRoom();
                    location_status.setText(roomSharableString);
                    Button btn = findViewById(R.id.btn_shareRoomString);
                    btn.setClickable(true);
                }catch (Exception e){

                }
            }
        };
    }


}
