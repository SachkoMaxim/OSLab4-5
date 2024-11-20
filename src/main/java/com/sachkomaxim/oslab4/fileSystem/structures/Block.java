package com.sachkomaxim.oslab4.fileSystem.structures;

import com.sachkomaxim.oslab4.operatingSystem.Configuration;

public class Block {
    public final byte[] data = new byte[Configuration.BLOCK_SIZE];

    public Object getData() {
        return data;
    }
}
