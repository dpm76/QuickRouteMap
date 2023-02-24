package com.dpm.quickroutemap;

import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public class ExtendedMyLocationNewOverlay extends MyLocationNewOverlay implements SensorEventListener{

	private final Display _display;
	
	private final SensorManager _sensorManager;
	private final Sensor _accelerometer;
	private final Sensor _magnetometer;
	
	private float[] _gravity;
	private float[] _geomagnetic;
	private float _azimuth;
	
	public ExtendedMyLocationNewOverlay(Context context, MapView mapView) {
		
		super(mapView);
		_display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		_sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
	    _accelerometer = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    _magnetometer = _sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	@Override
	protected void drawMyLocation(Canvas canvas, Projection projection, Location lastFix) {
		
		float orientation = getCurrentOrientation();
		lastFix.setBearing(orientation);
		
		super.drawMyLocation(canvas, projection, lastFix);
	}
	
	/**
	 * Calcula la orientación teniendo en cuenta la orientación de la pantalla.
	 * 
	 * @return Orientación del lado superior de la pantalla
	 */
	private float getCurrentOrientation(){
		
		float offset;

		switch (_display.getRotation()) {
			case Surface.ROTATION_90:
				offset = 90f;
				break;
			case Surface.ROTATION_180:
				offset = 180f;
				break;
			case Surface.ROTATION_270:
				offset = 270f;
				break;
			case Surface.ROTATION_0:
			default:
				offset = 0f;
				break;
		}
		
		return _azimuth + offset;
	}
	
	@Override
	public boolean enableMyLocation() {
		
		_sensorManager.registerListener(this, _accelerometer, SensorManager.SENSOR_DELAY_UI);
		_sensorManager.registerListener(this, _magnetometer, SensorManager.SENSOR_DELAY_UI);
		 
		
		return super.enableMyLocation();
	}
	
	@Override
	public void disableMyLocation() {
		super.disableMyLocation();
		_sensorManager.unregisterListener(this);
		
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		//Nada que hacer
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			
			_gravity = event.values;
			
		}else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
			
	    	_geomagnetic = event.values;
	    	
		}
		
		if (_gravity != null && _geomagnetic != null) {
		    	
	    	float[] R = new float[9];
	    	float[] I = new float[9];
	    	
	    	boolean success = SensorManager.getRotationMatrix(R, I, _gravity, _geomagnetic);
	    	if (success) {
	    		
	    		float[] orientation = new float[3];
	    		SensorManager.getOrientation(R, orientation);
	    		_azimuth = (float)(((double)orientation[0])*180d/Math.PI); // orientation contains: azimut (yaw), pitch and roll
	    		mMapView.invalidate();
	    	}
		}
	}
}
