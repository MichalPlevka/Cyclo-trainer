package application.cyclotrainer.Database.DataAccessObjects;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import application.cyclotrainer.Database.Entities.Data;
import application.cyclotrainer.Database.Entities.LocationPoints;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

@Dao
public interface LocationPointsDao {
    @Query("SELECT * FROM LocationPoints")
    List<LocationPoints> getAll();

    @Query("SELECT * FROM LocationPoints WHERE id IN (:routeIds)")
    List<LocationPoints> loadAllByIds(int[] routeIds);

    @Query("SELECT * FROM LocationPoints WHERE id = :id LIMIT 1")
    LocationPoints findById(int id);

    @Insert(onConflict = IGNORE)
    void insertLocationPoints(LocationPoints... locationPoints);

    @Delete
    void delete(LocationPoints locationPoints);

    @Query("DELETE FROM LocationPoints")
    public void deleteLocationPointsTable();
}