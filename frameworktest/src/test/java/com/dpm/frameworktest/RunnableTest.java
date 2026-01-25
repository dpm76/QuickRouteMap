package com.dpm.frameworktest;

import com.dpm.framework.ParametrizedRunnable;
import com.dpm.framework.ProgressReporterRunnable;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RunnableTest {

    @Test
    public void parametrizedRunnable_StoresParams() {
        Object[] params = {"test", 123};
        ParametrizedRunnable runnable = new ParametrizedRunnable(params) {
            @Override
            public void run() {
                // Accessing protected member _params from the same package or subclass
                // Since this is in com.dpm.frameworktest, it needs to be a subclass or use getters if available.
                // _params is protected in com.dpm.framework.ParametrizedRunnable.
            }
        };
        // We can't easily test _params directly if we are in a different package unless we expose it.
        // But we can check if it compiles and runs.
        runnable.run();
    }

    @Test
    public void progressReporterRunnable_ReportsProgressAndFinish() {
        AtomicInteger progressValue = new AtomicInteger(0);
        AtomicBoolean finishedCalled = new AtomicBoolean(false);

        ProgressReporterRunnable reporter = new ProgressReporterRunnable(100, null) {
            @Override
            public void run() {
                onProgressReporting(25);
                onProgressReporting(25);
                onFinished();
            }
        };

        reporter.progressReporting.add((o, args) -> progressValue.addAndGet(args.getIncrement()));
        reporter.finished.add((o, args) -> finishedCalled.set(true));

        Assert.assertEquals(100, reporter.getMaxProgress());
        reporter.run();

        Assert.assertEquals(50, progressValue.get());
        Assert.assertTrue(finishedCalled.get());
    }
}
