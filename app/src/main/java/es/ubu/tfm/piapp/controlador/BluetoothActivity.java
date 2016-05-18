package es.ubu.tfm.piapp.controlador;

import java.util.Set;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import es.ubu.tfm.piapp.FontManager;
import es.ubu.tfm.piapp.R;


/**
 * The type Bluetooth activity.
 */
public class BluetoothActivity extends AppCompatActivity {
    // Debugging
    private static final String TAG = "DeviceListActivity";//modificar
    private static final boolean D = true;

    /**
     * The constant EXTRA_DEVICE_ADDRESS.
     */
// Retorno extra del Intent
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter mBtAdapter;//adaptador BT
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;//Array disp emparejados
    private ArrayAdapter<String> mNewDevicesArrayAdapter; //Array disp nuevos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Establecemos la ventana y el layout
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.bt_activity);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Ponemos el resultado CANCELED en caso de que el usuario vuelva hacia atras
        setResult(Activity.RESULT_CANCELED);
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.btnActivar), iconFont);

        //getSupportActionBar().setSubtitle(getString(R.string.no_conectado));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(ContextCompat.getDrawable(this, R.drawable.ic_logo_ubu));
        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Inicializamos el boton qe permite descubrir nuevos dispositivos
        Button scanButton = (Button) findViewById(R.id.btnActivar);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        // Inicializamos los arrays de los dispositivos
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Buscamos y establecemos el listener para los item del ListView
        // para los DISPOSITIVOS EMPAREJADOS
        ListView pairedListView = (ListView) findViewById(R.id.dispositivosEmparejados);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Buscamos y establecemos el listener para los item del ListView
        // para los NUEVOS DISPOSITIVOS ENCONTRADOS
        ListView newDevicesListView = (ListView) findViewById(R.id.nuevosDispositivos);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Registra el broadcasts cuando un dispositivo nuevo es encontrado
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Registra el broadcasts cuando ha finalizado la busqueda
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Obtenemos el adaptador Bluetooth
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Obtenemos los dispositivos emparejados
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // Si hay dispositivos emparejados se añade cada uno de ellos al arrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.lbldispositivosEmparejados).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.no_emparejados).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    /*
    private void startBluetoothService() {
        // Inicializamos el BluetoothService para poder realizar las conexiones de bluetooth.
        mService = new BluetoothService(this, mHandler);
    }*/

    //private String mConnectedDeviceName = null;

    // Nombres de claves recibidas desde el Handler de BluetoothService
    //public static final String DEVICE_NAME = "device_name";
    //public static final String TOAST = "toast";

    /**
     *  Handler que recibe la información de BluetoothService
     *//*
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // Nombre del dispositivo conectado
                case MESSAGE_DEVICE_NAME:
                    // Guardamos el nombre del dispositivo con el que estamos conectados
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "tatata" + " " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                // Mensaje a mostrar al usuario
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), getString((int) msg.getData().getLong(TOAST)), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };*/


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Nos aseguramos de que no estamos haciendo mas busquedas
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Desregistramos el broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    //Empieza el descubrimiento de dispositivos con el adaptador Bluetooth.
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");

        // Cambiamos el titulo a escaneando
        setProgressBarIndeterminateVisibility(true);
       // setTitle(R.string.scanning);
        getSupportActionBar().setSubtitle(getString(R.string.scanning));

        // Habilitamos la visibilidad del sub-titulo de nuevos dispositivos
        findViewById(R.id.lblnuevosDispositivos).setVisibility(View.VISIBLE);

        // Si estaba ya buscando, paramos esto
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Petición de descubrimiento de Bluetooth Adapter, es decir, buscamos.
        mBtAdapter.startDiscovery();
    }


    //onClickListener para todos los disp. en el listView
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancelamos el descubrimiento porque es costosa y estamos a punto de conectar
            mBtAdapter.cancelDiscovery();

            // Obtenemos la dirección MAC del dispositivo que son los ultimos 17 caracteres de la Vista
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Creamos el Intent resultante y añadimos la dirección MAC
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Ponemos el resultado a OK y finalizamos la Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Obtenemos la acción
            String action = intent.getAction();

            // Cuando se descubre un dispositivo
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Obtenemos el objeto BluetoothDevice del Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Si ya esta emparejado, evitarlo, porque ha sido ya publicado
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }

                // Cuando la busqueda finaliza cambiamos el titulo
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.seleccionar_dispositivo);
                // Comprobamos si habia nuevos dispositivos
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.no_encontrado).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bt_activity_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_atras:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            case R.id.menu_info:
                // Mostramos dialogo con la información de la aplicación
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.about);
                builder.setIcon(android.R.drawable.ic_menu_info_details);
                builder.setMessage(getString(R.string.author) + ":" + '\n' +
                        "Sandra Ajates Glez" + '\n' + '\n' +
                        getString(R.string.tutor) + ":" + '\n' +
                        "Alejandro Merino Gómez" + '\n' + '\n' +
                        getString(R.string.version) + ":" + '\n' +
                        "2016, Version 1.0" + '\n' + '\n' +
                        getString(R.string.license) + ":" +'\n'+
                        "Apache License, Version 2.0");
                builder.create();
                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
