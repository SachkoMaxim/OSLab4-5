package com.sachkomaxim.oslab4.fileSystem.structures;

import com.sachkomaxim.oslab4.operatingSystem.Configuration;

import java.util.HashMap;
import java.util.Map;

public class FileDir extends FileDesc {
    private final Map<String, FileDesc> links = new HashMap<>();

    public FileDir(FileDesc parentDesc) {
        links.put(Configuration.NAME_DOT, this);
        links.put(Configuration.NAME_DOT_DOT, parentDesc != null ? parentDesc : this);
    }

    public Map<String, FileDesc> getLinks() {
        return links;
    }
}
