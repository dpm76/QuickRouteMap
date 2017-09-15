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

	private String _key;
	private String _name;
	private String _description;
	private boolean _isClosed;
	//private final List<IGeoPoint> _wayPointsList = new ArrayList<IGeoPoint>();
	private IGeoPoint[] _wayPoints;
	private GuidancePoint[] _guidancePoints;
	
	public Route(){		
	}
	
	public Route(String key, String name, String description, boolean isClosed, IGeoPoint[] wayPoints, GuidancePoint[] guidancePoints){
		_key = key;
		_name = name;
		_description = description;
		_isClosed = isClosed;
		_wayPoints = wayPoints;
		_guidancePoints = guidancePoints;
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
		//return _wayPointsList;
		
		ArrayList<IGeoPoint> wayPoints = new ArrayList<IGeoPoint>();
		wayPoints.addAll(Arrays.asList(_wayPoints));
		
		return wayPoints;
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
	
}
