package com.dpm.framework;

/**
 * Parámetros del evento de progreso de trabajo
 * @author David
 *
 */
public class JobProgressedEventArgs extends EventArgs {

	private float _totalProgress;
	private float _stepProgress;
	
	/**
	 * Constructor
	 * @param totalProgress Porcentaje acumulado del progreso
	 * @param stepProgress Porcentaje relativo al paso informado
	 */
	public JobProgressedEventArgs(float totalProgress, float stepProgress){
		_totalProgress = totalProgress;
		_stepProgress = stepProgress;
	}
	
	/**
	 * @return Porcentaje acumulado del progreso
	 */
	public float getTotalProgress(){
		return _totalProgress;
	}
	
	/**
	 * @return Porcentaje relativo al paso informado
	 */
	public float getStepProgress(){
		return _stepProgress;
	}
}
