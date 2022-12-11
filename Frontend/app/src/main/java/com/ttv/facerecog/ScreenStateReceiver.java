package com.ttv.facerecog;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            //code
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            ((Activity) context).finish();
        }
    }
}
