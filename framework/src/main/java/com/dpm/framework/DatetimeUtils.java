package com.dpm.framework;

import java.util.Calendar;
import java.util.Date;

/**
 * Utilidades para las operaciones con fechas y horas.
 * @author David
 *
 */
public final class DatetimeUtils {

	/**
	 * Indica si dos fechas son el mismo día
	 * @param date1 fecha 1
	 * @param date2 fecha 2
	 * @return
	 */
	public static boolean areSameDay(Date date1, Date date2){
		
		Calendar cal1, cal2;
		cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		
		return 
				((cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) &&
				(cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) &&
				(cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)));
	}
	
	/**
	 * Indica si una fecha es hoy
	 * @param date fecha
	 * @return
	 */
	public static boolean isToday(Date date){
		return areSameDay(date, new Date());
	}
}
