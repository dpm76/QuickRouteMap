package com.dpm.quickroutemap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import com.dpm.quickroutemap.navigation.Route;

import android.text.TextUtils;

@Deprecated
public class RouteFactory {

	//Divide la cantidad de puntos de ruta por este valor. Siempre se añade el último punto.
	private final static int POINTS_DIVIDER = 8; 
	
	public Route create(InputStream in) throws IOException{
		Route route = new Route();
		route.getWayPoints().addAll(parse(in));	
		
		route.setKey("RUTA");
		route.setName("Ruta");
		route.setDescription("Ruta.");
		route.setClosed(false);
		
		return route;
	}
	
	/**
	 * Procesa un stream para generar las coordenadas.
	 * El formato esperado es [lat, long, lat, long, ...]
	 *  
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private List<IGeoPoint> parse(InputStream in) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in));		
		ArrayList<IGeoPoint> points = new ArrayList<IGeoPoint>();
		double longitude = 0d;
		double latitude = 0d;
		boolean isLatitude = true;
		int coordsCounter = 0;
		
		String line;
		while( (line = br.readLine()) != null){
			if(!TextUtils.isEmpty(line)){
				String[] strCoords = line.split(",");
				for (String strCoord:strCoords){
					if(!TextUtils.isEmpty(strCoord)){						
						double component = Double.parseDouble(strCoord.trim()); 
						if(isLatitude){
							latitude = component;
						}else{
							longitude = component;						
							IGeoPoint point = new GeoPoint(latitude, longitude);
							if(coordsCounter%POINTS_DIVIDER == 0){
								points.add(point);
							}
							coordsCounter++;
						}
						isLatitude = !isLatitude;
					}
				}
			}
		}

		//El último punto debe añadirse siempre. Si el último punto no se añadió, se añade ahora. 
		if((coordsCounter-1 )%POINTS_DIVIDER != 0){
			IGeoPoint point = new GeoPoint(latitude, longitude);
			points.add(point);
		}
		
        return points;
    }
	
}
