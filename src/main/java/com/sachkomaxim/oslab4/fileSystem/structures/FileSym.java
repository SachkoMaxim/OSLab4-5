package com.sachkomaxim.oslab4.fileSystem.structures;

public class FileSym extends FileDesc {
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
