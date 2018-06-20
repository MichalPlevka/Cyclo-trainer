package application.cyclotrainer.Database.DataAccessObjects;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import application.cyclotrainer.Database.Entities.LocationPoint;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

@Dao
public interface LocationPointDao {
    @Query("SELECT * FROM LocationPoint")
    List<LocationPoint> getAll();

    @Query("SELECT * FROM LocationPoint WHERE id IN (:routeIds)")
    List<LocationPoint> loadAllByIds(int[] routeIds);

    @Query("SELECT * FROM LocationPoint WHERE id = :id LIMIT 1")
    LocationPoint findById(int id);

    @Insert(onConflict = IGNORE)
    void insertLocationPoints(LocationPoint... locationPoints);

    @Delete
    void delete(LocationPoint locationPoint);

    @Query("DELETE FROM LocationPoint")
    public void deleteLocationPointsTable();
}