package com.dpm.quickroutemap.navigation;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

public class GuidancePoint {
	
	private final static int DEFAULT_RADIUS = 500;
	
	private final String _key;
	private final IGeoPoint _point;
	private final String _narrative;
	private final int _radius;

	public GuidancePoint(String key, double latitude, double longitude, String narrative) {
		this(key, latitude, longitude, narrative, DEFAULT_RADIUS);
	}

	public GuidancePoint(String key, double latitude, double longitude, String narrative, int radius){
		_key = key;
		_point = new GeoPoint(latitude, longitude); 
		_narrative = narrative;
		_radius = radius;
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
