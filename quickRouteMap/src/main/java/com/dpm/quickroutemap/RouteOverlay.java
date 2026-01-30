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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;

import com.dpm.quickroutemap.navigation.GuidancePoint;
import com.dpm.quickroutemap.navigation.Route;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
	private final Paint _guidancePointPaint = new Paint();
	private final Paint _guidanceRadiusPaint = new Paint();
	private final Paint _warningPointPaint = new Paint();
	private final Paint _warningRadiusPaint = new Paint();
	private final Paint _editIconPaint = new Paint();

	private boolean _isEditMode = false;
	private int _draggedPointIndex = -1;
	private int _longPressedPointIndex = -1;
	private int _draggedGuidancePointIndex = -1;
	private int _longPressedGuidancePointIndex = -1;
	private int _resizingGuidancePointIndex = -1;
	private float _initialRadius;
	private float _initialTouchDist;
	private boolean _justDismissedDeleteIcon = false;
	
	private final float _pointRadius = 20f;
	
	private final Handler _handler = new Handler(Looper.getMainLooper());
	private boolean _isMoving = false;
	private float _initialX, _initialY;
	private final int _longPressTimeout = 500;
	private Bitmap _deleteBitmap;
	private Bitmap _editBitmap;
	private Bitmap _warningBitmap;
	private final Point _deleteIconScreenPos = new Point();
	private final Point _editIconScreenPos = new Point();
	private final int _deleteIconSize = 80;
	private final int _editIconSize = 80;
	private final int _warningIconSize = 30;

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
			if (_draggedGuidancePointIndex != -1 && !_isMoving) {
				_longPressedGuidancePointIndex = _draggedGuidancePointIndex;
				_draggedGuidancePointIndex = -1;
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


		_guidancePointPaint.setColor(Color.BLUE);
		_guidancePointPaint.setAlpha(200);
		_guidancePointPaint.setStyle(Paint.Style.FILL);
		_guidancePointPaint.setAntiAlias(true);

		_guidanceRadiusPaint.setColor(Color.BLUE);
		_guidanceRadiusPaint.setAlpha(50);
		_guidanceRadiusPaint.setStyle(Paint.Style.FILL);
		_guidanceRadiusPaint.setAntiAlias(true);

		_warningPointPaint.setColor(Color.RED);
		_warningPointPaint.setAlpha(200);
		_warningPointPaint.setStyle(Paint.Style.FILL);
		_warningPointPaint.setAntiAlias(true);

		_warningRadiusPaint.setColor(Color.RED);
		_warningRadiusPaint.setAlpha(50);
		_warningRadiusPaint.setStyle(Paint.Style.FILL);
		_warningRadiusPaint.setAntiAlias(true);

		_editIconPaint.setColorFilter(new PorterDuffColorFilter(Color.rgb(0, 120, 255), PorterDuff.Mode.SRC_IN));

		_route = route;
	}

	public void setEditMode(boolean editMode) {
		this._isEditMode = editMode;
		this._draggedPointIndex = -1;
		this._longPressedPointIndex = -1;
		this._draggedGuidancePointIndex = -1;
		this._longPressedGuidancePointIndex = -1;
		this._resizingGuidancePointIndex = -1;
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

			if (_isEditMode && _route.getGuidancePoints() != null) {
				GuidancePoint[] guidancePoints = _route.getGuidancePoints();
				for (int i = 0; i < guidancePoints.length; i++) {
					GuidancePoint gp = guidancePoints[i];
					if (gp.getPoint() == null) continue;

					mapView.getProjection().toPixels(gp.getPoint(), screenPoint);

					// Draw radius
					float radiusInMeters = gp.getRadius();
					float radiusInPixels = (float) (mapView.getProjection().metersToEquatorPixels(radiusInMeters) * (1 / Math.cos(Math.toRadians(gp.getLatitude()))));
					
					boolean isEmpty = (gp.getNarrative() == null || gp.getNarrative().trim().isEmpty());
					Paint pointPaint = isEmpty ? _warningPointPaint : _guidancePointPaint;
					Paint radiusPaint = isEmpty ? _warningRadiusPaint : _guidanceRadiusPaint;

					canvas.drawCircle(screenPoint.x, screenPoint.y, radiusInPixels, radiusPaint);

					// Draw point
					canvas.drawCircle(screenPoint.x, screenPoint.y, _pointRadius, pointPaint);
					
					if (isEmpty) {
						drawWarningIcon(canvas, screenPoint);
					}

					if (_longPressedGuidancePointIndex == i) {
						drawGuidancePointIcons(canvas, screenPoint);
					}
				}
			}
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
			drawIconButton(canvas, _deleteBitmap, x, y, _iconPaint);
		}
	}

	private void drawGuidancePointIcons(Canvas canvas, Point pointPos) {
		if (_deleteBitmap == null && _mapView != null) {
			_deleteBitmap = BitmapFactory.decodeResource(_mapView.getResources(), android.R.drawable.ic_menu_delete);
			_deleteBitmap = Bitmap.createScaledBitmap(_deleteBitmap, _deleteIconSize, _deleteIconSize, true);
		}
		if (_editBitmap == null && _mapView != null) {
			_editBitmap = BitmapFactory.decodeResource(_mapView.getResources(), android.R.drawable.ic_menu_edit);
			_editBitmap = Bitmap.createScaledBitmap(_editBitmap, _editIconSize, _editIconSize, true);
		}
		
		if (_deleteBitmap != null && _editBitmap != null) {
			int offset = (int)_pointRadius + 20;
			int xDelete = pointPos.x + offset;
			int yDelete = pointPos.y - offset - _deleteIconSize;

			// Reposicionar si se sale por la derecha
			if (xDelete + _deleteIconSize + _editIconSize + 20 > _mapView.getWidth()) {
				xDelete = pointPos.x - offset - _deleteIconSize - _editIconSize - 20;
			}
			// Reposicionar si se sale por arriba
			if (yDelete < 0) {
				yDelete = pointPos.y + offset;
			}
			// Reposicionar si se sale por la izquierda (caso extremo)
			if (xDelete < 0) {
				xDelete = offset;
			}

			_deleteIconScreenPos.set(xDelete, yDelete);
			drawIconButton(canvas, _deleteBitmap, xDelete, yDelete, _iconPaint);

			int xEdit = xDelete + _deleteIconSize + 20;
			int yEdit = yDelete;
			
			_editIconScreenPos.set(xEdit, yEdit);
			drawIconButton(canvas, _editBitmap, xEdit, yEdit, _editIconPaint);
		}
	}

	private void drawIconButton(Canvas canvas, Bitmap bitmap, int x, int y, Paint iconPaint) {
		// Dibujar fondo del botón (blanco con sombra para contraste)
		float centerX = x + bitmap.getWidth() / 2f;
		float centerY = y + bitmap.getHeight() / 2f;
		_buttonPaint.setColor(Color.WHITE);
		_buttonPaint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(centerX, centerY, bitmap.getWidth() / 2f + 5, _buttonPaint);

		// Dibujar el icono
		canvas.drawBitmap(bitmap, x, y, iconPaint);
	}

	private void drawWarningIcon(Canvas canvas, Point pointPos) {
		if (_warningBitmap == null && _mapView != null) {
			_warningBitmap = BitmapFactory.decodeResource(_mapView.getResources(), android.R.drawable.stat_sys_warning);
			if (_warningBitmap == null) {
				_warningBitmap = BitmapFactory.decodeResource(_mapView.getResources(), android.R.drawable.ic_dialog_alert);
			}
			if (_warningBitmap != null) {
				_warningBitmap = Bitmap.createScaledBitmap(_warningBitmap, _warningIconSize, _warningIconSize, true);
			}
		}
		if (_warningBitmap != null) {
			int x = pointPos.x - _warningIconSize / 2;
			int y = pointPos.y - _warningIconSize / 2;
			canvas.drawBitmap(_warningBitmap, x, y, null);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		this._mapView = mapView;
		if (!_isEditMode) {
			return false;
		}

		if (event.getPointerCount() > 1) {
			_isMoving = false;
			_draggedPointIndex = -1;
			_draggedGuidancePointIndex = -1;
			_resizingGuidancePointIndex = -1;
			_handler.removeCallbacks(_longPressRunnable);
			return super.onTouchEvent(event, mapView);
		}

		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();

		Projection projection = mapView.getProjection();

		if (action == MotionEvent.ACTION_DOWN) {
			_isMoving = false;
			_initialX = x;
			_initialY = y;

			// Comprobar si se pulsa en el icono de borrar (Guidance Point)
			if (_longPressedGuidancePointIndex != -1) {
				if (x >= _deleteIconScreenPos.x - 10 && x <= _deleteIconScreenPos.x + _deleteIconSize + 10 &&
					y >= _deleteIconScreenPos.y - 10 && y <= _deleteIconScreenPos.y + _deleteIconSize + 10) {
					deleteGuidancePoint(_longPressedGuidancePointIndex);
					_longPressedGuidancePointIndex = -1;
					mapView.invalidate();
					return true;
				}
				
				if (x >= _editIconScreenPos.x - 10 && x <= _editIconScreenPos.x + _editIconSize + 10 &&
					y >= _editIconScreenPos.y - 10 && y <= _editIconScreenPos.y + _editIconSize + 10) {
					editGuidancePoint(_longPressedGuidancePointIndex);
					_longPressedGuidancePointIndex = -1;
					mapView.invalidate();
					return true;
				}
			}

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

			_justDismissedDeleteIcon = (_longPressedPointIndex != -1 || _longPressedGuidancePointIndex != -1);

			_longPressedPointIndex = -1; // Resetear si se pulsa en otro sitio
			_longPressedGuidancePointIndex = -1;

			// Comprobar si se pulsa sobre un punto de guiado
			if (_route.getGuidancePoints() != null) {
				GuidancePoint[] guidancePoints = _route.getGuidancePoints();
				Point screenPoint = new Point();
				for (int i = 0; i < guidancePoints.length; i++) {
					if (guidancePoints[i].getPoint() == null) continue;
					projection.toPixels(guidancePoints[i].getPoint(), screenPoint);
					if (Math.hypot(screenPoint.x - x, screenPoint.y - y) < _pointRadius * 2) {
						_draggedGuidancePointIndex = i;
						_handler.postDelayed(_longPressRunnable, _longPressTimeout);
						return true;
					}
				}
			}

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

			// Comprobar si se pulsa sobre el área de un punto de guiado (Resize)
			if (_route.getGuidancePoints() != null) {
				GuidancePoint[] guidancePoints = _route.getGuidancePoints();
				for (int i = 0; i < guidancePoints.length; i++) {
					if (guidancePoints[i].getPoint() == null) continue;
					mapView.getProjection().toPixels(guidancePoints[i].getPoint(), screenPoint);
					
					float radiusInMeters = guidancePoints[i].getRadius();
					float radiusInPixels = (float) (mapView.getProjection().metersToEquatorPixels(radiusInMeters) * (1 / Math.cos(Math.toRadians(guidancePoints[i].getLatitude()))));
					
					if (Math.hypot(screenPoint.x - x, screenPoint.y - y) <= radiusInPixels) {
						_resizingGuidancePointIndex = i;
						_initialRadius = radiusInMeters;
						IGeoPoint touchGeo = projection.fromPixels((int)x, (int)y);
						_initialTouchDist = (float) new GeoPoint(guidancePoints[i].getLatitude(), guidancePoints[i].getLongitude()).distanceToAsDouble(touchGeo);
						return true;
					}
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

			if (_draggedGuidancePointIndex != -1) {
				IGeoPoint newGeoPoint = projection.fromPixels((int)x, (int)y);
				GuidancePoint[] guidancePoints = _route.getGuidancePoints();
				if (guidancePoints != null && _draggedGuidancePointIndex >= 0 && _draggedGuidancePointIndex < guidancePoints.length) {
					GuidancePoint oldGp = guidancePoints[_draggedGuidancePointIndex];
					guidancePoints[_draggedGuidancePointIndex] = new GuidancePoint(oldGp.getKey(), newGeoPoint.getLatitude(), newGeoPoint.getLongitude(), oldGp.getNarrative(), (int)oldGp.getRadius());
					_route.setGuidancePoints(guidancePoints);
					mapView.invalidate();
					return true;
				}
			}

			if (_resizingGuidancePointIndex != -1) {
				GuidancePoint[] guidancePoints = _route.getGuidancePoints();
				if (guidancePoints != null && _resizingGuidancePointIndex >= 0 && _resizingGuidancePointIndex < guidancePoints.length) {
					GuidancePoint gp = guidancePoints[_resizingGuidancePointIndex];
					IGeoPoint touchGeo = projection.fromPixels((int)x, (int)y);
					double currentDist = new GeoPoint(gp.getLatitude(), gp.getLongitude()).distanceToAsDouble(touchGeo);
					
					float newRadius = _initialRadius + (float)(currentDist - _initialTouchDist);
					if (newRadius < 10) newRadius = 10;
					
					guidancePoints[_resizingGuidancePointIndex] = new GuidancePoint(gp.getKey(), gp.getLatitude(), gp.getLongitude(), gp.getNarrative(), (int)newRadius);
					_route.setGuidancePoints(guidancePoints);
					mapView.invalidate();
					return true;
				}
			}
		} else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
			_handler.removeCallbacks(_longPressRunnable);
			boolean wasEditing = _draggedPointIndex != -1 || _draggedGuidancePointIndex != -1 || _resizingGuidancePointIndex != -1;
			
			_draggedPointIndex = -1;
			_draggedGuidancePointIndex = -1;
			_resizingGuidancePointIndex = -1;
			return wasEditing;
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
	
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
		if (!_isEditMode) {
			return false;
		}

		if (_justDismissedDeleteIcon) {
			_justDismissedDeleteIcon = false;
			return true;
		}
		
		IGeoPoint p = mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY());
		
		GuidancePoint newGp = new GuidancePoint(UUID.randomUUID().toString(), p.getLatitude(), p.getLongitude(), "", 50);
		
		GuidancePoint[] currentPoints = _route.getGuidancePoints();
		List<GuidancePoint> pointsList;
		if (currentPoints == null) {
			pointsList = new ArrayList<>();
		} else {
			pointsList = new ArrayList<>(Arrays.asList(currentPoints));
		}
		pointsList.add(newGp);
		_route.setGuidancePoints(pointsList.toArray(new GuidancePoint[0]));
		
		mapView.invalidate();
		return true;
	}
	
	private void deleteGuidancePoint(final int index) {
		AlertDialog.Builder builder = new AlertDialog.Builder(_mapView.getContext());
		builder.setTitle("Confirmar borrado");
		builder.setMessage("¿Estás seguro de que quieres borrar este punto de guiado?");
		builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				performDeleteGuidancePoint(index);
				_mapView.invalidate();
			}
		});
		builder.setNegativeButton("No", null);
		builder.show();
	}
	
	private void editGuidancePoint(final int index) {
		if (_route.getGuidancePoints() == null || index < 0 || index >= _route.getGuidancePoints().length) return;
		final GuidancePoint gp = _route.getGuidancePoints()[index];
		
		AlertDialog.Builder builder = new AlertDialog.Builder(_mapView.getContext());
		builder.setTitle("Editar Punto de Guiado");
		
		final EditText input = new EditText(_mapView.getContext());
		input.setText(gp.getNarrative());
		builder.setView(input);
		
		builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newNarrative = input.getText().toString();
				GuidancePoint[] guidancePoints = _route.getGuidancePoints();
				guidancePoints[index] = new GuidancePoint(gp.getKey(), gp.getLatitude(), gp.getLongitude(), newNarrative, (int)gp.getRadius());
				_route.setGuidancePoints(guidancePoints);
				_mapView.invalidate();
			}
		});
		builder.setNegativeButton("Cancelar", null);
		builder.show();
	}

	private void performDeleteGuidancePoint(int index) {
		if (_route.getGuidancePoints() == null) return;
		List<GuidancePoint> guidancePoints = new ArrayList<>(Arrays.asList(_route.getGuidancePoints()));
		if (index >= 0 && index < guidancePoints.size()) {
			guidancePoints.remove(index);
			_route.setGuidancePoints(guidancePoints.toArray(new GuidancePoint[0]));
		}
	}

}
