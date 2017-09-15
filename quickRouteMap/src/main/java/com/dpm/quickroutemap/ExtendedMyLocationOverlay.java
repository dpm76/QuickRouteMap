package com.dpm.quickroutemap;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;

import android.content.Context;
import android.location.Location;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Extiende las funcionalidades de la capa de posición del usuario.
 * 
 * @author David 
 */
@Deprecated
public class ExtendedMyLocationOverlay extends MyLocationOverlay {

	//private final static String LOG_TAG = ExtendedMyLocationOverlay.class.getSimpleName(); 
	
	private static GeoPoint _lastUserLocation = null;
	
	private boolean _drawCompass = false;
	private final Display _display;
	
	public ExtendedMyLocationOverlay(Context context, MapView mapView) {		
		super(context, mapView);		
		_display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	}
	
	@Override
	public void onLocationChanged(Location location){
		super.onLocationChanged(location);
		_lastUserLocation = this.getMyLocation();
	}
	
	@Override
	public GeoPoint getMyLocation(){
		GeoPoint myLocation = super.getMyLocation();		
		return (myLocation != null)?myLocation:_lastUserLocation;
	}
	
	/*
	@Override
	protected void drawMyLocation(
			final Canvas canvas,
	        final MapView mapView,
	        final Location lastFix,
	        final GeoPoint myLocation) {
	
		Point mapCoords = new Point();
        final Projection pj = mapView.getProjection();
        pj.toMapPixels(myLocation, mapCoords);

        if (mDrawAccuracyEnabled) {
                final float radius = pj.metersToEquatorPixels(lastFix.getAccuracy());

                mCirclePaint.setAlpha(50);
                mCirclePaint.setStyle(Style.FILL);
                canvas.drawCircle(mapCoords.x, mapCoords.y, radius, mCirclePaint);

                mCirclePaint.setAlpha(150);
                mCirclePaint.setStyle(Style.STROKE);
                canvas.drawCircle(mapCoords.x, mapCoords.y, radius, mCirclePaint);
        }

        Matrix matrix = new Matrix();
        float[] matrixValues = new float[9];
        canvas.getMatrix(matrix);
        matrix.getValues(matrixValues);

        Matrix directionRotater = new Matrix();
        
        if (isCompassEnabled() && hasOrientation()){ //(lastFix.hasBearing()) {
                /*
                 * Rotate the direction-Arrow according to the bearing we are driving. And draw it
                 * to the canvas.
                 *//*
                directionRotater.setRotate(
                                getTopScreenOrientation(), //lastFix.getBearing(),
                                DIRECTION_ARROW_CENTER_X, DIRECTION_ARROW_CENTER_Y);

                directionRotater.postTranslate(-DIRECTION_ARROW_CENTER_X, -DIRECTION_ARROW_CENTER_Y);
                directionRotater.postScale(
                                1 / matrixValues[Matrix.MSCALE_X],
                                1 / matrixValues[Matrix.MSCALE_Y]);
                directionRotater.postTranslate(mapCoords.x, mapCoords.y);
                canvas.drawBitmap(DIRECTION_ARROW, directionRotater, mPaint);
        } else {
                directionRotater.setTranslate(-PERSON_HOTSPOT.x, -PERSON_HOTSPOT.y);
                directionRotater.postScale(
                                1 / matrixValues[Matrix.MSCALE_X],
                                1 / matrixValues[Matrix.MSCALE_Y]);
                directionRotater.postTranslate(mapCoords.x, mapCoords.y);
                canvas.drawBitmap(PERSON_ICON, directionRotater, mPaint);
        }                
    }
	*/
	
	/*
	@Override
	protected void drawCompass(Canvas canvas, float bearing){
		if(_drawCompass){
			super.drawCompass(canvas, bearing);
		}
	}
	*/

	/**
	 * @return Indica si se va a pintar la brújula
	 */
	public boolean drawCompass() {
		return _drawCompass;
	}

	/**
	 * @param drawCompass Indica si se va a pintar la brújula
	 */
	public void setDrawCompass(boolean drawCompass) {
		this._drawCompass = drawCompass;
	}

	private boolean hasOrientation(){
		return !Float.isNaN(getOrientation());
	}
	
	/**
	 * Calcula la orientación teniendo en cuenta la orientación de la pantalla.
	 * 
	 * @return Orientación del lado superior de la pantalla
	 */
	private float getTopScreenOrientation(){
		float offset;
		switch(_display.getOrientation()){
			case Surface.ROTATION_90:
				offset = 90f;
				break;
			case Surface.ROTATION_180:
				offset = 180f;
				break;
			case Surface.ROTATION_270:
				offset = 270f;
				break;				
			default:
				offset = 0f;
				break;
		}
		return getOrientation() + offset;
	}

}
