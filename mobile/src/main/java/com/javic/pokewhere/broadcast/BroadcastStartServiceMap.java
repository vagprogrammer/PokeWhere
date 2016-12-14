package com.javic.pokewhere.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.javic.pokewhere.services.ServiceMapObjects;

/**
 * Created by iMac_Vic on 13/12/16.
 */

public class BroadcastStartServiceMap extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, ServiceMapObjects.class);
        context.startService(service);
    }
}