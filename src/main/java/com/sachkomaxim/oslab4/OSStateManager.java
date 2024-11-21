package com.sachkomaxim.oslab4;

import com.sachkomaxim.oslab4.operatingSystem.Configuration;
import com.sachkomaxim.oslab4.operatingSystem.OS;

import java.io.*;

public class OSStateManager {
    // Зберігає стан системи
    public static void saveState(OS os) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Configuration.SAVE_FILE))) {
            out.writeObject(os); // Записує об'єкт ОС в файл
        } catch (IOException e) {
            System.out.println("Error saving state: " + e.getMessage());
        }
    }

    // Завантажує стан системи
    public static OS loadState() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(Configuration.SAVE_FILE))) {
            return (OS) in.readObject(); // Завантажує об'єкт ОС
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading state: " + e.getMessage());
            return null; // Якщо не вдалося завантажити, повертаємо null
        }
    }

    // Очищає вміст SAVE_FILE
    public static void reset() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Configuration.SAVE_FILE))) {
            // Очищаємо файл, записуючи порожній об'єкт
            out.writeObject(null); // Записує порожній об'єкт в файл
            System.out.println("State reset successfully.");
        } catch (IOException e) {
            System.out.println("Error resetting state: " + e.getMessage());
        }
    }
}
