package org.iota.ict.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class IOHelper {

    protected static final Logger LOGGER = LogManager.getLogger("IOHelper");
    protected static final File CODE_SOURCE;

    static {
        CODE_SOURCE = new File(IOHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    }

    public static String readInputStream(InputStream in) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null)
            sb.append(line);
        return sb.toString();
    }

    public static boolean deleteRecursively(File file) {
        if(!file.exists())
            return true;
        boolean success = true;
        LOGGER.info("Deleting " + file + " ...");
        try {
            if(file.isDirectory())
                for(String subPath : file.list())
                    success = success && deleteRecursively(new File(file.getPath(), subPath));
            success = success && file.delete();
        } catch (Throwable t) {
            LOGGER.error("Could not delete " + file.getAbsolutePath(), t);
            success = false;
        }
        return success;
    }

    public static void extractDirectoryFromJarFile(String relativePath, String target) throws IOException {
        JarFile jarFile = new java.util.jar.JarFile(CODE_SOURCE);
        try {
            java.util.Enumeration enumEntries = jarFile.entries();
            LOGGER.info("extracting "+relativePath+" in .jar file to " + target + " ...");
            while (enumEntries.hasMoreElements()) {
                java.util.jar.JarEntry source = (java.util.jar.JarEntry) enumEntries.nextElement();
                if(source.getName().startsWith(relativePath)) {
                    java.io.File destination = new java.io.File(target + java.io.File.separator + source.getName().substring(relativePath.length()));
                    extractFile(jarFile, source, destination);
                }
            }
        } finally {
            jarFile.close();
        }
    }

    private static void extractFile(JarFile jarFile, JarEntry source, File destination) throws IOException {
        LOGGER.info("extracting file: " + source.getName() + " ...");
        if (source.isDirectory()) {
            destination.mkdirs();
            return;
        }
        destination.createNewFile();
        java.io.InputStream is = jarFile.getInputStream(source);
        try {
            java.io.FileOutputStream fos = new java.io.FileOutputStream(destination);
            try {
                while (is.available() > 0)
                    fos.write(is.read());
            } finally {
                fos.close();
            }
        } finally {
            is.close();
        }
    }
}