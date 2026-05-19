import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class WeightTracker {
    private final ArrayList<WeeklyWeighIn> history;

    public WeightTracker() {
        history = new ArrayList<>();
    }

    public void promptWeighIn(User user) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n=== Add Weekly Weigh-In ===");

        float weightKg;
        do {
            System.out.println("Enter weight (kg): ");
            weightKg = scanner.nextFloat();

            if (weightKg <= 0) {
                System.out.println("Weight must be positive.");
            }
        } while (weightKg <= 0);
        scanner.nextLine();

        LocalDate date = LocalDate.now();

        WeeklyWeighIn weighIn = new WeeklyWeighIn(date, weightKg);
        history.add(weighIn);
        saveWeighIn(user, weighIn);

        System.out.println("Weigh-In saved!");
    }

    public ArrayList<WeeklyWeighIn> getHistory() {
        return history;
    }

    private void saveWeighIn(User user, WeeklyWeighIn weighIn) {
        String filename = "Data/" + user.getId() + "_weights.csv";

        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.write(weighIn.getDate() + ";" + weighIn.getWeightKg() + "\n");
        } catch (IOException e) {
            System.out.println("Error saving weigh-in.");
        }
    }

    public void loadHistory(User user) {
        history.clear();

        String filename = "Data/" + user.getId() + "_weights.csv";
        File file = new File(filename);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true;

            while ((line = reader.readLine()) != null) {
                if (first) {
                    first = false;
                    if (line.toLowerCase().startsWith("date")) {
                        continue;
                    }
                }
                String[] parts = line.split(";");
                if (parts.length < 2) {
                    continue;
                }
                LocalDate date = LocalDate.parse(parts[0].trim());
                float weight = Float.parseFloat(parts[1].trim());
                history.add(new WeeklyWeighIn(date, weight));
            }
        } catch (Exception e) {
            System.out.println("Error loading history.");
        }
    }

    public void displayHistory() {
        if (history.isEmpty()) {
            System.out.println("No weigh-in found");
            return;
        }
        System.out.println("\n=== Weight History ===");
        for (WeeklyWeighIn weighIn : history) {
            System.out.println(weighIn.getDate() + " | " + weighIn.getWeightKg() + " kg");
        }
    }
}
