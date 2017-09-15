package com.dpm.framework;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.dpm.framework.ProgressReporterRunnable.ProgressReportingEventArgs;

/**
 * Detiene la interfaz de usuario y muestra un diálogo de espera 
 * mientras se ejecuta una tarea
 * 
 * @author David
 *
 */
public class EndRunnableExecutionUiHalter {
	
	private static String LOG_TAG = EndRunnableExecutionUiHalter.class.getSimpleName();
	private static EndRunnableExecutionUiHalter _instance;
	
	private ProgressDialog _dialog;
	private boolean _isRunning = false;
	private CharSequence _dialogTitle;
	private CharSequence _dialogMessage;
	private ProgressReporterRunnable _runnable;
	private int _progress = 0;
	
	public static EndRunnableExecutionUiHalter getInstance(){
		if(_instance == null){
			_instance = new EndRunnableExecutionUiHalter();
		}
		
		return _instance;
	}
	
	private EndRunnableExecutionUiHalter(){
	}
	
	/**
	 * Inicia la tarea y muestra el diálogo de espera
	 * 
	 * @param context Contexto
	 * @param title Título del diálogo
	 * @param message Mensaje del diálogo
	 * @param runnable Tarea que se va a ejecutar
	 */
	public void start(Context context, CharSequence title, CharSequence message, final ProgressReporterRunnable runnable){
		
		if(!_isRunning){
			_isRunning = true;
			_dialogTitle = title;
			_dialogMessage = message;
			_runnable = runnable;
			_progress = 0;
	
			showDialog(context);
			
			final Invoker _invoker = new Invoker();
			
			runnable.progressReporting.add(new EventDispatcher<ProgressReporterRunnable.ProgressReportingEventArgs>() {
				
				@Override
				public void dispatch(Object o, final ProgressReportingEventArgs args) {
					_invoker.invokeIfRequired(new Runnable() {
						
						@Override
						public void run() {
							_progress += args.getIncrement();
							Log.v(LOG_TAG, String.format("Progreso: %1$d", _progress));
							if(_dialog != null && _dialog.isShowing()){
								_dialog.setProgress(_progress);
							}						
						}
					});								
				}
			});
			
			ExecutorService executor = Executors.newSingleThreadExecutor();		
			final Handler handler = new Handler() {
				public void handleMessage(Message msg) {
					_isRunning = false;
					if(_dialog != null){
						_dialog.dismiss();
					}					
			    }
			};
			executor.submit(new Runnable() {
				
				@Override
				public void run() {
					try {
						runnable.run();
					} catch (Exception e) {						
						e.printStackTrace();
					}finally{
						handler.sendEmptyMessage(0);
					}
				}
			});		
			executor.shutdown();
		}else{
			Log.w(LOG_TAG, "No se puede comenzar. Ya se está realizando una acción.");
		}
	}
	
	public boolean isRunning(){
		return _isRunning;
	}
	
	public void hideDialog(){
		if(_dialog != null){
			_dialog.dismiss();
			_dialog = null;
		}
	}
	
	public void showDialog(Context context){
		_dialog = new ProgressDialog(context);
		_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		_dialog.setTitle(_dialogTitle);
		_dialog.setMessage(_dialogMessage);		
		_dialog.setCancelable(false);
		_dialog.setMax(_runnable.getMaxProgress());
		_dialog.setProgress(_progress);
		_dialog.show();
	}
	
}
