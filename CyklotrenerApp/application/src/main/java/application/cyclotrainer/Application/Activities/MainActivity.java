package application.cyclotrainer.Application.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.LocationServices;

import application.cyclotrainer.Application.ApplicationManagement;
import application.cyclotrainer.Application.DatabaseSettingsOperations;
import application.cyclotrainer.Application.Services.BluetoothService;
import application.cyclotrainer.Application.Services.MapService;
import application.cyclotrainer.Application.Services.NativeSensorService;
import application.cyclotrainer.Application.Threads.DatabaseThread;
import application.cyclotrainer.Application.Fragments.MapFragment;
import application.cyclotrainer.Application.Fragments.MeasurementsFragment;
import application.cyclotrainer.Application.OnSwipeTouchListener;
import application.cyclotrainer.Application.Fragments.OptionsFragment;
import application.cyclotrainer.R;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, MeasurementsFragment.OnFragmentInteractionListener, MapFragment.OnFragmentInteractionListener, OptionsFragment.OnFragmentInteractionListener {

    Context context;

    public FragmentManager fragmentManager;

    public Button startButton;
    public Button stopButton;
    public ImageButton buttonOptions;
    public ImageButton buttonDevices;

    public View appView;

    public TextView speedValue;
    public TextView rpmValue;
    public TextView hrmValue;
    public TextView distanceValue;
    public TextView textViewChainring;
    public TextView gearTextView;
    public Chronometer chronometer;

    private boolean workoutRunning = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMenu);
        setSupportActionBar(toolbar);

        this.setTitle("Measurements");

        this.context = this.getApplicationContext();
        this.appView = findViewById(R.id.mainLayout);
        this.fragmentManager = getSupportFragmentManager();

        ApplicationManagement.getInstance().setDatabase(this);
        DatabaseSettingsOperations.getInstance(); //create instance before createFragments

        createFragments();

        appView.setOnTouchListener(new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeLeft() {
                if (MeasurementsFragment.getInstance().isFragmentCurrentlyVisible() == false && MapFragment.getInstance().isFragmentCurrentlyVisible() == false) {
                    FragmentTransaction fragmentTransactionTwo = fragmentManager.beginTransaction();

                    fragmentTransactionTwo.show(MeasurementsFragment.getInstance()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    MeasurementsFragment.getInstance().setFragmentCurrentlyVisible(true);

                    fragmentTransactionTwo.commit();
                }
            }

            @Override
            public void onSwipeRight() {
                if (MeasurementsFragment.getInstance().isFragmentCurrentlyVisible() == true && MapFragment.getInstance().isFragmentCurrentlyVisible() == false) {
                    FragmentTransaction fragmentTransactionTwo = fragmentManager.beginTransaction();
                    fragmentTransactionTwo.hide(MeasurementsFragment.getInstance()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                    MeasurementsFragment.getInstance().setFragmentCurrentlyVisible(false);

                    fragmentTransactionTwo.commit();
                }

                else if (MeasurementsFragment.getInstance().isFragmentCurrentlyVisible() == false && MapFragment.getInstance().isFragmentCurrentlyVisible() == false) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.show(MapFragment.getInstance()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    MapFragment.getInstance().setFragmentCurrentlyVisible(true);

                    fragmentTransaction.commit();
                }

            }
        });

        speedValue = (TextView) findViewById(R.id.speedValue);
        rpmValue = (TextView) findViewById(R.id.rpmValue);
        hrmValue = (TextView) findViewById(R.id.hrmValue);
        distanceValue = findViewById(R.id.distanceValue);
        textViewChainring = findViewById(R.id.textViewChainring);
        gearTextView = findViewById(R.id.gearTextView);

        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        buttonOptions = (ImageButton) findViewById(R.id.optionsButton);
        buttonDevices = (ImageButton) findViewById(R.id.devicesButton);

        chronometer = findViewById(R.id.chronometer);

        buttonOptions.setOnClickListener(this);
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        buttonDevices.setOnClickListener(this);

        ApplicationManagement.getInstance().setAppInstances(this); //Create instance of ApplicationManagement and set instances of static classes

        checkLocationPermission();
    }


    private static final int MY_PERMISSIONS_REQUEST_LOCATION_FINE = 99;

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //request the permissions.
            ActivityCompat.requestPermissions(this,
                    new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION_FINE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION_FINE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(this, "Permission for location access allowed", Toast.LENGTH_LONG).show();

                        MapService mapService = ApplicationManagement.getInstance().getMapService();

                        if (mapService.googleApiClient == null) {
                            mapService.buildGoogleApiClient();
                        }
                        MapFragment.getInstance().getGoogleMap().setMyLocationEnabled(true);
                        LocationServices.FusedLocationApi.requestLocationUpdates(mapService.googleApiClient, mapService.locationRequest, mapService);
                    }
                } else {
                    // permission was denied
                    Toast.makeText(this, "Permission for location access denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    public void createFragments() {
        if (fragmentManager.findFragmentByTag("measurementsFragment") == null && fragmentManager.findFragmentByTag("optionsFragment") == null && fragmentManager.findFragmentByTag("mapFragment") == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.add(R.id.fragmentMeasurementContainer, MeasurementsFragment.getInstance(), "measurementsFragment");
            fragmentTransaction.hide(MeasurementsFragment.getInstance());

            fragmentTransaction.add(R.id.fragmentMeasurementContainer, OptionsFragment.getInstance(), "optionsFragment");
            fragmentTransaction.hide(OptionsFragment.getInstance());

            fragmentTransaction.add(R.id.fragmentGoogleMapsContainer, MapFragment.getInstance(), "mapFragment");
            fragmentTransaction.hide(MapFragment.getInstance());

            fragmentTransaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent intentBluetoothService = new Intent(this, BluetoothService.class);
        ApplicationManagement.getInstance().getBluetoothService().stopService(intentBluetoothService);

        Intent intentNativeSensorService = new Intent(this, NativeSensorService.class);
        ApplicationManagement.getInstance().getBluetoothService().stopService(intentNativeSensorService);

        Intent intentMapService = new Intent(this, MapService.class);
        ApplicationManagement.getInstance().getBluetoothService().stopService(intentMapService);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.optionsButton:
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                if(OptionsFragment.getInstance().isFragmentCurrentlyVisible() == false) {
                    fragmentTransaction.show(OptionsFragment.getInstance()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    OptionsFragment.getInstance().setFragmentCurrentlyVisible(true);
                    buttonOptions.setBackgroundColor(Color.parseColor("#7682d6"));
                } else {
                    fragmentTransaction.hide(OptionsFragment.getInstance()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                    OptionsFragment.getInstance().setFragmentCurrentlyVisible(false);
                    buttonOptions.setBackgroundColor(Color.parseColor("#303F9F"));
                }


                fragmentTransaction.commit();
                break;
            case R.id.startButton:
                if (workoutRunning == false) {
                    ApplicationManagement.getInstance().setDatabaseThread(new DatabaseThread(ApplicationManagement.getInstance().getBluetoothService()));
                    ApplicationManagement.getInstance().getDatabaseThread().start();

                    startButton.setBackgroundColor(Color.parseColor("#7682d6"));
                    stopButton.setBackgroundColor(Color.parseColor("#303F9F"));

                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    workoutRunning = true;
                }
                break;
            case R.id.stopButton:
                if (workoutRunning) {
                    ApplicationManagement.getInstance().getDatabaseThread().interrupt();

                    startButton.setBackgroundColor(Color.parseColor("#303F9F"));
                    stopButton.setBackgroundColor(Color.parseColor("#7682d6"));

                    chronometer.stop();
                    workoutRunning = false;
                }
                break;
            case R.id.devicesButton:
                Intent intent = new Intent(getApplicationContext(), DevicesActivity.class);
                startActivity(intent);
                break;
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    //GETTERS AND SETTERS


}