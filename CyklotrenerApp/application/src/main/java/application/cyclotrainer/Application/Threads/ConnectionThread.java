package application.cyclotrainer.Application.Threads;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

import application.cyclotrainer.Application.Activities.MainActivity;
import application.cyclotrainer.Application.Fragments.OptionsFragment;
import application.cyclotrainer.Application.Services.BluetoothService;
import application.cyclotrainer.R;


public class ConnectionThread extends Thread {
    MainActivity mainActivity;
    BluetoothService bluetoothService;
    BluetoothDevice bluetoothDevice;

    final BluetoothSocket clientSocket;
    Handler handler;

    BufferedReader br;
    BufferedWriter bw;

    int hrmSensorValue = 0;

    public ConnectionThread(Handler handler, BluetoothSocket clientSocket, MainActivity mainActivity, BluetoothService bluetoothService, BluetoothDevice bluetoothDevice) {

        this.mainActivity = mainActivity;
        this.bluetoothService = bluetoothService;
        this.clientSocket = clientSocket;
        this.bluetoothDevice = bluetoothDevice;
        this.handler = handler;

        Log.d("BT",bluetoothService.model+":"+"connected");
        try {
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            Log.e("BT", ":"+e.getMessage());

            bluetoothService.getConnectedDevices().remove(bluetoothDevice);
            bluetoothService.addConnectedDevices();

        }
    }
    public void run() {
        Log.d("BT",":"+"reading");

        while (true) {
            try {

                handler.post(new Runnable() {
                    public void run() {
                        bluetoothService.getConnectedDevices().add(bluetoothDevice);
                        bluetoothService.addConnectedDevices();
                    }
                });

                int dataPayloadBytesRemaining = 0;
                int byteBufferIndex = 0;
                byte[] byteBuffer = new byte[1024];

                int readByte = 0;
                while((readByte = br.read()) != 0x02) {
                    byteBuffer[byteBufferIndex] = (byte) readByte;
                    byteBufferIndex++;
                }
                if ((readByte = br.read()) != 0x26){
                    continue;
                }
                Log.d("HRM",":"+"MAM MSG ID");
                byteBuffer[byteBufferIndex] = (byte) readByte;
                byteBufferIndex++;

                if((readByte = br.read()) != 55) {
                    continue;
                }
                Log.d("HRM",":"+"MAM DLC");
                byteBuffer[byteBufferIndex] = (byte) readByte;
                byteBufferIndex++;

                dataPayloadBytesRemaining = readByte;
                Log.d("HRM",":"+"Payloud Bytes remaining: " + dataPayloadBytesRemaining);

                while(dataPayloadBytesRemaining > 0) {
                    readByte = br.read();

                    byteBuffer[byteBufferIndex] = (byte) readByte;
                    byteBufferIndex++;
                    dataPayloadBytesRemaining--;
                }

                readByte = br.read();
                byteBuffer[byteBufferIndex] = (byte) readByte; //CRC
                byteBufferIndex++;

                Log.d("HRM",":"+"MAM CRC");

                Log.d("HRM",":"+"Bytes remaining: " + readByte);
                Log.d("HRM",":"+"Buffer count: " + byteBufferIndex);

                if ((readByte = br.read()) != 0x03) {
                    continue;
                }
                Log.d("HRM",":"+"MAM ETX");

                byteBuffer[byteBufferIndex] = (byte) readByte;
                byteBufferIndex++;

                hrmSensorValue = (int)byteBuffer[11];

                handler.post(new Runnable() {
                    public void run() {
                        mainActivity.hrmValue.setText(hrmSensorValue + "");
                        checkMaximumHeartRateInterval();
                    }
                });

            } catch (Exception e) {
                Log.e("BT", ":"+e.getMessage());

                handler.post(new Runnable() {
                    public void run() {
                        bluetoothService.getConnectedDevices().remove(bluetoothDevice);
                        bluetoothService.addConnectedDevices();
                    }
                });
                break;
            }
        }
    }

    public void checkMaximumHeartRateInterval() {
        String trainingZoneString = OptionsFragment.getInstance().trainingZone;
        double maximumHR = OptionsFragment.getInstance().maximumHeartRate;

        if (trainingZoneString == "ENDURANCE") {
            if (hrmSensorValue < maximumHR*0.60) {
                mainActivity.hrmValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrowupfinal, 0);
            } else if (hrmSensorValue > maximumHR*0.75) {
                mainActivity.hrmValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrowdownfinal, 0);
            } else {
                mainActivity.hrmValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrowgoodfinal, 0);
            }
        } else if (trainingZoneString == "STAMINA") {
            if (hrmSensorValue < maximumHR*0.75) {
                mainActivity.hrmValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrowupfinal, 0);
            } else if (hrmSensorValue > maximumHR*0.85) {
                mainActivity.hrmValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrowdownfinal, 0);
            } else {
                mainActivity.hrmValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrowgoodfinal, 0);
            }
        } else if (trainingZoneString == "ECONOMY") {
            if (hrmSensorValue < maximumHR*0.85) {
                mainActivity.hrmValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrowupfinal, 0);
            } else if (hrmSensorValue > maximumHR*0.95) {
                mainActivity.hrmValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrowdownfinal, 0);
            } else {
                mainActivity.hrmValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrowgoodfinal, 0);
            }
        } else if (trainingZoneString == "SPEED") {
            if (hrmSensorValue < maximumHR*0.95) {
                mainActivity.hrmValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrowupfinal, 0);
            } else if (hrmSensorValue > maximumHR*1) {
                mainActivity.hrmValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrowdownfinal, 0);
            } else {
                mainActivity.hrmValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrowgoodfinal, 0);
            }
        }
    }
}