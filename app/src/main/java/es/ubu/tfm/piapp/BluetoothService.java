package es.ubu.tfm.piapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class BluetoothService {

    // Debugging
    private static final String TAG = "PIapp";
    private static final boolean D = true;

    // Constantes que indican el estado de la conexión
    public static final int STATE_NONE = 0;       // No hace nada
    public static final int STATE_CONNECTING = 1; // Inicializando conexión saliente
    public static final int STATE_CONNECTED = 2;  // Conectado con un dispositivo remoto


    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private int mState;

    public BluetoothService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }


    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Pasamos el nuevo estado al manejador para que lo actualice en la pantalla
        mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }


    public synchronized int getState() {

        return mState;
    }

    public synchronized void restart() {

        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        setState(STATE_NONE);
    }


    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }


    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }


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


    private void connectionFailed() {
        // Enviamos mensaje de que no se ha podido conectar
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putLong(MainActivity.TOAST, R.string.imposible_to_connect);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Reiniciamos
        restart();
    }


    private void connectionLost() {

        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putLong(MainActivity.TOAST, R.string.lost_connection);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Reiniciamos
        restart();
    }


    public void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            mmSocket = tmp;
        }

        /**
         * run.
         */
        @Override
        public void run() {
            setName("ConnectThread");

            // Cancelamos el descubrimiento para una mayor rapidez en la conexión
            mAdapter.cancelDiscovery();

            // Hacemos una conexión al BluetoothSocket
            try {
                // Esta es una llamada de bloqueo y sólo regresará en una conexión correcta o una excepción
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

            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice);
        }

        /**
         * Cierra el socket.
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Fallo al cerrar el socket", e);
            }
        }
    }


    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error al crear los stream del socket.", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        /**
         * run.
         */
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);

                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Desconectada la conexión Bluetooth", e);
                    connectionLost();
                    BluetoothService.this.restart();
                    break;
                }
            }
        }


        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Excepción al enviar.", e);
            }
        }

        /**
         * Cierra el socket.
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Fallo al cerrar el socket de conexión.", e);
            }
        }
    }
}