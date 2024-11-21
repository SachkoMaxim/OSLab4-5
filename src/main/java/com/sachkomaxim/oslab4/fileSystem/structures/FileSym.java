package com.sachkomaxim.oslab4.fileSystem.structures;

import java.io.Serializable;

public class FileSym extends FileDesc implements Serializable {
    private String value;

    public FileSym(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
