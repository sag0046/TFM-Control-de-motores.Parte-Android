package es.ubu.tfm.piapp.controlador;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import es.ubu.tfm.piapp.FontManager;
import es.ubu.tfm.piapp.R;
import es.ubu.tfm.piapp.modelo.BluetoothService;

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
    private StringBuilder sb = new StringBuilder();
    public static int speed;
    public static int k_P; //cte k para algoritmo proporcional
    public static int k_PI; // cte k para algoritmo PI
    public static int t;
    public static double [] vecValoresEjeX = new double[2000];
    public static int posEjeX=0;
    public static boolean deviceConnected=false;

    //Servicio BT
    private BluetoothService mService = null;
    //Adapatdor BT
    private BluetoothAdapter mBluetoothAdapter = null;
    //nombre dispositivo conectado
    private String mConnectedDeviceName = null;

    private RadioGroup radioGroupMio;
    private RadioButton radioButtonMio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);

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
        btnStop.setOnClickListener(this);

        getSupportActionBar().setSubtitle(getString(R.string.no_conectado));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(ContextCompat.getDrawable(this,R.drawable.ic_logo_ubu));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.play):
                move(MOVE_PLAY);
                break;
            case (R.id.stop):
                move(MOVE_STOP);
                break;
        }
    }

    //Check para elegir un algoritmo
    public void elegirAlgoritmo(View v) {
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
    }

    //Lanza la conexion Bluetooth
    public void lanzarBT() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // En otro caso, iniciamos
        }else {
            Intent i = new Intent(this, BluetoothActivity.class);
            startActivityForResult(i, REQUEST_CONNECT_DEVICE);
        }
    }


    public void lanzarGR() {
        Intent j = new Intent(this, GraphActivity.class );
        if(getPosEjeX()!=0) {
            startActivity(j);
        }else{
            Toast.makeText(this,R.string.encoderNoDefinido, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Se para el servicio BT
        if (mService != null) mService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void move (int movement){
        radioGroupMio = (RadioGroup) findViewById(R.id.GrbGrupo1);

        // get selected radio button from radioGroup
        int selectedId = radioGroupMio.getCheckedRadioButtonId();

        //deviceConnected=true;

        if(deviceConnected==false){
            Toast.makeText(this,R.string.bluetoothNoConectado, Toast.LENGTH_SHORT).show();
        }else {
            switch (movement) {
                case (MOVE_PLAY):
                    seleccionAlgoritmo(selectedId);
                    break;
                case (MOVE_STOP):
                    stopMotor();
                    break;
            }
        }
    }

    private void stopMotor() {
        message = "S";
        //message = "0000,0000";
        // Obtenemos ldeviceConnecteda cadena de bytes a enviar
        byte[] send = message.getBytes();
        mService.write(send);
    }

    private void seleccionAlgoritmo(int algoritmo) {
        switch (algoritmo){
            case R.id.rdProporcional:
                //Toast toast32 = Toast.makeText(getApplicationContext(), "algoritmo 1", Toast.LENGTH_SHORT);
                //toast32.show();
                algoritmoProporcional();
                break;
            case R.id.rdIntegral:
                //Toast toast33 = Toast.makeText(getApplicationContext(), "algoritmo 2", Toast.LENGTH_SHORT);
                //toast33.show();
                algoritmoIntegral();
                break;
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

 /*       //Comprobamos si esta en el rango la vel, k_P
        if(speed > 255 || speed < 0){
            Toast.makeText(this, R.string.velocidadIncorrecta, Toast.LENGTH_SHORT).show();
            return;
        }
        if(k_P > 255 || k_P < 0){
            Toast.makeText(this, R.string.kPincorrecta, Toast.LENGTH_SHORT).show();
            return;
        }
 */

        //Comprobamos si esta en el rango la velocidad
        if(checkValue(speed, R.string.velocidadIncorrecta)==false){
            return;
        }

        //Comprobamos si esta en el rango la constante k_P
        if(checkValue(k_P, R.string.kPIincorrecta)==false){
            return;
        }

        message = "P" + passToString(speed) + passToString(k_P);

        // Reseteo variable posición Array datos a recibir por Bluetooth
        setPosEjeX();
        // Obtenemos la cadena de bytes a enviar
        byte[] send = message.getBytes();

        //Toast toast1 = Toast.makeText(getApplicationContext(), send.toString()+ " parte 1: "+ message, Toast.LENGTH_SHORT);
        //toast1.show();

        // Enviamos el mensaje
        mService.write(send);
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

        //Comprobamos si esta en el rango la velocidad
        if(checkValue(speed, R.string.velocidadIncorrecta)==false){
            return;
        }

        //Comprobamos si esta en el rango la constante k_PI
        if(checkValue(k_PI, R.string.kPIincorrecta)==false){
            return;
        }

        //Comprobamos si esta en el rango la constante tiempo
        if(checkValue(t, R.string.tiempoIncorrecta)==false){
            return;
        }
/*
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
*/
        message = "I" +passToString(speed) + passToString(k_PI) + passToString(t);

        // Obtenemos la cadena de bytes a enviar
        byte[] send = message.getBytes();

        //Toast toast1 = Toast.makeText(getApplicationContext(), send.toString(), Toast.LENGTH_SHORT);
        //toast1.show();

        // Reseteo variable posición Array datos a recibir por Bluetooth
        setPosEjeX();

        // Enviamos el mensaje
        mService.write(send);
    }

    public boolean checkValue(int valor, int mensaje){
        if(valor > 255 || valor < 0) {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
    }


    //Esta dependerá si necesitamos pasarlar a ese formato o queremos enviar los datos en formato hexadecimal.
    private String passToString(int value){
        String valueString = "";

        valueString += Integer.toString(Math.abs(value%1000)/100);
        valueString += Integer.toString(Math.abs(value%100)/10);
        valueString += Integer.toString(Math.abs(value%10));

        return valueString;
    }

    //recibe info de BluetoothService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // Nombre del dispositivo conectado
                case MESSAGE_DEVICE_CONNECTED:
                    //CONECTADO CON EL DISPOSITIVO, POR TANTO SE PUEDE ENVIAR LA SEÑAL AL DISPOSITIVO BLUETOOTH PARA LEERSE EN EL CASE MESSAGE_READ
                    //mService.write();
                    //mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    //getSupportActionBar().setSubtitle(String.format(getString(R.string.conectado_a),mConnectedDeviceName));
                    setEstadoConectado();
                    break;
                // Mensaje a mostrar al usuario
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), getString((int) msg.getData().getLong(TOAST)), Toast.LENGTH_SHORT).show();
                    getSupportActionBar().setSubtitle(getString((int) msg.getData().getLong(TOAST)));
                    break;
                case MESSAGE_READ:
                    // Obtenemos la cadena de bytes recibidos
                    byte[] readBuf = (byte[]) msg.obj;
                    // Pasamos a string
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    String sbprint="";

                    boolean esNumero = isNumeric(readMessage);
                    sb.append(readMessage);

                    if(!esNumero){
                        sb.delete(0, sb.length());
                    }

                    if(sb.length()==3){
                        try {
                            vecValoresEjeX[posEjeX] = Double.parseDouble(sb.toString());
                            posEjeX++;
                            sb.delete(0, sb.length());
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(), "Excepción " + sb.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        }
    };

    private final void setEstadoConectado() {
        getSupportActionBar().setSubtitle(Html.fromHtml("<small>" + getString(R.string.conectado_a) + mConnectedDeviceName + "</small>"));
        deviceConnected=true;
    }


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

    protected int getVelDeseada(){
        return speed;
    }

    public static double[] getVelEncoder(){
        return vecValoresEjeX;
    }

    public static boolean isNumeric(String str) {
        if(str == null || str.isEmpty()){
            return false;
        }
        if (!Character.isDigit(str.charAt(0))){
            return false;
        }
        int i = 0;
        for (i=0; i< str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

    public void setPosEjeX() {
        posEjeX=0;
    }

    public static int getPosEjeX() {
        return posEjeX;
    }

    public static void setEstadoConexion(){
        deviceConnected=false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mn_activity_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_bluetooth:
                lanzarBT();
                return true;
            case R.id.menu_graphics:
                lanzarGR();
                return true;
            case R.id.menu_info:
                // Mostramos dialogo con la información de la aplicación
                AlertDialog.Builder builder = new Builder(this);
                builder.setTitle(R.string.about);
                builder.setIcon(android.R.drawable.ic_menu_info_details);
                builder.setMessage(getString(R.string.author) + ":" + '\n' +
                        "Sandra Ajates Glez" + '\n' + '\n' +
                        getString(R.string.tutor) + ":" + '\n' +
                        "Alejandro Merino Gómez" + '\n' + '\n' +
                        getString(R.string.version) + ":" + '\n' +
                        "2016, Version 1.0" + '\n' + '\n' +
                        getString(R.string.license) + ":" + '\n' +
                        "Apache License, Version 2.0");
                builder.create();
                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}