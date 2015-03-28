package nagini.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class NaginiZipUtils {

    private static void innerZip(ZipOutputStream zos,
                                 File srcFile,
                                 String parentPath,
                                 PrintStream stream) throws IOException {
        if(srcFile == null || !srcFile.exists()) {
            return;
        }
        String currentPath = srcFile.getName();
        if(parentPath != null && parentPath.length() != 0) {
            currentPath = parentPath + File.separator + currentPath;
        }
        if(stream != null) {
            stream.println(currentPath);
        }
        if(srcFile.isDirectory()) {
            // zip folder
            int fileCount = 0;
            // list all files and recurse into sub-folders
            for(File file: srcFile.listFiles()) {
                fileCount++;
                innerZip(zos, file, currentPath, stream);
            }
            // create empty folder entry if this is an empty folder
            if(fileCount == 0) {
                zos.putNextEntry(new ZipEntry(currentPath + File.separator));
                zos.closeEntry();
            }
        } else {
            // zip file
            FileInputStream fis = new FileInputStream(srcFile);
            byte[] buffer = new byte[65536];
            int read;
            zos.putNextEntry(new ZipEntry(currentPath));
            while((read = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, read);
            }
            zos.closeEntry();
            fis.close();
        }
    }

    private static void innerUnzip(ZipInputStream zis, String destPath, PrintStream stream)
            throws IOException {
        if(destPath == null) {
            destPath = new String();
        }
        ZipEntry entry = null;
        while((entry = zis.getNextEntry()) != null) {
            String currentPath = destPath + File.separator + entry.getName();
            if(stream != null) {
                stream.println(currentPath);
            }
            File currentFile = new File(currentPath);
            File parentPath = currentFile.getParentFile();
            if(!parentPath.exists()) {
                parentPath.mkdirs();
            }
            if(entry.isDirectory()) {
                currentFile.mkdirs();
            } else {
                FileOutputStream fos = new FileOutputStream(currentFile);

                int read;
                byte data[] = new byte[65536];
                while((read = zis.read(data, 0, 65536)) != -1) {
                    fos.write(data, 0, read);
                }
                fos.flush();
                fos.close();
            }
            zis.closeEntry();
        }
    }

    public static void zip(String src, String dest, PrintStream stream) throws IOException {
        src = src.replace("~", System.getProperty("user.home"));
        dest = dest.replace("~", System.getProperty("user.home"));
        File srcFile = new File(src);
        if(!srcFile.exists()) {
            throw new RuntimeException(src + " does not exist.");
        }
        ZipOutputStream zos = new ZipOutputStream(new CheckedOutputStream(new FileOutputStream(dest),
                                                                          new CRC32()));
        innerZip(zos, srcFile, null, stream);
        zos.flush();
        zos.close();
    }

    public static void unzip(String src, String dest, PrintStream stream) throws IOException {
        src = src.replace("~", System.getProperty("user.home"));
        dest = dest.replace("~", System.getProperty("user.home"));
        if(!new File(src).exists()) {
            throw new RuntimeException(src + " does not exist.");
        }
        ZipInputStream zis = new ZipInputStream(new CheckedInputStream(new FileInputStream(src),
                                                                       new CRC32()));
        innerUnzip(zis, dest, stream);
        zis.close();
    }
}
