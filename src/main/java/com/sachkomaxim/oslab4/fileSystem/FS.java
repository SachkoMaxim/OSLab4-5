package com.sachkomaxim.oslab4.fileSystem;

import com.sachkomaxim.oslab4.fileSystem.structures.*;
import com.sachkomaxim.oslab4.operatingSystem.Configuration;

import java.io.Serializable;
import java.util.*;

public class FS implements Serializable {
    private final FileDir rootDir = new FileDir(null);
    private final Map<Integer, FileTableEntry> fileDescriptorTable = new HashMap<>();

    public FileDir getRootDir() {
        return rootDir;
    }

    public FileDesc lookup(FileDir dir, String name) {
        return dir.getLinks().get(name);
    }

    public String reverseLookup(FileDir dir, FileDesc desc) {
        for (Map.Entry<String, FileDesc> entry : dir.getLinks().entrySet()) {
            if (entry.getValue() == desc) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void create(FileDir dir, String name) {
        FileDesc newFile = new FileReg();
        newFile.setHardlinkCount(1);
        dir.getLinks().put(name, newFile);
    }

    public void link(FileDir dir, String name, FileDesc dest) {
        dest.setHardlinkCount(dest.getHardlinkCount() + 1);
        dir.getLinks().put(name, dest);
    }

    public void unlink(FileDir dir, String name) {
        FileDesc desc = dir.getLinks().remove(name);
        if (desc != null) {
            desc.setHardlinkCount(desc.getHardlinkCount() - 1);
        }
    }

    public void symlink(FileDir dir, String name, String value) {
        FileDesc newSymlink = new FileSym(value);
        newSymlink.setHardlinkCount(1);
        dir.getLinks().put(name, newSymlink);
    }

    public void mkdir(FileDir dir, String name) {
        FileDesc newDir = new FileDir(dir);
        newDir.setHardlinkCount(1);
        dir.getLinks().put(name, newDir);
    }

    public void rmdir(FileDir dir, String name) {
        FileDesc desc = dir.getLinks().remove(name);
        if (desc != null) {
            desc.setHardlinkCount(desc.getHardlinkCount() - 1);
        }
    }

    public void ls(FileDir directory) {
        for (Map.Entry<String, FileDesc> entry : directory.getLinks().entrySet()) {
            String name = entry.getKey();
            FileDesc desc = entry.getValue();
            System.out.printf("\t%-" + Configuration.NAME_WIDTH + "s => %s", name, desc);
            if (desc instanceof FileSym sym) {
                System.out.printf(" -> \"%s\"", sym.getValue());
            }
            System.out.println();
        }
    }

    public int open(FileReg desc) {
        int fd = 0;
        while (fileDescriptorTable.containsKey(fd)) {
            fd++;
        }
        if (fd == Configuration.MAX_FD) {
            throw new RuntimeException("Too many open files");
        }
        FileTableEntry entry = new FileTableEntry();
        entry.setAccessMode(FileAccess.READ_WRITE);
        entry.setOffset(0);
        entry.setDesc(desc);
        fileDescriptorTable.put(fd, entry);
        return fd;
    }

    public void close(int fd) {
        FileTableEntry entry = fileDescriptorTable.remove(fd);
        if (entry == null) {
            throw new IllegalArgumentException("Invalid file descriptor");
        }
        entry.setReferenceCount(entry.getReferenceCount() - 1);
    }

    public void seek(int fd, int offset) {
        FileTableEntry entry = fileDescriptorTable.get(fd);
        if (entry == null) {
            throw new IllegalArgumentException("Invalid file descriptor");
        }
        if (offset < 0 || offset >= entry.getDesc().getSize()) {
            throw new IndexOutOfBoundsException("Invalid offset");
        }
        entry.setOffset(offset);
    }

    public byte[] read(int fd, int size) {
        FileTableEntry entry = fileDescriptorTable.get(fd);
        if (entry == null) {
            throw new IllegalArgumentException("Invalid file descriptor");
        }
        if (entry.getAccessMode() != FileAccess.READ && entry.getAccessMode() != FileAccess.READ_WRITE) {
            throw new IllegalStateException("File is not opened for reading");
        }
        if (size <= 0 || entry.getDesc().getSize() - entry.getOffset() < size) {
            throw new IllegalArgumentException("Invalid size");
        }
        int bytesToRead = Math.min(size, entry.getDesc().getSize() - entry.getOffset());
        byte[] data = new byte[bytesToRead];
        int index = 0;
        int blockIndex = entry.getOffset() / Configuration.BLOCK_SIZE;
        int blockOffset = entry.getOffset() % Configuration.BLOCK_SIZE;
        while (index < bytesToRead) {
            int bytesToCopy = Math.min(Configuration.BLOCK_SIZE - blockOffset, bytesToRead - index);
            Block block = entry.getDesc().getData().get(blockIndex);
            if (block != null) {
                System.arraycopy(block.getData(), blockOffset, data, index, bytesToCopy);
            } else {
                Arrays.fill(data, index, index + bytesToCopy, (byte) 0);
            }
            index += bytesToCopy;
            blockIndex++;
            blockOffset = 0;
        }
        entry.setOffset(entry.getOffset() + bytesToRead);
        return data;
    }

    public void write(int fd, int size, byte[] data) {
        FileTableEntry entry = fileDescriptorTable.get(fd);
        if (entry == null) {
            throw new IllegalArgumentException("Invalid file descriptor");
        }
        if (entry.getAccessMode() != FileAccess.WRITE && entry.getAccessMode() != FileAccess.READ_WRITE) {
            throw new IllegalStateException("File is not opened for writing");
        }
        if (size <= 0 || size != data.length) {
            throw new IllegalArgumentException("Invalid size or data length");
        }
        int bytesToWrite = size;
        int index = 0;
        int blockIndex = entry.getOffset() / Configuration.BLOCK_SIZE;
        int blockOffset = entry.getOffset() % Configuration.BLOCK_SIZE;
        while (index < bytesToWrite) {
            Block block = entry.getDesc().getData().computeIfAbsent(blockIndex, k -> new Block());
            int bytesToCopy = Math.min(Configuration.BLOCK_SIZE - blockOffset, bytesToWrite - index);
            System.arraycopy(data, index, block.getData(), blockOffset, bytesToCopy);
            index += bytesToCopy;
            blockIndex++;
            blockOffset = 0;
        }
        entry.setOffset(entry.getOffset() + bytesToWrite);
        entry.getDesc().setSize(Math.max(entry.getDesc().getSize(), entry.getOffset()));
    }

    public void truncate(FileReg desc, int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Invalid size");
        }
        int blocksToKeep = (size + Configuration.BLOCK_SIZE - 1) / Configuration.BLOCK_SIZE;
        desc.getData().keySet().removeIf(key -> key >= blocksToKeep);
        desc.setSize(size);
    }
}
