package com.sachkomaxim.oslab4.fileSystem.structures;

import com.sachkomaxim.oslab4.Log;

import java.io.Serializable;

public abstract class FileDesc implements Serializable {
    private int size;
    private int hardlinkCount;

    public FileDesc() {
        Log.logInfo("New FileDesc " + this);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.logInfo("Del FileDesc " + this);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getHardlinkCount() {
        return hardlinkCount;
    }

    public void setHardlinkCount(int hardlinkCount) {
        this.hardlinkCount = hardlinkCount;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ", " + super.hashCode();
    }

    public String toStat() {
        return String.format("id=" + hashCode() +
                            ", type=" + getClass().getSimpleName() +
                            ", nlink=" + hardlinkCount +
                            ", size=" + size +
                            ", nblock=" + 0
        );
    }
}
