package es.ubu.tfm.piapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

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
    public static final int MESSAGE_DEVICE_NAME = 3;
    public static final int MESSAGE_TOAST = 4;

    // Codigos para movimientos
    public static final int MOVE_STOP = 1;
    public static final int MOVE_DOWN = 2;
    public static final int MOVE_UP = 3;
    public static final int MOVE_LEFT = 4;
    public static final int MOVE_RIGHT = 5;

    // Nombres de claves recibidas desde el Handler de BluetoothService
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.btnBt), iconFont);
    }

    public void lanzarBT(View v) {
        Intent i = new Intent(this, BluetoothActivity.class );
        startActivity(i);
    }

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
}
