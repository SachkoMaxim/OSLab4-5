package com.sachkomaxim.oslab4.operatingSystem;

import com.sachkomaxim.oslab4.fileSystem.*;
import com.sachkomaxim.oslab4.fileSystem.structures.*;

import java.io.Serializable;
import java.util.*;

import static com.sachkomaxim.oslab4.Helpers.*;
import static com.sachkomaxim.oslab4.Log.*;

public class OS implements Serializable {
    private static final long serialVersionUID = 1L;
    private int symlinkRefCount;
    private final FS fs = new FS();
    private final List<FileDesc> fd = new ArrayList<>();
    private Deque<FileDir> cwdPath = new ArrayDeque<>();

    public OS() {
        setCWD(fs.getRootDir());
    }

    public void saveState() {
        OSStateManager.saveState(this);
        logInfo("OS state saved");
    }

    public static OS loadState() {
        return loadState("");
    }

    public static OS loadState(String string) {
        OS os = OSStateManager.loadState();
        if (os == null) {
            logFail("Failed to load OS state");
            if (Objects.equals(string, "access")) {
                logInfo("A new OS instance has been created");
                return new OS(); // If not loaded, a new instance is created
            } else {
                return null;
            }
        }
        logInfo("Loaded OS state, " + os.getCWD().toString());
        return os;
    }

    public void resetState() {
        OSStateManager.reset();
        logInfo("State reset successfully");
    }

    public void deleteAll() {
        // Check if the user is in the root directory
        String currentPath = getCurrentPath();
        if (!currentPath.equals("/")) {
            cd("/");
        }

        // Delete everything in the root directory except "." and "..'
        FileDir rootDir = fs.getRootDir();
        rootDir.getLinks().entrySet().removeIf(entry -> !entry.getKey().equals(Configuration.NAME_DOT) &&
                !entry.getKey().equals(Configuration.NAME_DOT_DOT));

        logInfo("All files and directories have been deleted, except the root directory");
    }

    public void create(String path) {
        logInfo("Create regular file '" + path + "'");
        var lookupResult = lookup(path, getCWD());
        FileDir parDir = lookupResult.parDir;
        FileDesc desc = lookupResult.desc;
        String name = lookupResult.name;

        if (pathExists(parDir, desc, path)) {
            return;
        }
        fs.create(parDir, name);
    }

    public void link(String path1, String path2) {
        logInfo("Create link '" + path2 + "' to '" + path1 + "'");
        var lookupResult1 = lookup(path1, getCWD(), true);
        FileDir parDir1 = lookupResult1.parDir;
        FileDesc dest = lookupResult1.desc;

        if (pathNotExists(parDir1, dest, path1)) {
            return;
        }

        var lookupResult2 = lookup(path2, getCWD());
        FileDir parDir2 = lookupResult2.parDir;
        FileDesc desc = lookupResult2.desc;
        String name2 = lookupResult2.name;

        if (pathExists(parDir2, desc, path2)) {
            return;
        }

        fs.link(parDir2, name2, dest);
    }

    public void unlink(String path) {
        logInfo("Unlink link '" + path + "'");
        var lookupResult = lookup(path, getCWD(), false);
        FileDir parDir = lookupResult.parDir;
        FileDesc desc = lookupResult.desc;
        String name = lookupResult.name;

        if (pathNotExists(parDir, desc, path) || parDir == null) {
            return;
        }

        fs.unlink(parDir, name);
    }

    public void ls() {
        ls("");
    }

    public void ls(String path) {
        logInfo("List for '" + path + "'");
        FileDesc desc;
        if (path == null || path.isEmpty()) {
            desc = getCWD();
        } else {
            var lookupResult = lookup(path, getCWD());
            FileDir parDir = lookupResult.parDir;
            FileDesc descTmp = lookupResult.desc;
            desc = descTmp;

            if (pathNotExists(parDir, descTmp, path)) {
                return;
            }
        }

        if (isDirectory(desc, path)) {
            fs.ls((FileDir) desc);
        }
    }

