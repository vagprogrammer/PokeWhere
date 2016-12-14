package com.javic.pokewhere.broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.google.android.gms.maps.model.Marker;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.javic.pokewhere.util.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by iMac_Vic on 13/12/16.
 */

public class BroadcastScheduleStartServiceMap extends BroadcastReceiver {

    public static final String ACTION_SHEDULE = "com.javic.pokewhere.broadcast.action.SHEDULE_STARTSERVICEMAP";

    // restart service every 15 seconds
    private static final long REPEAT_TIME = 1000 * Constants.REPEAT_TIME;

    private PendingIntent pending;
    private AlarmManager service;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();


        if (extras != null)
            {
                if (extras.getString("action").equalsIgnoreCase("schedule")){

                    service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                    Intent i = new Intent(context, BroadcastStartServiceMap.class);

                    pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

                    Calendar cal = Calendar.getInstance();

                    // start 30 seconds after boot completed
                    cal.add(Calendar.MILLISECOND, 0);
                    // fetch every 30 seconds

                    // InexactRepeating allows Android to optimize the energy consumption
                    //service.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);

                    service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);
                }

                else if (extras.getString("action").equalsIgnoreCase("cancel")){
                    service.cancel(pending);
                }
            }
    }



}
