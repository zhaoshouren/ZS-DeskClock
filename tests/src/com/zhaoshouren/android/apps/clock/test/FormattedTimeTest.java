package com.zhaoshouren.android.apps.clock.test;

import android.content.Context;
import android.test.AndroidTestCase;

import com.zhaoshouren.android.apps.clock.R;
import com.zhaoshouren.android.apps.clock.util.FormattedTime;

import java.util.Locale;

public class FormattedTimeTest extends AndroidTestCase {

    private Context mContext;
    private FormattedTime mFormattedTime;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        mFormattedTime = new FormattedTime(mContext);
        mFormattedTime.set(0, 30, 22, 28, 5, 2011); // 12:30 PM, Tuesday, June 28, 2011
        mFormattedTime.normalize(true);
    }

    public void testFormatStrings() {
        assertEquals("PM", mFormattedTime.formattedAmPm);
        assertEquals("Tuesday, June 28", mFormattedTime.formattedDate);
        assertEquals("10:30", mFormattedTime.formattedTime);
        assertEquals("10:30 PM", mFormattedTime.formattedTimeAmPm);

        assertEquals("Jun", mFormattedTime.format(mContext.getString(R.string.format_abbrev_month)));
        assertEquals("Jun 28", mFormattedTime.format(mContext.getString(R.string.format_abbrev_month_day)));
        assertEquals("Jun 28, 2011",
                mFormattedTime.format(mContext.getString(R.string.format_abbrev_month_day_year)));
        assertEquals("Jun 2011",
                mFormattedTime.format(mContext.getString(R.string.format_abbrev_month_year)));
        assertEquals("Tue, 22:30",
                mFormattedTime.format(mContext.getString(R.string.format_abbrev_weekday_hour_minute_24)));
        assertEquals("Tue, 10:30 PM", mFormattedTime.format(mContext
                .getString(R.string.format_abbrev_weekday_hour_minute_cap_am_pm)));
        assertEquals("Tue, June 28, 2011",
                mFormattedTime.format(mContext.getString(R.string.format_abbrev_weekday_month_day_year)));
        assertEquals("PM", mFormattedTime.format(mContext.getString(R.string.format_cap_am_pm)));
        assertEquals("Jun 28, 2011, 10:30:00 pm",
                mFormattedTime.format(mContext.getString(R.string.format_date_and_time)));
        assertEquals("10:30", mFormattedTime.format(mContext.getString(R.string.format_hour_minute)));
        assertEquals("22:30", mFormattedTime.format(mContext.getString(R.string.format_hour_minute_24)));
        assertEquals("10:30 pm",
                mFormattedTime.format(mContext.getString(R.string.format_hour_minute_am_pm)));
        assertEquals("10:30 PM",
                mFormattedTime.format(mContext.getString(R.string.format_hour_minute_cap_am_pm)));
        assertEquals("10:30:00 pm",
                mFormattedTime.format(mContext.getString(R.string.format_hour_minute_second_am_pm)));
        assertEquals("June", mFormattedTime.format(mContext.getString(R.string.format_month)));
        assertEquals("June 28", mFormattedTime.format(mContext.getString(R.string.format_month_day)));
        assertEquals("June 28, 2011",
                mFormattedTime.format(mContext.getString(R.string.format_month_day_year)));
        assertEquals("June 2011", mFormattedTime.format(mContext.getString(R.string.format_month_year)));
        assertEquals("6/28/2011", mFormattedTime.format(mContext.getString(R.string.format_numeric_date)));
        assertEquals("Tuesday, 22:30",
                mFormattedTime.format(mContext.getString(R.string.format_weekday_hour_minute_24)));
        assertEquals("Tuesday, 10:30 PM",
                mFormattedTime.format(mContext.getString(R.string.format_weekday_hour_minute_cap_am_pm)));
        assertEquals("Tuesday, June 28",
                mFormattedTime.format(mContext.getString(R.string.format_weekday_month_day)));   
    }
    
    public void TestLocaleSpecificFormatStrings() {
        mFormattedTime.set(0, 30, 6, 8, 5, 2011); // 12:30 PM, Tuesday, June 28, 2011
        mFormattedTime.normalize(true);

        LocaleTest.changeLocale(mContext, Locale.CHINESE);

        assertEquals("时钟", mContext.getString(R.string.app_label));
        
        //R.string.format_abbrev_month should not be localized
        assertEquals("Jun", mFormattedTime.format(mContext.getString(R.string.format_abbrev_month)));
    }
}
