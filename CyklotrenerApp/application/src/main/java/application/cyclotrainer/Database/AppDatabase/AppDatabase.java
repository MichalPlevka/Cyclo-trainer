package application.cyclotrainer.Database.AppDatabase;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import application.cyclotrainer.Database.DataAccessObjects.DataDao;
import application.cyclotrainer.Database.DataAccessObjects.LocationPointsDao;
import application.cyclotrainer.Database.DataAccessObjects.WorkoutDao;
import application.cyclotrainer.Database.Entities.Data;
import application.cyclotrainer.Database.Entities.LocationPoints;
import application.cyclotrainer.Database.Entities.Workout;

@Database(entities = {Data.class, Workout.class, LocationPoints.class}, version = 9)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WorkoutDao workoutDao();
    public abstract DataDao dataDao();
    public abstract LocationPointsDao locationPointsDao();
}