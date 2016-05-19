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

    //@Rule
    //public ActivityTestRule<MainActivity> mActivityTest = new ActivityTestRule<>(MainActivity.class);

    public TestMainActivity() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        //getActivity();
        super.setUp();
        getActivity();

    }

    // Test validación básica botones.
    @Test
    public void testSimpleClickButton(){
        try{
            //Button play
            onView(withId(R.id.play))
                    .perform(click());

            //Button stop
            onView(withId(R.id.stop))
                    .perform(click());

            //Button Bluetooth
            onView(withId(R.id.menu_bluetooth))
                    .perform(click());

            //Button Graphic
            onView(withId(R.id.menu_graphics))
                    .perform(click());

            //Button info
            onView(withId(R.id.menu_info))
                    .perform(click());
        } catch (Exception e) {
            Log.e("PIapp", "Error al pulsar Button", e);
        }
    }

    // Test validación básica textoslabels.
    @Test
    public void testSimpleLabelValue(){
        try{
            //Label Velocidad Referencia
            onView(withId(R.id.lblVelocidad))
                    .check(matches(withText("Velocidad de Referencia(rpm)")));

            //Label Selección algoritmo
            onView(withId(R.id.lblAlgoritmos))
                    .check(matches(withText("Algoritmos de Control")));

            //RadioButton Proporcional
            onView(withId(R.id.rdProporcional))
                    .check(matches(withText("Accion Proporcional")));

            //Label Ganancia Proporcional algoritmo P
            onView(withId(R.id.lblConstanteP))
                    .check(matches(withText("Ganancia Proporcional")));

            //RadioButton Integral
            onView(withId(R.id.rdIntegral))
                    .check(matches(withText("Accion Integral")));

            //Label Ganancia Proporcional algoritmo PI
            onView(withId(R.id.lblConstantePI))
                    .check(matches(withText("Ganancia Proporcional")));

            //Label Tiempo algoritmo PI
            onView(withId(R.id.lblTiempo))
                    .check(matches(withText("T.Integral(s-1)")));

        } catch (Exception e) {
            Log.e("PIapp", "Error al pulsar Button", e);
        }
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
            Log.e("PIapp", "Error al invocar el metodo setStatus", e);
        }
    }

    // Test validación rellenar Labels datos
    @Test
    public void testLabelsInformation(){
        try {
            String valor = "122";
            onView(withId(R.id.txtVelocidad)).perform(typeText(valor));
            //click on Send
            onView(withId(R.id.play)).perform(click());
            // verify is displayed
            onView(withId(R.id.txtVelocidad)).check(ViewAssertions.matches(ViewMatchers.withText(valor)));

        } catch (Exception e) {
            Log.e("PIapp", "Error al validar los TextFiles", e);
        }
    }

    // Test validación cambio estado del Bluetooth
    @Test
    public void testCheckValue(){
        final Method methodCheckValue;
        final Method methodSetEstadoConectado2;
        final Method methodSetStatus3;
        final Method methodSetStatus4;
        try {
            methodCheckValue = MainActivity.class.getDeclaredMethod("checkValue", Integer.class, Integer.class);
            methodCheckValue.setAccessible(true);


            getActivity().runOnUiThread(new Runnable() {
                public void run(){
                    try {
                        result = methodCheckValue.invoke(getActivity());
                    } catch (Exception e) {
                        Log.e("PIapp", "Error al ejecutar el método methodCheckValue", e);
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
            Log.e("PIapp", "Error al invocar el metodo setStatus", e);
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