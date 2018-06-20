package application.cyclotrainer.Application.Threads;

import application.cyclotrainer.Application.DatabaseSettingsOperations;

public class DatabaseSettingsOperationsThread extends Thread {

    public int operationType;

    public DatabaseSettingsOperationsThread(int operationType) {
        this.operationType = operationType;
    }

    @Override
    public void run() {

        switch(operationType) {
            case 0:
                DatabaseSettingsOperations.getInstance().selectUserSettings();
                break;

            case 1:
                DatabaseSettingsOperations.getInstance().insertUserSettings();
                break;

            case 2:
                DatabaseSettingsOperations.getInstance().updateUserSettings();
                break;
        }

    }

}

