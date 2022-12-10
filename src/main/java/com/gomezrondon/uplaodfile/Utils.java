package com.gomezrondon.uplaodfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {


    public static void zipFiles(List<File> files) {
        // The name of the zip file that you want to create
        String zipFileName = "files.zip";

        try {
            // Create a new zip output stream
            FileOutputStream fos = new FileOutputStream(zipFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            // Loop over the files in the list and add them to the zip file
            for (File file : files) {
                // Create a new zip entry with the file's name
                ZipEntry ze = new ZipEntry(file.getName());

                // Add the zip entry to the zip output stream
                zos.putNextEntry(ze);

                // Create a new input stream for the file and write its contents to the zip file
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                // Close the zip entry and the input stream
                zos.closeEntry();
                fis.close();
            }

            // Close the zip output stream
            zos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
