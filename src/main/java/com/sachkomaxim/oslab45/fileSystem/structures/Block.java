package com.sachkomaxim.oslab45.fileSystem.structures;

import com.sachkomaxim.oslab45.operatingSystem.Configuration;

import java.io.Serializable;

public class Block implements Serializable {
    public final byte[] data = new byte[Configuration.BLOCK_SIZE];

    public Object getData() {
        return data;
    }
}
