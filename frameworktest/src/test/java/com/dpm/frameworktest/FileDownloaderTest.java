package com.dpm.frameworktest;


import com.dpm.framework.FileDownloader;
import com.dpm.framework.FileHelper;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Objects;

public class FileDownloaderTest {

    @Test
    public void tileUrl_downloadFile_createsTileFile() {

        final String  packageName = Objects.requireNonNull(getClass().getPackage()).getName();
        final String rootPath = "data-test";
        final String filePath = String.format("%1$s/tile-test.png", rootPath);
        final String sourceUrl = "https://b.tile.openstreetmap.org/12/2137/1417.png";

        FileDownloader.download(packageName, sourceUrl, filePath);

        File tileFile = new File(filePath);
        Assert.assertTrue(String.format("The file '%1$s' wasn't created.", filePath), tileFile.exists());

        if(new File(rootPath).exists())
            FileHelper.ForceDelete(rootPath);
    }
}
