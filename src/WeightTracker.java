import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class WeightTracker {
    private ArrayList<WeeklyWeighIn> history;

    public WeightTracker(){
        history = new ArrayList<>();
    }

    //Metode til at tilføje Weigh-In og et loop for at sikre user ikke skriver negativt
    public void promptWeighIn(User user){
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n=== Add Weekly Weigh-In ===");

        float weightKg;
        do{
            System.out.println("Enter weight (kg): ");
            weightKg = scanner.nextFloat();

            if(weightKg <= 0){
                System.out.println("Weight must be positive.");
            }
        }while(weightKg <= 0);
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

    //Metode der gemmer Weigh-In i CSV fil der passer til userID
    private void saveWeighIn(User user, WeeklyWeighIn weighIn){

        String filename = "Data/" + user.getId() + "_weights.csv";

        try(FileWriter writer = new FileWriter(filename, true)){
            writer.write(weighIn.getDate() + ";" + weighIn.getWeightKG() + "\n");
        } catch (IOException e) {
            System.out.println("Error saving weigh-in.");
        }
    }

    //Metode til at få History fra CSV fil
    public void loadHistory(User user){
        String filename = "Data/" + user.getId() + "_weights.csv";

        File file = new File(filename);

        if(!file.exists()){
            return;
        }

        try(BufferedReader reader = new BufferedReader(new FileReader(file))){

            String line;

            while((line = reader.readLine()) != null){
                String[] parts = line.split(";");
                LocalDate date = LocalDate.parse(parts[0]);
                float weight = Float.parseFloat(parts[1]);
                history.add(new WeeklyWeighIn(date, weight));
            }
        } catch (Exception e) {
            System.out.println("Error loading history.");
        }
    }
}
