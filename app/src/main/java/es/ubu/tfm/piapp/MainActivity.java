package es.ubu.tfm.piapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Debugging
    private static final String TAG = "PIapp";
    private static final boolean D = true;

    // Codigos de solicitud de Intent
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_POINT = 3;

    // Tipos de mensajes enviados desde el Handler de BluetoothService
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_DEVICE_CONNECTED = 3;
    public static final int MESSAGE_TOAST = 4;

    //Códigos para ejecutar y parar
    public static final int MOVE_STOP = 1;
    public static final int MOVE_PLAY = 2;

    // Nombres de claves recibidas desde el Handler de BluetoothService
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Variables algoritmo
    public static String message; //PIDE INICIALIZAR A NULL
    public static int speed;
    public static int k_P; //cte k para algoritmo proporcional
    public static int k_PI; // cte k para algoritmo PI
    public static int t;

    private BluetoothService mService = null;

    //Adapatdor BT
    private BluetoothAdapter mBluetoothAdapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.btnBt), iconFont);

        // Obtenemos el adaptador Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Si el Bluetooth no está activo, requiere su activación.
        // start() sera llamado en onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // En otro caso, iniciamos
        } else {
            if (mService == null) startBluetoothService();
        }

        Button btnPlay = (Button)findViewById(R.id.play);
        Button btnStop = (Button)findViewById(R.id.stop);

        btnPlay.setOnClickListener(this);


            /*@Override
            public boolean onTouch(View v, MotionEvent event) {
                //PLAY para enviar Velocidad,k,tiempo
                /*Toast toast1 =
                        Toast.makeText(getApplicationContext(),
                                "PLAY", Toast.LENGTH_SHORT);

                toast1.show();
                // Obtenemos el tipo de acción
                int action = MotionEventCompat.getActionMasked(event);

                // Actuamos según el tipo de acción
                switch (action) {
                    case (MotionEvent.ACTION_DOWN):
                        switch (v.getId()) {
                            case (R.id.play):
                                move(MOVE_PLAY);
                                break;
                            case (R.id.stop):
                                move(MOVE_STOP);
                                break;
                        }
                }
                return true;
            }*/
        ;

        /*asociamos el listener a los botones
        btnPlay.setOnTouchListener(mOnTouchListener);
        btnStop.setOnTouchListener(mOnTouchListener);
        */


        /*Button play = (Button)findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //PLAY para enviar Velocidad,k,tiempo
                Toast toast1 =
                        Toast.makeText(getApplicationContext(),
                                "PLAY", Toast.LENGTH_SHORT);

                toast1.show();

            }
        });*/

        /*Button btnStop = (Button)findViewById(R.id.stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Parar motor
                Toast toast2 =
                        Toast.makeText(getApplicationContext(),
                                "STOP ", Toast.LENGTH_SHORT);

                toast2.show();

            }
        });*/

    }

    @Override
    public void onClick(View v) {

        //int action = MotionEventCompat.getActionMasked(event);

        // Actuamos según el tipo de acción
        //switch (action) {
        //    case (MotionEvent.ACTION_DOWN):
        switch (v.getId()) {
            case (R.id.play):
                move(MOVE_PLAY);
                break;
            case (R.id.stop):
                move(MOVE_STOP);
                break;
        }
        //}
        //return true;

    }

    //Check para elegir un algoritmo
    public void elegirAlgoritmo(View v) {
       /* Toast toast1 =
                Toast.makeText(getApplicationContext(),
                        "Toast por defecto", Toast.LENGTH_SHORT);

        toast1.show();*/
        switch(v.getId()) {
            case R.id.rdIntegral:
                findViewById(R.id.txtVelocidad).setEnabled(true);
                findViewById(R.id.txtConstantePI).setEnabled(true);
                findViewById(R.id.txtTiempo).setEnabled(true);

                findViewById(R.id.txtConstanteP).setEnabled(false);
                ((EditText)findViewById(R.id.txtConstanteP)).setText("");
                break;

            case R.id.rdProporcional:
                findViewById(R.id.txtVelocidad).setEnabled(true);
                findViewById(R.id.txtConstanteP).setEnabled(true);

                findViewById(R.id.txtTiempo).setEnabled(false);
                findViewById(R.id.txtConstantePI).setEnabled(false);
                ((EditText)findViewById(R.id.txtConstantePI)).setText("");
                ((EditText)findViewById(R.id.txtTiempo)).setText("");
                break;
        }

    }
    private void startBluetoothService() {
        // Inicializamos el BluetoothService para poder realizar las conexiones de bluetooth.
        mService = new BluetoothService(this, mHandler);

        lanzarBT();
    }

    //Lanza la conexion Bluetooth
    public void lanzarBT() {
        Intent i = new Intent(this, BluetoothActivity.class );
        startActivityForResult(i, REQUEST_CONNECT_DEVICE);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }


    //  parará el motor, pero es necesaria cuando de al boton btnStop.
    @Override
    public void onStop() {
        // Mandamos parar al motor
        move(MOVE_STOP);
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    //repasar
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Se para el servicio BT
        if (mService != null) mService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void move (int movement){
        switch (movement){
            case (MOVE_PLAY):
                seleccionAlgoritmo(R.id.rdProporcional);
                break;
            case (MOVE_STOP):
                break;
        }
    }

    private void seleccionAlgoritmo(int algoritmo) {

        switch (algoritmo){
            case R.id.rdProporcional:
                algoritmoProporcional();
                break;
            case R.id.rdIntegral:
                algoritmoIntegral();
        }
    }

    private void algoritmoProporcional() {
        //Recogemos los valores de Velocidad, k
        EditText speedTxt = (EditText)findViewById(R.id.txtVelocidad);
        EditText k_PTxt = (EditText)findViewById(R.id.txtConstanteP);

        //capturamos
        try{
            //Obtenemos el valor de la velocidad
            speed = Integer.parseInt(speedTxt.getText().toString());
        } catch(Exception e) {
            Toast.makeText(this,R.string.no_vel, Toast.LENGTH_SHORT).show();
            return;
        }
        try{
            //Obtenemos el valor de la cte k_P
            k_P = Integer.parseInt(k_PTxt.getText().toString());
        } catch(Exception e) {
            Toast.makeText(this,R.string.no_kp, Toast.LENGTH_SHORT).show();
            return;
        }

        //Comprobamos si esta en el rango la vel, k_P
        if(speed > 255 || speed < 0){
            Toast.makeText(this, R.string.velocidadIncorrecta, Toast.LENGTH_SHORT).show();
            return;
        }
        if(k_P > 255 || k_P < 0){
            Toast.makeText(this, R.string.kPincorrecta, Toast.LENGTH_SHORT).show();
            return;
        }

        message = passToString(speed) + passToString(k_P) + passToString(500);
        //message = "0000,0000";

        // Obtenemos la cadena de bytes a enviar
        byte[] send = message.getBytes();

        Toast toast1 =
                Toast.makeText(getApplicationContext(),
                        send.toString(), Toast.LENGTH_SHORT);

        toast1.show();

        // Enviamos el mensaje
        //mService.write(send);
    }

    private void algoritmoIntegral() {
        //Recogemos los valores de Velocidad, k, tiempo
        EditText speedTxt = (EditText)findViewById(R.id.txtVelocidad);
        EditText k_PITxt = (EditText)findViewById(R.id.txtConstantePI);
        EditText tTxt = (EditText)findViewById(R.id.txtTiempo);

        //capturamos
        try{
            //Obtenemos el valor de la velocidad
            speed = Integer.parseInt(speedTxt.getText().toString());
        } catch(Exception e) {
            Toast.makeText(this,R.string.no_vel, Toast.LENGTH_SHORT).show();
            return;
        }
        try{
            //Obtenemos el valor de la cte k_PI
            k_PI = Integer.parseInt(k_PITxt.getText().toString());
        } catch(Exception e) {
            Toast.makeText(this,R.string.no_kpi, Toast.LENGTH_SHORT).show();
            return;
        }
        try{
            //Obtenemos el valor del tiempo
            t = Integer.parseInt(tTxt.getText().toString());
        } catch(Exception e) {
            Toast.makeText(this,R.string.no_t, Toast.LENGTH_SHORT).show();
            return;
        }

        //Comprobamos si esta en el rango la vel, k_PI, tiempo
        if(speed > 255 || speed < 0){
            Toast.makeText(this, R.string.velocidadIncorrecta, Toast.LENGTH_SHORT).show();
            return;
        }

        if(k_PI > 255 || k_PI < 0){
            Toast.makeText(this, R.string.kPIincorrecta, Toast.LENGTH_SHORT).show();
            return;
        }
        //para hacer los dos tipos de algoritmos habrá que separar en 2 métodos, para el que tiene "t" hay que añadir esto,
// para el otro pues esta validación no tiene sentido.

        if(t > 255 || t < 0){
            Toast.makeText(this, R.string.tiempoIncorrecta, Toast.LENGTH_SHORT).show();
            return;
        }

        message = passToString(speed) + passToString(k_PI) + passToString(t);
        //message = "0000,0000";

        // Obtenemos la cadena de bytes a enviar
        byte[] send = message.getBytes();

        Toast toast1 =
                Toast.makeText(getApplicationContext(),
                        send.toString(), Toast.LENGTH_SHORT);

        toast1.show();

        // Enviamos el mensaje
        //mService.write(send);
    }


    //Esta dependerá si necesitamos pasarlar a ese formato o queremos enviar los datos en formato hexadecimal.
    // devolvería esto, y por ejemplo si:
//  value = 120 nos devuelve 1,220
//  value = 25 nos devuelve 0,252,55
//  value = 37 nos devuelve 0,373,77
    private String passToString(int value){
        String valueString = "";

        valueString += Integer.toString(Math.abs(value%1000)/100);
        valueString += Integer.toString(Math.abs(value%100)/10);
        valueString += Integer.toString(Math.abs(value%10));

        return valueString;
    }

  //nombre dispositivo conectado
    private String mConnectedDeviceName = null;


    //recibe info de BluetoothService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // Nombre del dispositivo conectado
                case MESSAGE_DEVICE_CONNECTED:
                    //CONECTADO CON EL DISPOSITIVO, POR TANTO SE PUEDE ENVIAR LA SEÑAL AL DISPOSITIVO BLUETOOTH PARA LEERSE EN EL CASE MESSAGE_READ
                    //mService.write();
                    break;
                // Mensaje a mostrar al usuario
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), getString((int) msg.getData().getLong(TOAST)), Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_READ:
                    int bytes = msg.arg1;
                    Toast.makeText(getApplicationContext(), "bytes" + " " + bytes, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Comprueba que solicitamos que estamos respondiendo a
        if (requestCode == REQUEST_ENABLE_BT) {
            // estamos seguros de que la respuesta es correcta
            if (resultCode == RESULT_OK) {
                startBluetoothService();
            }
        }
        else if(requestCode == REQUEST_CONNECT_DEVICE){
            if (resultCode == RESULT_OK) {
                String address = data.getStringExtra(BluetoothActivity.EXTRA_DEVICE_ADDRESS);
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                mConnectedDeviceName = device.getName();
                Toast.makeText(getApplicationContext(), "Conectando con..." + " " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                mService.connect(device);
            }
        }
    }


}
