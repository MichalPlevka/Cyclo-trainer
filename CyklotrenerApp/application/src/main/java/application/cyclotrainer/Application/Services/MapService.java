package application.cyclotrainer.Application.Services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import application.cyclotrainer.Application.Activities.MainActivity;
import application.cyclotrainer.Application.ApplicationManagement;
import application.cyclotrainer.Application.Fragments.MapFragment;
import application.cyclotrainer.Application.Fragments.MeasurementsFragment;

public class MapService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener
{

    private MainActivity mainActivity;
    public MapFragment mapFragment;
    public GoogleApiClient googleApiClient;
    public LocationRequest locationRequest;

    private Location lastLocation;
    public double altitudeFromGPS;

    public float zoom = 18;
    public boolean setStartCamera = false;
    private float distanceTravelled;

    private boolean moving = false;

    Polyline line;
    List<LatLng> locationPoints = new ArrayList<LatLng>();


    public MapService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationManagement.getInstance().setMapService(this);

        this.distanceTravelled = 0;
        this.mainActivity = ApplicationManagement.getInstance().getMainActivity();

        if (mainActivity != null) {
            this.mapFragment = (MapFragment) mainActivity.fragmentManager.findFragmentByTag("mapFragment");
        }
        if (googleApiClient == null) {
            buildGoogleApiClient();
        }


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }



    public synchronized void buildGoogleApiClient() {
        if (ApplicationManagement.getInstance().getMainActivity() != null) {
            googleApiClient = new GoogleApiClient.Builder(ApplicationManagement.getInstance().getMainActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
            Log.d("", "GOOGLE API CLIENT CREATED");
        }
    }



//    @Override
//    public void onProviderDisabled(String provider) {
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d("LOCATION", "CONNECTED");
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("Connection failed", "ERROR: " + connectionResult);
    }


    @Override
    public void onLocationChanged(Location location) {

        if (ApplicationManagement.getInstance().getNativeSensorService().getSensorPressure() == null) {
            altitudeFromGPS = location.getAltitude();
            ApplicationManagement.getInstance().getNativeSensorService().setAndFilterAltitudeForMeasurements( (float) altitudeFromGPS);
            Log.d("B", "ALTITUDE:  " + altitudeFromGPS);
        }

        if (lastLocation != null) {
            if (location.distanceTo(lastLocation) > 15) {
                moving = false;

            } else {
                distanceTravelled += (location.distanceTo(lastLocation) / 1000); //distance between two points in km

                mainActivity.distanceValue.setText(((float) Math.round(distanceTravelled * 100) / 100) + " km");
                moving = true;
            }

        }

        lastLocation = location;

        if (moving) {
            if (MapFragment.getInstance() != null && MapFragment.getInstance().getGoogleMap() != null) {

                if (MapFragment.getInstance().getmCurrLocationMarker() != null) {
                    MapFragment.getInstance().getmCurrLocationMarker().remove();
                }

                //Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                if (setStartCamera == false) {
                    MapFragment.getInstance().getGoogleMap().moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
                    setStartCamera = true;
                }

                //move map camera
                if (mapFragment.track == true) {
                    zoom = MapFragment.getInstance().getGoogleMap().getCameraPosition().zoom;
                    MapFragment.getInstance().getGoogleMap().moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
                }

                //paint track line
                if (mapFragment.paint == true) {
                    if (location.distanceTo(lastLocation) <= 15) {
                        line = MapFragment.getInstance().getGoogleMap().addPolyline(new PolylineOptions());
                        locationPoints.add(latLng);
                        line.setPoints(locationPoints);
                        line.setColor(Color.RED);
                        line.setWidth(17);
                    }
                } else {
                    locationPoints.clear();
                }
            }
        }

    }



    @Override
    public void onDestroy()
    {
        super.onDestroy();

        //stop location updates when Activity is no longer active
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }

        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }


    //GETTERS
    public Location getLastLocation() {
        return lastLocation;
    }

    public float getDistanceTravelled() {
        return distanceTravelled;
    }

    //SETTERS
    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public void setDistanceTravelled(float distanceTravelled) {
        this.distanceTravelled = distanceTravelled;
    }
}
