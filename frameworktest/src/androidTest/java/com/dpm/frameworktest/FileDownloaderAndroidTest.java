package com.dpm.frameworktest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.dpm.framework.FileDownloader;
import com.dpm.framework.FileHelper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Objects;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FileDownloaderAndroidTest {

    //TODO DPM 20230214: mirar por qué no se pueden crear archivos o directorios desde frameworktest
    // pero sí desde los tests de quickRouteMap. ¿Faltan permisos?
    //@Test
    public void givenAPath_writeFile_fileIsWritten(){

        final String rootPath = String.format("/storage/emulated/0/%1$s",
                Objects.requireNonNull(getClass().getPackage()).getName());
        final String path = String.format("%1$s/tiles/12/123/", rootPath);
        final String fileName = "test.txt";
        final String fileContent = "abcde1234";

        File directory = new File(path);
        File file = new File(path + fileName);

        try {
            if (!directory.exists() && !directory.mkdirs())
                Assert.fail(String.format("Cannot create directory '%1$s'.", directory.getAbsolutePath()));
            if(!file.exists() && !file.createNewFile())
                Assert.fail(String.format("Cannot create file '%1$s'.", file.getAbsolutePath()));
            final FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            fileOutputStream.write(fileContent.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();

            Assert.assertTrue(directory.exists());
            Assert.assertTrue(file.exists());
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String readLine = reader.readLine();
            reader.close();

            Assert.assertEquals(fileContent, readLine);
        }catch (Exception ex){
            Assert.fail(ex.getMessage());
        }finally {
            if(new File(rootPath).exists())
                FileHelper.ForceDelete(rootPath);
            if(file.exists())
                Assert.fail(String.format("The file '%1$s' wasn't deleted.", file.getAbsolutePath()));
        }
    }

    @Test
    public void tileUrl_downloadFile_dreatesTileFile() {

        final String  packageName = Objects.requireNonNull(getClass().getPackage()).getName();
        final String rootPath = String.format("/storage/emulated/0/Android/data/%1$s/files/tiles",
                packageName);
        final String filePath = String.format("%1$s/tile-test.png", rootPath);
        final String sourceUrl = "https://b.tile.openstreetmap.org/12/2137/1417.png";

        FileDownloader.download(packageName, sourceUrl, filePath);

        File tileFile = new File(filePath);
        Assert.assertTrue(String.format("The file '%1$s' wasn't created.", filePath), tileFile.exists());

        if(new File(rootPath).exists())
            FileHelper.ForceDelete(rootPath);
    }
}
