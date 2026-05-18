import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class User {

    private UUID id;
    private String username;
    private String password;

    private UserProfile profile;
    private WeightTracker tracker;

    // Constructor til nye brugere
    public User(String username, String password) {

        this.id = UUID.randomUUID();
        this.username = username;
        this.password = password;
    }

    // Constructor til brugere læst fra fil
    public User(UUID id, String username, String password) {

        this.id = id;
        this.username = username;
        this.password = password;
    }

    // CSV dokumentet hvor brugere bliver gemt
    public static void saveUsers(User user) {

        try (FileWriter writer = new FileWriter("Data/Users.csv", true)) {


                writer.write(
                            user.getId() + ";" +
                                user.getUsername() + ";" +
                                user.getPassword() + "\n"
                );

        } catch (IOException e) {

            System.out.println("Error! File can't be written to");
        }
    }

    // Register metode
    public void register(ArrayList<User> users) {

        // Tjek om brugernavn allerede findes
        for (User user : users) {

            if (user.getUsername().equalsIgnoreCase(this.username)) {

                System.out.println("Username already exists!");
                return;
            }
        }


        users.add(this);
        saveUsers(this);

        createWeightFile();

        System.out.println("User registered successfully!");
    }

    // Login metode
    public static boolean login(ArrayList<User> users, String username, String password) {

        for (User user : users) {

            if (user.getUsername().equals(username) &&
                    user.getPassword().equals(password)) {

                return true;
            }
        }

        return false;
    }

    //Laver Weight CSV fil for hver ny brugerID der bliver registreret
    private void createWeightFile(){
        String filename = "Data/" + id + "_weights.csv";

        File file = new File(filename);

        try{
            if(file.createNewFile()){
                FileWriter writer = new FileWriter(file);

                writer.write("Date;WeightKg\n");
                writer.close();

                System.out.println("Weight file created: " + file.getName());
            }else{
                System.out.println("Weight file already exists");
            }
        }catch(IOException e){
            System.out.println("Error creating weight file.");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {

        return "ID: " + id +
                " Username: " + username;
    }
}