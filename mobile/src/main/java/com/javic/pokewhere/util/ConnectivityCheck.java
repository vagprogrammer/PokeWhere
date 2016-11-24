package com.javic.pokewhere.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by franciscojimenezjimenez on 23/11/16.
 */

public class ConnectivityCheck {

    Context mContext;

    public ConnectivityCheck(Context mContext) {
        this.mContext = mContext;
    }


    private boolean isDeviceOnline() {

        boolean isConnected = false;
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                isConnected = true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                isConnected = true;
            }
        }
        else {
            // not connected to the internet
            isConnected = false;
        }

        return isConnected;
    }
}
