package es.ubu.tfm.piapp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Sandra on 21/02/2016.
 */
public class FontManager {

    /**
     * The constant ROOT.
     */
    public static final String ROOT = "fonts/",
    /**
     * The Fontawesome.
     */
    FONTAWESOME = ROOT + "fontawesome-webfont.ttf";

    /**
     * Gets typeface.
     *
     * @param context the context
     * @param font    the font
     * @return the typeface
     */
    public static Typeface getTypeface(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), font);
    }

    /**
     * Mark as icon container.
     *
     * @param v        the v
     * @param typeface the typeface
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
