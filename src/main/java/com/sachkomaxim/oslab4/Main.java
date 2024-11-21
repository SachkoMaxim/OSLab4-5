package com.sachkomaxim.oslab4;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import com.sachkomaxim.oslab4.operatingSystem.OS;

public class Main {
    private static OS os = new OS();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Додаємо хук для обробки Ctrl + C (SIGINT)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Exiting the system..."); // Повідомлення перед закриттям програми
            // os.saveState();
            // Зберігаємо стан ОС перед виходом
        }));

        while (true) {
            try {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break; // Якщо ввід закрито (наприклад, через Ctrl + C), виходимо з циклу
                }
                String input = scanner.nextLine();
                if (input == null || input.isEmpty()) {
                    continue;
                }
                sendCommand(input);
            } catch (Exception e) {
                System.out.println("Error occurred: " + e.getMessage());
                break;
            }
        }
    }

    private static void sendCommand(String input) {
        String[] inputWords = input.split("\\s+", 2); // Розділяємо команду і решту аргументів
        String command = inputWords[0];
        String remainingArgs = inputWords.length > 1 ? inputWords[1] : "";

        Runnable action = getCommand(command, remainingArgs);
        action.run();
    }

    private static Runnable getCommand(String command, String remainingArgs) {
        Map<String, Runnable> commands = new HashMap<>();

        commands.put("stat", () -> {
            if (!remainingArgs.isEmpty()) os.fstat(remainingArgs);
        });
        commands.put("ls", () -> {
            if (remainingArgs.isEmpty()) os.ls();
            else os.ls(remainingArgs);
        });
        commands.put("create", () -> {
            if (!remainingArgs.isEmpty()) os.create(remainingArgs);
        });
        commands.put("open", () -> {
            if (!remainingArgs.isEmpty()) os.open(remainingArgs);
        });
        commands.put("close", () -> {
            if (!remainingArgs.isEmpty()) os.close(Integer.parseInt(remainingArgs));
        });
        commands.put("seek", () -> {
            String[] args = remainingArgs.split("\\s+");
            if (args.length >= 2) os.seek(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        });
        commands.put("read", () -> {
            String[] args = remainingArgs.split("\\s+");
            if (args.length >= 2) readString(args[0], args[1]);
        });
        commands.put("write", () -> processWriteCommand(remainingArgs));
        commands.put("link", () -> {
            String[] args = remainingArgs.split("\\s+");
            if (args.length >= 2) os.link(args[0], args[1]);
        });
        commands.put("unlink", () -> {
            if (!remainingArgs.isEmpty()) os.unlink(remainingArgs);
        });
        commands.put("truncate", () -> {
            String[] args = remainingArgs.split("\\s+");
            if (args.length >= 2) os.truncate(args[0], Integer.parseInt(args[1]));
        });
        commands.put("mkdir", () -> {
            if (!remainingArgs.isEmpty()) os.mkdir(remainingArgs);
        });
        commands.put("rmdir", () -> {
            if (!remainingArgs.isEmpty()) os.rmdir(remainingArgs);
        });
        commands.put("cd", () -> {
            if (!remainingArgs.isEmpty()) os.cd(remainingArgs);
        });
        commands.put("pwd", os::pwd);
        commands.put("symlink", () -> {
            String[] args = remainingArgs.split("\\s+");
            if (args.length >= 2) os.symlink(args[0], args[1]);
        });
        commands.put("save", () -> {
            os.saveState();
            System.out.println("OS state saved.");
        });
        commands.put("load", () -> {
            os = OS.loadState();
            if (os != null) {
                System.out.println("OS state loaded.");
            } else {
                System.out.println("Failed to load OS state. A new OS instance has been created.");
            }
        });
        commands.put("reset", () -> {
            os.resetState();
        });
        commands.put("delete_all", () -> {
            os.deleteAll();
        });
        commands.put("exit", () -> {
            System.exit(0); // Завершуємо програму
        });

        return commands.getOrDefault(command, () -> System.out.println("Wrong command or insufficient argument number: " + command));
    }

    private static void processWriteCommand(String remainingArgs) {
        // Розділяємо аргументи за пробілом і шукаємо текст у ``.
        String[] args = remainingArgs.split("\\s+");
        if (args.length < 3) {
            System.out.println("Invalid arguments for 'write' command.");
            return;
        }

        try {
            int fd = Integer.parseInt(args[0]);
            int size = Integer.parseInt(args[1]);

            // Знаходимо текст у ``
            String data = extractQuotedData(remainingArgs);
            if (data == null) {
                System.out.println("Data for 'write' must be enclosed in ``.");
                return;
            }

            // Виконуємо запис
            writeString(fd, size, data);
        } catch (NumberFormatException e) {
            System.out.println("Invalid file descriptor or size for 'write' command.");
        }
    }

    private static String extractQuotedData(String input) {
        int startIndex = input.indexOf('`');
        int endIndex = input.lastIndexOf('`');
        if (startIndex != -1 && endIndex > startIndex) {
            return input.substring(startIndex + 1, endIndex);
        }
        return null; // Повертаємо null, якщо немає парних ``.
    }

    private static void readString(String fd, String size) {
        byte[] byteData = os.read(Integer.parseInt(fd), Integer.parseInt(size));
        String strData = new String(byteData, StandardCharsets.UTF_8);
        System.out.println("String data: " + strData);
    }

    private static void writeString(int fd, int size, String data) {
        byte[] byteData = data.getBytes(StandardCharsets.UTF_8);
        os.write(fd, size, byteData);
    }
}
