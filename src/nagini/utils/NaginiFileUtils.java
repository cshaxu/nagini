package nagini.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.common.collect.Lists;

public class NaginiFileUtils {

    public static String getSystemTempPath() {
        return System.getProperty("java.io.tmpdir") + File.separator + "Neko_" + System.nanoTime();
    }

    public static boolean copy(File src, File dest) throws IOException {
        boolean ret = true;
        File destParent = dest.getParentFile();
        if(!destParent.exists()) {
            destParent.mkdirs();
        }
        if(!destParent.exists() || !destParent.isDirectory()) {
            return false;
        }
        if(src.isDirectory()) {
            dest.mkdirs();
            for(File file: src.listFiles()) {
                ret = ret && copy(file, new File(dest.getPath() + File.separator + file.getName()));
            }
        } else {
            FileInputStream fis = new FileInputStream(src);
            FileOutputStream fos = new FileOutputStream(dest);
            byte[] buffer = new byte[65536];
            int read;
            while((read = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, read);
            }
            fos.flush();
            fos.close();
            fis.close();
        }
        return ret;
    }

    public static boolean copy(String src, String dest) throws IOException {
        return copy(new File(src), new File(dest));
    }

    public static boolean delete(File target) {
        if(target == null || !target.exists()) {
            return true;
        }
        if(target.isDirectory()) {
            for(File file: target.listFiles()) {
                delete(file);
            }
        }
        target.delete();
        return !target.exists();
    }

    public static boolean delete(String target) {
        return delete(new File(target));
    }

    public static boolean move(File src, File dest) {
        if(src == null || dest == null || !src.exists() || dest.exists()) {
            return false;
        }
        src.renameTo(dest);
        return dest.exists();
    }

    public static boolean move(String src, String dest) {
        return move(new File(src), new File(dest));
    }

    public static List<String> read(File textFile) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(textFile)));
        List<String> result = Lists.newArrayList();
        String line;
        while((line = br.readLine()) != null) {
            result.add(line);
        }
        br.close();
        return result;
    }

    public static List<String> read(String textFileName) throws IOException {
        return read(new File(textFileName));
    }
}
