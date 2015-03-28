package nagini.utils;

import java.io.File;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;

public class NaginiGitUtils {

    public static void clone(String uri, String branch, String destPath) throws Exception {
        destPath = destPath.replace("~", System.getProperty("user.home"));
        CloneCommand cmd;
        File dir = new File(destPath);
        if(!dir.getParentFile().exists() || !dir.getParentFile().isDirectory()) {
            throw new RuntimeException("Destination path " + destPath + " is invalid.");
        }
        cmd = Git.cloneRepository().setURI(uri).setBranch(branch).setDirectory(dir);
        System.out.println("Cloning git repository from " + uri + " [" + branch + "] ...");
        cmd.call();
    }
}
