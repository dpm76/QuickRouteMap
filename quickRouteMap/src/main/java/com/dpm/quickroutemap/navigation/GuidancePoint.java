package com.dpm.quickroutemap.navigation;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

public class GuidancePoint {
	
	private final static float DEFAULT_RADIUS = 500f;
	
	private String _key;
	private IGeoPoint _point;
	private String _narrative;
	private int _radius;
	
	public GuidancePoint(String key, double latitude, double longitude, String narrative){
		_key = key;
		_point = new GeoPoint(latitude, longitude); 
		_narrative = narrative; 
	}
	
	public String getKey(){
		return _key;
	}
	
	public IGeoPoint getPoint() {
		return _point;
	}

	public double getLatitude(){
		return _point.getLatitude();
	}
	
	public double getLongitude(){
		return _point.getLongitude();
	}
	
	public String getNarrative() {
		return _narrative;
	}
	
	public float getRadius(){
		return (float)_radius;
	}
}
