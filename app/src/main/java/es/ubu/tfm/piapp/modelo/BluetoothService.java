package es.ubu.tfm.piapp.modelo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import es.ubu.tfm.piapp.controlador.MainActivity;
import es.ubu.tfm.piapp.R;


public class BluetoothService {

    // Debugging
    private static final String TAG = "PIapp";
    private static final boolean D = true;

    // Identificador unico UUID para esta aplicación
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Constantes que indican el estado de la conexion actual
    public static final int STATE_NONE = 0;       // no estamos haciendo anda
    public static final int STATE_CONNECTING = 1; // ahora inicializando una conexión saliente
    public static final int STATE_CONNECTED = 2;  // ahora conectado con un dispositivo remoto

    // Campos miembros
    private ConnectThread mConnectThread; //Thread para conectar
    private ConnectedThread mConnectedThread; // Thread conectado
    private final BluetoothAdapter mAdapter; //adaptador BT
    private final Handler mHandler; //Handler
    private int mState;//estado

    private MainActivity mainPrinc = new MainActivity();


    //Se encarga de preparar una sesion BT
    public BluetoothService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    //Establece el estado conexion actual
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Pasamos el nuevo estado al manejador para que lo actualice en la pantalla
        mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    //Devuelve el estado de conexion actual
    public synchronized int getState() {
        return mState;
    }

    //Se encarga de reiniciar el servicio cancelando todos los hilos de conexion
    public synchronized void restart() {

        // Cancela cualquier Threads que estuviera intentado conectar
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancela cualquier Threads que estuviera conectado
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cambiamos el estado
        setState(STATE_NONE);
    }

    //Inicia el ConnectThread para inicilizar una conexion a un dispositivo remoto
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancela cualquier Threads que estuviera intentando conectar
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancela cualquier Threads que estuviera ya conectado
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}



        // Inicia el Thread para conectar con el dispositivo en cuestion
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

   //Inicia el ConnectedThread para comenzar a gestionar una conexión Bluetooth
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        // Cancela cualquier Threads que estuviera intentado conectar
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancela cualquier Threads que estuviera conectado
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Iniciamos thread para manejar la conexión
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_CONNECTED);
        mHandler.sendMessage(msg);

        // Cambiamos el estado
        setState(STATE_CONNECTED);
    }

    //PARA todos los threads
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cambiamos el estado
        setState(STATE_NONE);
    }

    //Escribie en el ConnectedThread de una manera no sincronizada
    // es decir, manda por conexión bluetooth la cadena de bytes.
    public void write(byte[] out) {
        ConnectedThread r; // Se crea un objeto temporal
        // Sincronizamos con el ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Escribimos desde el objeto temporal
        r.write(out);
    }


     //Indica que la conexión ha fallado y se lo comunica a la actividad de la interfaz de usuario.
    private void connectionFailed() {
        // Envia un mensaje de fallo de vuelta a la actividad de que no se ha podido enviar
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putLong(MainActivity.TOAST, R.string.imposible_to_connect);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Reiniciamos
        restart();
    }


     //Indica que la conexión se ha perdido y se lo comunica a la UI Activity (actividad de la interfaz de usuario).
    private void connectionLost() {
        // Envia un mensaje de que se ha perdido la conexion
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putLong(MainActivity.TOAST, R.string.lost_connection);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mainPrinc.setEstadoConexion();

        // Reiniciamos
        restart();
    }

    //Este hilo se ejecuta al intentar realizar una conexión de salida con un dispositivo.
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice; //dispositivo BT

       //Constructor. Inicializa el Socket
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Obtenemos el BluetoothSocket para una conexion con el
            // BluetoothDevice pasado
            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Fallo en la creación del socket Bluetooth.", e);
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            setName("ConnectThread");

            //Siempre cancelar descubrimiento porque va a reducir la velocidad de una conexión
            mAdapter.cancelDiscovery();

            // Hacemos una conexión al BluetoothSocket
            try {
                // Esta es una llamada de bloqueo y sólo regresará
                // en una conexión correcta o una excepción
                mmSocket.connect();
            } catch (IOException e) {
                Log.i(TAG, "Fallo en mConnectThread en la conexión Bluetooth.", e);
                // Cerramos el socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "Fallo durante la conexión, imposible cerrar el socket", e2);
                }
                connectionFailed();
                return;
            }

            // Reseteamos el ConnectThread porque hemos terminado
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // Comenzamos el thread de conexión
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Fallo al cerrar el socket", e);
            }
        }
    }


    //Este hilo se ejecuta durante una conexión con un dispositivo remoto.

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket; //Socket BT
        private final InputStream mmInStream; //Stream entrada
        private final OutputStream mmOutStream; //Stream SAlida

        //Constructor que obtiene ambos streams
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Obtenemos los streams de entrada y salida del BluetoothSocket
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error al crear los stream del socket.", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Nos mantememos escuchando InputStream mientras este conectado
            while (true) {
                try {

                    // Leemos el InputStream
                    bytes = mmInStream.read(buffer);

                    // Enviamos los bytes recogidos al Handler del MainActivity
                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Desconectada la conexión Bluetooth", e);
                    connectionLost();
                    // Iniciamos el servicio para restaurar los thread
                    BluetoothService.this.restart();
                    break;
                }
            }
        }

        //Escribe en el Stream de salida las cadena de bytes a enviar
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Excepción al enviar.", e);
            }
        }

      //cierra o cancela el Socket
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Fallo al cerrar el socket de conexión.", e);
            }
        }
    }
}