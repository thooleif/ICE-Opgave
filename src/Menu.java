import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

public class Menu {

    private Scanner scanner;
    private ArrayList<User> users;
    private User loggedInUser;
    private UserProfile userProfile;

    // Stier til CSV-filer - bygges ud fra projektets rod-mappe
    private static final String USERS_FILE = "Data/Users.csv";
    private static final String STATS_FILE = "Data/UserStats.csv";




    public Menu() {
        scanner = new Scanner(System.in);
        users = new ArrayList<>();
        loggedInUser = null;
        userProfile = null;

        // Sørg for at Data-mappen og filerne eksisterer
        ensureFilesExist();

        // Indlæs eksisterende brugere fra CSV når programmet starter
        loadUsers();
    }

    // Opretter Data-mappen og CSV-filerne hvis de ikke findes
    private void ensureFilesExist() {
        try {
            File dataDir = new File("Data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            File usersFile = new File(USERS_FILE);
            if (!usersFile.exists()) {
                usersFile.createNewFile();
            }

            File statsFile = new File(STATS_FILE);
            if (!statsFile.exists()) {

                statsFile.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error creating data files: " + e.getMessage());
        }
    }

    // Læser brugere fra CSV-filen så de er tilgængelige ved login
    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 3) {
                    UUID id = UUID.fromString(parts[0]);
                    String username = parts[1];
                    String password = parts[2];
                    users.add(new User(id, username, password));
                }
            }
        } catch (IOException e) {
            System.out.println("No existing users found, starting fresh.");
        }
    }

    // Tjekker om der allerede findes en profil-linje for denne bruger i filen
    // Bruges til at undgå at lave duplikater når profilen gemmes
    private boolean profileExistsInFile(UUID userId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(STATS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length > 0 && parts[0].equals(userId.toString())) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    // Gemmer profilen til UserStats.csv - tjekker først om brugeren allerede har en linje
    // og kalder updateSavedProfile i stedet for at undgå duplikater
    private void saveProfile(UUID userId) {
        if (profileExistsInFile(userId)) {
            updateSavedProfile(userId);
            return;
        }

        try (FileWriter writer = new FileWriter(STATS_FILE, true)) {
            writer.write(
                    userId + ";" +
                            userProfile.getAge() + ";" +
                            userProfile.getGender() + ";" +
                            userProfile.getHeightCm() + ";" +
                            userProfile.getWeightKg() + ";" +
                            userProfile.getExperienceLevel() + ";" +
                            userProfile.getInjuryNotes() + "\n"
            );
            System.out.println("Profile saved.");
        } catch (IOException e) {
            System.out.println("Error saving profile: " + e.getMessage());
        }
    }

    // Overskriver linjen for denne bruger når stats opdateres
    // Læser hele filen ind i en StringBuilder FØRST og skriver bagefter
    // så vi undgår at have både reader og writer åbne samtidig (gav problemer på Windows)
    private void updateSavedProfile(UUID userId) {
        StringBuilder sb = new StringBuilder();
        boolean foundUser = false;

        // Trin 1: Læs hele filen ind i hukommelsen, og erstat brugerens linje
        try (BufferedReader reader = new BufferedReader(new FileReader(STATS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length > 0 && parts[0].equals(userId.toString())) {
                    sb.append(
                            userId + ";" +
                                    userProfile.getAge() + ";" +
                                    userProfile.getGender() + ";" +
                                    userProfile.getHeightCm() + ";" +
                                    userProfile.getWeightKg() + ";" +
                                    userProfile.getExperienceLevel() + ";" +
                                    userProfile.getInjuryNotes() + "\n"
                    );



                    foundUser = true;
                } else {
                    sb.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading profile: " + e.getMessage());
            return;
        }




        // Hvis brugeren ikke fandtes i filen, så tilføj linjen til sidst
        if (!foundUser) {
            sb.append(
                    userId + ";" +
                            userProfile.getAge() + ";" +
                            userProfile.getGender() + ";" +
                            userProfile.getHeightCm() + ";" +
                            userProfile.getWeightKg() + ";" +
                            userProfile.getExperienceLevel() + ";" +
                            userProfile.getInjuryNotes() + "\n"
            );
        }
//
        // Trin 2: Skriv hele filen tilbage (efter reader er lukket)
        try (FileWriter writer = new FileWriter(STATS_FILE, false)) {
            writer.write(sb.toString());


        } catch (IOException e) {
            System.out.println("Error updating profile: " + e.getMessage());
        }
    }

    // Læser profilen for den bruger der er logget ind
    // Returnerer true hvis profilen blev fundet, ellers false
    private boolean loadProfile(UUID userId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(STATS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 6 && parts[0].equals(userId.toString())) {
                    int age = Integer.parseInt(parts[1]);
                    UserProfile.Gender gender = UserProfile.Gender.valueOf(parts[2]);
                    float height = Float.parseFloat(parts[3]);
                    float weight = Float.parseFloat(parts[4]);
                    UserProfile.ExperienceLevel level = UserProfile.ExperienceLevel.valueOf(parts[5]);
                    String injuries = parts.length > 6 ? parts[6] : "";
                    userProfile = new UserProfile(age, gender, height, weight, level, injuries);
                    return true;



                }
            }
        } catch (IOException e) {
            System.out.println("Error loading profile: " + e.getMessage());
        }
        return false;
    }









    // Første menu brugeren ser når programmet startes
    public void start() {
        System.out.println("=== Fitness App ===");

        boolean running = true;
        while (running) {
            System.out.println("\n1. Login");
            System.out.println("2. Create user");
            System.out.println("3. Exit");
            System.out.print("Choose: ");



            int choice = readInt();
            if (choice == 1) {
                login();
            } else if (choice == 2) {
                register();
            } else if (choice == 3) {
                System.out.println("Goodbye!");


                running = false;
            } else {
                System.out.println("Invalid choice, try again.");
            }
        }

        scanner.close();
    }


    // Beder brugeren om brugernavn og kodeord og tjekker om de findes i listen
    // Hvis brugeren ikke har en profil endnu, tvinger vi dem til at oprette en med det samme
    private void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine();



        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (User.login(users, username, password)) {
            loggedInUser = findUserByUsername(username);
            boolean hasProfile = loadProfile(loggedInUser.getId());

            System.out.println("Welcome, " + username + "!");

            // Hvis brugeren ikke har en profil endnu, tving dem til at lave en før hovedmenuen
            if (!hasProfile) {


                System.out.println("Your stats have not been inputted yet. Let's do it now");
                createProfile();
            } else {
                System.out.println("Profile loaded.");
            }

            mainMenu();
        } else {
            System.out.println("Wrong username or password.");
        }
    }

    // Opretter en ny bruger og gemmer den i CSV-filen via User.saveUsers()
    private void register() {
        System.out.print("Choose a username: ");

        String username = scanner.nextLine();


        System.out.print("Choose a password: ");
        String password = scanner.nextLine();



        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                System.out.println("Username already taken, try another.");
                return;
            }


        }

        User newUser = new User(username, password);
        users.add(newUser);

        ArrayList<User> toSave = new ArrayList<>();
        toSave.add(newUser);
        User.saveUsers(toSave);

        System.out.println("User created! You can now log in.");
    }


    // Hovedmenuen som vises efter login - profil er altid oprettet på dette tidspunkt
    // fordi login() tvinger brugeren til at lave en hvis de ikke har en
    private void mainMenu() {
        boolean running = true;
        while (running) {

            System.out.println("\n=== Main Menu ===");


            System.out.println("1. View profile");
            System.out.println("2. Update stats");
            System.out.println("3. Logout");
            System.out.print("Choose: ");

            int choice = readInt();
            if (choice == 1) {
                viewProfile();


            } else if (choice == 2) {
                updateStats();
            } else if (choice == 3) {
                loggedInUser = null;
                userProfile = null;
                System.out.println("You have been logged out.");
                running = false;
            } else {
                System.out.println("Invalid choice, try again.");
            }


        }
    }


    // Samler alle profiloplysninger fra brugeren og opretter et UserProfile-objekt
    private void createProfile() {
        System.out.println("\n=== Create Profile ===");

        System.out.print("Age: ");
        int age = readInt();

        System.out.println("Gender (1 = Male, 2 = Female): ");
        int genderChoice = readInt();
        UserProfile.Gender gender;
        if (genderChoice == 1) {
            gender = UserProfile.Gender.MALE;
        } else {


            gender = UserProfile.Gender.FEMALE;
        }

        System.out.print("Height (cm): ");
        float height = readFloat();

        System.out.print("Weight (kg): ");
        float weight = readFloat();

        System.out.println("Experience level (1 = Beginner, 2 = Novice, 3 = Intermediate, 4 = Advanced): ");
        int levelChoice = readInt();
        UserProfile.ExperienceLevel level;
        if (levelChoice == 2) {
            level = UserProfile.ExperienceLevel.NOVICE;
        } else if (levelChoice == 3) {
            level = UserProfile.ExperienceLevel.INTERMEDIATE;
        } else if (levelChoice == 4) {
            level = UserProfile.ExperienceLevel.ADVANCED;
        } else {
            level = UserProfile.ExperienceLevel.BEGINNER;
        }

        System.out.print("Injury notes (leave blank if none): ");
        String injuryNotes = scanner.nextLine();



        userProfile = new UserProfile(age, gender, height, weight, level, injuryNotes);
        saveProfile(loggedInUser.getId());
        System.out.println("Profile created!");
    }

    private void viewProfile() {
        if (userProfile == null) {
            System.out.println("No profile found.");
            return;
        }

        System.out.println("\n" + userProfile);
        System.out.printf("BMI: %.1f%n", userProfile.calculateBMI());
    }

    // Lader brugeren opdatere individuelle stats efter profilen er oprettet
    private void updateStats() {
        boolean running = true;
        while (running) {

            System.out.println("\n=== Update Stats ===");
            System.out.println("1. Update weight");
            System.out.println("2. Update height");
            System.out.println("3. Update age");
            System.out.println("4. Update experience level");
            System.out.println("5. Update injury notes");
            System.out.println("6. Back");
            System.out.print("Choose: ");


            int choice = readInt();
            if (choice == 1) {
                System.out.print("New weight (kg): ");
                userProfile.setWeightKg(readFloat());
                updateSavedProfile(loggedInUser.getId());
                System.out.println("Weight updated.");
            } else if (choice == 2) {
                System.out.print("New height (cm): ");
                userProfile.setHeightCm(readFloat());
                updateSavedProfile(loggedInUser.getId());
                System.out.println("Height updated.");
            } else if (choice == 3) {

                System.out.print("New age: ");
                userProfile.setAge(readInt());
                updateSavedProfile(loggedInUser.getId());
                System.out.println("Age updated.");
            } else if (choice == 4) {
                System.out.println("Experience level (1 = Beginner, 2 = Novice, 3 = Intermediate, 4 = Advanced): ");
                int levelChoice = readInt();
                if (levelChoice == 2) {
                    userProfile.setExperienceLevel(UserProfile.ExperienceLevel.NOVICE);
                } else if (levelChoice == 3) {
                    userProfile.setExperienceLevel(UserProfile.ExperienceLevel.INTERMEDIATE);
                } else if (levelChoice == 4) {
                    userProfile.setExperienceLevel(UserProfile.ExperienceLevel.ADVANCED);
                } else {
                    userProfile.setExperienceLevel(UserProfile.ExperienceLevel.BEGINNER);
                }
                updateSavedProfile(loggedInUser.getId());
            } else if (choice == 5) {
                System.out.print("Injury notes: ");
                userProfile.setInjuryNotes(scanner.nextLine());
                updateSavedProfile(loggedInUser.getId());
                System.out.println("Injury notes updated.");
            } else if (choice == 6) {
                running = false;
            } else {
                System.out.println("Invalid choice, try again.");
            }
        }


    }


    // Leder listen igennem og returnerer User-objektet der matcher brugernavnet
    private User findUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }


        return null;


    }

    // Bruges til menuvalg - returnerer -1 hvis input ikke er et tal så if/else rammer default
    private int readInt() {
        try {

            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Bruges til højde og vægt - sætter til 0 ved ugyldigt input frem for at crashe
    private float readFloat() {
        try {
            return Float.parseFloat(scanner.nextLine().trim());
        } catch (NumberFormatException e) {


            System.out.println("Invalid number, defaulting to 0.");
            return 0f;
        }

    }

}