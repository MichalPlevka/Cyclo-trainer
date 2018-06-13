package application.cyclotrainer.Database.DataAccessObjects;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import application.cyclotrainer.Database.Entities.Data;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

@Dao
public interface DataDao {
    @Query("SELECT * FROM data")
    List<Data> getAll();

    @Query("SELECT * FROM data WHERE id IN (:dataIds)")
    List<Data> loadAllByIds(int[] dataIds);

    @Query("SELECT * FROM data WHERE speed LIKE :speedVariable LIMIT 1")
    Data findByName(double speedVariable);

    @Query("SELECT workout.id, workout.date, data.id, data.workout_id, data.speed, data.cadence, data.distanceValue, data.altitudeValue, data.hrmValue, data.slopeValue FROM workout INNER JOIN data WHERE workout.id == data.workout_id")
    List<Data> getWorkoutsAndTheirData();

    @Insert(onConflict = IGNORE)
    void insertData(Data... data);

    @Delete
    void delete(Data data);

    @Query("DELETE FROM data")
    public void deleteDataTable();
}