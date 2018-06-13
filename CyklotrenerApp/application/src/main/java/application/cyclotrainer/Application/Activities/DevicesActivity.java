package application.cyclotrainer.Application.Activities;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import application.cyclotrainer.Application.ApplicationManagement;
import application.cyclotrainer.Application.Services.BluetoothService;
import application.cyclotrainer.R;

public class DevicesActivity extends AppCompatActivity implements View.OnClickListener {

    MainActivity mainActivity;
    BluetoothService bluetoothService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMenu);
        setSupportActionBar(toolbar);

        this.bluetoothService = ApplicationManagement.getInstance().getBluetoothService();
        this.mainActivity = ApplicationManagement.getInstance().getMainActivity();

        // zoznam sparovanych zariadeni
        bluetoothService.setConnectedAdapter(new ArrayAdapter<String>(this, R.layout.list_item_connected, R.id.deviceItem));
        bluetoothService.setConnectedListView((ListView) findViewById(R.id.connectedDeviceList));
        bluetoothService.getConnectedListView().setAdapter(bluetoothService.getConnectedAdapter());

        // zoznam najdenych, ale nesparovanych zariadeni
        bluetoothService.setFoundAdapter(new ArrayAdapter<String>(this, R.layout.list_item_found, R.id.deviceItem));
        bluetoothService.setFoundListView((ListView) findViewById(R.id.foundDeviceList));
        bluetoothService.getFoundListView().setAdapter(bluetoothService.getFoundAdapter());

        bluetoothService.addConnectedDevices();

        Button backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_devices_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btList:
                bluetoothService.getBtAdapter().cancelDiscovery();
                bluetoothService.getBtAdapter().startDiscovery();
                bluetoothService.callfindBluetoothDevices();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backButton:
                finish();
                break;
        }
    }

}
