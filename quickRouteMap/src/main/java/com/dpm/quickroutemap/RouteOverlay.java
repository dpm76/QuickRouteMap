package com.dpm.quickroutemap;

import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import com.dpm.quickroutemap.navigation.Route;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

/**
 * Capa de ruta
 * 
 * @author David
 *
 */
public class RouteOverlay extends Overlay {

	private final Route _route;
	private Paint _paint = new Paint();
	
	public RouteOverlay(Context context, Route route, int color, float width) {
		super(context);
		
		_paint.setColor(color);
		_paint.setStrokeWidth(width);
		_paint.setAntiAlias(true);
		_paint.setStyle(Paint.Style.STROKE);
		_route = route;
	}
	
	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean isShadow) {
		if(!isShadow && (_route != null) && (_route.getWayPoints().size()>1)){
			
			List<IGeoPoint> wayPoints = _route.getWayPoints();
			
			Point screenPoint = new Point();			
			Path path = new Path();
			
			mapView.getProjection().toMapPixels(wayPoints.get(0), screenPoint);
			path.moveTo(screenPoint.x, screenPoint.y);
			
			for(int i = 1; i < wayPoints.size(); i++){
				mapView.getProjection().toMapPixels(wayPoints.get(i), screenPoint);
				path.lineTo(screenPoint.x, screenPoint.y);
			}
			if(_route.isClosed()){
				mapView.getProjection().toMapPixels(wayPoints.get(0), screenPoint);
				path.lineTo(screenPoint.x, screenPoint.y);
			}
			
			canvas.drawPath(path, _paint);
		}		
	}

}
