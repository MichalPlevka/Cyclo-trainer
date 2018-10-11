package application.cyclotrainer.Application.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import application.cyclotrainer.Application.Activities.MainActivity;
import application.cyclotrainer.Application.ApplicationManagement;
import application.cyclotrainer.R;


public class MeasurementsFragment extends Fragment {

    private static MeasurementsFragment fragmentInstance;

    private MainActivity mainActivity;
    private View fragmentView;

    private TextView altitudeView;
    private TextView slopeView;

    private boolean fragmentCurrentlyVisible = false;

    private OnFragmentInteractionListener mListener;

    public MeasurementsFragment() {
        // Required empty public constructor
    }

    public static MeasurementsFragment getInstance() {
        if (fragmentInstance == null) {
            fragmentInstance = new MeasurementsFragment();
            return fragmentInstance;
        } else {
            return fragmentInstance;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mainActivity = ApplicationManagement.getInstance().getMainActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.fragmentView = inflater.inflate(R.layout.fragment_measurements, container, false);
        this.altitudeView = fragmentView.findViewById(R.id.altitudeValue);
        this.slopeView = fragmentView.findViewById(R.id.slopeValue);
        return fragmentView;
    }

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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    //GETTERS AND SETTERS
    public TextView getAltitudeView() {
        return altitudeView;
    }

    public void setAltitudeView(TextView altitudeView) {
        this.altitudeView = altitudeView;
    }

    public TextView getSlopeView() {
        return slopeView;
    }

    public void setSlopeView(TextView slopeView) {
        this.slopeView = slopeView;
    }

    public boolean isFragmentCurrentlyVisible() {
        return fragmentCurrentlyVisible;
    }

    public void setFragmentCurrentlyVisible(boolean fragmentCurrentlyVisible) {
        this.fragmentCurrentlyVisible = fragmentCurrentlyVisible;
    }
}
