package application.cyclotrainer.Application.Threads;


import android.util.Log;

import java.sql.Date;

import application.cyclotrainer.Application.Activities.MainActivity;
import application.cyclotrainer.Application.ApplicationManagement;
import application.cyclotrainer.Application.Services.BluetoothService;
import application.cyclotrainer.Application.Services.MapService;
import application.cyclotrainer.Application.Services.NativeSensorService;
import application.cyclotrainer.Database.AppDatabase.AppDatabase;
import application.cyclotrainer.Database.Entities.Data;
import application.cyclotrainer.Database.Entities.LocationPoint;
import application.cyclotrainer.Database.Entities.Workout;

public class DatabaseThread extends Thread {

    private BluetoothService bluetoothService;
    private MapService mapService;
    private AppDatabase appDatabase;
    private MainActivity mainActivity;
    private NativeSensorService nativeSensorService;

    private Workout currentWorkout;

    public DatabaseThread(BluetoothService bluetoothService) {
        this.bluetoothService = bluetoothService;
        this.mapService = ApplicationManagement.getInstance().getMapService();
        this.appDatabase = ApplicationManagement.getInstance().getDb();
        this.mainActivity = ApplicationManagement.getInstance().getMainActivity();
        this.nativeSensorService = ApplicationManagement.getInstance().getNativeSensorService();
    }

    public void run() {
        Log.d("DatabaseThread",":"+"reading");

        Long tsLong = System.currentTimeMillis()/1000;

        Workout workout = new Workout();
        this.currentWorkout = workout;
        workout.setDate(new Date(tsLong));


        appDatabase.workoutDao().insertWorkout(workout);

        while (true) {
            try {
                if (appDatabase != null && bluetoothService != null && mapService != null) {
                    //Insert speed, cadence, workout_id, ...
                    Data data = new Data();
                    data.setWorkoutId(appDatabase.workoutDao().getLastWorkoutId().getId());
                    data.setSpeedValue(bluetoothService.speed);
                    data.setCadenceValue(bluetoothService.rpm);
                    data.setDistanceValue(mapService.getDistanceTravelled());
                    data.setAltitudeValue(nativeSensorService.getAltitudeAveragedSmoothed());
                    data.setSlopeValue(nativeSensorService.getSlope());

                    if (!mainActivity.hrmValue.getText().toString().equals("N/A")) {
                       data.setHrmValue(Double.parseDouble(mainActivity.hrmValue.getText().toString()));
                    } else {
                        data.setHrmValue(0);
                    }
                    appDatabase.dataDao().insertData(data);

                    //Insert workout_id, latitude and longitude
                    LocationPoint locationPoint = new LocationPoint();
                    locationPoint.setWorkoutId(appDatabase.workoutDao().getLastWorkoutId().getId());
                    locationPoint.setLatitude(mapService.getLastLocation().getLatitude());
                    locationPoint.setLongitude(mapService.getLastLocation().getLongitude());
                    appDatabase.locationPointsDao().insertLocationPoints(locationPoint);

                    //Update distanceTravelled
                    currentWorkout.setDistanceTravelled(mapService.getDistanceTravelled());
                    appDatabase.workoutDao().updateWorkout(currentWorkout);

                }

                Thread.sleep(10000);
            } catch (Exception e) {
                Log.e("DatabaseThread", ":"+e.getMessage());
                workout.setDistanceTravelled(mapService.getDistanceTravelled());
                workout.setTime(mainActivity.chronometer.getFormat());
                appDatabase.workoutDao().updateWorkout(workout);
                break;
            }
        }
    }
}
