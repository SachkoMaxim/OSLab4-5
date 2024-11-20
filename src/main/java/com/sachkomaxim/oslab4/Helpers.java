package com.sachkomaxim.oslab4;

import com.sachkomaxim.oslab4.fileSystem.structures.*;
import static com.sachkomaxim.oslab4.Log.*;

public class Helpers {

    public static boolean pathExists(FileDir parDir, FileDesc desc, String path) {
        if (parDir == null) {
            logFail("Wrong path '" + path + "'");
            return true;
        }
        if (desc != null) {
            logFail("Link '" + path + "' already exists");
            return true;
        }
        return false;
    }

    public static boolean pathNotExists(FileDir parDir, FileDesc desc, String path) {
        if (parDir == null) {
            logFail("Wrong path '" + path + "'");
            return true;
        }
        if (desc == null) {
            logFail("Link '" + path + "' does not exist");
            return true;
        }
        return false;
    }

    public static boolean isDirectory(FileDesc desc, String path) {
        if (!(desc instanceof FileDir)) {
            logFail("'" + path + "' is not a directory");
            return false;
        }
        return true;
    }

    public static boolean isRegularFile(FileDesc desc, String path) {
        if (!(desc instanceof FileReg)) {
            logFail("'" + path + "' is not a regular file");
            return true;
        }
        return false;
    }

    public static boolean isRootDirectory(String path) {
        return path.equals("/");
    }
}
