package application.cyclotrainer.Application.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import application.cyclotrainer.Application.ApplicationManagement;
import application.cyclotrainer.Application.DatabaseSettingsOperations;
import application.cyclotrainer.Application.Threads.DatabaseSettingsOperationsThread;
import application.cyclotrainer.R;


public class OptionsFragment extends Fragment implements View.OnClickListener {

    public static OptionsFragment fragmentInstance;

    private View fragmentView;

    private EditText ageEdit;
    private EditText wheelDiameterEdit;
    private EditText cogsEdit;
    private EditText chainringsEdit;
    private Button applyButton;
    private ToggleButton toggleButtonGender;

    public Button buttonZoneOne;
    public Button buttonZoneTwo;
    public Button buttonZoneThree;
    public Button buttonZoneFour;


    private int wheelDiameterValue = 622;
    private int ageValue = 23;
    private String gender = "MALE";
    public String trainingZone = "ENDURANCE";
    public double maximumHeartRate;

    private List<Integer> cogsListValues;
    private List<Integer> chainringsListValues;

    private List<Double> gearsListSmall;
    private List<Double> gearsListBig;

    private boolean ageCorrect = true;
    private boolean wheelDiameterCorrect = true;
    private boolean cogsCorrect = true;
    private boolean chainringsCorrect = true;


    private OnFragmentInteractionListener mListener;

    private boolean fragmentCurrentlyVisible = false;

    public OptionsFragment() {
        // Required empty public constructor
    }

    public static OptionsFragment getInstance() {
        if (fragmentInstance == null) {
            fragmentInstance = new OptionsFragment();
            return fragmentInstance;
        } else {
            return fragmentInstance;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.fragmentView = inflater.inflate(R.layout.fragment_options, container, false);

        this.toggleButtonGender = fragmentView.findViewById(R.id.toggleButtonGender);
        this.ageEdit = fragmentView.findViewById(R.id.editTextAge);
        this.wheelDiameterEdit = fragmentView.findViewById(R.id.editTextWheelDiameter);
        this.cogsEdit = fragmentView.findViewById(R.id.editTextCogs);
        this.chainringsEdit = fragmentView.findViewById(R.id.editTextChainrings);

        this.buttonZoneOne = fragmentView.findViewById(R.id.buttonZoneOne);
        this.buttonZoneTwo = fragmentView.findViewById(R.id.buttonZoneTwo);
        this.buttonZoneThree = fragmentView.findViewById(R.id.buttonZoneThree);
        this.buttonZoneFour = fragmentView.findViewById(R.id.buttonZoneFour);
        buttonZoneOne.setBackgroundColor(Color.parseColor("#FFFFFF"));


        ageValue = 23;
        gender = "MALE";
        wheelDiameterValue = 622;

        ageEdit.setText(String.valueOf(ageValue));
        wheelDiameterEdit.setText(String.valueOf(wheelDiameterValue));
        cogsEdit.setText("11 12 13 14 15 16 17 18 19 20 21 22 23");
        chainringsEdit.setText("34 50");

        DatabaseSettingsOperationsThread databaseSettingsOperationsThread = new DatabaseSettingsOperationsThread(0);
        databaseSettingsOperationsThread.start();

        this.applyButton = (Button) fragmentView.findViewById(R.id.applyButton);
        applyButton.setOnClickListener(this);
        buttonZoneOne.setOnClickListener(this);
        buttonZoneTwo.setOnClickListener(this);
        buttonZoneThree.setOnClickListener(this);
        buttonZoneFour.setOnClickListener(this);
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

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.applyButton:
                saveUserDefinedValues();
                break;

            case R.id.buttonZoneOne:
                buttonZoneOne.setBackgroundColor(Color.parseColor("#FFFFFF"));
                trainingZone = "ENDURANCE";

                buttonZoneTwo.setBackgroundColor(Color.parseColor("#40bf80"));
                buttonZoneThree.setBackgroundColor(Color.parseColor("#e6e600"));
                buttonZoneFour.setBackgroundColor(Color.parseColor("#ff6600"));
                break;

            case R.id.buttonZoneTwo:
                buttonZoneTwo.setBackgroundColor(Color.parseColor("#FFFFFF"));
                trainingZone = "STAMINA";

                buttonZoneOne.setBackgroundColor(Color.parseColor("#6666ff"));
                buttonZoneThree.setBackgroundColor(Color.parseColor("#e6e600"));
                buttonZoneFour.setBackgroundColor(Color.parseColor("#ff6600"));
                break;

            case R.id.buttonZoneThree:
                buttonZoneThree.setBackgroundColor(Color.parseColor("#FFFFFF"));
                trainingZone = "ECONOMY";

                buttonZoneOne.setBackgroundColor(Color.parseColor("#6666ff"));
                buttonZoneTwo.setBackgroundColor(Color.parseColor("#40bf80"));
                buttonZoneFour.setBackgroundColor(Color.parseColor("#ff6600"));
                break;

            case R.id.buttonZoneFour:
                buttonZoneFour.setBackgroundColor(Color.parseColor("#FFFFFF"));
                trainingZone = "SPEED";

                buttonZoneOne.setBackgroundColor(Color.parseColor("#6666ff"));
                buttonZoneTwo.setBackgroundColor(Color.parseColor("#40bf80"));
                buttonZoneThree.setBackgroundColor(Color.parseColor("#e6e600"));
                break;
        }
    }

