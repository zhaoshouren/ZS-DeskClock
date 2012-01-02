package com.zhaoshouren.android.apps.clock.util;

import android.content.Intent;

public class Action extends Intent {
    /**
     * This action triggers the AlarmBroadcastReceiver as well as the AlarmPlayerService. It is
     * a public action used in the manifest for receiving Alarm broadcasts from the alarm
     * manager.
     */
    public static final String ALERT =
            "zs.clock.intent.action.ALARM_ALERT";
    /**
     *  This action triggers the AlarmBroadcastReceiver as well as the AlarmPlayerService. It is
     * a public action used in the manifest for receiving Alarm broadcasts from the alarm
     * manager.
     */
    public static final String ALERT_FULL_SCREEN =
           "zs.clock.intent.action.ALARM_ALERT_FULL_SCREEN";
    /**
     * A public action sent by AlarmPlayerService when the alarm has stopped sounding for any
     * reason (e.g. because it has been dismissed from AlarmAlertFullScreen, or killed due to an
     * incoming phone call, etc).
     */
    public static final String DONE =
            "zs.clock.intent.action.ALARM_DONE";
    /**
     * AlarmAlertFullScreen listens for this broadcast intent, so that other applications can
     * snooze the alarm (after ALARM_ALERT_ACTION and before ALARM_DONE_ACTION).
     */
    public static final String SNOOZE =
            "zs.clock.intent.action.ALARM_SNOOZE";
    /**
     * AlarmAlertFullScreen listens for this broadcast intent, so that other applications can
     * dismiss the alarm (after ALARM_ALERT_ACTION and before ALARM_DONE_ACTION).
     */
    public static final String DISMISS =
            "zs.clock.intent.action.ALARM_DISMISS";
    /**
     * This is an action used by the AlarmPlayerService to update the UI to show the alarm has
     * been killed.
     */
    public static final String KILLED =
            "zs.clock.intent.action.ALARM_KILLED";
    /**
     * Extra in the ACTION_ALARM_KILLED intent to indicate to the user how long the alarm played
     * before being killed.
     */

    public static final String SNOOZE_CANCEL =
            "zs.clock.intent.action.ALARM_SNOOZE_CANCEL";
    public static final String PLAY =
            "zs.clock.intent.action.ALARM_PLAY";
    public static final String STOP =
            "zs.clock.intent.action.ALARM_STOP";
    
    public static final String SET_ALARM =
            "zs.clock.intent.action.ALARM_SET";
    public static final String LIST_ALARMS =
            "zs.clock.intent.action.ALARM_LIST";
}
