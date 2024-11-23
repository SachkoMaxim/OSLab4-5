package com.sachkomaxim.oslab4.operatingSystem;

import java.io.*;

public class OSStateManager {
    // Saves OS state
    public static void saveState(OS os) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Configuration.SAVE_FILE))) {
            out.writeObject(os); // Writes an OS object to a file
        } catch (IOException e) {
            System.out.println("Error saving state: " + e.getMessage());
        }
    }

    // Loads OS state
    public static OS loadState() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(Configuration.SAVE_FILE))) {
            return (OS) in.readObject(); // Loads the OS object
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading state: " + e.getMessage());
            return null; // If the download failed, return null
        }
    }

    // Clears the state contents
    public static void reset() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Configuration.SAVE_FILE))) {
            // Clearing a file by writing an empty object
            out.writeObject(null); // Writes an empty object to a file
        } catch (IOException e) {
            System.out.println("Error resetting state: " + e.getMessage());
        }
    }
}
