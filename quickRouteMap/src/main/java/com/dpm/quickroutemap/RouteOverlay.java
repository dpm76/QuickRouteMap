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
import android.graphics.RectF;

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
import java.util.Locale;
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
	private int _pressedButton = -1;
	private boolean _gestureCaptured = false;
	private static final int PRESS_NONE = -1;
	private static final int PRESS_DELETE_WAYPOINT = 0;
	private static final int PRESS_DELETE_GUIDANCE = 1;
	private static final int PRESS_EDIT_GUIDANCE = 2;
	private static final int PRESS_CONTEXT_GUIDANCE = 3;
	private static final int PRESS_CONTEXT_GEOMETRY = 4;
	private static final int PRESS_CREATE_GUIDANCE_TO_WAYPOINT = 5;
	private int _resizingGuidancePointIndex = -1;
	private float _initialRadius;
	private float _initialTouchDist;
	private boolean _justDismissedDeleteIcon = false;

	private final float _pointRadius = 20f;

	private final Handler _handler = new Handler(Looper.getMainLooper());
	private boolean _isMoving = false;
	private float _initialX, _initialY;
	private final int _longPressTimeout = 500;
	private final int _motionThreshold = 25;
	private Bitmap _deleteBitmap;
	private Bitmap _editBitmap;
	private Bitmap _addBitmap;
	private Bitmap _warningBitmap;
	private final Point _deleteIconScreenPos = new Point();
	private final Point _editIconScreenPos = new Point();
	private final int _deleteIconSize = 80;
	private final int _editIconSize = 80;
	private final int _warningIconSize = 30;

	// Variables para el menú contextual
	private boolean _showContextButtons = false;
	private IGeoPoint _contextMenuGeoPosition = null;
	private final Point _contextMenuScreenPos = new Point();
	private final int _contextButtonSize = 100;
	private final int _contextButtonSpacing = 40;
	private final Point _guidanceButtonPos = new Point();
	private final Point _geometryButtonPos = new Point();
	private final Paint _contextButtonPaint = new Paint();
	private final Paint _contextIconPaint = new Paint();
	private final Paint _radiusLabelPaint = new Paint();
	private final Paint _radiusLabelBackgroundPaint = new Paint();

	private float _currentX, _currentY;
	private final float _snapInterval = 50f;
	private final float _snapThreshold = 15f;
	private final float _minGuidanceRadius = 10f;
	private final float _radiusLabelOffsetY = 100f;
	private final float _radiusLabelTextSize = 35f;
	private final float _radiusLabelPaddingH = 15f;
	private final float _radiusLabelPaddingV = 10f;
	private final int _radiusLabelBgAlpha = 160;
	private final float _radiusLabelRoundedCorner = 10f;
	private final int _defaultGuidanceRadius = 200;

	private final Runnable _longPressRunnable = new Runnable() {
		@Override
		public void run() {
			if (_draggedPointIndex != -1 && !_isMoving) {
				_longPressedPointIndex = _draggedPointIndex;
				_draggedPointIndex = -1; // Detener
											// arrastre
											// si
											// se
											// detecta
											// pulsación
											// larga
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

		_contextButtonPaint.setColor(Color.WHITE);
		_contextButtonPaint.setStyle(Paint.Style.FILL);
		_contextButtonPaint.setShadowLayer(10, 0, 0, Color.BLACK);
		_contextButtonPaint.setAntiAlias(true);

		_contextIconPaint.setColor(Color.BLACK);
		_contextIconPaint.setStyle(Paint.Style.STROKE);
		_contextIconPaint.setStrokeWidth(5);
		_contextIconPaint.setAntiAlias(true);
		_contextIconPaint.setStrokeCap(Paint.Cap.ROUND);
		_contextIconPaint.setStrokeJoin(Paint.Join.ROUND);

		_radiusLabelPaint.setColor(Color.WHITE);
		_radiusLabelPaint.setTextSize(_radiusLabelTextSize);
		_radiusLabelPaint.setAntiAlias(true);
		_radiusLabelPaint.setTextAlign(Paint.Align.CENTER);
		_radiusLabelPaint.setFakeBoldText(true);

		_radiusLabelBackgroundPaint.setColor(Color.BLACK);
		_radiusLabelBackgroundPaint.setAlpha(_radiusLabelBgAlpha);
		_radiusLabelBackgroundPaint.setStyle(Paint.Style.FILL);
		_radiusLabelBackgroundPaint.setAntiAlias(true);

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
		if (!isShadow && (_route != null)) {

			List<IGeoPoint> wayPoints = _route.getWayPoints();
			Point screenPoint = new Point();

			if (!wayPoints.isEmpty()) {
				Path path = new Path();

				mapView.getProjection().toPixels(wayPoints.get(0), screenPoint);
				path.moveTo(screenPoint.x, screenPoint.y);

				if (_isEditMode) {
					canvas.drawCircle(screenPoint.x, screenPoint.y, _pointRadius, _pointPaint);
				}

				for (int i = 1; i < wayPoints.size(); i++) {
					mapView.getProjection().toPixels(wayPoints.get(i), screenPoint);
					path.lineTo(screenPoint.x, screenPoint.y);
					if (_isEditMode) {
						canvas.drawCircle(screenPoint.x, screenPoint.y, _pointRadius, _pointPaint);
					}
				}
				if (_route.isClosed() && wayPoints.size() > 1) {
					mapView.getProjection().toPixels(wayPoints.get(0), screenPoint);
					path.lineTo(screenPoint.x, screenPoint.y);
				}

				canvas.drawPath(path, _paint);
			}

			if (_isEditMode && _route.getGuidancePoints() != null) {
				GuidancePoint[] guidancePoints = _route.getGuidancePoints();
				for (int i = 0; i < guidancePoints.length; i++) {
					GuidancePoint gp = guidancePoints[i];
					if (gp.getPoint() == null)
						continue;

					mapView.getProjection().toPixels(gp.getPoint(), screenPoint);

					// Draw radius
					float radiusInMeters = gp.getRadius();
					float radiusInPixels = (float) (mapView.getProjection().metersToEquatorPixels(radiusInMeters)
							* (1 / Math.cos(Math.toRadians(gp.getLatitude()))));

					boolean isEmpty = (gp.getNarrative() == null || gp.getNarrative().trim().isEmpty());
					Paint pointPaint = isEmpty ? _warningPointPaint : _guidancePointPaint;
					Paint radiusPaint = isEmpty ? _warningRadiusPaint : _guidanceRadiusPaint;

					canvas.drawCircle(screenPoint.x, screenPoint.y, radiusInPixels, radiusPaint);

					// Draw point
					canvas.drawCircle(screenPoint.x, screenPoint.y, _pointRadius, pointPaint);

					if (isEmpty) {
						drawWarningIcon(canvas, screenPoint);
					}
				}
			}

			// --- DRAW UI OVERLAYS (Must be after all points/path to be on top) ---
			if (_isEditMode) {
				// Draw Waypoint Icons
				if (_longPressedPointIndex != -1 && _longPressedPointIndex < wayPoints.size()) {
					mapView.getProjection().toPixels(wayPoints.get(_longPressedPointIndex), screenPoint);
					drawWayPointIcons(canvas, screenPoint);
				}

				// Draw Guidance Point Icons / Labels
				if (_route.getGuidancePoints() != null) {
					GuidancePoint[] guidancePoints = _route.getGuidancePoints();

					// Icons for long pressed guidance point
					if (_longPressedGuidancePointIndex != -1
							&& _longPressedGuidancePointIndex < guidancePoints.length) {
						GuidancePoint gp = guidancePoints[_longPressedGuidancePointIndex];
						if (gp.getPoint() != null) {
							mapView.getProjection().toPixels(gp.getPoint(), screenPoint);
							drawGuidancePointIcons(canvas, screenPoint);
						}
					}

					// Radius label for resizing guidance point
					if (_resizingGuidancePointIndex != -1 && _resizingGuidancePointIndex < guidancePoints.length) {
						GuidancePoint gp = guidancePoints[_resizingGuidancePointIndex];
						String label = String.format(Locale.getDefault(), "%.0f m", gp.getRadius());
						float textWidth = _radiusLabelPaint.measureText(label);
						Paint.FontMetrics fm = _radiusLabelPaint.getFontMetrics();
						float textHeight = fm.descent - fm.ascent;

						float boxX = _currentX;
						float boxY = _currentY - _radiusLabelOffsetY;

						RectF bgRect = new RectF(
								boxX - textWidth / 2 - _radiusLabelPaddingH,
								boxY - textHeight / 2 - _radiusLabelPaddingV,
								boxX + textWidth / 2 + _radiusLabelPaddingH,
								boxY + textHeight / 2 + _radiusLabelPaddingV);

						canvas.drawRoundRect(bgRect, _radiusLabelRoundedCorner, _radiusLabelRoundedCorner,
								_radiusLabelBackgroundPaint);
						canvas.drawText(label, boxX, boxY - fm.ascent / 2 - fm.descent / 2, _radiusLabelPaint);
					}
				}
			}
		}

		if (_showContextButtons && _contextMenuGeoPosition != null) {
			drawContextButtons(canvas, mapView);
		}
	}

	private void drawWayPointIcons(Canvas canvas, Point pointPos) {
		if (_deleteBitmap == null && _mapView != null) {
			_deleteBitmap = BitmapFactory.decodeResource(_mapView.getResources(), android.R.drawable.ic_menu_delete);
			_deleteBitmap = Bitmap.createScaledBitmap(_deleteBitmap, _deleteIconSize, _deleteIconSize, true);
		}
		if (_addBitmap == null && _mapView != null) {
			_addBitmap = BitmapFactory.decodeResource(_mapView.getResources(), android.R.drawable.ic_menu_add);
			_addBitmap = Bitmap.createScaledBitmap(_addBitmap, _editIconSize, _editIconSize, true);
		}

		if (_deleteBitmap != null && _addBitmap != null) {
			int offset = (int) _pointRadius + 20;
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
			// Reposicionar si se sale por abajo (caso extremo)
			if (yDelete + _deleteIconSize > _mapView.getHeight()) {
				yDelete = _mapView.getHeight() - offset - _deleteIconSize;
			}

			_deleteIconScreenPos.set(xDelete, yDelete);
			drawIconButton(canvas, _deleteBitmap, xDelete, yDelete, _iconPaint);

			int xAdd = xDelete + _deleteIconSize + 20;
			int yAdd = yDelete;

			_editIconScreenPos.set(xAdd, yAdd);
			drawIconButton(canvas, _addBitmap, xAdd, yAdd, _editIconPaint);
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
			int offset = (int) _pointRadius + 20;
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
				_warningBitmap = BitmapFactory.decodeResource(_mapView.getResources(),
						android.R.drawable.ic_dialog_alert);
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

	private boolean isInsideDeleteIcon(float x, float y) {
		return x >= _deleteIconScreenPos.x - 40 && x <= _deleteIconScreenPos.x + _deleteIconSize + 40 &&
				y >= _deleteIconScreenPos.y - 40 && y <= _deleteIconScreenPos.y + _deleteIconSize + 40;
	}

	private boolean isInsideEditIcon(float x, float y) {
		return x >= _editIconScreenPos.x - 40 && x <= _editIconScreenPos.x + _editIconSize + 40 &&
				y >= _editIconScreenPos.y - 40 && y <= _editIconScreenPos.y + _editIconSize + 40;
	}

	private boolean isInsideContextGuidance(float x, float y) {
		return Math.hypot(x - _guidanceButtonPos.x, y - _guidanceButtonPos.y) <= _contextButtonSize / 2f + 40;
	}

	private boolean isInsideContextGeometry(float x, float y) {
		return Math.hypot(x - _geometryButtonPos.x, y - _geometryButtonPos.y) <= _contextButtonSize / 2f + 40;
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
			_draggedGuidancePointIndex = -1;
			_resizingGuidancePointIndex = -1;
			_pressedButton = PRESS_NONE;
			_handler.removeCallbacks(_longPressRunnable);
			_showContextButtons = false;
			return super.onTouchEvent(event, mapView);
		}

		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();

		Projection projection = mapView.getProjection();

		if (action == MotionEvent.ACTION_DOWN) {
			_isMoving = false;
			_gestureCaptured = false;
			_initialX = x;
			_initialY = y;
			_currentX = x;
			_currentY = y;

			if (_showContextButtons) {
				if (isInsideContextGuidance(x, y)) {
					_pressedButton = PRESS_CONTEXT_GUIDANCE;
					_gestureCaptured = true;
					return true;
				}
				if (isInsideContextGeometry(x, y)) {
					_pressedButton = PRESS_CONTEXT_GEOMETRY;
					_gestureCaptured = true;
					return true;
				}
				// Si se pulsa fuera, cerrar menú y consumir el evento para que el mapa no se
				// mueva
				_showContextButtons = false;
				mapView.invalidate();
				_justDismissedDeleteIcon = true;
				_gestureCaptured = true;
				return true;
			}

			if (_longPressedGuidancePointIndex != -1) {
				if (isInsideDeleteIcon(x, y)) {
					_pressedButton = PRESS_DELETE_GUIDANCE;
					_gestureCaptured = true;
					return true;
				}
				if (isInsideEditIcon(x, y)) {
					_pressedButton = PRESS_EDIT_GUIDANCE;
					_gestureCaptured = true;
					return true;
				}
				// Si se pulsa fuera, descartar iconos y consumir evento
				_longPressedGuidancePointIndex = -1;
				mapView.invalidate();
				_justDismissedDeleteIcon = true;
				_gestureCaptured = true;
				return true;
			}

			if (_longPressedPointIndex != -1) {
				if (isInsideDeleteIcon(x, y)) {
					_pressedButton = PRESS_DELETE_WAYPOINT;
					_gestureCaptured = true;
					return true;
				}
				if (isInsideEditIcon(x, y)) {
					_pressedButton = PRESS_CREATE_GUIDANCE_TO_WAYPOINT;
					_gestureCaptured = true;
					return true;
				}
				// Si se pulsa fuera, descartar icono y consumir evento
				_longPressedPointIndex = -1;
				mapView.invalidate();
				_justDismissedDeleteIcon = true;
				_gestureCaptured = true;
				return true;
			}

			_pressedButton = PRESS_NONE;
			_justDismissedDeleteIcon = false;

			// Comprobar si se pulsa sobre un punto de guiado
			if (_route.getGuidancePoints() != null) {
				GuidancePoint[] guidancePoints = _route.getGuidancePoints();
				Point screenPoint = new Point();
				for (int i = 0; i < guidancePoints.length; i++) {
					if (guidancePoints[i].getPoint() == null)
						continue;
					projection.toPixels(guidancePoints[i].getPoint(), screenPoint);
					if (Math.hypot(screenPoint.x - x, screenPoint.y - y) < _pointRadius * 2) {
						_draggedGuidancePointIndex = i;
						_handler.postDelayed(_longPressRunnable, _longPressTimeout);
						_gestureCaptured = true;
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
					_gestureCaptured = true;
					return true;
				}
			}

			// Comprobar si se pulsa sobre el área de un punto de guiado (Resize)
			if (_route.getGuidancePoints() != null) {
				GuidancePoint[] guidancePoints = _route.getGuidancePoints();
				for (int i = 0; i < guidancePoints.length; i++) {
					if (guidancePoints[i].getPoint() == null)
						continue;
					mapView.getProjection().toPixels(guidancePoints[i].getPoint(), screenPoint);

					float radiusInMeters = guidancePoints[i].getRadius();
					float radiusInPixels = (float) (mapView.getProjection().metersToEquatorPixels(radiusInMeters)
							* (1 / Math.cos(Math.toRadians(guidancePoints[i].getLatitude()))));

					if (Math.hypot(screenPoint.x - x, screenPoint.y - y) <= radiusInPixels) {
						_resizingGuidancePointIndex = i;
						_initialRadius = radiusInMeters;
						IGeoPoint touchGeo = projection.fromPixels((int) x, (int) y);
						_initialTouchDist = (float) new GeoPoint(guidancePoints[i].getLatitude(),
								guidancePoints[i].getLongitude()).distanceToAsDouble(touchGeo);
						_gestureCaptured = true;
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
				_gestureCaptured = true;
				return true;
			}
		} else if (action == MotionEvent.ACTION_MOVE) {
			if (_gestureCaptured) {
				if (!_isMoving && Math.hypot(x - _initialX, y - _initialY) > _motionThreshold) {
					_isMoving = true;
					_handler.removeCallbacks(_longPressRunnable);
				}

				_currentX = x;
				_currentY = y;

				if (_isMoving) {
					if (_draggedPointIndex != -1) {
						IGeoPoint newGeoPoint = projection.fromPixels((int) x, (int) y);
						List<IGeoPoint> wayPoints = _route.getWayPoints();
						IGeoPoint[] pointsArray = new IGeoPoint[wayPoints.size()];
						wayPoints.toArray(pointsArray);
						pointsArray[_draggedPointIndex] = newGeoPoint;
						_route.setWayPoints(pointsArray);
						mapView.invalidate();
					} else if (_draggedGuidancePointIndex != -1) {
						IGeoPoint newGeoPoint = projection.fromPixels((int) x, (int) y);
						GuidancePoint[] guidancePoints = _route.getGuidancePoints();
						if (guidancePoints != null && _draggedGuidancePointIndex >= 0
								&& _draggedGuidancePointIndex < guidancePoints.length) {
							GuidancePoint oldGp = guidancePoints[_draggedGuidancePointIndex];
							guidancePoints[_draggedGuidancePointIndex] = new GuidancePoint(oldGp.getKey(),
									newGeoPoint.getLatitude(), newGeoPoint.getLongitude(), oldGp.getNarrative(),
									(int) oldGp.getRadius());
							_route.setGuidancePoints(guidancePoints);
							mapView.invalidate();
						}
					} else if (_resizingGuidancePointIndex != -1) {
						GuidancePoint[] guidancePoints = _route.getGuidancePoints();
						if (guidancePoints != null && _resizingGuidancePointIndex >= 0
								&& _resizingGuidancePointIndex < guidancePoints.length) {
							GuidancePoint gp = guidancePoints[_resizingGuidancePointIndex];
							IGeoPoint touchGeo = projection.fromPixels((int) x, (int) y);
							double currentDist = new GeoPoint(gp.getLatitude(), gp.getLongitude())
									.distanceToAsDouble(touchGeo);

							float newRadius = _initialRadius + (float) (currentDist - _initialTouchDist);
							if (newRadius < _minGuidanceRadius)
								newRadius = _minGuidanceRadius;

							// Magnet effect: snap to multiples of 50 if within 15 meters
							float nearestSnap = Math.round(newRadius / _snapInterval) * _snapInterval;
							if (Math.abs(newRadius - nearestSnap) < _snapThreshold) {
								newRadius = nearestSnap;
							}

							guidancePoints[_resizingGuidancePointIndex] = new GuidancePoint(gp.getKey(),
									gp.getLatitude(), gp.getLongitude(), gp.getNarrative(), (int) newRadius);
							_route.setGuidancePoints(guidancePoints);
							mapView.invalidate();
						}
					}
				}
				return true;
			}
		} else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
			_handler.removeCallbacks(_longPressRunnable);

			if (_pressedButton != PRESS_NONE && action == MotionEvent.ACTION_UP) {
				int btn = _pressedButton;
				_pressedButton = PRESS_NONE;
				_gestureCaptured = false;

				if (btn == PRESS_CONTEXT_GUIDANCE && isInsideContextGuidance(x, y)) {
					createGuidancePoint(_contextMenuGeoPosition);
					_showContextButtons = false;
					mapView.invalidate();
				} else if (btn == PRESS_CONTEXT_GEOMETRY && isInsideContextGeometry(x, y)) {
					extendRoute(_contextMenuGeoPosition);
					_showContextButtons = false;
					mapView.invalidate();
				} else if (btn == PRESS_DELETE_GUIDANCE && isInsideDeleteIcon(x, y)) {
					deleteGuidancePoint(_longPressedGuidancePointIndex);
					_longPressedGuidancePointIndex = -1;
					mapView.invalidate();
				} else if (btn == PRESS_EDIT_GUIDANCE && isInsideEditIcon(x, y)) {
					editGuidancePoint(_longPressedGuidancePointIndex);
					_longPressedGuidancePointIndex = -1;
					mapView.invalidate();
				} else if (btn == PRESS_DELETE_WAYPOINT && isInsideDeleteIcon(x, y)) {
					deleteWayPoint(_longPressedPointIndex);
					_longPressedPointIndex = -1;
					mapView.invalidate();
				} else if (btn == PRESS_CREATE_GUIDANCE_TO_WAYPOINT && isInsideEditIcon(x, y)) {
					IGeoPoint p = _route.getWayPoints().get(_longPressedPointIndex);
					createGuidancePoint(p);
					_longPressedPointIndex = -1;
					mapView.invalidate();
				}
				_justDismissedDeleteIcon = false;
				return true;
			}

			boolean consumed = _gestureCaptured;
			_pressedButton = PRESS_NONE;
			_gestureCaptured = false;

			boolean wasEditing = _draggedPointIndex != -1 || _draggedGuidancePointIndex != -1
					|| _resizingGuidancePointIndex != -1;

			if (!_isMoving && action == MotionEvent.ACTION_UP) {
				if (_draggedPointIndex != -1) {
					_longPressedPointIndex = _draggedPointIndex;
					mapView.invalidate();
					consumed = true;
				} else if (_draggedGuidancePointIndex != -1) {
					_longPressedGuidancePointIndex = _draggedGuidancePointIndex;
					mapView.invalidate();
					consumed = true;
				}
			}

			_draggedPointIndex = -1;
			_draggedGuidancePointIndex = -1;
			_resizingGuidancePointIndex = -1;
			return consumed || wasEditing;
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

		if (_showContextButtons) {
			_showContextButtons = false;
			mapView.invalidate();
			return true;
		}

		IGeoPoint p = mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY());

		_contextMenuGeoPosition = p;
		_showContextButtons = true;
		mapView.invalidate();
		return true;
	}

	private void createGuidancePoint(IGeoPoint p) {
		GuidancePoint newGp = new GuidancePoint(UUID.randomUUID().toString(), p.getLatitude(), p.getLongitude(), "",
				_defaultGuidanceRadius);

		GuidancePoint[] currentPoints = _route.getGuidancePoints();
		List<GuidancePoint> pointsList;
		if (currentPoints == null) {
			pointsList = new ArrayList<>();
		} else {
			pointsList = new ArrayList<>(Arrays.asList(currentPoints));
		}
		pointsList.add(newGp);
		_route.setGuidancePoints(pointsList.toArray(new GuidancePoint[0]));
	}

	private void extendRoute(IGeoPoint p) {
		List<IGeoPoint> wayPoints = new ArrayList<>(_route.getWayPoints());

		if (wayPoints.isEmpty()) {
			wayPoints.add(p);
		} else {
			IGeoPoint first = wayPoints.get(0);
			IGeoPoint last = wayPoints.get(wayPoints.size() - 1);

			double distToFirst = new GeoPoint(p.getLatitude(), p.getLongitude())
					.distanceToAsDouble(new GeoPoint(first.getLatitude(), first.getLongitude()));
			double distToLast = new GeoPoint(p.getLatitude(), p.getLongitude())
					.distanceToAsDouble(new GeoPoint(last.getLatitude(), last.getLongitude()));

			if (distToFirst < distToLast) {
				wayPoints.add(0, p);
			} else {
				wayPoints.add(p);
			}
		}

		_route.setWayPoints(wayPoints.toArray(new IGeoPoint[0]));
	}

	private void drawContextButtons(Canvas canvas, MapView mapView) {
		if (_contextMenuGeoPosition == null)
			return;

		mapView.getProjection().toPixels(_contextMenuGeoPosition, _contextMenuScreenPos);

		int y = _contextMenuScreenPos.y - _contextButtonSize; // Arriba del punto
		int xCenter = _contextMenuScreenPos.x;

		// Calcular posiciones
		int xGuidance = xCenter - _contextButtonSize / 2 - _contextButtonSpacing / 2;
		int xGeometry = xCenter + _contextButtonSize / 2 + _contextButtonSpacing / 2;

		// Ajustar si se salen de la pantalla
		if (xGuidance - _contextButtonSize / 2 < 0) {
			int diff = -(xGuidance - _contextButtonSize / 2) + 20;
			xGuidance += diff;
			xGeometry += diff;
		} else if (mapView.getWidth() > 0 && xGeometry + _contextButtonSize / 2 > mapView.getWidth()) {
			int diff = (xGeometry + _contextButtonSize / 2) - mapView.getWidth() + 20;
			xGuidance -= diff;
			xGeometry -= diff;
		}

		if (y - _contextButtonSize / 2 < 0) {
			y = _contextMenuScreenPos.y + _contextButtonSize + 20;
		} else if (mapView.getHeight() > 0 && y + _contextButtonSize / 2 > mapView.getHeight()) {
			// Logic for bottom overflow if needed, though usually above point is fine
			// unless point is at top.
			// Currently not fully implemented for bottom overflow, but preventing
			// regression.
		}

		_guidanceButtonPos.set(xGuidance, y);
		_geometryButtonPos.set(xGeometry, y);

		// Dibujar botones
		canvas.drawCircle(xGuidance, y, _contextButtonSize / 2f, _contextButtonPaint);
		canvas.drawCircle(xGeometry, y, _contextButtonSize / 2f, _contextButtonPaint);

		// Dibujar iconos
		drawGuidanceIcon(canvas, xGuidance, y);
		drawGeometryIcon(canvas, xGeometry, y);
	}

	private void drawGuidanceIcon(Canvas canvas, int x, int y) {
		Path path = new Path();
		float size = _contextButtonSize * 0.5f;
		float halfSize = size / 2f;

		RectF bubble = new RectF(x - halfSize, y - halfSize - 5, x + halfSize, y + halfSize - 10);
		path.addOval(bubble, Path.Direction.CW);

		path.moveTo(x - 5, y + halfSize - 10);
		path.lineTo(x - 10, y + halfSize + 5);
		path.lineTo(x + 5, y + halfSize - 8);

		canvas.drawPath(path, _contextIconPaint);

		// Puntos suspensivos simulados
		float dotY = y - 5;
		canvas.drawPoint(x - 10, dotY, _contextIconPaint);
		canvas.drawPoint(x, dotY, _contextIconPaint);
		canvas.drawPoint(x + 10, dotY, _contextIconPaint);
	}

	private void drawGeometryIcon(Canvas canvas, int x, int y) {
		Path path = new Path();
		float size = _contextButtonSize * 0.5f;
		float halfSize = size / 2f;

		path.moveTo(x - halfSize, y + halfSize);
		path.lineTo(x - halfSize / 3, y - halfSize);
		path.lineTo(x + halfSize / 3, y + halfSize);
		path.lineTo(x + halfSize, y - halfSize / 4);

		canvas.drawPath(path, _contextIconPaint);

		// Puntos en los vértices
		Paint dotPaint = new Paint(_contextIconPaint);
		dotPaint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(x - halfSize, y + halfSize, 4, dotPaint);
		canvas.drawCircle(x - halfSize / 3, y - halfSize, 4, dotPaint);
		canvas.drawCircle(x + halfSize / 3, y + halfSize, 4, dotPaint);
		canvas.drawCircle(x + halfSize, y - halfSize / 4, 4, dotPaint);
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
		if (_route.getGuidancePoints() == null || index < 0 || index >= _route.getGuidancePoints().length)
			return;
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
				guidancePoints[index] = new GuidancePoint(gp.getKey(), gp.getLatitude(), gp.getLongitude(),
						newNarrative, (int) gp.getRadius());
				_route.setGuidancePoints(guidancePoints);
				_mapView.invalidate();
			}
		});
		builder.setNegativeButton("Cancelar", null);
		builder.show();
	}

	private void performDeleteGuidancePoint(int index) {
		if (_route.getGuidancePoints() == null)
			return;
		List<GuidancePoint> guidancePoints = new ArrayList<>(Arrays.asList(_route.getGuidancePoints()));
		if (index >= 0 && index < guidancePoints.size()) {
			guidancePoints.remove(index);
			_route.setGuidancePoints(guidancePoints.toArray(new GuidancePoint[0]));
		}
	}

}
