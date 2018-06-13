package application.cyclotrainer.Application.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
//import android.location.LocationListener;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;

//import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;


import application.cyclotrainer.Application.Activities.MainActivity;
import application.cyclotrainer.Application.ApplicationManagement;
import application.cyclotrainer.Application.Services.MapService;
import application.cyclotrainer.R;


public class MapFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback {

    private MainActivity mainActivity;

    private static MapFragment fragmentInstance;

    private View fragmentView;

    private boolean fragmentCurrentlyVisible = false;
    private MapFragment.OnFragmentInteractionListener mListener;

    private GoogleMap googleMap;
    private SupportMapFragment mapFrag;

    public boolean track = true;
    public boolean paint = true;
    public boolean mapType = true;

    private Marker mCurrLocationMarker;

    public static MapFragment getInstance() {
        if (fragmentInstance == null) {
            fragmentInstance = new MapFragment();
            return fragmentInstance;
        } else {
            return fragmentInstance;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = ApplicationManagement.getInstance().getMainActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.fragmentView = inflater.inflate(R.layout.activity_maps, container, false);

        mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        ToggleButton toggleTrack = (ToggleButton) fragmentView.findViewById(R.id.toggleButton);
        ToggleButton togglePaint = (ToggleButton) fragmentView.findViewById(R.id.toggleButton2);
        ToggleButton changeMapTypeButton = (ToggleButton) fragmentView.findViewById(R.id.toggleButton3);
        Button backButton = (Button) fragmentView.findViewById(R.id.backButton);

        toggleTrack.setOnClickListener(this);
        togglePaint.setOnClickListener(this);
        changeMapTypeButton.setOnClickListener(this);
        backButton.setOnClickListener(this);

        return fragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(mainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                this.googleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                mainActivity.checkLocationPermission();
            }
        } else {
            this.googleMap.setMyLocationEnabled(true);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toggleButton:
                track = !track;
                break;
            case R.id.toggleButton2:
                paint = !paint;
                break;
            case R.id.toggleButton3:
                if (mapType == false) {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    mapType = !mapType;
                } else if (mapType == true) {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    mapType = !mapType;
                }
                break;
            case R.id.backButton:
                FragmentTransaction fragmentTransaction = mainActivity.fragmentManager.beginTransaction();

                fragmentTransaction.hide(this);
                setFragmentCurrentlyVisible(false);

                fragmentTransaction.commit();

                break;
        }
    }


    //GETTERS===============================================================
    public GoogleMap getGoogleMap() {
        return googleMap;
    }

    public Marker getmCurrLocationMarker() {
        return mCurrLocationMarker;
    }

    public boolean isFragmentCurrentlyVisible() {
        return fragmentCurrentlyVisible;
    }

    //SETTERS===============================================================
    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public void setmCurrLocationMarker(Marker mCurrLocationMarker) {
        this.mCurrLocationMarker = mCurrLocationMarker;
    }

    public void setFragmentCurrentlyVisible(boolean fragmentCurrentlyVisible) {
        this.fragmentCurrentlyVisible = fragmentCurrentlyVisible;
    }
}