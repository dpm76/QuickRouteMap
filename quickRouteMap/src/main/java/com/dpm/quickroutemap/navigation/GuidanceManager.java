package com.dpm.quickroutemap.navigation;

public class GuidanceManager implements IGuidanceProvider{

    private GuidancePoint[] _route;
    private IGuidanceConsumer _consumer;

    private static GuidanceManager _instance = null;

    public static GuidanceManager getInstance(){
        if(_instance == null){
            _instance = new GuidanceManager();
        }
        return _instance;
    }

    private GuidanceManager(){}

    public void setConsumer(IGuidanceConsumer consumer){
        _consumer = consumer;
    }

    public void setRouteGuidance(GuidancePoint[] routeGuidance) {
        _route = routeGuidance;
        if (_consumer != null){
            _consumer.setRouteGuidance(_route);
        }
    }

    @Override
    public GuidancePoint[] getCurrentRouteGuidance() {
        return _route;
    }
}
