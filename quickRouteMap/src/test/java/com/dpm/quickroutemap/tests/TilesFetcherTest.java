package com.dpm.quickroutemap.tests;

import com.dpm.quickroutemap.TilesFetcher;
import com.dpm.quickroutemap.navigation.Route;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TilesFetcherTest {

    @Test
    public void fetchTiles_CompletesAndRisesEvent() throws InterruptedException {
        // Mocks de osmdroid
        MapView mockMapView = Mockito.mock(MapView.class);
        MapTileProviderBase mockProvider = Mockito.mock(MapTileProviderBase.class);
        OnlineTileSourceBase mockTileSource = Mockito.mock(OnlineTileSourceBase.class);
        
        // Configuramos los mocks para que devuelvan valores válidos y no null
        Mockito.when(mockMapView.getTileProvider()).thenReturn(mockProvider);
        Mockito.when(mockProvider.getTileSource()).thenReturn(mockTileSource);
        Mockito.when(mockTileSource.getBaseUrl()).thenReturn("http://tile.openstreetmap.org/");
        Mockito.when(mockTileSource.name()).thenReturn("OpenStreetMap");

        TilesFetcher fetcher = new TilesFetcher(mockMapView, "UserAgent", "/tmp/qrm", "/tmp/osm");

        // Usamos un mock para IGeoPoint para evitar lógica interna de GeoPoint en el test
        IGeoPoint mockPoint = Mockito.mock(IGeoPoint.class);
        Mockito.when(mockPoint.getLatitudeE6()).thenReturn(40000000); // 40.0
        Mockito.when(mockPoint.getLongitudeE6()).thenReturn(-3000000); // -3.0

        Route mockRoute = Mockito.mock(Route.class);
        List<IGeoPoint> waypoints = new ArrayList<>();
        waypoints.add(mockPoint);
        Mockito.when(mockRoute.getWayPoints()).thenReturn(waypoints);

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Throwable> threadException = new AtomicReference<>();

        fetcher.FetchFinished.add((sender, args) -> latch.countDown());

        // Ejecución
        fetcher.fetchTiles(mockRoute, 10, false);

        // Esperamos un tiempo razonable
        boolean finished = latch.await(15, TimeUnit.SECONDS);
        
        if (!finished) {
            // Si no ha terminado, es posible que haya habido una excepción en el hilo secundario
            // que no hemos capturado. En JUnit 4 es difícil capturar excepciones de otros hilos
            // sin instrumentar el código, pero el timeout de 15s es generoso.
            Assert.fail("FetchFinished event was not raised. Check if an exception occurred in the background thread.");
        }
    }
}
