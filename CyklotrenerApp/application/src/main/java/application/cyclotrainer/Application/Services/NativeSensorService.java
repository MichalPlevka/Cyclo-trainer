package application.cyclotrainer.Application.Services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import application.cyclotrainer.Application.Activities.MainActivity;
import application.cyclotrainer.Application.ApplicationManagement;
import application.cyclotrainer.Application.Fragments.MeasurementsFragment;

import static android.hardware.SensorManager.PRESSURE_STANDARD_ATMOSPHERE;

public class NativeSensorService extends Service implements SensorEventListener {

    private MainActivity mainActivity;
    private ApplicationManagement applicationManagement;

    private SensorManager sensorManager;
    private Sensor sensorPressure;

    private float altitudeAveraged;
    private float altitudeAveragedSmoothed;
    private float lastAltitudeAveragedSmoothed = -999999999;
    private float lastDistanceTravelled;

    private float slope;
    private float distance;
    private float rise;
    private float run;

    private boolean startMeasuringSlope = false;

    //Floating average measurement variables
    int bufferSize;
    float[] buffer;
    int count;
    int lastIndex;
    float average;

    float lastValueAltitude;
    float smoothingFactor = 0.01f;  //=1.0f nič nežehlíme //= 0.5f a.priemer posebeidúcich //= 0.1f vysoké žehlenie


    private static final int BUFFER_SIZE_GPS = 1;
    private static final int BUFFER_SIZE_BAROMETER = 100;

    public NativeSensorService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.mainActivity = ApplicationManagement.getInstance().getMainActivity();
        this.applicationManagement = ApplicationManagement.getInstance();
        applicationManagement.setNativeSensorService(this);

        if (mainActivity != null) {
            sensorManager = (SensorManager) mainActivity.getSystemService(SENSOR_SERVICE);
            sensorPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        }

        registerSensorListener();


        if (mainActivity != null) {
            if (sensorPressure == null) {
                setVariablesForFloatingAverageMeasurement(BUFFER_SIZE_GPS);
                timerRunnable.run();
                Toast.makeText(mainActivity, "Barometer is not available in your device. Altitude and slope measurements are going to be inaccurate.", Toast.LENGTH_LONG).show();

            } else {
                setVariablesForFloatingAverageMeasurement(BUFFER_SIZE_BAROMETER);
                timerRunnable.run();
            }
        }


    }

    private void registerSensorListener() {
        if (sensorManager != null) {
            sensorManager.registerListener(this, sensorPressure, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            timerHandler.removeCallbacks(timerRunnable);
            sensorManager.unregisterListener(this);
        } catch (IllegalArgumentException e) {
            //IGNORE
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("", "accuracy:" + accuracy);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (sensorPressure != null) {
            if (mainActivity.fragmentManager != null && mainActivity.fragmentManager.findFragmentByTag("measurementsFragment") != null && MeasurementsFragment.getInstance().getAltitudeView() != null && sensorManager != null) {

                float altitude = -999999;

                if (sensorPressure != null) {
                    altitude = sensorManager.getAltitude(PRESSURE_STANDARD_ATMOSPHERE, event.values[0]);
                    setAndFilterAltitudeForMeasurements(altitude);
                }

            }
        }
    }

    public void setAndFilterAltitudeForMeasurements(float altitude) {
        altitudeAveraged = addValueToFloatingAverageMeasurement(altitude);

        altitudeAveragedSmoothed = weightedSmoothing(altitudeAveraged, lastValueAltitude);
        lastValueAltitude = altitudeAveraged;

        altitudeAveragedSmoothed = ((float) Math.round(altitudeAveragedSmoothed * 10) / 10);
        MeasurementsFragment.getInstance().getAltitudeView().setText(altitudeAveragedSmoothed + " m a.s.l.");

        if (!startMeasuringSlope) {
            lastDistanceTravelled = ApplicationManagement.getInstance().getMapService().getDistanceTravelled();
            lastAltitudeAveragedSmoothed = altitudeAveragedSmoothed;
            startMeasuringSlope = !startMeasuringSlope;
        }
    }

    // Timer for measuring slope
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            if (lastAltitudeAveragedSmoothed != -999999999) {
                distance = (ApplicationManagement.getInstance().getMapService().getDistanceTravelled()-lastDistanceTravelled)*1000;
                if (distance > 0) { //if I am moving then calculate slope
                    rise = (altitudeAveragedSmoothed - lastAltitudeAveragedSmoothed);
                    run = (float) Math.sqrt(Math.abs(Math.pow(rise, 2) - Math.pow(distance, 2)));

                    slope = measureSlopeAngle(run, rise);
                    slope = Math.round(slope);

                    if (slope < 100) {
                        MeasurementsFragment.getInstance().getSlopeView().setText(String.format("%.0f", slope) + " %");
                    }
                    startMeasuringSlope = !startMeasuringSlope;
                } else { // if I am not moving then slope = 0
                    slope = 0;
                    MeasurementsFragment.getInstance().getSlopeView().setText(String.format("%.0f", slope) + " %");
                    startMeasuringSlope = !startMeasuringSlope;
                }
            }
            timerHandler.postDelayed(this, 4000);
        }
    };

    private void setVariablesForFloatingAverageMeasurement(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new float[bufferSize];
        count = 0;
        lastIndex = 0;
        average = 0;
    }

    private float addValueToFloatingAverageMeasurement(float value) {
        if (count++ == 0) { // if buffer is empty then fill buffer with "value" values
            for (int i = 0; i < bufferSize; ++i)
                buffer[i] = value;

            average = value;
        }

        float lastValue = buffer[lastIndex]; // get last value from buffer
        average += (value - lastValue) / bufferSize; // update average
        buffer[lastIndex] = value; // add "value" as the last element of buffer
        lastIndex = (++lastIndex) % bufferSize;// get new last index
        return average;
    }


    private float weightedSmoothing(float newVal, float oldVal) {
        return newVal*smoothingFactor + (1.0f-smoothingFactor)*oldVal;
    }

    private float measureSlopeAngle(float runValue, float riseValue) {
        Log.d("", "run: " + runValue +  " rise: " + riseValue);
        if (riseValue < 0) {
            return (riseValue/runValue)*100;
        } else if (riseValue >= 0) {
            return (riseValue/runValue)*100;
        }
        return -999999;
    }

    //GETTERS AND SETTERS
    public float getAltitudeAveragedSmoothed() {
        return altitudeAveragedSmoothed;
    }

    public void setAltitudeAveragedSmoothed(float altitudeAveragedSmoothed) {
        this.altitudeAveragedSmoothed = altitudeAveragedSmoothed;
    }

    public float getAltitudeAveraged() {
        return altitudeAveraged;
    }

    public SensorManager getSensorManager() {
        return sensorManager;
    }

    public Sensor getSensorPressure() {
        return sensorPressure;
    }

    public void setAltitudeAveraged(float altitudeAveraged) {
        this.altitudeAveraged = altitudeAveraged;
    }

    public void setSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public void setSensorPressure(Sensor sensorPressure) {
        this.sensorPressure = sensorPressure;
    }

    public float getSlope() {
        return slope;
    }

    public void setSlope(float slope) {
        this.slope = slope;
    }
}
