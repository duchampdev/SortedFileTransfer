package duchampdev.sft.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author duchampdev
 */
public class FileUtils {

    private static FileUtils instance;
    private final List<FileCopyObserver> observers = new ArrayList<>();
    private boolean alive = false;


    public static FileUtils getInstance() {
        return instance == null ? instance = new FileUtils() : instance;
    }

    private FileUtils() {

    }

    public void copy(File sourceDir, File targetDir, int recursionDepth, boolean includeSourceRootDir) {
        assert sourceDir != null && sourceDir.isDirectory() && targetDir != null && targetDir.isDirectory();

        File newTarget;
        if (includeSourceRootDir) {
            newTarget = new File(targetDir.getAbsolutePath() + File.separator + sourceDir.getName());
            newTarget.mkdir();
        } else {
            newTarget = targetDir;
        }
        Arrays.stream(sourceDir.listFiles()).sorted(Comparator.comparing(File::getName)).forEachOrdered(f ->
        {
            if (!alive) return; // do not start any further copy process
            if (f.isFile()) {
                try {
                    File targetFile = new File(newTarget.getAbsolutePath() + File.separator + f.getName());
                    boolean targetFileExisted = targetFile.exists();
                    if (!targetFileExisted) {
                        Files.copy(f.toPath(), targetFile.toPath());
                    }
                    notifyFileProcessed(targetFileExisted);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            } else if (f.isDirectory()) {
                copy(f, newTarget, recursionDepth + 1, true);
            }
        });
        if (recursionDepth == 0) {
            setAlive(false);
            notifyFinished();
        }
    }

    public long countFiles(File dir) {
        assert dir != null && dir.isDirectory();
        long count = 0;
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                count++;
            } else if (f.isDirectory()) {
                count += countFiles(f);
            }
        }
        return count;
    }

    /**
     * check if source directory contains target directory
     *
     * @param sourceDir the source directory
     * @param targetDir the target directory
     * @return see above
     */
    public boolean checkTargetLocation(File sourceDir, File targetDir) {
        assert sourceDir.isDirectory() && targetDir.isDirectory();
        if (Arrays.stream(sourceDir.listFiles()).anyMatch(s -> s.getAbsolutePath().equals(targetDir.getAbsolutePath()))) {
            return false;
        } else {
            List<File> childrenDirs = Arrays.stream(sourceDir.listFiles()).filter(File::isDirectory).collect(Collectors.toList());
            if (childrenDirs.isEmpty()) {
                return true; // trivial case
            } else {
                return childrenDirs.stream().allMatch(s -> checkTargetLocation(s, targetDir));
            }
        }
    }

    private void notifyFileProcessed(boolean existed) {
        observers.forEach(o -> o.fileCopied(existed));
    }

    private void notifyFinished() {
        observers.forEach(FileCopyObserver::hasFinished);
    }

    public interface FileCopyObserver {
        public abstract void fileCopied(boolean existed);

        public abstract void hasFinished();
    }

    public void attach(FileCopyObserver o) {
        observers.add(o);
    }

    public void detach(FileCopyObserver o) {
        observers.remove(o);
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
