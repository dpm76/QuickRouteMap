/**
 * 
 */
package com.dpm.framework;


/**
 * 
 * Runnable con parámetros
 * 
 * @author David
 *
 */
public abstract class ParametrizedRunnable implements Runnable {
	
	protected Object[] _params;
	
	public ParametrizedRunnable(Object[] params){
		_params = params;
	}
	
}
