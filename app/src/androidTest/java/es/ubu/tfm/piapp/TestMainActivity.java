package es.ubu.tfm.piapp;


import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import es.ubu.tfm.piapp.controlador.MainActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestMainActivity extends ActivityInstrumentationTestCase2<MainActivity> {
    private String mStringToBetyped;
    private MainActivity mActivity;

    // Componentes de la interfaz grafica
    private ImageButton play;
    private ImageButton stop;
    private ImageButton menuBT;
    private ImageButton menuGr;
    private TextView lbSpeed;
    private EditText speed;
    private TextView lbAlgPConstK;
    private EditText AlgPConstK;
    private TextView lbAlgPIConstK;
    private EditText AlgPIConstK;
    private TextView lbAlgPITemp;
    private EditText AlgPITemp;

    // Resultado de invocaciones
    Object result;

    public TestMainActivity() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        //getActivity();
        mActivity = getActivity();

        //play = (ImageButton) mActivity.findViewById(R.id.play);
        //lbSpeed = (TextView) mActivity.getView(R.id.lb_speed);
    }

    // Test validación parseo variables a enviar por Bluetooth
    @Test
    public void testPassToString(){
        final Method methodPassToString;
        try {
            methodPassToString = MainActivity.class.getDeclaredMethod("passToString", new Class[] {Integer.TYPE});
            methodPassToString.setAccessible(true);

            getActivity().runOnUiThread(new Runnable() {
                public void run(){
                    try {
                        result = methodPassToString.invoke(getActivity(), 12);
                    } catch (Exception e) {
                        Log.e("PIapp", "Error al ejecutar método passToString", e);
                    }
                }
            });


            Thread.sleep(100);

            // Comprobamos que no sea vacia
            assertNotNull("No deberia ser null",result);
            // Comprobamos que sea un string
            assertTrue("Deberia ser un String", result.getClass() == String.class);
            // Comprobamos que es el mismo string
            assertEquals("Deberia devolver 012", result,"012");

        } catch (Exception e) {
            Log.e("PIapp", "Error al ejecutar método passToString", e);
        }
    }

    // Test validación cambio estado del Bluetooth
    @Test
    public void testSetEstadoConectado(){
        final Method methodSetEstadoConectado;
        final Method methodSetEstadoConectado2;
        final Method methodSetStatus3;
        final Method methodSetStatus4;
        try {
            methodSetEstadoConectado = MainActivity.class.getDeclaredMethod("setEstadoConectado");
            methodSetEstadoConectado.setAccessible(true);

            getActivity().runOnUiThread(new Runnable() {
                public void run(){
                    try {
                        result = methodSetEstadoConectado.invoke(getActivity(), R.string.conectado_a);
                    } catch (Exception e) {
                        Log.e("PIapp", "Error al ejecutar el método setEstadoConectado", e);
                    }
                }
            });

            Thread.sleep(60);

            // Comprobamos que se ha cambiado el texto
            /*onView(withId(R.id.name_field))
                    .perform(typeText("Steve"));
            onView(withId(R.id.greet_button))
                    .perform(click());
            onView(withText(R.string.conectado_a))
                    .check(matches(isDisplayed()));
            */
            //assertTrue("Deberia mostrarse texto No conectado",solo.searchText(solo.getString(R.string.conectado_a)));

            // Realizamos otra prueba
            methodSetEstadoConectado2 = MainActivity.class.getDeclaredMethod("setEstadoConectado");
            methodSetEstadoConectado2.setAccessible(true);

            getActivity().runOnUiThread(new Runnable() {
                public void run(){
                    try {
                        result = methodSetEstadoConectado2.invoke(getActivity(), R.string.no_conectado);
                    } catch (Exception e) {
                        Log.e("PIapp", "Error al ejecutar el método setEstadoConectado", e);
                    }
                }
            });

            Thread.sleep(60);

            // Comprobamos que se ha cambiado el texto
            /*onView(withId(R.id.menu_bluetooth))
                    .check(matches(isDisplayed()));
            */
        } catch (Exception e) {
            Log.e("TestUbuBot", "Error al invocar el metodo setStatus", e);
        }
    }




    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }


    /*
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    public TestMainActivity(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    @Test
    public void elegirAlgoritmoTest() {
        onView(withId(R.id.rdIntegral)).perform(click());               // click() is a ViewAction
        //onView(withId(R.id.rdIntegral)).check(matches(isDisplayed())); // matches(isDisplayed()) is a ViewAssertion;
    }

    @Test
    public void testClickActionBarItem() {
        // We make sure the contextual action bar is hidden.
        onView(withId(R.id.menu_bluetooth)).perform(click());

        // Click on the icon - we can find it by the r.Id.
        onView(withId(R.id.menu_graphics)).perform(click());

        // Verify that we have really clicked on the icon by checking the TextView content.
        onView(withId(R.id.menu_atras)).check(matches(withText("Save")));
    }

    @Before
    public void initValidString() {
        // Specify a valid string.
        mStringToBetyped = "Espresso";
    }



    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }


    @Test
    public void greeterSaysHello() {
        onView(withId(R.id.txtVelocidad))
                .perform(typeText(""));
        onView(withId(R.id.play))
                .perform(click());
        onView(withText(R.))
                .check(matches(isDisplayed()));

    }


    @RunWith(AndroidJUnit4.class)
    @LargeTest
    public class HelloWorldEspressoTest {

        @Rule
        public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);

        @Test
        public void listGoesOverTheFold() {
            onView(withText("Hello world!")).check(matches(isDisplayed()));
        }
    }*/
}