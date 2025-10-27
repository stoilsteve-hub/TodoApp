import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TodoApp {
    private static final String FILE_NAME = "tasks.tsv"; // tab-separated
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Scanner in = new Scanner(System.in);

    private static class Task {
        int id;
        String text;
        boolean done;
        LocalDateTime createdAt;

        Task(int id, String text, boolean done, LocalDateTime createdAt) {
            this.id = id;
            this.text = text;
            this.done = done;
            this.createdAt = createdAt;
        }

        String serialize() {
            // replace tabs/newlines to keep TSV intact
            String safe = text.replace("\t", " ").replace("\n", " ");
            return id + "\t" + (done ? "1" : "0") + "\t" + createdAt.format(FMT) + "\t" + safe;
        }

        static Task parse(String line) {
            String[] parts = line.split("\t", 4);
            if (parts.length < 4) return null;
            int id = Integer.parseInt(parts[0]);
            boolean done = "1".equals(parts[1]);
            LocalDateTime ts = LocalDateTime.parse(parts[2], FMT);
            String text = parts[3];
            return new Task(id, text, done, ts);
        }
    }

    private static final List<Task> tasks = new ArrayList<>();
    private static int nextId = 1;

    public static void main(String[] args) {
        load();
        updateNextId();

        while (true) {
            System.out.println("\n--- TO-DO MENU ---");
            System.out.println("1) Add task");
            System.out.println("2) List tasks");
            System.out.println("3) Toggle done/undone");
            System.out.println("4) Delete task");
            System.out.println("5) Save & Exit");
            System.out.print("Choose: ");
            String choice = in.nextLine().trim();

            switch (choice) {
                case "1": addTask(); break;
                case "2": listTasks(); break;
                case "3": toggleTask(); break;
                case "4": deleteTask(); break;
                case "5": save(); System.out.println("👋 Bye!"); return;
                default: System.out.println("❌ Invalid choice.");
            }
        }
    }

    private static void addTask() {
        System.out.print("Task description: ");
        String text = in.nextLine().trim();
        if (text.isEmpty()) {
            System.out.println("⚠️ Empty task ignored.");
            return;
        }
        Task t = new Task(nextId++, text, false, LocalDateTime.now());
        tasks.add(t);
        System.out.println("✅ Added task #" + t.id);
    }

    private static void listTasks() {
        if (tasks.isEmpty()) {
            System.out.println("(No tasks yet)");
            return;
        }
        System.out.println("\nID  Status  Created At          Task");
        System.out.println("-----------------------------------------------");
        for (Task t : tasks) {
            String status = t.done ? "✅" : "❌";
            System.out.printf("%-3d %-6s %-19s %s%n",
                    t.id, status, t.createdAt.format(FMT), t.text);
        }
    }

    private static void toggleTask() {
        int id = askId("Enter task ID to toggle: ");
        Task t = findById(id);
        if (t == null) {
            System.out.println("⚠️ Task not found.");
            return;
        }
        t.done = !t.done;
        System.out.println((t.done ? "✅ Marked done: " : "↩️ Marked undone: ") + "#" + t.id);
    }

    private static void deleteTask() {
        int id = askId("Enter task ID to delete: ");
        Task t = findById(id);
        if (t == null) {
            System.out.println("⚠️ Task not found.");
            return;
        }
        tasks.remove(t);
        System.out.println("🗑️ Deleted task #" + id);
    }

    private static int askId(String prompt) {
        System.out.print(prompt);
        String s = in.nextLine().trim();
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return -1; }
    }

    private static Task findById(int id) {
        for (Task t : tasks) if (t.id == id) return t;
        return null;
    }

    private static void load() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                Task t = Task.parse(line);
                if (t != null) tasks.add(t);
            }
            System.out.println("📂 Loaded " + tasks.size() + " tasks from " + FILE_NAME);
        } catch (IOException e) {
            System.out.println("❌ Error loading: " + e.getMessage());
        }
    }

    private static void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Task t : tasks) bw.write(t.serialize() + System.lineSeparator());
            System.out.println("💾 Saved to " + FILE_NAME);
        } catch (IOException e) {
            System.out.println("❌ Error saving: " + e.getMessage());
        }
    }

    private static void updateNextId() {
        for (Task t : tasks) nextId = Math.max(nextId, t.id + 1);
    }
}
