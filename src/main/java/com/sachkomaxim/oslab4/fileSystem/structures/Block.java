package com.sachkomaxim.oslab4.fileSystem.structures;

import com.sachkomaxim.oslab4.operatingSystem.Configuration;

import java.io.Serializable;

public class Block implements Serializable {
    public final byte[] data = new byte[Configuration.BLOCK_SIZE];

    public Object getData() {
        return data;
    }
}
