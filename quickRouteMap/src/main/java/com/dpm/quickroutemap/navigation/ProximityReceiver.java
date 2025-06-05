package com.dpm.quickroutemap.navigation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.LinkedList;

public final class ProximityReceiver extends BroadcastReceiver {

	private static final String LOG_TAG = ProximityReceiver.class.getSimpleName();
	
	public static final String GUIDANCE_POINT_KEY = "guidance-point_key";
	public static final String GUIDANCE_POINT_NARRATIVE = "guidance-point_narrative";	
	
	private static final int MAX_RECENT_QUEUE = 32;
	
	private final LinkedList<String> _recentGuidancePoints= new LinkedList<String>();
	
	private TextToSpeech _tts;
	
	public ProximityReceiver(TextToSpeech tts){
		_tts = tts;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(LOG_TAG, "Alerta de proximidad");
		Bundle extras = intent.getExtras();
  	    if((extras != null) 
  	    		&& intent.hasExtra(GUIDANCE_POINT_KEY) 
  	    		&& intent.hasExtra(GUIDANCE_POINT_NARRATIVE)){
  	    	
  	    	String guidancePointKey = extras.getString(GUIDANCE_POINT_KEY);
  	    	String guidanceNarrative = extras.getString(GUIDANCE_POINT_NARRATIVE);
			Log.d(LOG_TAG, "Message: " + guidancePointKey);
			
			boolean entering = extras.getBoolean(LocationManager.KEY_PROXIMITY_ENTERING);
			Log.d(LOG_TAG, entering?"Entrando":"Saliendo");
			
			if(entering && !isRecent(guidancePointKey)){
				setAsRecent(guidancePointKey);
				Log.i(LOG_TAG, String.format("TTS: %s", guidanceNarrative));
				_tts.speak(guidanceNarrative, TextToSpeech.QUEUE_ADD, null);
			}else{
				Log.d(LOG_TAG, "Esté saliendo o se ha reproducido recientemente.");
			}
		}
	}
	
	private boolean isRecent(String guidancePointKey){		
		return _recentGuidancePoints.contains(guidancePointKey);
	}
	
	private void setAsRecent(String guidancePointKey){
		if (_recentGuidancePoints.size() > MAX_RECENT_QUEUE){
			_recentGuidancePoints.removeFirst();
		}
		_recentGuidancePoints.add(guidancePointKey);
	}
	
}

