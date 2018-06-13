package application.cyclotrainer.Database.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(entity = Workout.class, parentColumns = "id", childColumns = "workout_id"))
public class Data {


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @PrimaryKey(autoGenerate = true)
    private int id;

    public int getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(int workoutId) {
        this.workoutId = workoutId;
    }

    @ColumnInfo(name = "workout_id")
    private int workoutId;

    public double getSpeedValue() {
        return speedValue;
    }

    public void setSpeedValue(double speedValue) {
        this.speedValue = speedValue;
    }

    @ColumnInfo(name = "speed")
    private double speedValue;


    public double getCadenceValue() {
        return cadenceValue;
    }

    public void setCadenceValue(double cadenceValue) {
        this.cadenceValue = cadenceValue;
    }

    @ColumnInfo(name = "cadence")
    private double cadenceValue;


    public double getDistanceValue() {
        return distanceValue;
    }

    public void setDistanceValue(double distanceValue) {
        this.distanceValue = distanceValue;
    }

    @ColumnInfo(name = "distanceValue")
    private double distanceValue;

    public double getHrmValue() {
        return hrmValue;
    }

    public void setHrmValue(double hrmValue) {
        this.hrmValue = hrmValue;
    }

    @ColumnInfo(name = "hrmValue")
    private double hrmValue;

    public double getAltitudeValue() {
        return altitudeValue;
    }

    public void setAltitudeValue(double altitudeValue) {
        this.altitudeValue = altitudeValue;
    }

    @ColumnInfo(name = "altitudeValue")
    private double altitudeValue;

    public double getSlopeValue() {
        return slopeValue;
    }

    public void setSlopeValue(double slopeValue) {
        this.slopeValue = slopeValue;
    }

    @ColumnInfo(name = "slopeValue")
    private double slopeValue;

    // Getters and setters are ignored for brevity,
    // but they're required for Room to work.
}
