package com.phantomgeek.calleridentity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	
	private static String TAG = BootReceiver.class.getName();
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.d(TAG, "boot message received");
		
		Intent callerSpeakService = new Intent(context, CallerIdentityService.class);
		context.startService(callerSpeakService);
	}

}
