package com.zhaoshouren.android.apps.clock.test;

import android.content.Context;
import android.content.res.Configuration;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.Locale;

public class LocaleTest extends AndroidTestCase {

    private static Locale[] sLocales = Locale.getAvailableLocales();
    private static Configuration sConfiguration = new Configuration();
    private Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        changeLocale(mContext, Locale.ENGLISH);
    }

    public void testLocales() {
        for (Locale locale : sLocales) {
            Log.i("Locale-locales", locale.toString());
        }

        assertEquals(88, sLocales.length);
    }

    public static void changeLocale(final Context context, final Locale locale) {
        Locale.setDefault(locale);
        sConfiguration.locale = locale;
        context.getResources().updateConfiguration(sConfiguration,
                context.getResources().getDisplayMetrics());

    }

}
