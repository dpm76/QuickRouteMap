package com.dpm.quickroutemap.navigation;

public class GuidanceManager implements IGuidanceProvider{

    private Route _route;
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

    public void setCurrentRoute(Route route) {
        _route = route;
        if (_consumer != null){
            _consumer.setCurrentRouteGuidance(_route != null ? _route.getGuidancePoints() : null);
        }
    }

    public Route getCurrentRoute() {
        return _route;
    }

    public void setCurrentRouteGuidance(GuidancePoint[] routeGuidance) {
        if (_route == null) {
            _route = new Route();
        }
        _route.setGuidancePoints(routeGuidance);
        if (_consumer != null){
            _consumer.setCurrentRouteGuidance(routeGuidance);
        }
    }

    @Override
    public GuidancePoint[] getCurrentRouteGuidance() {
        return _route != null ? _route.getGuidancePoints() : null;
    }
}
