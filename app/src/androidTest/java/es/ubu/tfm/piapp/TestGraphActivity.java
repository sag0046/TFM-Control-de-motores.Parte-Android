package es.ubu.tfm.piapp;


import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import es.ubu.tfm.piapp.controlador.GraphActivity;
import es.ubu.tfm.piapp.controlador.MainActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.reflect.Method;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.junit.Assert.*;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * The type Test graph activity.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestGraphActivity  extends ActivityInstrumentationTestCase2<GraphActivity> {

    /**
     * The Result.
     */
// Resultado de invocaciones
    Object result;

    /**
     * Instantiates a new Test graph activity.
     */
    public TestGraphActivity() {
        super(GraphActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    /**
     * Test add xy series.
     */
// Test validación add series
    @Test
        public void testAddXYSeries(){
        final Method methodCheckValue;
        try {
            methodCheckValue = GraphActivity.class.getDeclaredMethod("addXYSeries", Integer.class, Integer.class);
            methodCheckValue.setAccessible(true);


            getActivity().runOnUiThread(new Runnable() {
                public void run(){
                    try {
                        result = methodCheckValue.invoke(getActivity());
                    } catch (Exception e) {
                        Log.e("PIapp", "Error al ejecutar el método addXYSeries", e);
                    }
                }
            });

        } catch (Exception e) {
            Log.e("PIapp", "Error al invocar el metodo addXYSeries", e);
        }
    }
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
