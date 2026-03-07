package com.dpm.quickroutemap.navigation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osmdroid.api.IGeoPoint;

/**
 * Ruta
 * 
 * @author David
 *
 */
public class Route {

	@com.google.gson.annotations.SerializedName("_key")
	private String _key;
	@com.google.gson.annotations.SerializedName("_name")
	private String _name;
	@com.google.gson.annotations.SerializedName("_description")
	private String _description;
	@com.google.gson.annotations.SerializedName("_isClosed")
	private boolean _isClosed;
	//private final List<IGeoPoint> _wayPointsList = new ArrayList<IGeoPoint>();
	@com.google.gson.annotations.SerializedName("_wayPoints")
	private IGeoPoint[] _wayPoints;
	@com.google.gson.annotations.SerializedName("_guidancePoints")
	private GuidancePoint[] _guidancePoints;
	@com.google.gson.annotations.SerializedName("_totalDistance")
	private double _totalDistance;
	@com.google.gson.annotations.SerializedName("_totalTime")
	private double _totalTime;
	
	public Route(){		
	}
	
	public Route(String key, String name, String description, boolean isClosed, IGeoPoint[] wayPoints, GuidancePoint[] guidancePoints, double totalDistance, double totalTime){
		_key = key;
		_name = name;
		_description = description;
		_isClosed = isClosed;
		_wayPoints = wayPoints;
		_guidancePoints = guidancePoints;
		_totalDistance = totalDistance;
		_totalTime = totalTime;
	}
	
	/**
	 * @return Nombre de la ruta
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * @param name Nombre de la ruta
	 */
	public void setName(String name) {
		this._name = name;
	}

	/**
	 * @return Descripción de la ruta
	 */
	public String getDescription() {
		return _description;
	}

	/**
	 * @param description Descripción de la ruta
	 */
	public void setDescription(String description) {
		this._description = description;
	}

	/**
	 * @return Lista de los puntos de la ruta 
	 */
	public List<IGeoPoint> getWayPoints() {
		return Arrays.asList(_wayPoints);
	}

	/**
	 * @return Indica si la ruta es cerrada (Después del último punto va el primero otra vez)
	 */
	public boolean isClosed() {
		return _isClosed;
	}

	/**
	 * @param isClosed Indica si la ruta es cerrada (Después del último punto va el primero otra vez)
	 */
	public void setClosed(boolean isClosed) {
		this._isClosed = isClosed;
	}

	/**
	 * @return Clave de la ruta
	 */
	public String getKey() {
		return _key;
	}

	/**
	 * @param key Clave de la ruta
	 */
	public void setKey(String key) {
		this._key = key;
	}
	
	/**
	 * @return Puntos de guiado
	 */
	public GuidancePoint[] getGuidancePoints(){
		return _guidancePoints;
	}

	public void setWayPoints(IGeoPoint[] wayPoints) {
		this._wayPoints = wayPoints;
	}

	public void setGuidancePoints(GuidancePoint[] guidancePoints) {
		this._guidancePoints = guidancePoints;
	}

	public double getTotalDistance() {
		return _totalDistance;
	}

	public void setTotalDistance(double totalDistance) {
		this._totalDistance = totalDistance;
	}

	public double getTotalTime() {
		return _totalTime;
	}

	public void setTotalTime(double totalTime) {
		this._totalTime = totalTime;
	}
}
