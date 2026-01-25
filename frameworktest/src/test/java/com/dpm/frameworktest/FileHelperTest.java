package com.dpm.frameworktest;

import com.dpm.framework.FileHelper;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileHelperTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void writeAndReadTextFile() throws IOException {
        File file = tempFolder.newFile("test.txt");
        String content = "Hello World";
        FileHelper.writeTextFile(file.getAbsolutePath(), content);

        String readContent = FileHelper.readTextFile(file.getAbsolutePath());
        Assert.assertEquals(content, readContent);
    }

    @Test
    public void writeBinaryFile() throws IOException {
        File file = tempFolder.newFile("test.bin");
        byte[] data = "Binary Data".getBytes(StandardCharsets.UTF_8);
        FileHelper.writeBinaryFile(file.getAbsolutePath(), data);

        String readContent = FileHelper.readTextFile(file.getAbsolutePath());
        Assert.assertEquals("Binary Data", readContent);
    }

    @Test
    public void copyFile() throws IOException {
        File src = tempFolder.newFile("src.txt");
        File dst = new File(tempFolder.getRoot(), "dst.txt");
        FileHelper.writeTextFile(src.getAbsolutePath(), "Copy Me");

        FileHelper.copy(src, dst);

        Assert.assertTrue(dst.exists());
        Assert.assertEquals("Copy Me", FileHelper.readTextFile(dst.getAbsolutePath()));
    }

    @Test
    public void forceDelete_File() throws IOException {
        File file = tempFolder.newFile("toDelete.txt");
        Assert.assertTrue(file.exists());
        Assert.assertTrue(FileHelper.ForceDelete(file.getAbsolutePath()));
        Assert.assertFalse(file.exists());
    }

    @Test
    public void forceDelete_DirectoryRecursively() throws IOException {
        File dir = tempFolder.newFolder("subDir");
        File fileInDir = new File(dir, "inner.txt");
        boolean fileIsCreated = fileInDir.createNewFile();

        Assert.assertTrue(fileIsCreated);
        Assert.assertTrue(dir.exists());
        Assert.assertTrue(fileInDir.exists());

        Assert.assertTrue(FileHelper.ForceDelete(dir.getAbsolutePath()));

        Assert.assertFalse(dir.exists());
    }
}
