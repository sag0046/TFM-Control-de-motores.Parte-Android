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

/**
 * Actividad principal, desde la que se podra controlar el motor.
 * @author    Sandra Ajates Gonzalez
 * @version   1.0
 * @see AppCompatActivity
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Define nombre aplicacion.
     */
    private static final String TAG = "PIapp";
    private static final boolean D = true;

    /**
     * Codigos de solicitud de Intent.
     */
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_POINT = 3;

    /**
     * Mensajes recibidos por el BluetoothService.
     */

    /**
     * Constante del mensaje estado modificado.
     */
    public static final int MESSAGE_STATE_CHANGE = 1;
    /**
     * Constante del mensaje leido.
     */
    public static final int MESSAGE_READ = 2;
    /**
     * Constante del mensaje del dispositivo conectado.
     */
    public static final int MESSAGE_DEVICE_CONNECTED = 3;
    /**
     * Constante del mensaje Toast.
     */
    public static final int MESSAGE_TOAST = 4;

    /**
     * Constante con el movimiento de parada.
     */
    public static final int MOVE_STOP = 1;
    /**
     * Constante con el movimiento de play.
     */
    public static final int MOVE_PLAY = 2;

    /**
     * Constante con el nombre del dispositivo Bluetooth.
     */
    public static final String DEVICE_NAME = "device_name";

    /**
     * Constante con el toast.
     */
    public static final String TOAST = "toast";

    /**
     * Constante con el mensaje recibido.
     */
    public static String message; //PIDE INICIALIZAR A NULL
    /**
     * Instanciación del StringBuilder.
     */
    private StringBuilder sb = new StringBuilder();
    /**
     * Constante con la velocidad insertada por el usuario.
     */
    public static int speed;
    /**
     * Constante k_P usada en el algoritmo proporcional.
     */
    public static int k_P; //cte k para algoritmo proporcional
    /**
     * Constante k_PI usada en el algoritmo integral.
     */
    public static int k_PI; // cte k para algoritmo PI
    /**
     * Constante tiempo.
     */
    public static int t;
    /**
     * Vector que contendrá los valores del EjeX.
     */
    public static double [] vecValoresEjeX = new double[2000];
    /**
     * Constante posEjeX.
     */
    public static int posEjeX=0;
    /**
     * Constante con el estado del dispositivo conectado.
     */
    public static boolean deviceConnected=false;

    /**
     * Definicion del objeto BluetoothService.
     */
    private BluetoothService mService = null;

    /**
     * Definicion del objeto BluetoothAdapter.
     */
    private BluetoothAdapter mBluetoothAdapter = null;
    /**
     * Definicion del dispositivo conectado.
     */
    private String mConnectedDeviceName = null;
    /**
     * Definicion del RadioGroup.
     */
    private RadioGroup radioGroupMio;
    /**
     * Definicion del RadioButton.
     */
    private RadioButton radioButtonMio;

    /**
     * Definicion onCreate.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);

        /**
        * Obtiene el adaptador Bluetooth.
        */
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        /**
         * Si el Bluetooth no esta activo, requiere su activacion.
         */
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // En otro caso, iniciamos
        } else {
            if (mService == null) startBluetoothService();
        }

        /**
         * Instanciacion del boton play.
         */
        Button btnPlay = (Button)findViewById(R.id.play);
        /**
         * Instanciacion del boton stop.
         */
        Button btnStop = (Button)findViewById(R.id.stop);


        /**
         * Creacion del listerner del boton play.
         */
        btnPlay.setOnClickListener(this);
        /**
         * Creacion del listerner del boton stop.
         */
        btnStop.setOnClickListener(this);

        /**
         * Establece el nombre del subtítulo.
         */
        getSupportActionBar().setSubtitle(getString(R.string.no_conectado));
        /**
         * Establece el icono del Home.
         */
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        /**
         * Establece el icono del UBU.
         */
        getSupportActionBar().setIcon(ContextCompat.getDrawable(this,R.drawable.ic_logo_ubu));
    }

    /**
     * Creacion del onClick.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /**
             * Accion al pulsar el boton play.
             */
            case (R.id.play):
                move(MOVE_PLAY);
                break;
            /**
             * Accion al pulsar el boton stop.
             */
            case (R.id.stop):
                move(MOVE_STOP);
                break;
        }
    }

    /**
     * Elegir algoritmo.
     *
     * @param v view seleccionado
     */
    public void elegirAlgoritmo(View v) {
        switch(v.getId()) {
            /**
             * Accion al elegir la opción del algoritmo integral.
             */
            case R.id.rdIntegral:
                findViewById(R.id.txtVelocidad).setEnabled(true);
                findViewById(R.id.txtConstantePI).setEnabled(true);
                findViewById(R.id.txtTiempo).setEnabled(true);

                findViewById(R.id.txtConstanteP).setEnabled(false);
                ((EditText)findViewById(R.id.txtConstanteP)).setText("");
                break;
            /**
             * Accion al elegir la opción del algoritmo integral.
             */
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

    /**
     * Inicio del BluetoothService.
     */
    private void startBluetoothService() {
        // Inicializamos el BluetoothService para poder realizar las conexiones de bluetooth.
        mService = new BluetoothService(this, mHandler);
    }

    /**
     * Lanza la conexion Bluetooth.
     */
    public void lanzarBT() {
        /**
         * Si la conexion Bluetooth esta deshabilitada pide su inicio.
         */
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // En otro caso, iniciamos
        }else {
            Intent i = new Intent(this, BluetoothActivity.class);
            startActivityForResult(i, REQUEST_CONNECT_DEVICE);
        }
    }


    /**
     * Lanza la instancia de GraphActivity para pintar el grafico.
     */
    public void lanzarGR() {
        /**
         * Intent del GraphActivity.
         */
        Intent j = new Intent(this, GraphActivity.class );
        if(getPosEjeX()!=0) {
            startActivity(j);
        }else{
            Toast.makeText(this,R.string.encoderNoDefinido, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Pausado de la sincronizacion.
     */
    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    /**
     * Parada de la sincronizacion.
     */
    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    /**
     * Metodo onDestroy.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Se para el servicio BT
        if (mService != null) mService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    /**
     * Metodo que establece el moviento a realizar.
     * @param movement movimiento seleccionado.
     */
    private void move (int movement){
        /**
         * Obtiene el radioGroup seleccionado.
         */
        radioGroupMio = (RadioGroup) findViewById(R.id.GrbGrupo1);

        /**
         * Valida que el dispositivo Bluetooth este conectado.
         */
        int selectedId = radioGroupMio.getCheckedRadioButtonId();

        //deviceConnected=true;

        if(deviceConnected==false){
            Toast.makeText(this,R.string.bluetoothNoConectado, Toast.LENGTH_SHORT).show();
        }else {
            switch (movement) {
                /**
                 * Si el moviento es el play se llama a selección del algoritmo.
                 */
                case (MOVE_PLAY):
                    seleccionAlgoritmo(selectedId);
                    break;
                /**
                 * Si el moviento es el stop se llama al método que envía el movimiento stop.
                 */
                case (MOVE_STOP):
                    stopMotor();
                    break;
            }
        }
    }

    /**
     * Metodo para enviar el mensaje de parada del motor.
     */
    private void stopMotor() {
        message = "S";
        /**
         * Almacena la cadena de Bytes a mandar.
         */
        byte[] send = message.getBytes();
        /**
         * Envia el mensaje por Bluetooth.
         */
        mService.write(send);
    }

    /**
     * Metodo para seleccionar el algoritmo a enviar.
     * @param algoritmo valor seleccionado
     */
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

    /**
     * Metodo para establecer los parametros asociados al algoritmo proporcional.
     */
    private void algoritmoProporcional() {
        /**
         * Recoge la velocidad del algoritmo proporcional.
         */
        EditText speedTxt = (EditText)findViewById(R.id.txtVelocidad);
        /**
         * Método Recoge el valor de la constante k del algoritmo proporcional.
         */
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

        //Comprobamos si esta en el rango la velocidad
        if(checkValue(speed, R.string.velocidadIncorrecta)==false){
            return;
        }

        //Comprobamos si esta en el rango la constante k_P
        if(checkValue(k_P, R.string.kPIincorrecta)==false){
            return;
        }

        message = "P" + passToString(speed) + passToString(k_P);

        // Reseteo variable posicion Array datos a recibir por Bluetooth
        setPosEjeX();
        // Obtenemos la cadena de bytes a enviar
        byte[] send = message.getBytes();

        //Toast toast1 = Toast.makeText(getApplicationContext(), send.toString()+ " parte 1: "+ message, Toast.LENGTH_SHORT);
        //toast1.show();

        // Enviamos el mensaje
        mService.write(send);
    }

    /**
     * Metodo para establecer los parametros asociados al algoritmo integral.
     */
    private void algoritmoIntegral() {
        /**
         * Recoge la velocidad del algoritmo integral.
         */
        EditText speedTxt = (EditText)findViewById(R.id.txtVelocidad);
        /**
         * Recoge la constante k del algoritmo integral.
         */
        EditText k_PITxt = (EditText)findViewById(R.id.txtConstantePI);
        /**
         * Recoge la constante tiempo del algoritmo integral.
         */
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

        message = "I" +passToString(speed) + passToString(k_PI) + passToString(t);

        // Obtenemos la cadena de bytes a enviar
        byte[] send = message.getBytes();

        //Toast toast1 = Toast.makeText(getApplicationContext(), send.toString(), Toast.LENGTH_SHORT);
        //toast1.show();

        // Reseteo variable posicion Array datos a recibir por Bluetooth
        setPosEjeX();

        // Enviamos el mensaje
        mService.write(send);
    }

    /**
     * Metodo que valida que el valor recibido este dentro del rango establecido.
     *
     * @param valor entero a validar.
     * @param mensaje indicador asociado al texto a mostrar en caso de no estar comprendido en el rango.
     * @return retorna si el valor es correcto o no.
     */
    public boolean checkValue(int valor, int mensaje){
        if(valor > 255 || valor < 0) {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
    }


    /**
     * Metodo que parsea el valor a enviar por Bluetooth.
     *
     * @param value entero a parsear.
     * @return retorna el valor parseado.
     */
    private String passToString(int value){
        String valueString = "";

        valueString += Integer.toString(Math.abs(value%1000)/100);
        valueString += Integer.toString(Math.abs(value%100)/10);
        valueString += Integer.toString(Math.abs(value%10));

        return valueString;
    }

    /**
     * Metodo que recibe informacion de BluetoothService.
     */
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

    /**
     * Método que establece el estado conectado en el subtitulo.
     */
    private final void setEstadoConectado() {
        getSupportActionBar().setSubtitle(Html.fromHtml("<small>" + getString(R.string.conectado_a) + mConnectedDeviceName + "</small>"));
        deviceConnected=true;
    }

    /**
     * Método onActivityResult.
     */
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

    /**
     * Retorna la velocidad insertada por el usuario.
     *
     * @return velocidad insertada por el usuario
     */
    protected int getVelDeseada(){
        return speed;
    }

    /**
     * Obtiene los valores recibidos por el Bluetooth.
     *
     * @return vector con los datos recibidos
     */
    public static double[] getVelEncoder(){
        return vecValoresEjeX;
    }

    /**
     * Valida si los caracteres recibidos son enteros.
     *
     * @param str caracteres a validar
     * @return si son enteros o no
     */
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

    /**
     * Establece la posicion 0 para el eje X.
     */
    public void setPosEjeX() {
        posEjeX=0;
    }

    /**
     * Obtiene la ultima posicion de datos recibidos.
     *
     * @return la ultima posicion rellena del vector de datos
     */
    public static int getPosEjeX() {
        return posEjeX;
    }

    /**
     * Establece el estado conexion a NO conectado.
     */
    public static void setEstadoConexion(){
        deviceConnected=false;
    }

    /**
     * Método onCreateOptionsMenu.
     *
     * @param menu pinta el menu en la parte superior de la aplicación.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mn_activity_bar, menu);
        return true;
    }

    /**
     * Método onOptionsItemSelected.
     * Establece la accion a realizar en funcion del item seleccionado.
     *
     * @param item seleccionado.
     */
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