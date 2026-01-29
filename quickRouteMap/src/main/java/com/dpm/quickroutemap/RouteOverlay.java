package com.dpm.quickroutemap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;

import com.dpm.quickroutemap.navigation.Route;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.List;

/**
 * Capa de ruta
 * 
 * @author David
 *
 */
public class RouteOverlay extends Overlay {

	private final Route _route;
	private final Paint _paint = new Paint();
	private final Paint _pointPaint = new Paint();
	private final Paint _buttonPaint = new Paint();
	private final Paint _iconPaint = new Paint();

	private boolean _isEditMode = false;
	private int _draggedPointIndex = -1;
	private int _longPressedPointIndex = -1;
	private final float _pointRadius = 20f;
	
	private final Handler _handler = new Handler(Looper.getMainLooper());
	private boolean _isMoving = false;
	private float _initialX, _initialY;
	private final int _longPressTimeout = 500;
	private Bitmap _deleteBitmap;
	private final Point _deleteIconScreenPos = new Point();
	private final int _deleteIconSize = 80;

	private final Runnable _longPressRunnable = new Runnable() {
		@Override
		public void run() {
			if (_draggedPointIndex != -1 && !_isMoving) {
				_longPressedPointIndex = _draggedPointIndex;
				_draggedPointIndex = -1; // Detener arrastre si se detecta pulsación larga
				if (_mapView != null) {
					_mapView.invalidate();
				}
			}
		}
	};
	private MapView _mapView;

