package com.dpm.quickroutemap.navigation;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

public class GuidancePoint {
	
	private final static int DEFAULT_RADIUS = 500;
	
	@com.google.gson.annotations.SerializedName("_key")
	private String _key;
	@com.google.gson.annotations.SerializedName("_point")
	private IGeoPoint _point;
	@com.google.gson.annotations.SerializedName("_narrative")
	private String _narrative;
	@com.google.gson.annotations.SerializedName("_radius")
	private int _radius;

	public GuidancePoint() {
		_key = null;
		_point = null;
		_narrative = null;
		_radius = DEFAULT_RADIUS;
	}

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
