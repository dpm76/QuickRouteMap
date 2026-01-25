package com.dpm.frameworktest;

import com.dpm.framework.DatetimeUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class DatetimeUtilsTest {

    @Test
    public void areSameDay_WithSameDay_ReturnsTrue() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.OCTOBER, 27);
        Date date1 = cal.getTime();
        Date date2 = cal.getTime();

        Assert.assertTrue(DatetimeUtils.areSameDay(date1, date2));
    }

    @Test
    public void areSameDay_WithDifferentDay_ReturnsFalse() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2023, Calendar.OCTOBER, 27);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2023, Calendar.OCTOBER, 28);

        Assert.assertFalse(DatetimeUtils.areSameDay(cal1.getTime(), cal2.getTime()));
    }

    @Test
    public void areSameDay_WithDifferentMonth_ReturnsFalse() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2023, Calendar.OCTOBER, 27);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2023, Calendar.NOVEMBER, 27);

        Assert.assertFalse(DatetimeUtils.areSameDay(cal1.getTime(), cal2.getTime()));
    }

    @Test
    public void areSameDay_WithDifferentYear_ReturnsFalse() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2023, Calendar.OCTOBER, 27);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2024, Calendar.OCTOBER, 27);

        Assert.assertFalse(DatetimeUtils.areSameDay(cal1.getTime(), cal2.getTime()));
    }

    @Test
    public void isToday_WithToday_ReturnsTrue() {
        Assert.assertTrue(DatetimeUtils.isToday(new Date()));
    }

    @Test
    public void isToday_WithYesterday_ReturnsFalse() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertFalse(DatetimeUtils.isToday(cal.getTime()));
    }
}
