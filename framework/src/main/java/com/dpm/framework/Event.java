package com.dpm.framework;

import java.util.ArrayList;

/**
 * Eventos al estilo de C#
 * 
 * @author David
 *
 */
public class Event<T extends EventArgs> {

	private ArrayList<EventDispatcher<T>> _dispatchers = new ArrayList<EventDispatcher<T>>();
	
	/**
	 * Añade un tratador del evento
	 * 
	 * @param dispatcher Tratador del evento
	 * @return Indica si se ha añadido correctamente
	 */
	
	public boolean add(EventDispatcher<T> dispatcher){
		boolean done = false;
		
		if(!this._dispatchers.contains(dispatcher)){
			this._dispatchers.add(dispatcher);
			done = true;
		}
		
		return done;
	}
	
	/**
	 * Elimina una tratador del evento
	 * 
	 * @param dispatcher Tratador del evento
	 * @return Indica si se ha eliminado correctamente
	 */
	public boolean remove(EventDispatcher<T> dispatcher){
		boolean done = false;
		
		if(this._dispatchers.contains(dispatcher)){
			this._dispatchers.remove(dispatcher);
			done = true;
		}
		
		return done;
	}
	
	/**
	 * Indica si está registrado un tratador
	 * @param dispatcher
	 * @return
	 */
	public boolean isRegistered(EventDispatcher<T> dispatcher){
		return this._dispatchers.contains(dispatcher);
	}
	
	/**
	 * Levanta el evento
	 * @param o Objeto que levanta el evento
	 * @param args Argumentos del evento
	 */
	public void rise(Object o, T args){
		for (EventDispatcher<T> dispatcher : this._dispatchers) {
			dispatcher.dispatch(o, args);
		}
	}
}
