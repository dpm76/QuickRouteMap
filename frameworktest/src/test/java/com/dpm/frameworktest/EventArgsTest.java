package com.dpm.frameworktest;

import com.dpm.framework.EventArgs;
import com.dpm.framework.JobProgressedEventArgs;

import org.junit.Assert;
import org.junit.Test;

public class EventArgsTest {

    @Test
    public void empty_NotNull() {
        Assert.assertNotNull(EventArgs.empty);
    }

    @Test
    public void jobProgressedEventArgs_Getters() {
        JobProgressedEventArgs args = new JobProgressedEventArgs(0.5f, 0.1f);
        Assert.assertEquals(0.5f, args.getTotalProgress(), 0.0f);
        Assert.assertEquals(0.1f, args.getStepProgress(), 0.0f);
    }
}
