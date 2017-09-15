package com.dpm.framework;



/**
 * Tratador de eventos
 * @author David
 *
 */
public interface EventDispatcher<T extends EventArgs> {
	/**
	 * Trata un evento
	 * @param o Objeto que ha lanzado el evento
	 * @param args Argumentos del evento
	 */
	void dispatch(Object o, T args);	
}
