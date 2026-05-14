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

    // CSV dokumentet hvor brugere bliver gemt. fandt ud af at append gør at ting ikke bliver slettet eller overwritten
    public static void saveUsers(ArrayList<User> users) {

        try (FileWriter writer = new FileWriter("Data/Users.csv", true)) {

            for (User user : users) {

                writer.write(user.getId() + ";" + user.getUsername() + ";" + user.getPassword() + "\n");
            }

        } catch (IOException e) {
            System.out.println("Error! File can't be written to");
        }
    }

    // Login metode skal måske flyttes
    public static boolean login(ArrayList<User> users,
                                String username,
                                String password) {

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

        return "ID: " + id + " Use";
    }
}