    public void setSettingsAndCalculateGears() {
        ageValue = Integer.parseInt(DatabaseSettingsOperations.getInstance().age);
        gender = DatabaseSettingsOperations.getInstance().gender;
        wheelDiameterValue = Integer.parseInt(DatabaseSettingsOperations.getInstance().wheelDiameter);

        toggleButtonGender.setText(gender);
        ageEdit.setText(String.valueOf(ageValue));
        wheelDiameterEdit.setText(String.valueOf(wheelDiameterValue));
        cogsEdit.setText(DatabaseSettingsOperations.getInstance().cogs);
        chainringsEdit.setText(DatabaseSettingsOperations.getInstance().chainrings);


        this.maximumHeartRate = 202 - (0.55 * ageValue);

        String[] cogsList = cogsEdit.getText().toString().split(" ");
        cogsListValues = new ArrayList<Integer>();
        for (String cogVal : cogsList) {
            cogsListValues.add(Integer.parseInt(cogVal));
        }

        String[] chainringsList = chainringsEdit.getText().toString().split(" ");
        chainringsListValues = new ArrayList<Integer>();
        for (String chainVal : chainringsList) {
            chainringsListValues.add(Integer.parseInt(chainVal));
        }

        calculateUserGears();
    }

    public void saveUserDefinedValues() {
        gender = toggleButtonGender.getText().toString();

        try {

            if (!ageEdit.getText().toString().equals("") && Integer.parseInt(ageEdit.getText().toString()) > 0 && Integer.parseInt(ageEdit.getText().toString()) <= 120) {
                ageValue = Integer.parseInt(ageEdit.getText().toString());
                ageCorrect = true;
            } else {
                ageCorrect = false;
            }

            if (!wheelDiameterEdit.getText().toString().equals("") && Integer.parseInt(wheelDiameterEdit.getText().toString()) > 299 && Integer.parseInt(wheelDiameterEdit.getText().toString()) <= 800) {
                wheelDiameterValue = Integer.parseInt(wheelDiameterEdit.getText().toString());
                wheelDiameterCorrect = true;
            } else {
                wheelDiameterCorrect = false;
            }

            if (!cogsEdit.getText().toString().equals("")) {
                String[] cogsList = cogsEdit.getText().toString().split(" ");
                cogsListValues = new ArrayList<Integer>();
                for (String cogVal : cogsList) {
                    if (cogVal != "") {
                        cogsListValues.add(Integer.parseInt(cogVal));
                    }
                }
                cogsCorrect = true;
            } else {
                cogsCorrect = false;
            }

            if (!chainringsEdit.getText().toString().equals("")) {
                String[] chainringsList = chainringsEdit.getText().toString().split(" ");
                chainringsListValues = new ArrayList<Integer>();

                if (chainringsList.length != 2) {
                    chainringsListValues.add(34);
                    chainringsListValues.add(50);
                    chainringsEdit.setText("34 50");

                    chainringsCorrect = false;
                    Toast.makeText(ApplicationManagement.getInstance().getMainActivity(), "You need to enter two chainrings!", Toast.LENGTH_LONG).show();

                } else {

                    for (String chainVal : chainringsList) {
                        if (chainVal != "") {
                            chainringsListValues.add(Integer.parseInt(chainVal));
                        }
                    }

                    chainringsCorrect = true;
                }

                chainringsCorrect = true;
            } else {
                chainringsCorrect = false;
            }

            if (gender == "MALE") {
                maximumHeartRate = 202 - (0.55 * ageValue);
            } else if (gender == "FEMALE") {
                maximumHeartRate = 216 - (1.09 * ageValue);
            }

            if (ageCorrect && wheelDiameterCorrect && cogsCorrect && chainringsCorrect) {
                calculateUserGears();
                Toast.makeText(ApplicationManagement.getInstance().getMainActivity(), "Changes were saved!", Toast.LENGTH_LONG).show();

                DatabaseSettingsOperationsThread databaseSettingsOperationsThread = new DatabaseSettingsOperationsThread(2);
                databaseSettingsOperationsThread.start();


            } else {
                Toast.makeText(ApplicationManagement.getInstance().getMainActivity(), "All fields are required and correct values have to be entered. Please use a predefined format of field values.", Toast.LENGTH_LONG).show();

                if (!ageCorrect) {
                    Toast.makeText(ApplicationManagement.getInstance().getMainActivity(), "Age field has to be a number in interval <1, 120>", Toast.LENGTH_LONG).show();
                }
                if (!wheelDiameterCorrect) {
                    Toast.makeText(ApplicationManagement.getInstance().getMainActivity(), "Wheel diameter field has to be a number in interval <300, 800>", Toast.LENGTH_LONG).show();
                }
                if (!cogsCorrect) {
                    Toast.makeText(ApplicationManagement.getInstance().getMainActivity(), "Cogs field cannot be empty and need to be in format: 'X Y Z ...' where X,Y,Z are sizes of cogs", Toast.LENGTH_LONG).show();
                }
                if (!chainringsCorrect) {
                    Toast.makeText(ApplicationManagement.getInstance().getMainActivity(), "Chainrings field cannot be empty and need to be in format: 'X Y' where X,Y are sizes of chainrings", Toast.LENGTH_LONG).show();
                }
            }

        } catch (NumberFormatException e){
            Toast.makeText(ApplicationManagement.getInstance().getMainActivity(), "Fields cannot be empty and have to contain only numbers!", Toast.LENGTH_LONG).show();
        }
    }

