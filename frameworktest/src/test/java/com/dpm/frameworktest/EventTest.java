package com.dpm.frameworktest;

import com.dpm.framework.Event;
import com.dpm.framework.EventArgs;
import com.dpm.framework.EventDispatcher;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class EventTest {

    @Test
    public void add_NewDispatcher_ReturnsTrue() {
        Event<EventArgs> event = new Event<>();
        EventDispatcher<EventArgs> dispatcher = (o, args) -> {};
        Assert.assertTrue(event.add(dispatcher));
    }

    @Test
    public void add_ExistingDispatcher_ReturnsFalse() {
        Event<EventArgs> event = new Event<>();
        EventDispatcher<EventArgs> dispatcher = (o, args) -> {};
        event.add(dispatcher);
        Assert.assertFalse(event.add(dispatcher));
    }

    @Test
    public void remove_ExistingDispatcher_ReturnsTrue() {
        Event<EventArgs> event = new Event<>();
        EventDispatcher<EventArgs> dispatcher = (o, args) -> {};
        event.add(dispatcher);
        Assert.assertTrue(event.remove(dispatcher));
    }

    @Test
    public void remove_NonExistingDispatcher_ReturnsFalse() {
        Event<EventArgs> event = new Event<>();
        EventDispatcher<EventArgs> dispatcher = (o, args) -> {};
        Assert.assertFalse(event.remove(dispatcher));
    }

    @Test
    public void isRegistered_ReturnsCorrectStatus() {
        Event<EventArgs> event = new Event<>();
        EventDispatcher<EventArgs> dispatcher = (o, args) -> {};
        Assert.assertFalse(event.isRegistered(dispatcher));
        event.add(dispatcher);
        Assert.assertTrue(event.isRegistered(dispatcher));
    }

    @Test
    public void rise_CallsDispatchers() {
        Event<EventArgs> event = new Event<>();
        AtomicBoolean called = new AtomicBoolean(false);
        Object source = new Object();
        EventArgs eventArgs = new EventArgs();

        event.add((o, args) -> {
            Assert.assertEquals(source, o);
            Assert.assertEquals(eventArgs, args);
            called.set(true);
        });

        event.rise(source, eventArgs);
        Assert.assertTrue(called.get());
    }
}
