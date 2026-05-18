import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class User {

    private UUID id;
    private String username;
    private String password;

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
    public static void saveUsers(ArrayList<User> users) {

        try (FileWriter writer = new FileWriter("Data/Users.csv", true)) {

            for (User user : users) {

                writer.write(user.getId() + ";" + user.getUsername() + ";" + user.getPassword() + "\n");
            }

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
        saveUsers(users);

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