package es.ubu.tfm.piapp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Servicios de Bluetooth.
 * @author    Sandra Ajates Gonzalez
 * @version   1.0
 */
public class FontManager {

    /**
     * Constante ROOT.
     */
    public static final String ROOT = "fonts/",
    /**
     * libreria Fontawesome.
     */
    FONTAWESOME = ROOT + "fontawesome-webfont.ttf";

    /**
     * Retorna el typeface.
     *
     * @param context contexto
     * @param font    fuente
     * @return devuelve el typeface
     */
    public static Typeface getTypeface(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), font);
    }

    /**
     * Marca como icono contenedor
     *
     * @param v        vista v
     * @param typeface el typeface
     */
    public static void markAsIconContainer(View v, Typeface typeface) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                markAsIconContainer(child, typeface);
            }
        } else if (v instanceof TextView) {
            ((TextView) v).setTypeface(typeface);
        }
    }

}
