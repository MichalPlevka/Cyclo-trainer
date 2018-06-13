package application.cyclotrainer.Application.Services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import application.cyclotrainer.Application.Activities.MainActivity;
import application.cyclotrainer.Application.ApplicationManagement;
import application.cyclotrainer.Application.Fragments.OptionsFragment;
import application.cyclotrainer.Application.Threads.ConnectThread;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT16;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT32;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;
import static android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
import static android.content.ContentValues.TAG;


public class BluetoothService extends Service {

    public MainActivity mainActivity;

    private BluetoothService bluetoothService;
    private BluetoothManager bluetoothManager;
    private Set<BluetoothDevice> connectedDevices = new HashSet<BluetoothDevice>();
    private ListView connectedListView, foundListView;
    private ArrayAdapter<String> connectedAdapter, foundAdapter;

    private Handler handler;


    public String model;

    //User defined variables
    public int wheelDiameter;
    public double wheelCircumference;

    public double gearRatio;

    //Variables for RPM calculation
    public int lastCrankEventTimeValue = 0;
    public int currentCrankEventTimeValue = 0;
    public int currentCrankRevolutionsValue = 0;
    public int lastCrankRevolutionsValue = 0;
    public boolean getFirstTimeDataRPM = true;
    public double rpm = 0;

    //Variables for speed calculation
    public int lastWheelEventTimeValue = 0;
    public int currentWheelEventTimeValue = 0;
    public int currentWheelRevolutionsValue = 0;
    public int lastWheelRevolutionsValue = 0;
    public boolean getFirstTimeDataSpeed = true;
    public double speed = 0;

    private BluetoothGatt bluetoothGatt;
    private BluetoothAdapter bluetoothAdapter;

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";

