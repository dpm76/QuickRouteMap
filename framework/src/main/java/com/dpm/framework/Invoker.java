package com.dpm.framework;

import android.os.Handler;

/**
 * Invoca un método en el hilo propietario del objeto.
 * El hilo propietario es donde se llame al constructor.
 * 
 * @author David
 *
 */
public class Invoker {
	
	private final Long _ownerThreadId = Thread.currentThread().getId();
	private final Handler _handler = new Handler();
	
	/**
	 * Indica si se llama desde el hilo propietario. En ese caso habría que invocar.
	 * 
	 * @return
	 */
	public boolean invokeRequired(){
		
		return (Thread.currentThread().getId() != _ownerThreadId);
		
	}

	/**
	 * Invoca la tarea en el hilo propietario.
	 * @param runnable Tarea a ejecutar
	 */
	public void invoke(Runnable runnable){
		_handler.post(runnable);
	}
	
	/**
	 * Invoca la tarea en el hilo propietario, si se llama desde un hilo distinto;
	 * o se ejecuta inmediatamente si ya se está en el hilo propietario.
	 * 
	 * @param runnable Tarea a ejecutar
	 */
	public void invokeIfRequired(Runnable runnable){
		if(invokeRequired()){
			invoke(runnable);
		}else{
			runnable.run();
		}
	}

}
