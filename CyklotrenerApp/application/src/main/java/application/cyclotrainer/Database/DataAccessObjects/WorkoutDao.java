package application.cyclotrainer.Database.DataAccessObjects;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.icu.text.Replaceable;

import java.util.List;

import application.cyclotrainer.Database.Entities.Workout;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface WorkoutDao {
    @Query("SELECT * FROM workout")
    List<Workout> getAll();

    @Query("SELECT * FROM workout WHERE id IN (:workoutIds)")
    List<Workout> loadAllByIds(int[] workoutIds);

    @Query("SELECT * FROM workout WHERE id LIKE :workoutId")
    Workout getWorkoutById(int workoutId);

    @Query("SELECT * FROM workout WHERE date LIKE :date LIMIT 1")
    Workout findByName(Long date);

    @Query("SELECT * FROM workout ORDER BY id DESC LIMIT 1")
    Workout getLastWorkoutId();

    @Insert(onConflict = IGNORE)
    void insertWorkout(Workout... workout);

    @Update(onConflict = REPLACE)
    void updateWorkout(Workout... workout);

    @Delete
    void delete(Workout workout);

    @Query("DELETE FROM workout")
    public void deleteWorkoutTable();
}