	public RouteOverlay(Route route, int color, float width) {

		_paint.setColor(color);
		_paint.setStrokeWidth(width);
		_paint.setAntiAlias(true);
		_paint.setStyle(Paint.Style.STROKE);

		_pointPaint.setColor(color);
		_pointPaint.setAlpha(128);
		_pointPaint.setStyle(Paint.Style.FILL);
		_pointPaint.setAntiAlias(true);

		_buttonPaint.setAntiAlias(true);
		_buttonPaint.setShadowLayer(10, 0, 0, Color.BLACK);

		_iconPaint.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN));

		_route = route;
	}

	public void setEditMode(boolean editMode) {
		this._isEditMode = editMode;
		this._draggedPointIndex = -1;
		this._longPressedPointIndex = -1;
		_isMoving = false;
	}

	public boolean isEditMode() {
		return _isEditMode;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean isShadow) {
		this._mapView = mapView;
		if(!isShadow && (_route != null) && (_route.getWayPoints().size()>1)){
			
			List<IGeoPoint> wayPoints = _route.getWayPoints();
			
			Point screenPoint = new Point();			
			Path path = new Path();
			
			mapView.getProjection().toPixels(wayPoints.get(0), screenPoint);
			path.moveTo(screenPoint.x, screenPoint.y);

			if (_isEditMode) {
				canvas.drawCircle(screenPoint.x, screenPoint.y, _pointRadius, _pointPaint);
				if (_longPressedPointIndex == 0) {
					drawDeleteIcon(canvas, screenPoint);
				}
			}
			
			for(int i = 1; i < wayPoints.size(); i++){
				mapView.getProjection().toPixels(wayPoints.get(i), screenPoint);
				path.lineTo(screenPoint.x, screenPoint.y);
				if (_isEditMode) {
					canvas.drawCircle(screenPoint.x, screenPoint.y, _pointRadius, _pointPaint);
					if (_longPressedPointIndex == i) {
						drawDeleteIcon(canvas, screenPoint);
					}
				}
			}
			if(_route.isClosed()){
				mapView.getProjection().toPixels(wayPoints.get(0), screenPoint);
				path.lineTo(screenPoint.x, screenPoint.y);
			}
			
			canvas.drawPath(path, _paint);
		}		
	}

	private void drawDeleteIcon(Canvas canvas, Point pointPos) {
		if (_deleteBitmap == null && _mapView != null) {
			_deleteBitmap = BitmapFactory.decodeResource(_mapView.getResources(), android.R.drawable.ic_menu_delete);
			_deleteBitmap = Bitmap.createScaledBitmap(_deleteBitmap, _deleteIconSize, _deleteIconSize, true);
		}
		if (_deleteBitmap != null) {
			int offset = (int)_pointRadius + 20;
			int x = pointPos.x + offset;
			int y = pointPos.y - offset - _deleteIconSize;

			// Reposicionar si se sale por la derecha
			if (x + _deleteIconSize > _mapView.getWidth()) {
				x = pointPos.x - offset - _deleteIconSize;
			}
			// Reposicionar si se sale por arriba
			if (y < 0) {
				y = pointPos.y + offset;
			}
			// Reposicionar si se sale por la izquierda (caso extremo)
			if (x < 0) {
				x = offset;
			}
			// Reposicionar si se sale por abajo (caso extremo)
			if (y + _deleteIconSize > _mapView.getHeight()) {
				y = _mapView.getHeight() - offset - _deleteIconSize;
			}

			_deleteIconScreenPos.set(x, y);

			// Dibujar fondo del botón (blanco con sombra para contraste)
			float centerX = x + _deleteIconSize / 2f;
			float centerY = y + _deleteIconSize / 2f;
			_buttonPaint.setColor(Color.WHITE);
			_buttonPaint.setStyle(Paint.Style.FILL);
			canvas.drawCircle(centerX, centerY, _deleteIconSize / 2f + 5, _buttonPaint);

			// Dibujar el icono rojizo
			canvas.drawBitmap(_deleteBitmap, x, y, _iconPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		this._mapView = mapView;
		if (!_isEditMode) {
			return false;
		}

		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();

		Projection projection = mapView.getProjection();

		if (action == MotionEvent.ACTION_DOWN) {
			_isMoving = false;
			_initialX = x;
			_initialY = y;

			// Comprobar si se pulsa en el icono de borrar
			if (_longPressedPointIndex != -1) {
				if (x >= _deleteIconScreenPos.x - 10 && x <= _deleteIconScreenPos.x + _deleteIconSize + 10 &&
					y >= _deleteIconScreenPos.y - 10 && y <= _deleteIconScreenPos.y + _deleteIconSize + 10) {
					deleteWayPoint(_longPressedPointIndex);
					_longPressedPointIndex = -1;
					mapView.invalidate();
					return true;
				}
			}

			_longPressedPointIndex = -1; // Resetear si se pulsa en otro sitio

			List<IGeoPoint> wayPoints = _route.getWayPoints();
			Point screenPoint = new Point();
			for (int i = 0; i < wayPoints.size(); i++) {
				projection.toPixels(wayPoints.get(i), screenPoint);
				if (Math.hypot(screenPoint.x - x, screenPoint.y - y) < _pointRadius * 2) {
					_draggedPointIndex = i;
					_handler.postDelayed(_longPressRunnable, _longPressTimeout);
					return true;
				}
			}

			// Comprobar si se pulsa sobre un segmento
			Point p1 = new Point();
			Point p2 = new Point();
			Point projected = new Point();
			Point bestProjection = new Point();
			float bestDistance = Float.MAX_VALUE;
			int bestSegmentIndex = -1;

			for (int i = 0; i < wayPoints.size() - 1; i++) {
				projection.toPixels(wayPoints.get(i), p1);
				projection.toPixels(wayPoints.get(i + 1), p2);
				float dist = getDistanceToSegment(x, y, p1.x, p1.y, p2.x, p2.y, projected);
				if (dist < _pointRadius * 2 && dist < bestDistance) {
					bestDistance = dist;
					bestSegmentIndex = i;
					bestProjection.set(projected.x, projected.y);
				}
			}

			if (_route.isClosed() && wayPoints.size() > 1) {
				projection.toPixels(wayPoints.get(wayPoints.size() - 1), p1);
				projection.toPixels(wayPoints.get(0), p2);
				float dist = getDistanceToSegment(x, y, p1.x, p1.y, p2.x, p2.y, projected);
				if (dist < _pointRadius * 2 && dist < bestDistance) {
					bestDistance = dist;
					bestSegmentIndex = wayPoints.size() - 1;
					bestProjection.set(projected.x, projected.y);
				}
			}

			if (bestSegmentIndex != -1) {
				IGeoPoint newPoint = projection.fromPixels(bestProjection.x, bestProjection.y);
				insertWayPoint(bestSegmentIndex + 1, newPoint);
				_draggedPointIndex = bestSegmentIndex + 1;
				_handler.postDelayed(_longPressRunnable, _longPressTimeout);
				mapView.invalidate();
				return true;
			}
		} else if (action == MotionEvent.ACTION_MOVE) {
			if (Math.hypot(x - _initialX, y - _initialY) > 10) {
				_isMoving = true;
				_handler.removeCallbacks(_longPressRunnable);
			}

			if (_draggedPointIndex != -1) {
				IGeoPoint newGeoPoint = projection.fromPixels((int)x, (int)y);
				List<IGeoPoint> wayPoints = _route.getWayPoints();
				IGeoPoint[] pointsArray = new IGeoPoint[wayPoints.size()];
				wayPoints.toArray(pointsArray);
				pointsArray[_draggedPointIndex] = newGeoPoint;
				_route.setWayPoints(pointsArray);
				mapView.invalidate();
				return true;
			}
		} else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
			_handler.removeCallbacks(_longPressRunnable);
			_draggedPointIndex = -1;
			return true;
		}

		return super.onTouchEvent(event, mapView);
	}

	private void deleteWayPoint(int index) {
		List<IGeoPoint> wayPoints = new ArrayList<>(_route.getWayPoints());
		if (index >= 0 && index < wayPoints.size()) {
			wayPoints.remove(index);
			_route.setWayPoints(wayPoints.toArray(new IGeoPoint[0]));
		}
	}

	private void insertWayPoint(int index, IGeoPoint point) {
		List<IGeoPoint> wayPoints = new ArrayList<>(_route.getWayPoints());
		if (index >= 0 && index <= wayPoints.size()) {
			wayPoints.add(index, point);
			_route.setWayPoints(wayPoints.toArray(new IGeoPoint[0]));
		}
	}

	private float getDistanceToSegment(float px, float py, float x1, float y1, float x2, float y2, Point outProjected) {
		float dx = x2 - x1;
		float dy = y2 - y1;

		if (dx == 0 && dy == 0) {
			outProjected.set((int) x1, (int) y1);
			return (float) Math.hypot(px - x1, py - y1);
		}

		float t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);

		if (t < 0) {
			outProjected.set((int) x1, (int) y1);
		} else if (t > 1) {
			outProjected.set((int) x2, (int) y2);
		} else {
			outProjected.set((int) (x1 + t * dx), (int) (y1 + t * dy));
		}

		return (float) Math.hypot(px - outProjected.x, py - outProjected.y);
	}

}
