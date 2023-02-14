package com.cobranza.impresora;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cobranza.movil.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class device_list extends Activity {

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Address preference name
    public static String PREF_DEVICE_ADDRESS = "device_address";

    // Class that explain bluetooth device node
    private class DeviceNode {
        private String mName;
        private String mAddress;
        private int mIconResId;

        public DeviceNode(String name, String address, int iconResId) {
            mName = name;
            mAddress = address;
            mIconResId = iconResId;
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }

        public String getAddress() {
            return mAddress;
        }

        public int getIcon() {
            return mIconResId;
        }

        public void setIcon(int resId) {
            mIconResId = resId;
        }
    }

    // Bluetooth device adapter
    public class DeviceAdapter extends BaseAdapter {
        private List<DeviceNode> mNodeList = new ArrayList<DeviceNode>();

        @Override
        public int getCount() {
            return mNodeList.size();
        }

        @Override
        public Object getItem(int location) {
            return mNodeList.get(location);
        }

        @Override
        public long getItemId(int location) {
            return location;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get layout to populate
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = LayoutInflater.from(device_list.this);
                v = vi.inflate(R.layout.device_node, null);
            }

            // Populate the layout with new data
            DeviceNode node = (DeviceNode)getItem(position);
            ((ImageView)v.findViewById(R.id.icon)).setImageResource(node.getIcon());
            ((TextView)v.findViewById(R.id.name)).setText(node.getName());
            ((TextView)v.findViewById(R.id.address)).setText(node.getAddress());

            return v;
        }

        public void add(String name, String address, int iconResId) {
            DeviceNode node = new DeviceNode(name, address, iconResId);
            mNodeList.add(node);
        }

        public void clear() {
            mNodeList.clear();
        }

        public DeviceNode find(String address) {
            for (DeviceNode d : mNodeList) {
                if (address.equals(d.getAddress())) return d;
            }

            return null;
        }
    }

    // The on-click listener for all devices in the ListViews
    private final AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int location, long id) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            DeviceNode node = (DeviceNode)mDevicesAdapter.getItem(location);
            String address = node.getAddress();

            if (BluetoothAdapter.checkBluetoothAddress(address)) {
                finishActivityWithResult(address);
            }
        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                boolean bonded = device.getBondState() == BluetoothDevice.BOND_BONDED;
                int iconId = bonded ? R.mipmap.bluetooth_paired : R.mipmap.bluetooth;
                // Find is device is already exists
                DeviceNode node = mDevicesAdapter.find(device.getAddress());
                // Skip if device is already in list
                if (node == null) {
                    mDevicesAdapter.add(device.getName(), device.getAddress(), iconId);
                } else {
                    node.setName(device.getName());
                    node.setIcon(iconId);
                }

                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.title_select_device);
                findViewById(R.id.scanLayout).setVisibility(View.VISIBLE);
            }
        }
    };

    // Represents the local device Bluetooth adapter.
    private BluetoothAdapter mBtAdapter;
    // Device adapter which handle list of devices.
    private DeviceAdapter mDevicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        setFinishOnTouchOutside(false);


        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mDevicesAdapter = new DeviceAdapter();

        // Initialize the button to perform connect
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final EditText addrView =  findViewById(R.id.device_address);
        addrView.setText(prefs.getString(PREF_DEVICE_ADDRESS, ""));
        Button connButton =  findViewById(R.id.connect);
        connButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String address  = addrView.getText().toString();
                finishActivityWithResult(address);
            }
        });

        // Find and set up the ListView for paired devices
        ListView devicesView =  findViewById(R.id.devices_list);
        devicesView.setAdapter(mDevicesAdapter);
        devicesView.setOnItemClickListener(mDeviceClickListener);
        Log.i("OnCreate","Entra al oncreate");
        // Initialize the button to perform device discovery
        Button scanButton =  findViewById(R.id.scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startDiscovery();
            }
        });


        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        if (mBtAdapter != null && mBtAdapter.isEnabled()) {
            // Get a set of currently paired devices
            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

            // If there are paired devices, add each one to the ArrayAdapter
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    mDevicesAdapter.add(device.getName(), device.getAddress(), R.mipmap.bluetooth_paired);
                }
            }
            findViewById(R.id.title_disabled).setVisibility(View.GONE);
        } else {
            findViewById(R.id.scanLayout).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor edit = prefs.edit();
        final EditText addrView =  findViewById(R.id.device_address);
        edit.putString(PREF_DEVICE_ADDRESS, addrView.getText().toString());
        edit.commit();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onKeyUp( int keyCode, KeyEvent event) {
        if( keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_FIRST_USER);
            finish();
            return true;
        }
        return super.onKeyUp( keyCode, event );
    }

    private void finishActivityWithResult(String address) {
        // Create the result Intent and include the MAC address
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

        // Set result and finish this Activity
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void startDiscovery() {
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.title_scanning);
        findViewById(R.id.scanLayout).setVisibility(View.GONE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }
}