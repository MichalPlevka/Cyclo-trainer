package application.cyclotrainer.Database.AppDatabase;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import application.cyclotrainer.Database.DataAccessObjects.DataDao;
import application.cyclotrainer.Database.DataAccessObjects.LocationPointDao;
import application.cyclotrainer.Database.DataAccessObjects.SettingDao;
import application.cyclotrainer.Database.DataAccessObjects.WorkoutDao;
import application.cyclotrainer.Database.Entities.Data;
import application.cyclotrainer.Database.Entities.LocationPoint;
import application.cyclotrainer.Database.Entities.Setting;
import application.cyclotrainer.Database.Entities.Workout;

@Database(entities = {Data.class, Workout.class, LocationPoint.class, Setting.class}, version = 15)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WorkoutDao workoutDao();
    public abstract DataDao dataDao();
    public abstract LocationPointDao locationPointsDao();
    public abstract SettingDao settingDao();
}