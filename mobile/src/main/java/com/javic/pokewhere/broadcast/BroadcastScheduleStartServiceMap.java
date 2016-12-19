package com.javic.pokewhere.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.javic.pokewhere.services.ServiceMapObjects;
import com.javic.pokewhere.util.Constants;

/**
 * Created by iMac_Vic on 13/12/16.
 */

public class BroadcastScheduleStartServiceMap extends BroadcastReceiver {

    private static final String TAG = ServiceMapObjects.class.getSimpleName();

    public static final String ACTION_SHEDULE = "com.javic.pokewhere.broadcast.action.SHEDULE_START_SERVICEMAP";

    private static CounterToStartService mCounterToStartService;

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();

        if (extras != null)
            {
                if (extras.getString("action").equalsIgnoreCase("schedule")){

                    Toast.makeText(context, "Start Service", Toast.LENGTH_SHORT).show();

                    if (mCounterToStartService == null) {
                        //Instantiate Counter
                        Log.i(TAG, "Initiate  Counter");
                        mCounterToStartService = new CounterToStartService(10000, 1000, context);
                        mCounterToStartService.start();
                    }
                    else{
                        mCounterToStartService.start();
                    }
                }

                else if (extras.getString("action").equalsIgnoreCase("cancel")){

                    Toast.makeText(context, "Stop Service", Toast.LENGTH_SHORT).show();

                    Log.i(TAG, "Cancel Counter");

                    NotificationManagerCompat.from(context).cancel(Constants.NOTIFICATION_ID);


                    mCounterToStartService.cancel();
                }
            }
    }

    private static class CounterToStartService extends CountDownTimer {

        private Context mContext;

        public CounterToStartService(long millisInFuture, long countDownInterval, Context mContext) {
            super(millisInFuture, countDownInterval);
            this.mContext = mContext;
        }

        @Override
        public void onFinish() {
            Intent intent = new Intent(BroadcastStartServiceMap.ACTION_START);
            mContext.sendBroadcast(intent);
            mCounterToStartService.start();
            Log.i(TAG, "Start Service");
        }


        @Override
        public void onTick(long millisUntilFinished) {
            //Toast.makeText(mContext, "onTick --> " + String.valueOf(millisUntilFinished/1000) + " seconds to start the Service", Toast.LENGTH_SHORT).show();
            Log.i(TAG, String.valueOf(millisUntilFinished/1000) + " seconds to start the Service");
        }


    }
}
