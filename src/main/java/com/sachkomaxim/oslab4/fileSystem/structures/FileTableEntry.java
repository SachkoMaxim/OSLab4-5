package com.sachkomaxim.oslab4.fileSystem.structures;

import java.io.Serializable;

public class FileTableEntry implements Serializable {
    private FileReg desc = new FileReg();
    private int offset;
    private FileAccess accessMode; //AccessMode
    private int referenceCount;

    public FileReg getDesc() {
        return desc;
    }

    public void setDesc(FileReg desc) {
        this.desc = desc;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public FileAccess getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(FileAccess accessMode) {
        this.accessMode = accessMode;
    }

    public int getReferenceCount() {
        return referenceCount;
    }

    public void setReferenceCount(int referenceCount) {
        this.referenceCount = referenceCount;
    }
}
