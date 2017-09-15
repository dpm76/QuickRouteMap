/**
 * 
 */
package com.dpm.framework;

/**
 * @author David
 *
 * Ejecuci�n que informa de su progreso
 */
public abstract class ProgressReporterRunnable extends ParametrizedRunnable {
	
	/**
	 * Par�metros del evento de informe de progreso
	 * @author David
	 *
	 */
	public class ProgressReportingEventArgs extends EventArgs{
		
		private int _increment;
		
		ProgressReportingEventArgs(int increment){
			_increment = increment;
		}
		
		/**
		 * 
		 * @return Incremento del progreso
		 */
		public int getIncrement(){
			return _increment;
		}
	}
	
	private final int _maxProgress;
	
	/**
	 * Se lanza cuando la ejecuci�n ha terminado
	 */
	public final Event<EventArgs> finished = new Event<EventArgs>();

	/**
	 * Se lanza cuando se informa sobre el avance de la ejecuci�n 
	 */
	public final Event<ProgressReportingEventArgs> progressReporting = new Event<ProgressReportingEventArgs>();
	
	
	/**
	 * Constructor
	 * 
	 * @param maxProgress Cantidad m�xima de progreso
	 */
	public ProgressReporterRunnable(int maxProgress, Object[] params){
		super(params);
		_maxProgress = maxProgress;		
	}	

	/**
	 * @return Cantidad m�xima de progreso
	 */
	public int getMaxProgress() {
		return _maxProgress;
	}
	
	protected void onFinished(){
		finished.rise(this, EventArgs.empty);
	}
	
	protected void onProgressReporting(int increment){
		progressReporting.rise(this, new ProgressReportingEventArgs(increment));
	}

}
