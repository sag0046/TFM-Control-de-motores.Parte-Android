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
 * Actividad de Bluetooth.
 * @author    Sandra Ajates Gonzalez
 * @version   1.0
 */
public class BluetoothActivity extends AppCompatActivity {
    /**
     * Constante con listado de dispositivos.
     */
    private static final String TAG = "DeviceListActivity";
    /**
     * Constante con booleano de acceso.
     */
    private static final boolean D = true;

    /**
     * Constante con el nombre de la direccion.
     */
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    /**
     * Definicion adaptador Bluetooth.
     */
    private BluetoothAdapter mBtAdapter;
    /**
     * Definicion dispositivos emparejados.
     */
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    /**
     * Definicion dispositivos nuevos.
     */
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    /**
     * Metodo onCreate.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Establece la ventana y el layout.
         */
        setContentView(R.layout.bt_activity);
        /**
         * Establece la orientacion de la pantalla.
         */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        /**
         * Pone el resultado CANCELED en caso de que el usuario vuelva hacia atras.
         */
        setResult(Activity.RESULT_CANCELED);
        /**
         * Establece propiedades del contenedor.
         */
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.btnActivar), iconFont);

        /**
         * Pinta los botones del bar de la parte superior.
         */
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(ContextCompat.getDrawable(this, R.drawable.ic_logo_ubu));
        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /**
         * Inicializa el boton que permite descubrir nuevos dispositivos.
         */
        Button scanButton = (Button) findViewById(R.id.btnActivar);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        /**
         * Inicializa los arrays de los dispositivos.
         */
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        /**
         * Busca y establece el listener para los item del ListView
         * para los DISPOSITIVOS EMPAREJADOS
         */
        ListView pairedListView = (ListView) findViewById(R.id.dispositivosEmparejados);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        /**
         * Busca y establece el listener para los item del ListView
         * para los NUEVOS DISPOSITIVOS ENCONTRADOS.
         */
        ListView newDevicesListView = (ListView) findViewById(R.id.nuevosDispositivos);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        /**
         * Registra el broadcasts cuando un dispositivo nuevo es encontrado.
         */
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        /**
         * Registra el broadcasts cuando ha finalizado la busqueda.
         */
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        /**
         * Obtiene el adaptador Bluetooth.
         */
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        /**
         * Obtiene los dispositivos emparejados.
         */
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        /**
         * Añade los dispositivos emparejados al arrayAdapter.
         */
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

    /**
     * Metodo onDestroy.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        /**
         * Valida de que no realice mas busquedas.
         */
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        /**
         * Elimina el registro del broadcast listeners.
         */
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Hace visibles los dispositivos con el adaptador Bluetooth.
     */
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");

        /**
         * Actualiza el titulo a escaneando.
         */
        setProgressBarIndeterminateVisibility(true);
        getSupportActionBar().setSubtitle(getString(R.string.scanning));

        /**
         * Habilita la visibilidad del sub-titulo de nuevos dispositivos.
         */
        findViewById(R.id.lblnuevosDispositivos).setVisibility(View.VISIBLE);

        /**
         * En el caso de que este realizando la busqueda, se para.
         */
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        /**
         * Visibilidad del Bluetooth Adapter.
         */
        mBtAdapter.startDiscovery();
    }


    /**
     * onClickListener  para todos los disp. en el listView.
     */
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            /**
             * Cancela el descubrimiento de dispositivos ya que se va a producir el enlace a continuacion.
             */
            mBtAdapter.cancelDiscovery();

            /**
             * Obtiene la dirección MAC del dispositivo que son los ultimos 17 caracteres de la Vista.
             */
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            /**
             * Crea el Intent resultante y añade la direccion MAC.
             */
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            /**
             * Pone el resultado a OK y finaliza la Activity.
             */
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    /**
     * Recibe el broadcast de dispositivos.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /**
             * Obtiene la accion.
             */
            String action = intent.getAction();

            /**
             * Realiza las tareas asociadas cuando detecta un dispositivo.
             */
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                /**
                 * Obtiene el objeto BluetoothDevice del Intent.
                 */
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                /**
                 * Si ya esta emparejado, evitarlo, porque ha sido ya publicado.
                 */
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }

                /**
                 * Cuando la busqueda finaliza cambia el titulo.
                 */
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.seleccionar_dispositivo);
                /**
                 * Comprueba si hay nuevos dispositivos.
                 */
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.no_encontrado).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

    /**
     * Método para hacer visible el menu.
     * @param menu el menu a hacer visible.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /**
         * Hace visible el menú correspondiente.
         */
        getMenuInflater().inflate(R.menu.bt_activity_bar, menu);
        return true;
    }

    /**
     * Metodo para interactuar cuando se selecciona un item del menú.
     * @param item botón seleccionado.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /**
             * Si el boton pulsado es la flecha, volverá al menú principal.
             */
            case R.id.menu_atras:
                this.finish();
                return true;
            /**
             * Si el boton pulsado es el caracter i, mostrara la inforamción de la aplicacion.
             */
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
