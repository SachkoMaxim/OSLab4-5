package com.sachkomaxim.oslab4.fileSystem.structures;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FileReg extends FileDesc implements Serializable {
    private final Map<Integer, Block> data = new HashMap<>();

    public Map<Integer, Block> getData() {
        return data;
    }

    @Override
    public String toStat() {
        return String.format("id=" + super.hashCode() +
                            ", type=" + getClass().getSimpleName() +
                            ", nlink=" + getHardlinkCount() +
                            ", size=" + getSize() +
                            ", nblock=" + data.size()
        );
    }
}
