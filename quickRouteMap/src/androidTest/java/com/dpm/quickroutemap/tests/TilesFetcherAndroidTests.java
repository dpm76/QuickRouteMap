package com.dpm.quickroutemap.tests;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.dpm.framework.FileHelper;
import com.dpm.quickroutemap.TilesFetcher;
import com.dpm.quickroutemap.navigation.GeoPointSerializer;
import com.dpm.quickroutemap.navigation.Route;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.views.MapView;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TilesFetcherAndroidTests {

    @Test
    public void givenARoute_fetchTilesFromUrl_tileFilesAreDownloaded() {

        final String jsonRoute =
                "{"
                + "	\"_totalDistance\": 95.6369,"
                + "	\"_totalTime\": 4270,"
                + "	\"_key\": \"key\","
                + "	\"_name\": \"name\","
                + "	\"_description\": \"description\","
                + "	\"_isClosed\": false,"
                + "	\"_guidancePoints\": [{"
                + "		\"_key\": \"gp12\","
                + "		\"_point\": [48.531066,"
                + "		7.930531],"
                + "		\"_radius\": 300,"
                + "		\"_narrative\": \"Tome la salida 54 hacia Apenbaia.\""
                + "	},"
                + "	{"
                + "		\"_key\": \"gp13\","
                + "		\"_point\": [48.532997,"
                + "		7.933189],"
                + "		\"_radius\": 200,"
                + "		\"_narrative\": \"Sigue por la derecha hacia B28.\""
                + "	},"
                + "	{"
                + "		\"_key\": \"gp13b\","
                + "		\"_point\": [48.53479,"
                + "		8.05410],"
                + "		\"_radius\": 200,"
                + "		\"_narrative\": \"Entra en la glorieta y toma la seguna salida.\""
                + "	},"
                + "	{"
                + "		\"_key\": \"gp14\","
                + "		\"_point\": [48.520042,"
                + "		8.088012],"
                + "		\"_radius\": 200,"
                + "		\"_narrative\": \"Entra en la glorieta y toma la salida segunda para seguir en B28.\""
                + "	},	"
                + "	{"
                + "		\"_key\": \"gp18\","
                + "		\"_point\": [48.473907,"
                + "		8.156703],"
                + "		\"_radius\": 200,"
                + "		\"_narrative\": \"Entra en la glorieta y toma la salida tercera (a las 10). Continúe por la L92.\""
                + "	},"
                + "	{"
                + "		\"_key\": \"gp19\","
                + "		\"_point\": [48.474632,"
                + "		8.165143],"
                + "		\"_radius\": 100,"
                + "		\"_narrative\": \"Sigue recto.\""
                + "	},"
                + "	{"
                + "		\"_key\": \"gp20\","
                + "		\"_point\": [48.477558,"
                + "		8.171278],"
                + "		\"_radius\": 100,"
                + "		\"_narrative\": \"Siga recto para ir hacia Alajáiliguenestrase.\""
                + "	},"
                + "	{"
                + "		\"_key\": \"gp21\","
                + "		\"_point\": [48.490665,"
                + "		8.183947],"
                + "		\"_radius\": 100,"
                + "		\"_narrative\": \"Gire a la derecha para mantenerse en K5370.\""
                + "	},	"
                + "	{"
                + "		\"_key\": \"gp24\","
                + "		\"_point\": [48.52807,"
                + "		8.18818],"
                + "		\"_radius\": 200,"
                + "		\"_narrative\": \"Aparcamiento.\""
                + "	}],"
                + "	\"_wayPoints\": [[48.037181,"
                + "	7.866213],"
                + "	[48.037071,"
                + "	7.864346],"
                + "	[48.035907,"
                + "	7.863265],"
                + "	[48.03667,"
                + "	7.86166],"
                + "	[48.042358,"
                + "	7.857281],"
                + "	[48.04499,"
                + "	7.857466],"
                + "	[48.047042,"
                + "	7.859147],"
                + "	[48.048538,"
                + "	7.857346],"
                + "	[48.060157,"
                + "	7.821208],"
                + "	[48.061511,"
                + "	7.814566],"
                + "	[48.062843,"
                + "	7.813796],"
                + "	[48.068881,"
                + "	7.810948],"
                + "	[48.077487,"
                + "	7.806084],"
                + "	[48.09111,"
                + "	7.796404],"
                + "	[48.104541,"
                + "	7.791222],"
                + "	[48.119064,"
                + "	7.786139],"
                + "	[48.135986,"
                + "	7.775972],"
                + "	[48.150585,"
                + "	7.763146],"
                + "	[48.162376,"
                + "	7.755551],"
                + "	[48.172977,"
                + "	7.750996],"
                + "	[48.189655,"
                + "	7.746744],"
                + "	[48.206466,"
                + "	7.745876],"
                + "	[48.221279,"
                + "	7.747916],"
                + "	[48.234111,"
                + "	7.751924],"
                + "	[48.244728,"
                + "	7.756781],"
                + "	[48.257934,"
                + "	7.765147],"
                + "	[48.270782,"
                + "	7.775744],"
                + "	[48.281692,"
                + "	7.783123],"
                + "	[48.2947,"
                + "	7.788433],"
                + "	[48.309185,"
                + "	7.790523],"
                + "	[48.323692,"
                + "	7.790239],"
                + "	[48.341701,"
                + "	7.788765],"
                + "	[48.34988,"
                + "	7.790537],"
                + "	[48.357486,"
                + "	7.794391],"
                + "	[48.364616,"
                + "	7.800324],"
                + "	[48.378837,"
                + "	7.817533],"
                + "	[48.393939,"
                + "	7.839941],"
                + "	[48.401554,"
                + "	7.854006],"
                + "	[48.407379,"
                + "	7.864241],"
                + "	[48.416717,"
                + "	7.877116],"
                + "	[48.426628,"
                + "	7.887413],"
                + "	[48.437347,"
                + "	7.895638],"
                + "	[48.448966,"
                + "	7.901756],"
                + "	[48.45914,"
                + "	7.904867],"
                + "	[48.465599,"
                + "	7.905192],"
                + "	[48.477031,"
                + "	7.902904],"
                + "	[48.488887,"
                + "	7.901542],"
                + "	[48.498031,"
                + "	7.90366],"
                + "	[48.509963,"
                + "	7.910095],"
                + "	[48.518524,"
                + "	7.917113],"
                + "	[48.529682,"
                + "	7.928977],"
                + "	[48.533523,"
                + "	7.934405],"
                + "	[48.534339,"
                + "	7.938301],"
                + "	[48.533721,"
                + "	7.945386],"
                + "	[48.532165,"
                + "	7.957645],"
                + "	[48.529731,"
                + "	7.973367],"
                + "	[48.530181,"
                + "	7.982339],"
                + "	[48.532302,"
                + "	7.992521],"
                + "	[48.533962,"
                + "	8.00628],"
                + "	[48.535449,"
                + "	8.011296],"
                + "	[48.539619,"
                + "	8.018489],"
                + "	[48.541275,"
                + "	8.027948],"
                + "	[48.535617,"
                + "	8.051902],"
                + "	[48.534545,"
                + "	8.05406],"
                + "	[48.531379,"
                + "	8.058796],"
                + "	[48.527133,"
                + "	8.06971],"
                + "	[48.523361,"
                + "	8.077162],"
                + "	[48.521183,"
                + "	8.083902],"
                + "	[48.519851,"
                + "	8.088307],"
                + "	[48.517993,"
                + "	8.097488],"
                + "	[48.51699,"
                + "	8.107913],"
                + "	[48.517974,"
                + "	8.113613],"
                + "	[48.51593,"
                + "	8.120232],"
                + "	[48.514839,"
                + "	8.124552],"
                + "	[48.511775,"
                + "	8.129281],"
                + "	[48.508922,"
                + "	8.134861],"
                + "	[48.506584,"
                + "	8.136683],"
                + "	[48.503211,"
                + "	8.136281],"
                + "	[48.500316,"
                + "	8.138978],"
                + "	[48.495071,"
                + "	8.141698],"
                + "	[48.491485,"
                + "	8.142822],"
                + "	[48.490139,"
                + "	8.146825],"
                + "	[48.486396,"
                + "	8.150271],"
                + "	[48.482292,"
                + "	8.15265],"
                + "	[48.479259,"
                + "	8.153443],"
                + "	[48.474025,"
                + "	8.156425],"
                + "	[48.4738,"
                + "	8.157188],"
                + "	[48.474441,"
                + "	8.163438],"
                + "	[48.475139,"
                + "	8.166655],"
                + "	[48.477558,"
                + "	8.171278],"
                + "	[48.480037,"
                + "	8.179869],"
                + "	[48.481933,"
                + "	8.185947],"
                + "	[48.485755,"
                + "	8.185606],"
                + "	[48.488185,"
                + "	8.184429],"
                + "	[48.48983,"
                + "	8.184809],"
                + "	[48.491542,"
                + "	8.185287],"
                + "	[48.492805,"
                + "	8.186079],"
                + "	[48.494743,"
                + "	8.187022],"
                + "	[48.497505,"
                + "	8.187001],"
                + "	[48.499759,"
                + "	8.183158],"
                + "	[48.500339,"
                + "	8.181733],"
                + "	[48.502223,"
                + "	8.180803],"
                + "	[48.504642,"
                + "	8.180969],"
                + "	[48.511234,"
                + "	8.181719],"
                + "	[48.513339,"
                + "	8.18081],"
                + "	[48.515468,"
                + "	8.18204],"
                + "	[48.517391,"
                + "	8.181847],"
                + "	[48.519451,"
                + "	8.182211],"
                + "	[48.522777,"
                + "	8.184355],"
                + "	[48.525016,"
                + "	8.186346],"
                + "	[48.527599,"
                + "	8.187816],"
                + "	[48.528476,"
                + "	8.188499],"
                + "	[48.526287,"
                + "	8.188147],"
                + "	[48.526424,"
                + "	8.193334],"
                + "	[48.527221,"
                + "	8.196623],"
                + "	[48.527702,"
                + "	8.196699],"
                + "	[48.527202,"
                + "	8.193674],"
                + "	[48.528442,"
                + "	8.191331],"
                + "	[48.530635,"
                + "	8.190711],"
                + "	[48.533058,"
                + "	8.194093],"
                + "	[48.533996,"
                + "	8.197306],"
                + "	[48.534496,"
                + "	8.198625],"
                + "	[48.536231,"
                + "	8.195761],"
                + "	[48.536857,"
                + "	8.195388]]"
                + "}";

        MapView mapView = mock(MapView.class);
        MapTileProviderBase mapTileProviderBase = mock(MapTileProviderBase.class);
        OnlineTileSourceBase onlineTileSourceBase = mock(OnlineTileSourceBase.class);
        when(mapView.getTileProvider()).thenReturn(mapTileProviderBase);
        when(mapTileProviderBase.getTileSource()).thenReturn(onlineTileSourceBase);
        when(onlineTileSourceBase.getBaseUrl()).thenReturn("https://b.tile.openstreetmap.org/");
        when(onlineTileSourceBase.name()).thenReturn("Mapnik");

        final Gson serializer = new GsonBuilder()
                .registerTypeAdapter(IGeoPoint.class, new GeoPointSerializer())
                .create();
        final Route route = serializer.fromJson(jsonRoute, Route.class);

        final String packageName = Objects.requireNonNull(this.getClass().getPackage()).getName();
        final String rootPath = "/data/user/0/com.dpm.quickroutemap/test";
        final String qrmTilesCacheRootPath =  rootPath + "/qrm-tiles";
        final String osmTilesCacheRootPath =  rootPath + "/osm-tiles";

        TilesFetcher tilesFetcher = new TilesFetcher(mapView, packageName, qrmTilesCacheRootPath, osmTilesCacheRootPath);

        final AtomicBoolean finished = new AtomicBoolean(false);
        tilesFetcher.FetchFinished.add((o, args) ->  finished.set(true));
        tilesFetcher.fetchTiles(route, 12, true);

        try {
            while (!finished.get()) //noinspection BusyWait
                Thread.sleep(500);
            File qrmTilesCache = new File(qrmTilesCacheRootPath);
            Assert.assertTrue(
                   String.format("QRM tile cache at '%1$s' should exist.", qrmTilesCacheRootPath),
                   qrmTilesCache.exists());
            Assert.assertTrue(
                   String.format("QRM tile cache at '%1$s' should be not empty",  qrmTilesCacheRootPath),
                   Objects.requireNonNull(qrmTilesCache.list()).length > 0);
        }catch (InterruptedException ex)
        {
            Assert.fail(ex.getMessage());
        }finally {
            if(new File(rootPath).exists())
                Assert.assertTrue(FileHelper.ForceDelete(rootPath));
        }
    }
}