    public void calculateUserGears() {
        gearsListSmall = new ArrayList<Double>();
        gearsListBig = new ArrayList<Double>();
        for (int i = 0; i < chainringsListValues.size(); i++) {
            for (Integer cog : cogsListValues) {
                if (i == 0) {
                    gearsListSmall.add(((double)chainringsListValues.get(i)) / ((double)cog));
                }

                if (i == 1) {
                    gearsListBig.add(((double)chainringsListValues.get(i)) / ((double)cog));
                }
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    //GETTERS AND SETTERS

    public boolean isFragmentCurrentlyVisible() {
        return fragmentCurrentlyVisible;
    }

    public void setFragmentCurrentlyVisible(boolean fragmentCurrentlyVisible) {
        this.fragmentCurrentlyVisible = fragmentCurrentlyVisible;
    }

    public EditText getWheelDiameterEdit() {
        return wheelDiameterEdit;
    }

    public void setWheelDiameterEdit(EditText wheelDiameterEdit) {
        this.wheelDiameterEdit = wheelDiameterEdit;
    }

    public EditText getCogsEdit() {
        return cogsEdit;
    }

    public void setCogsEdit(EditText cogsEdit) {
        this.cogsEdit = cogsEdit;
    }

    public EditText getChainringsEdit() {
        return chainringsEdit;
    }

    public void setChainringsEdit(EditText chainringsEdit) {
        this.chainringsEdit = chainringsEdit;
    }

    public int getWheelDiameterValue() {
        return wheelDiameterValue;
    }

    public void setWheelDiameterValue(int wheelDiameterValue) {
        this.wheelDiameterValue = wheelDiameterValue;
    }

    public List<Double> getGearsListSmall() {
        return gearsListSmall;
    }

    public void setGearsListSmall(List<Double> gearsListSmall) {
        this.gearsListSmall = gearsListSmall;
    }

    public List<Double> getGearsListBig() {
        return gearsListBig;
    }

    public void setGearsListBig(List<Double> gearsListBig) {
        this.gearsListBig = gearsListBig;
    }

    public List<Integer> getCogsListValues() {
        return cogsListValues;
    }

    public void setCogsListValues(List<Integer> cogsListValues) {
        this.cogsListValues = cogsListValues;
    }

    public List<Integer> getChainringsListValues() {
        return chainringsListValues;
    }

    public void setChainringsListValues(List<Integer> chainringsListValues) {
        this.chainringsListValues = chainringsListValues;
    }

    public EditText getAgeEdit() {
        return ageEdit;
    }

    public void setAgeEdit(EditText ageEdit) {
        this.ageEdit = ageEdit;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public ToggleButton getToggleButtonGender() {
        return toggleButtonGender;
    }

    public void setToggleButtonGender(ToggleButton toggleButtonGender) {
        this.toggleButtonGender = toggleButtonGender;
    }
}
