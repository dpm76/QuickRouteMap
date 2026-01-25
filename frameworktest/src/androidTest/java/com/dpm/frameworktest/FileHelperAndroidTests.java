package com.dpm.frameworktest;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.dpm.framework.FileHelper;

import org.junit.Assert;
import org.junit.Rule;
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
public class FileHelperAndroidTests {

    @Rule
    public GrantPermissionRule permissionRule = androidx.test.rule.GrantPermissionRule.grant(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    );

    @Test
    public void givenAPath_writeFile_fileIsWritten(){

        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final String rootPath = Objects.requireNonNull(context.getExternalFilesDir(null))
                .getAbsolutePath() + "/testRoot";
        final String path = rootPath + "/tiles/12/123/";
        final String fileName = "test.txt";
        final String fileContent = "abcde1234";

        File directory = new File(path);
        File file = new File(path + fileName);

        try {
            if (!directory.exists() && !directory.mkdirs())
                Assert.fail(String.format("Cannot create directory '%s'.", directory.getAbsolutePath()));
            if(!file.exists() && !file.createNewFile())
                Assert.fail(String.format("Cannot create file '%s'.", file.getAbsolutePath()));
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
                Assert.fail(String.format("The file '%s' wasn't deleted.", file.getAbsolutePath()));
        }
    }
}
