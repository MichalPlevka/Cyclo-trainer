package application.cyclotrainer.Application;

import java.util.List;

import application.cyclotrainer.Application.Fragments.OptionsFragment;
import application.cyclotrainer.Database.AppDatabase.AppDatabase;
import application.cyclotrainer.Database.Entities.Setting;

public class DatabaseSettingsOperations {

    private AppDatabase db;
    public String gender;
    public String age;
    public String wheelDiameter;
    public String cogs;
    public String chainrings;


    private static DatabaseSettingsOperations ourInstance;

    public static DatabaseSettingsOperations getInstance() {
        if (ourInstance == null) {
            ourInstance = new DatabaseSettingsOperations();
        }
        return ourInstance;
    }

    private DatabaseSettingsOperations() {
        this.db = ApplicationManagement.getInstance().getDb();
    }


    public void selectUserSettings() {

        List<Setting> listOfSettings = db.settingDao().getAll();

        if (listOfSettings.size() == 1) {
            Setting setting = listOfSettings.get(0);
            gender = setting.getGender();
            age = setting.getAge();
            wheelDiameter = setting.getWheelDiameter();
            cogs = setting.getCogs();
            chainrings = setting.getChainrings();

            OptionsFragment.getInstance().setSettingsAndCalculateGears();
        } else {
            insertUserSettings();
            OptionsFragment.getInstance().setSettingsAndCalculateGears();
        }

    }

    public void insertUserSettings() {

        List<Setting> listOfSettings = db.settingDao().getAll();

        if (listOfSettings.size() == 0) {
            gender = OptionsFragment.getInstance().getToggleButtonGender().getText().toString();
            age = OptionsFragment.getInstance().getAgeEdit().getText().toString();
            wheelDiameter = OptionsFragment.getInstance().getWheelDiameterEdit().getText().toString();
            cogs = OptionsFragment.getInstance().getCogsEdit().getText().toString();
            chainrings = OptionsFragment.getInstance().getChainringsEdit().getText().toString();

            Setting setting = new Setting();

            setting.setGender(gender);
            setting.setAge(age);
            setting.setWheelDiameter(wheelDiameter);
            setting.setCogs(cogs);
            setting.setChainrings(chainrings);

            db.settingDao().insertSetting(setting);
        }

    }

    public void updateUserSettings() {

        List<Setting> listOfSettings = db.settingDao().getAll();

        if (listOfSettings.size() == 1) {
            Setting setting = listOfSettings.get(0);

            gender = OptionsFragment.getInstance().getToggleButtonGender().getText().toString();
            age = OptionsFragment.getInstance().getAgeEdit().getText().toString();
            wheelDiameter = OptionsFragment.getInstance().getWheelDiameterEdit().getText().toString();
            cogs = OptionsFragment.getInstance().getCogsEdit().getText().toString();
            chainrings = OptionsFragment.getInstance().getChainringsEdit().getText().toString();

            setting.setGender(gender);
            setting.setAge(age);
            setting.setWheelDiameter(wheelDiameter);
            setting.setCogs(cogs);
            setting.setChainrings(chainrings);

            db.settingDao().updateSetting(setting);
        }

    }
}
