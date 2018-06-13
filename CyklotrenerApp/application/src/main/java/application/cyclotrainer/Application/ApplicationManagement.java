package application.cyclotrainer.Application;

import android.arch.persistence.room.Room;
import android.content.Intent;

import application.cyclotrainer.Application.Activities.MainActivity;
import application.cyclotrainer.Application.Fragments.MapFragment;
import application.cyclotrainer.Application.Services.BluetoothService;
import application.cyclotrainer.Application.Services.MapService;
import application.cyclotrainer.Application.Services.NativeSensorService;
import application.cyclotrainer.Application.Threads.DatabaseThread;
import application.cyclotrainer.Database.AppDatabase.AppDatabase;

public class ApplicationManagement {

    private MainActivity mainActivity;
    private MapFragment mapFragment;
    private BluetoothService bluetoothService;
    private MapService mapService;
    private NativeSensorService nativeSensorService;
    private AppDatabase db;
    private DatabaseThread databaseThread;

    private static ApplicationManagement ourInstance;
    public static ApplicationManagement getInstance() {
        if (ourInstance == null) {
            ourInstance = new ApplicationManagement();
        }
        return ourInstance;
    }

    private ApplicationManagement() {

    }

    public void setNativeSensorService() {
        Intent intent = new Intent(mainActivity, NativeSensorService.class);
        if (mainActivity != null) {
            mainActivity.startService(intent);
        }
    }

    public void setBluetoothService() {
        Intent intent = new Intent(mainActivity, BluetoothService.class);
        if (mainActivity != null) {
            mainActivity.startService(intent);
        }
    }

    public void setMapService() {
        Intent intent = new Intent(mainActivity, MapService.class);
        if (mainActivity != null) {
            mainActivity.startService(intent);
        }
    }

    public void setDatabase() {
        this.db = Room.databaseBuilder(mainActivity.getApplicationContext(), AppDatabase.class, "database-name").fallbackToDestructiveMigration().build();

    }

    public BluetoothService getBluetoothService() {
        return bluetoothService;
    }

    public void setBluetoothService(BluetoothService bluetoothService) {
        this.bluetoothService = bluetoothService;
    }

    public MapService getMapService() {
        return mapService;
    }

    public void setMapService(MapService mapService) {
        this.mapService = mapService;
    }

    public NativeSensorService getNativeSensorService() {
        return nativeSensorService;
    }

    public void setNativeSensorService(NativeSensorService nativeSensorService) {
        this.nativeSensorService = nativeSensorService;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setAppInstances(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        setNativeSensorService();
        setBluetoothService();
        setMapService();
        setDatabase();
    }

    public MapFragment getMapFragment() {
        return mapFragment;
    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    public AppDatabase getDb() {
        return db;
    }

    public void setDb(AppDatabase db) {
        this.db = db;
    }

    public DatabaseThread getDatabaseThread() {
        return databaseThread;
    }

    public void setDatabaseThread(DatabaseThread databaseThread) {
        this.databaseThread = databaseThread;
    }
}
