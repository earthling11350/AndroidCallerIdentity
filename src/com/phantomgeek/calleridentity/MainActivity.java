package com.phantomgeek.calleridentity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ToggleButton serviceStartStopButton = (ToggleButton) findViewById(R.id.serviceToggleButton);
		setButtonState(serviceStartStopButton);
		
		serviceStartStopButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		    	Intent contentUpdateServiceIntent = new Intent(MainActivity.this, CallerIdentityService.class);
		        if (isChecked) {
		        	Log.d(TAG, "Starting service");
		    		startService(contentUpdateServiceIntent);
		        } else {
		        	Log.d(TAG, "Stopping service");
		    		stopService(contentUpdateServiceIntent);
		        }
		    }
		});
	}
	
	private void setButtonState(ToggleButton serviceStartStopButton) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (CallerIdentityService.class.getName().equals(service.service.getClassName())) {
				((ToggleButton) findViewById(R.id.serviceToggleButton)).setChecked(true);
				return;
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		setButtonState((ToggleButton) findViewById(R.id.serviceToggleButton));
	}

}
