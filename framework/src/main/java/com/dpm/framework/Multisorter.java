package com.dpm.framework;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Ordena una lista con verios criterios simultánemete
 * @author David
 *
 */
public class Multisorter {
	
	/**
	 * Ordena la lista por varios criterios
	 * 
	 * @param list Lista a ordenar
	 * @param comparatorList Lista de criterios por los que ordenar.
	 *  Es importante el orden de la lista, ya que se aplicarán en el orden 
	 *  en el que aparezcan.
	 */
	public static <T> void sort(List<T> list, List<Comparator<T>> comparatorList){
		for(Comparator<T> comparator: comparatorList){
			Collections.sort(list, comparator);
		}
	}
}
