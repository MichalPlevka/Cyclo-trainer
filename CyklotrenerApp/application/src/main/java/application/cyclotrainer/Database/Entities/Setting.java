package application.cyclotrainer.Database.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Setting {


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @PrimaryKey(autoGenerate = true)
    private int id;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getWheelDiameter() {
        return wheelDiameter;
    }

    public void setWheelDiameter(String wheelDiameter) {
        this.wheelDiameter = wheelDiameter;
    }

    public String getCogs() {
        return cogs;
    }

    public void setCogs(String cogs) {
        this.cogs = cogs;
    }

    public String getChainrings() {
        return chainrings;
    }

    public void setChainrings(String chainrings) {
        this.chainrings = chainrings;
    }

    @ColumnInfo(name = "gender")
    private String gender;

    @ColumnInfo(name = "age")
    private String age;

    @ColumnInfo(name = "wheel_diameter")
    private String wheelDiameter;

    @ColumnInfo(name = "cogs")
    private String cogs;

    @ColumnInfo(name = "chainrings")
    private String chainrings;


    // Getters and setters are ignored for brevity,
    // but they're required for Room to work.
}