    public final static UUID UUID_SERVICE_CADENCE_AND_SPEED_MEASUREMENT = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_CHARACTERISTIC_CADENCE_AND_SPEED_MEASUREMENT = UUID.fromString("00002a5b-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_CHARACTERISTIC_SC_CONTROL_POINT = UUID.fromString("00002a55-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_DESCRIPTOR_CADENCE_AND_SPEED_MEASUREMENT = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public final int BT_ENABLE_REQUEST = 777;

    public BluetoothService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.mainActivity = ApplicationManagement.getInstance().getMainActivity();

        ApplicationManagement.getInstance().setBluetoothService(this);
        this.model = android.os.Build.MODEL.split(" ")[0];
        this.handler = new Handler();

        setBluetoothManager();

        this.bluetoothGatt = null;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        enableBT();

        if (mainActivity != null) {
            mainActivity.registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        }
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean setBluetoothAdapter() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (bluetoothManager == null) {
            return false;
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    private void setBluetoothManager() {
        if (bluetoothManager == null && mainActivity != null) {
            bluetoothManager = (BluetoothManager) mainActivity.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
            }
        }
        setBluetoothAdapter();
    }

    public void enableBT() {
        Log.d("BT","enable BT");
        if (bluetoothAdapter != null && mainActivity != null) {
            if (!bluetoothAdapter.isEnabled())
                mainActivity.startActivityForResult(    // enable BT dialog
                        new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                        BT_ENABLE_REQUEST);
            else {
                Toast.makeText(mainActivity, model + ": Bluetooth is already enabled", Toast.LENGTH_LONG);
            }
        }
    }


    // * ak pride intent s BluetoothDevice.ACTION_FOUND, prida device do zoznamu
    // * foundAdapter
    // */
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                foundAdapter.add(device.getName() + "::" + device.getAddress());
                Log.d("BT",model+":"+"found device:" + device.getName() + "::" + device.getAddress());
                Toast.makeText(mainActivity, "found device:" + device.getName() + "::" + device.getAddress(), Toast.LENGTH_LONG).show();
            }
        }
    };

    public void addConnectedDevices() {
        connectedAdapter.clear();
        if (connectedDevices.size() > 0) {
            for (BluetoothDevice device : connectedDevices) {
                connectedAdapter.add(device.getName());
            }
        }
    }

    private void findBluetoothDevices() {
        foundAdapter.clear();

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        if (mainActivity != null) {
            mainActivity.registerReceiver(broadcastReceiver, intentFilter);
        }

        foundListView.setOnItemClickListener(new ListView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                        String item = foundListView.getItemAtPosition(position).toString();
                        String[] items = item.split("::");

                        if (items.length > 1) {
                            BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(items[1]);
                            boolean l = BluetoothDevice.DEVICE_TYPE_LE == remoteDevice.getType();

                            bluetoothAdapter.cancelDiscovery();
                            if (BluetoothDevice.DEVICE_TYPE_LE == remoteDevice.getType()) {
                                bluetoothGatt = remoteDevice.connectGatt(mainActivity, false, gattCallback);
                            } else {
                                new ConnectThread(handler, remoteDevice, mainActivity, BluetoothService.this).start();
                            }
                        }
                    }
                });
    }




    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;

                        connectedDevices.add(gatt.getDevice());

                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addConnectedDevices();
                            }
                        });

                        broadcastUpdate(intentAction);
                        Log.i(TAG, "Connected to GATT server.");
                        Log.i(TAG, "Attempting to start service discovery:" + bluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_DISCONNECTED;

                        connectedDevices.remove(gatt.getDevice());

                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addConnectedDevices();
                            }
                        });

                        Log.i(TAG, "Disconnected from GATT server.");
                        broadcastUpdate(intentAction);
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                        BluetoothGattCharacteristic characteristic = gatt.getService(UUID_SERVICE_CADENCE_AND_SPEED_MEASUREMENT).getCharacteristic(UUID_CHARACTERISTIC_CADENCE_AND_SPEED_MEASUREMENT);
                        gatt.setCharacteristicNotification(characteristic, true);

                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_DESCRIPTOR_CADENCE_AND_SPEED_MEASUREMENT);
                        descriptor.setValue(ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);

                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){
                    BluetoothGattCharacteristic characteristic = gatt.getService(UUID_SERVICE_CADENCE_AND_SPEED_MEASUREMENT).getCharacteristic(UUID_CHARACTERISTIC_SC_CONTROL_POINT);
                    characteristic.setValue(new byte[]{1, 1});
                    gatt.writeCharacteristic(characteristic);
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {

                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getSensorData(characteristic);
                        }
                    });
                }

            };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        mainActivity.sendBroadcast(intent);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    // result of read or notification operations.
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_GATT_CONNECTED.equals(action)) {
                Toast.makeText(mainActivity, "ACTION_GATT_CONNECTED", Toast.LENGTH_LONG).show();

            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(mainActivity, "ACTION_GATT_DISCONNECTED", Toast.LENGTH_LONG).show();

            } else if (ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Toast.makeText(mainActivity, "ACTION_GATT_SERVICES_DISCOVERED", Toast.LENGTH_LONG).show();

            } else if (ACTION_DATA_AVAILABLE.equals(action)) {
                Toast.makeText(mainActivity, "ACTION_DATA_AVAILABLE", Toast.LENGTH_LONG).show();

            }
        }
    };

    //Author: Gergely Kőrössy, link: https://stackoverflow.com/questions/45677032/how-do-you-get-data-from-a-bluetooth-le-device
    private void getSensorData(BluetoothGattCharacteristic characteristic) {
        int offset = 0; // we define the offset that is to be used when reading the next field

        // FORMAT_* values are constants in BluetoothGattCharacteristic
        // these represent the values you can find in the "Value Fields" table in the "Format" column
        int flags = characteristic.getIntValue(FORMAT_UINT8, offset);

        offset += 1; // UINT8 = 8 bits = 1 byte

        // we have to check the flags' 0th bit to see if C1 field exists
        if ((flags & 1) != 0) {
            int cumulativeWheelRevolutions = characteristic.getIntValue(FORMAT_UINT32, offset);
            offset += 4; // UINT32 = 32 bits = 4 bytes

            int lastWheelEventTime = characteristic.getIntValue(FORMAT_UINT16, offset);
            offset += 2; // UINT16 = 16 bits = 2 bytes

            calculateSpeed(lastWheelEventTime, cumulativeWheelRevolutions);
            calculateGear();
        }

        // we have to check the flags' 1st bit to see if C2 field exists
        if ((flags & 2) != 0) {
            int cumulativeCrankRevolutions = characteristic.getIntValue(FORMAT_UINT16, offset);
            offset += 2;

            int lastCrankEventTime = characteristic.getIntValue(FORMAT_UINT16, offset);
            offset += 2;

            calculateRPM(lastCrankEventTime, cumulativeCrankRevolutions);
            calculateGear();
        }




    }

    private void calculateRPM(int lastCrankEventTime, int cumulativeCrankRevolutions) {

        if (getFirstTimeDataRPM) {
            currentCrankRevolutionsValue = cumulativeCrankRevolutions;
            currentCrankEventTimeValue = lastCrankEventTime;
            getFirstTimeDataRPM = false;
        } else {
            if (currentCrankRevolutionsValue != cumulativeCrankRevolutions) {
                lastCrankRevolutionsValue = currentCrankRevolutionsValue;
                currentCrankRevolutionsValue = cumulativeCrankRevolutions;

                lastCrankEventTimeValue = currentCrankEventTimeValue;
                currentCrankEventTimeValue = lastCrankEventTime;

                if ((lastCrankEventTimeValue < currentCrankEventTimeValue)) {
                    rpm = ((60000*(currentCrankRevolutionsValue-lastCrankRevolutionsValue)) / Math.abs(currentCrankEventTimeValue - lastCrankEventTimeValue));

                    if (rpm < 300) {
                        mainActivity.rpmValue.setText("" + String.format("%.0f", rpm));
                    }
                }
            }
        }
    }

    private void calculateSpeed(int lastWheelEventTime, int cumulativeWheelRevolutions) {
        wheelDiameter = OptionsFragment.getInstance().getWheelDiameterValue(); //wheel diameter in mm
        wheelCircumference = (wheelDiameter*Math.PI) / 1000; //circumference of a wheel in m

        if (getFirstTimeDataSpeed) {
            currentWheelRevolutionsValue = cumulativeWheelRevolutions;
            currentWheelEventTimeValue = lastWheelEventTime;
            getFirstTimeDataSpeed = false;
        } else {
            if (currentWheelRevolutionsValue != cumulativeWheelRevolutions) {
                lastWheelRevolutionsValue = currentWheelRevolutionsValue;
                currentWheelRevolutionsValue = cumulativeWheelRevolutions;

                lastWheelEventTimeValue = currentWheelEventTimeValue;
                currentWheelEventTimeValue = lastWheelEventTime;

                if ((lastWheelEventTimeValue < currentWheelEventTimeValue)) {
                    speed = ((((currentWheelRevolutionsValue-lastWheelRevolutionsValue)*wheelCircumference)) / (Math.abs((currentWheelEventTimeValue - lastWheelEventTimeValue))));
                    speed = Math.round(speed*3600);
                    if (speed < 60) {
                        mainActivity.speedValue.setText("" + String.format("%.0f", speed) + " km/h");
                    }

                }
            }
        }
    }

    private void calculateGear(){
        if ((rpm > 20 && rpm <= 220) && speed > 0) {

            gearRatio = (speed*1000)/(wheelCircumference*rpm*60);

            if (ApplicationManagement.getInstance().getNativeSensorService().getSlope() >= 30) {
                double approximateGear = 999;
                double optimalGear = 999;
                for (Double gearRatioFromList : OptionsFragment.getInstance().getGearsListSmall()) {
                    if (Math.abs(gearRatio - gearRatioFromList) <= approximateGear) {
                        approximateGear = Math.abs(gearRatio - gearRatioFromList);
                        optimalGear = gearRatioFromList;
                    }

                }

                mainActivity.textViewChainring.setText("S");

                for (int gearNumber = 0; gearNumber < OptionsFragment.getInstance().getCogsListValues().size(); gearNumber++) {
                    if (((double)OptionsFragment.getInstance().getChainringsListValues().get(0) / (double)OptionsFragment.getInstance().getCogsListValues().get(gearNumber)) == optimalGear) {
                        mainActivity.gearTextView.setText("" + (gearNumber+1));
                        break;
                    }
                }
            }
            else if (ApplicationManagement.getInstance().getNativeSensorService().getSlope() < 30) {
                double approximateGear = 999;
                double optimalGear = 999;
                for (Double gearRatioFromList : OptionsFragment.getInstance().getGearsListBig()) {
                    if (Math.abs(gearRatio - gearRatioFromList) <= approximateGear) {
                        approximateGear = Math.abs(gearRatio - gearRatioFromList);
                        optimalGear = gearRatioFromList;
                    }

                }

                mainActivity.textViewChainring.setText("B");

                for (int gearNumber = 0; gearNumber < OptionsFragment.getInstance().getCogsListValues().size(); gearNumber++) {
                    if (((double)OptionsFragment.getInstance().getChainringsListValues().get(1) / (double)OptionsFragment.getInstance().getCogsListValues().get(gearNumber)) == optimalGear) {
                        mainActivity.gearTextView.setText("" + (gearNumber+1));
                        break;
                    }
                }
            }


        } else {
            mainActivity.gearTextView.setText("N/A");
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt.disconnect();
        }

        try {
            mainActivity.unregisterReceiver(gattUpdateReceiver);
            mainActivity.unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            //IGNORE
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //GETTERS and SETTERS
    public ArrayAdapter<String> getConnectedAdapter() {
        return connectedAdapter;
    }

    public ArrayAdapter<String> getFoundAdapter() {
        return foundAdapter;
    }

    public ListView getConnectedListView() {
        return connectedListView;
    }

    public ListView getFoundListView() {
        return foundListView;
    }



    //public BroadcastReceiver getBroadcastReceiver() { return broadcastReceiver; }

    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }


    public BluetoothAdapter getBtAdapter() { return bluetoothAdapter; }

    public BroadcastReceiver getGattUpdateReceiver() {
        return gattUpdateReceiver;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }


    public void callfindBluetoothDevices() { findBluetoothDevices(); }


    public void setConnectedAdapter(ArrayAdapter<String> pA) {
        this.connectedAdapter = pA;
    }

    public void setFoundAdapter(ArrayAdapter<String> fA) {
        this.foundAdapter = fA;
    }

    public void setConnectedListView(ListView pLV) {
        this.connectedListView = pLV;
    }

    public void setFoundListView(ListView fLV) {
        this.foundListView = fLV;
    }

    public void setBluetoothManager(BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
    }

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    public Set<BluetoothDevice> getConnectedDevices() {
        return connectedDevices;
    }

    public void setConnectedDevices(Set<BluetoothDevice> connectedDevices) {
        this.connectedDevices = connectedDevices;
    }
}