    public void fstat(String path) {
        logInfo("File stat for '" + path + "'");
        var lookupResult = lookup(path, getCWD(), false);
        FileDir parDir = lookupResult.parDir;
        FileDesc desc = lookupResult.desc;
        if (pathNotExists(parDir, desc, path)) {
            return;
        }
        System.out.println("\t" + (desc != null ? desc.toStat() : "null"));
    }

    public void symlink(String value, String path) {
        logInfo("Create symlink '" + path + "' -> \"" + value + "\"");
        var lookupResult = lookup(path, getCWD(), false);
        FileDir parDir = lookupResult.parDir;
        FileDesc desc = lookupResult.desc;
        String name = lookupResult.name;
        if (pathExists(parDir, desc, path)) {
            return;
        }
        fs.symlink(parDir, name, value);
    }

    public void mkdir(String path) {
        logInfo("Make directory '" + path + "'");
        var lookupResult = lookup(path, getCWD(), false);
        FileDir parDir = lookupResult.parDir;
        FileDesc desc = lookupResult.desc;
        String name = lookupResult.name;
        if (pathExists(parDir, desc, path)) {
            return;
        }
        fs.mkdir(parDir, name);
    }

    public void rmdir(String path) {
        logInfo("Remove directory '" + path + "'");
        var lookupResult = lookup(path, getCWD());
        FileDir parDir = lookupResult.parDir;
        FileDesc desc = lookupResult.desc;
        String name = lookupResult.name;
        if (pathNotExists(parDir, desc, path)) {
            return;
        }
        if (!isDirectory(desc, path)) {
            return;
        }
        if (desc == fs.getRootDir()) {
            logFail("Cannot remove root directory");
            return;
        }
        FileDir dirToRemove = (FileDir) desc;
        long nonSpecialEntriesCount = dirToRemove.getLinks().entrySet().stream()
                .filter(entry -> !entry.getKey().equals(".") && !entry.getKey().equals(".."))
                .count();

        if (nonSpecialEntriesCount > 0) {
            logFail("Directory '" + path + "' is not empty");
            return;
        }
        fs.rmdir(parDir, name);
        updateCWD();
    }

    public void cd(String path) {
        logInfo("Change CWD to '" + path + "'");
        var lookupResult = lookup(path, getCWD());
        FileDir parDir = lookupResult.parDir;
        FileDesc desc = lookupResult.desc;
        if (pathNotExists(parDir, desc, path)) {
            return;
        }
        if (isDirectory(desc, path)) {
            setCWD((FileDir) desc);
        }
    }

    public void pwd() {
        logInfo("Get CWD canonical absolute path");
        String cwdPath = getCurrentPath();
        logInfo("CWD canonical absolute path '" + cwdPath + "'");
    }

    public int open(String path) {
        logInfo("Open file '" + path + "'");
        var lookupResult = lookup(path, getCWD(), false);
        FileDesc desc = lookupResult.desc;
        if (isRegularFile(desc, path)) {
            return -1;
        }
        int fd;
        try {
            fd = fs.open((FileReg) desc);
        } catch (Exception e) {
            logFail(e.getMessage());
            return -1;
        }
        logInfo("File descriptor '" + fd + "'");
        return fd;
    }

    public void close(int fd) {
        logInfo("Close file with descriptor '" + fd + "'");
        try {
            fs.close(fd);
        } catch (Exception e) {
            logFail(e.getMessage());
        }
    }

    public void seek(int fd, int offset) {
        logInfo("Change file with descriptor '" + fd + "' current position at the offset '" + offset + "'");
        try {
            fs.seek(fd, offset);
        } catch (Exception e) {
            logFail(e.getMessage());
        }
    }

    public byte[] read(int fd, int size) {
        logInfo("Read data from file with descriptor '" + fd + "'");
        try {
            return fs.read(fd, size);
        } catch (Exception e) {
            logFail(e.getMessage());
            return new byte[0];
        }
    }

    public void write(int fd, int size, byte[] data) {
        logInfo("Write data to file with descriptor '" + fd + "'");
        try {
            fs.write(fd, size, data);
        } catch (Exception e) {
            logFail(e.getMessage());
        }
    }

