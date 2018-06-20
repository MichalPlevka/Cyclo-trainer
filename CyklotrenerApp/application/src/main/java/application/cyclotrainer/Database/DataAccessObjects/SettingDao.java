package application.cyclotrainer.Database.DataAccessObjects;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import application.cyclotrainer.Database.Entities.Data;
import application.cyclotrainer.Database.Entities.Setting;
import application.cyclotrainer.Database.Entities.Workout;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static android.arch.persistence.room.OnConflictStrategy.IGNORE;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface SettingDao {
    @Query("SELECT * FROM setting")
    List<Setting> getAll();

    @Query("SELECT * FROM setting WHERE id IN (:settingIds)")
    List<Setting> loadAllByIds(int[] settingIds);

    @Insert(onConflict = IGNORE)
    void insertSetting(Setting... settings);

    @Delete
    void delete(Setting setting);

    @Update(onConflict = REPLACE)
    void updateSetting(Setting... settings);

    @Query("DELETE FROM setting")
    public void deleteSettingTable();
}