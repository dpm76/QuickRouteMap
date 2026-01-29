package com.dpm.quickroutemap.navigation;

import java.util.UUID;

public class RouteUtils {

    /**
     * Asegura que las propiedades obligatorias de la ruta estén rellenas antes de guardar.
     */
    public static void populateMissingProperties(Route route, String fallbackName) {
        if (route == null) return;

        if (route.getKey() == null || route.getKey().isEmpty()) {
            route.setKey(UUID.randomUUID().toString().substring(0, 8));
        }

        if (route.getName() == null || route.getName().isEmpty()) {
            route.setName(fallbackName != null ? fallbackName : "Nueva Ruta");
        }

        if (route.getDescription() == null) {
            route.setDescription("");
        }
        
        // _totalDistance, _totalTime e _isClosed son tipos básicos o ya están gestionados por Gson/Java defaults.
    }
}
