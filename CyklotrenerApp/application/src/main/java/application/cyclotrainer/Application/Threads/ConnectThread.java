package application.cyclotrainer.Application.Threads;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

import application.cyclotrainer.Application.Activities.MainActivity;
import application.cyclotrainer.Application.Services.BluetoothService;


public class ConnectThread extends Thread {
    MainActivity mainActivity;
    BluetoothService bluetoothService;
    BluetoothDevice device;

    BluetoothSocket clientSocket = null;
    Handler handler;


    public ConnectThread(Handler handler, BluetoothDevice device, MainActivity mainActivity, BluetoothService bluetoothService) {
        this.mainActivity = mainActivity;
        this.bluetoothService = bluetoothService;
        this.device = device;
        this.handler = handler;
        Log.d("BT",":"+"prepare for connecting to " + device.getName());

        try {

            Method m = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
            clientSocket = (BluetoothSocket) m.invoke(device,1);

        } catch (Exception ee) {
            Log.e("BT", ":"+ee.getMessage());
        }
    }
    public void run() {
        Log.d("BT",":"+"connecting");
        try {
            if (clientSocket != null) {
                clientSocket.connect();
                new ConnectionThread(handler, clientSocket, mainActivity, bluetoothService, device).start();
            }
        } catch (IOException e) {
            Log.d("BT",":"+e.getMessage());
            Log.e("BT", ":"+e.getMessage());
            try {
                if (clientSocket != null)
                    clientSocket.close();
            } catch (IOException ee) {
                Log.e("BT", ":"+ee.getMessage());
            }
            return;
        }
    }
}