package com.phantomgeek.calleridentity;

import java.util.Locale;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract.PhoneLookup;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public class CallerIdentityService extends Service implements TextToSpeech.OnInitListener {

	private IBinder mBinder = new CallerIdentityServiceBinder();
	
	private static final String TAG = CallerIdentityService.class.getName();
	
	private TextToSpeech tts;
	private Handler repeatSpeakHandler = new Handler();
	private RepeatSpeakerRunnable repeatSpeakRunnable = new RepeatSpeakerRunnable();
	
	@Override
	public void onCreate() {
		
		Log.d(TAG, "Starting caller service");
		
		tts = new TextToSpeech(this, this);
		
		// Get the telephony manager
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        // Create a new PhoneStateListener
        PhoneStateListener listener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "Incoming call");
                    Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
                    ContentResolver resolver = getContentResolver();
                    Cursor cursor = resolver.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
                    cursor.moveToFirst();
                    
                    int columnIndex = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);
                    String callerName = "";
                    try {
						if(tts != null) {
							callerName = cursor.getString(columnIndex);
							if(TextUtils.isEmpty(callerName)) {
								callerName = "Unknown number";
							} 
						}
					} catch (Exception e) {
						Log.e(TAG, "error while determining caller name");
						callerName = "Unknown number";
					}
                    
                    if(!TextUtils.isEmpty(callerName)) {
                    	repeatSpeakRunnable.setCallerName(callerName);
                    	repeatSpeakHandler.postDelayed(repeatSpeakRunnable, 2000);
                    }
                    break;
                    
                case TelephonyManager.CALL_STATE_OFFHOOK:
                	repeatSpeakHandler.removeCallbacks(repeatSpeakRunnable);
                	break;
                	
                case TelephonyManager.CALL_STATE_IDLE:
                	repeatSpeakHandler.removeCallbacks(repeatSpeakRunnable);
                	break;
                }
            }
        };

        // Register the listener with the telephony manager
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
	
	private class RepeatSpeakerRunnable implements Runnable {
		
		private String callerName;
		
		public void setCallerName(String callerName) {
			this.callerName = callerName;
		}
		
		@Override
		public void run() {
			if(!TextUtils.isEmpty(callerName)) {
				tts.speak(callerName, TextToSpeech.QUEUE_FLUSH, null);
				repeatSpeakHandler.postDelayed(repeatSpeakRunnable, 7000);
			}
		}
	}
	
		
	@Override
    public void onDestroy() {
		super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        repeatSpeakHandler.removeCallbacks(repeatSpeakRunnable);
    }
	
	@Override
    public void onInit(int status) {
		Log.d(TAG, "onInit called");
		
        if (status == TextToSpeech.SUCCESS) {
        	Log.d(TAG, "Init success");
            int result = tts.setLanguage(Locale.US);
 
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
                tts = null;
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
            tts = null;
        }
 
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public class CallerIdentityServiceBinder extends Binder {
		CallerIdentityService getService() {
			return CallerIdentityService.this;
		}
	}

}