    public void truncate(String path, int size) {
        logInfo("Truncate file '" + path + "'");
        var lookupResult = lookup(path, getCWD(), false);
        FileDesc desc = lookupResult.desc;
        if (isRegularFile(desc, path)) {
            return;
        }
        try {
            fs.truncate((FileReg) desc, size);
        } catch (Exception e) {
            logFail(e.getMessage());
        }
    }

    public String getCurrentPath() {
        StringBuilder cwdPathBuilder = new StringBuilder();
        Queue<FileDir> actualPath = new LinkedList<>(cwdPath);
        while (actualPath.size() > 1) {
            FileDir dir1 = actualPath.poll();
            FileDir dir2 = actualPath.peek();
            String name = fs.reverseLookup(dir2, dir1);
            cwdPathBuilder.insert(0, "/" + name);
        }
        if (cwdPathBuilder.isEmpty()) {
            return "/";
        }
        return cwdPathBuilder.toString();
    }

    private FileDir getCWD() {
        return cwdPath.peek();
    }

    private void setCWD(FileDir fileDir) {
        cwdPath.clear();
        String path = "";
        while (true) {
            var lookupResult1 = lookup(path + Configuration.NAME_DOT, fileDir);
            FileDesc desc1 = lookupResult1.desc;

            path = Configuration.NAME_DOT_DOT + "/" + path;
            var lookupResult2 = lookup(path, fileDir);
            FileDesc desc2 = lookupResult2.desc;

            if (!(desc1 instanceof FileDir dir1) || !(desc2 instanceof FileDir dir2)) {
                throw new RuntimeException("Something went wrong");
            }

            cwdPath.addLast(dir1);

            if (dir1 == dir2) {
                break;
            }
        }
    }

    private void updateCWD() {
        Deque<FileDir> actualPath = new ArrayDeque<>(cwdPath);
        while (actualPath.size() > 1) {
            FileDir dir1 = actualPath.pollFirst();
            FileDir dir2 = actualPath.peekFirst();
            String name = fs.reverseLookup(dir2, dir1);

            if (name == null) {
                logFail("CWD was changed");
                cwdPath = new ArrayDeque<>(actualPath);
            }
        }
    }

    private LookupResult lookup(String path, FileDir cwd) {
        return lookup(path, cwd, true);
    }

    private LookupResult lookup(String path, FileDir cwd, boolean follow) {
        FileDir curDir = path.startsWith("/") ? fs.getRootDir() : cwd;
        FileDir parDir = curDir;

        String[] pathComponents = path.split("/");
        pathComponents = Arrays.stream(pathComponents)
                .filter(s -> !s.isEmpty()) // Filter out empty components
                .toArray(String[]::new);

        if (isRootDirectory(path)) {
            return new LookupResult(fs.getRootDir(), fs.getRootDir(), "");
        }

        String name = pathComponents[pathComponents.length - 1];
        FileDesc desc = null;

        for (int i = 0; i < pathComponents.length; i++) {
            String componentName = pathComponents[i];
            desc = fs.lookup(curDir, componentName);
            parDir = curDir;

            if (desc == null) {
                break;
            }

            if (desc instanceof FileDir) {
                curDir = (FileDir) desc;
            } else if (desc instanceof FileSym sym) {
                if (!follow && i == pathComponents.length - 1) {
                    return new LookupResult(null, null, "");
                }

                String currPath = "/" + String.join("/", Arrays.copyOfRange(pathComponents, i + 1, pathComponents.length));
                String newPath = sym.getValue() + currPath;

                if (symlinkRefCount++ >= Configuration.MAX_SYMLINK_REF_COUNT) {
                    logFail("Symlink maximum redirection count exceeded");
                    return new LookupResult(null, null, "");
                }

                LookupResult result = lookup(newPath, curDir, follow);
                symlinkRefCount = 0;
                return result;
            } else if (desc instanceof FileReg && i != pathComponents.length - 1) {
                return new LookupResult(null, null, "");
            }
        }
        return new LookupResult(parDir, desc, name);
    }

    private static class LookupResult {
        final FileDir parDir;
        final FileDesc desc;
        final String name;

        LookupResult(FileDir parDir, FileDesc desc, String name) {
            this.parDir = parDir;
            this.desc = desc;
            this.name = name;
        }
    }
}
