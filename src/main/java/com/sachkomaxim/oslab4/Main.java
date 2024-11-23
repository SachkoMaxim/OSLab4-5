package com.sachkomaxim.oslab4;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import com.sachkomaxim.oslab4.operatingSystem.OS;

import static com.sachkomaxim.oslab4.Log.logFail;

public class Main {
    private static OS os = OS.loadState("access");
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Add a hook to handle Ctrl + C (SIGINT) ant etc.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Exiting the system..."); // Message before closing the program
            // os.saveState();
            // Save the OS state before exiting (вирішив закоментувати, щоб самому зберігати те, що хочу)
        }));

        while (true) {
            try {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break; // If the input is closed (for example, via Ctrl + C), we exit the loop
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
        String[] inputWords = input.split("\\s+", 2); // Separate the command and the remaining arguments
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
        commands.put("write", () -> {
            processWriteCommand(remainingArgs);
        });
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
        });
        commands.put("load", () -> {
            os = OS.loadState();
        });
        commands.put("reset", () -> {
            os.resetState();
        });
        commands.put("delete_all", () -> {
            os.deleteAll();
        });
        commands.put("exit", () -> {
            System.exit(0); // Program completion
        });

        return commands.getOrDefault(command, () -> System.out.println("Wrong command or insufficient argument number: " + command));
    }

    private static void processWriteCommand(String remainingArgs) {
        // Separate arguments by space and search for text in ``
        String[] args = remainingArgs.split("\\s+");
        if (args.length < 3) {
            logFail("Invalid arguments for 'write' command");
            return;
        }

        try {
            int fd = Integer.parseInt(args[0]);
            int size = Integer.parseInt(args[1]);

            // Find the text in ``
            String data = extractQuotedData(remainingArgs);
            if (data == null) {
                logFail("Data for 'write' must be enclosed in ``");
                return;
            }

            // Recording
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
        return null; // Return null if there are no even ``
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
