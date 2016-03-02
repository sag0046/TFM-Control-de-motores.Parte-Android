package es.ubu.tfm.piapp;

import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Created by Sandra on 28/02/2016.
 */
public class BluetoothActivity extends Activity {


    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Establecemos la ventana y el layout
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.bt_activity);
        // Ponemos el resultado CANCELED en caso de que el usuario vuelva hacia atras
        setResult(Activity.RESULT_CANCELED);

        // Inicializamos el boton qe permite descubrir nuevos dispositivos
        Button scanButton = (Button) findViewById(R.id.btnActivar);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });


        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);


        ListView pairedListView = (ListView) findViewById(R.id.dispositivosEmparejados);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);


        ListView newDevicesListView = (ListView) findViewById(R.id.nuevosDispositivos);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);


        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);


        mBtAdapter = BluetoothAdapter.getDefaultAdapter();


        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();


        if (pairedDevices.size() > 0) {
            findViewById(R.id.lbldispositivosEmparejados).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.not_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    /**
     * onDestroy.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();


        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        this.unregisterReceiver(mReceiver);
    }


    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");


        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);


        findViewById(R.id.lblnuevosDispositivos).setVisibility(View.VISIBLE);

        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }


        mBtAdapter.startDiscovery();
    }


    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            mBtAdapter.cancelDiscovery();


            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);


            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);


            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Obtenemos la acción
            String action = intent.getAction();


            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }


            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.selection);

                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.not_discovered).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };


}
