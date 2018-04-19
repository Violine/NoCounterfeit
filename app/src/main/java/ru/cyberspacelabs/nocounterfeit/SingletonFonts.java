package ru.cyberspacelabs.nocounterfeit;

import android.content.Context;
import android.graphics.Typeface;

public class SingletonFonts {
    private static Typeface normalFont;
    private static Typeface boldFont;


    public Typeface getNormalFont() {
        return normalFont;
    }

    public  Typeface getBoldFont() {
        return boldFont;
    }


    public static void setCustomFont(Typeface normalFont) {
        SingletonFonts.normalFont = normalFont;
    }

    public static void setCustomBoldFont(Typeface boldFont) {
        SingletonFonts.boldFont = boldFont;
    }


    private static volatile SingletonFonts instance;

    private SingletonFonts() {}

    public static SingletonFonts getInstance(Context activity) {
        SingletonFonts localInstance = instance;
        if (localInstance == null) {
            synchronized (SingletonFonts.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new SingletonFonts();
                }
            }
            setCustomFont(Typeface.createFromAsset(activity.getAssets(), "segoeui.ttf"));
            setCustomBoldFont(Typeface.createFromAsset(activity.getAssets(), "segoeuib.ttf"));

        }
        return localInstance;
    }


}
