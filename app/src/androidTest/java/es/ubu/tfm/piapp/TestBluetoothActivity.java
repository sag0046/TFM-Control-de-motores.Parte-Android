package es.ubu.tfm.piapp;


import android.test.suitebuilder.annotation.LargeTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import es.ubu.tfm.piapp.controlador.BluetoothActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * The type Test bluetooth activity.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestBluetoothActivity extends ActivityInstrumentationTestCase2<BluetoothActivity> {
    private BluetoothActivity btActivity;

    /**
     * Instantiates a new Test bluetooth activity.
     */
    public TestBluetoothActivity() {
        super(BluetoothActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        //getActivity();
        btActivity = getActivity();
    }

    /**
     * Test do discovery.
     */
    @Test
    public void testDoDiscovery(){
        final Method methodDoDiscovery;
        try {
            methodDoDiscovery = BluetoothActivity.class.getDeclaredMethod("doDiscovery");
            methodDoDiscovery.setAccessible(true);

            getActivity().runOnUiThread(new Runnable() {
                public void run(){
                    try {
                        methodDoDiscovery.invoke(getActivity());
                    } catch (Exception e) {
                        Log.e("PIapp", "Error al ejecutar el método doDiscovery", e);
                    }
                }
            });

            Thread.sleep(100);

            // Comprobamos que se mueste el texto de escaneando dispositivos
            assertEquals("Deberia mostrarse texto Escanenado", methodDoDiscovery.invoke(getActivity().getTitle()), btActivity.getString(R.string.scanning));
        } catch (Exception e) {
            Log.e("PIapp", "Error al ejecutar el método doDiscovery", e);
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
