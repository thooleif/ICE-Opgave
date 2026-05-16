import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.UUID;

public class Menu {

    private Scanner scanner;
    private ArrayList<User> users;
    private User loggedInUser;
    private UserProfile userProfile;
    private FitnessGoal fitnessGoal;
    private TrainingPreference trainingPreference;
    private MacroPlan macroPlan;

    // Stier til CSV-filer - bygges ud fra projektets rod-mappe
    private static final String USERS_FILE = "Data/Users.csv";
    private static final String STATS_FILE = "Data/UserStats.csv";
    private static final String GOALS_FILE = "Data/FitnessGoals.csv";
    private static final String PREFS_FILE = "Data/TrainingPreferences.csv";
    private static final String MACROS_FILE = "Data/MacroPlans.csv";

    // Bruges til at læse en deadline-dato ind fra brugeren
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");




    public Menu() {
        scanner = new Scanner(System.in);
        users = new ArrayList<>();
        loggedInUser = null;
        userProfile = null;
        fitnessGoal = null;
        trainingPreference = null;
        macroPlan = null;

        // Sørg for at Data-mappen og filerne eksisterer DEN ER CRAZY LUKSUS DEN HER BOYs

        ensureFilesExist();

        // Indlæs eksisterende brugere fra CSV når programmet starter
        loadUsers();
    }

    // Opretter Data-mappen og CSV-filerne hvis de ikke findes den er meg afed den her
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

            // Nye filer til mål og præferencer
            File goalsFile = new File(GOALS_FILE);
            if (!goalsFile.exists()) {
                goalsFile.createNewFile();
            }

            File prefsFile = new File(PREFS_FILE);
            if (!prefsFile.exists()) {
                prefsFile.createNewFile();
            }

            // Fil til makroplaner - laves automatisk så load ikke fejler første gang
            File macrosFile = new File(MACROS_FILE);
            if (!macrosFile.exists()) {
                macrosFile.createNewFile();
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

            // Læser mål og præferencer direkte fra klasserne - de styrer selv deres CSV
            // Hvis brugeren ikke har en endnu, tvinger vi dem til at oprette en med det samme
            fitnessGoal = FitnessGoal.load(loggedInUser.getId());
            if (fitnessGoal == null) {
                System.out.println("You haven't set a fitness goal yet. Let's do that now");
                createFitnessGoal();
            }

            trainingPreference = TrainingPreference.load(loggedInUser.getId());
            if (trainingPreference == null) {
                System.out.println("You haven't picked training preferences yet. Let's do that now");
                createTrainingPreference();
            }

            // Makroplan er valgfri - brugeren generere den selv fra menuen
            // Hvis der ligger en på disk så loader vi den så de kan se den med det samme
            macroPlan = MacroPlan.load(loggedInUser.getId());

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


    // Hovedmenuen som vises efter login - profil, mål og præferencer er altid oprettet på dette tidspunkt
    // fordi login() tvinger brugeren til at lave dem hvis de ikke findes
    private void mainMenu() {
        boolean running = true;
        while (running) {

            System.out.println("\n=== Main Menu ===");


            System.out.println("1. View profile");
            System.out.println("2. Update stats");
            System.out.println("3. View fitness goal");
            System.out.println("4. Update fitness goal");
            System.out.println("5. View training preferences");
            System.out.println("6. Update training preferences");
            System.out.println("7. View / generate macro plan");
            System.out.println("8. Logout");
            System.out.print("Choose: ");

            int choice = readInt();
            if (choice == 1) {
                viewProfile();


            } else if (choice == 2) {
                updateStats();
            } else if (choice == 3) {
                viewFitnessGoal();
            } else if (choice == 4) {
                updateFitnessGoal();
            } else if (choice == 5) {
                viewTrainingPreference();
            } else if (choice == 6) {
                updateTrainingPreference();
            } else if (choice == 7) {
                macroPlanMenu();
            } else if (choice == 8) {
                loggedInUser = null;
                userProfile = null;
                fitnessGoal = null;
                trainingPreference = null;
                macroPlan = null;
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


    // Fitness Goal flow - kalder bare FitnessGoal.save/update fra klassen selv


    // Oprettelse af mål - kaldes første gang efter profil, eller hvis brugeren ikke har et endnu
    private void createFitnessGoal() {
        System.out.println("\n=== Set Fitness Goal ===");

        FitnessGoal.GoalType goalType = readGoalType();

        System.out.print("Target weight (kg): ");
        float target = readFloat();

        // Forslår at bruge nuværende vægt som startvægt - hvis brugeren har en profil
        // så de ikke skal skrive den samme tal to gange
        float defaultStart = userProfile != null ? userProfile.getWeightKg() : 0f;
        System.out.print("Start weight (kg) [press enter to use current weight " + defaultStart + "]: ");
        String startInput = scanner.nextLine().trim();
        float start;
        if (startInput.isEmpty()) {
            start = defaultStart;
        } else {
            try {
                start = Float.parseFloat(startInput);
            } catch (NumberFormatException e) {
                start = defaultStart;
            }
        }

        Date deadline = readDate();

        System.out.print("Weekly weight change (kg, fx 0.5 for at tage på, -0.5 for at tabe): ");
        float weekly = readFloat();

        fitnessGoal = new FitnessGoal(goalType, target, start, deadline, weekly);
        fitnessGoal.save(loggedInUser.getId());
        System.out.println("Fitness goal created!");
    }

    private void viewFitnessGoal() {
        if (fitnessGoal == null) {
            System.out.println("No fitness goal set.");
            return;
        }

        System.out.println("\n" + fitnessGoal);
        System.out.printf("Weekly target: %.2f kg%n", fitnessGoal.calculateWeeklyTarget());

        // Vis status ift. nuværende vægt - giver hurtigt et overblik over om man er i mål
        if (userProfile != null) {
            boolean reached = fitnessGoal.isGoalReached(userProfile.getWeightKg());
            if (reached) {
                System.out.println("Status: Goal reached! Godt gået!");
            } else {
                System.out.println("Status: Not there yet - keep going");
            }
        }
    }

    // Lader brugeren opdatere de enkelte felter på målet en ad gangen
    // Samme stil som updateStats - tæt på hinanden hvad gjorde det nemt at copy paste fra
    private void updateFitnessGoal() {
        if (fitnessGoal == null) {
            System.out.println("No fitness goal yet - let's create one.");
            createFitnessGoal();
            return;
        }

        boolean running = true;
        while (running) {

            System.out.println("\n=== Update Fitness Goal ===");
            System.out.println("1. Change goal type");
            System.out.println("2. Update target weight");
            System.out.println("3. Update start weight");
            System.out.println("4. Update deadline");
            System.out.println("5. Update weekly weight change");
            System.out.println("6. Reset entire goal (set everything from scratch)");
            System.out.println("7. Back");
            System.out.print("Choose: ");

            int choice = readInt();
            if (choice == 1) {
                FitnessGoal.GoalType type = readGoalType();
                // Bruger updateGoal til at sætte alt på en gang så vi beholder de andre værdier
                fitnessGoal.updateGoal(type,
                        fitnessGoal.getTargetWeightKg(),
                        fitnessGoal.getStartWeightKg(),
                        fitnessGoal.getDeadlineDate(),
                        fitnessGoal.getWeeklyWeightChangeKg());
                fitnessGoal.update(loggedInUser.getId());
            } else if (choice == 2) {
                System.out.print("New target weight (kg): ");
                float t = readFloat();
                fitnessGoal.updateGoal(fitnessGoal.getGoalType(),
                        t,
                        fitnessGoal.getStartWeightKg(),
                        fitnessGoal.getDeadlineDate(),
                        fitnessGoal.getWeeklyWeightChangeKg());
                fitnessGoal.update(loggedInUser.getId());
            } else if (choice == 3) {
                System.out.print("New start weight (kg): ");
                float s = readFloat();
                fitnessGoal.updateGoal(fitnessGoal.getGoalType(),
                        fitnessGoal.getTargetWeightKg(),
                        s,
                        fitnessGoal.getDeadlineDate(),
                        fitnessGoal.getWeeklyWeightChangeKg());
                fitnessGoal.update(loggedInUser.getId());
            } else if (choice == 4) {
                Date d = readDate();
                fitnessGoal.updateGoal(fitnessGoal.getGoalType(),
                        fitnessGoal.getTargetWeightKg(),
                        fitnessGoal.getStartWeightKg(),
                        d,
                        fitnessGoal.getWeeklyWeightChangeKg());
                fitnessGoal.update(loggedInUser.getId());
            } else if (choice == 5) {
                System.out.print("New weekly weight change (kg): ");
                float w = readFloat();
                fitnessGoal.updateGoal(fitnessGoal.getGoalType(),
                        fitnessGoal.getTargetWeightKg(),
                        fitnessGoal.getStartWeightKg(),
                        fitnessGoal.getDeadlineDate(),
                        w);
                fitnessGoal.update(loggedInUser.getId());
            } else if (choice == 6) {
                // Genbruger createFitnessGoal - den overskriver bare fitnessGoal og gemmer
                createFitnessGoal();
            } else if (choice == 7) {
                running = false;
            } else {
                System.out.println("Invalid choice, try again.");
            }
        }
    }

    // Lille helper der spørger om goal type - bruges flere steder
    private FitnessGoal.GoalType readGoalType() {
        System.out.println("Goal type:");
        System.out.println("  1 = Bulk up (tage på)");
        System.out.println("  2 = Lose weight (tabe sig)");
        System.out.println("  3 = Maintain (holde vægten)");
        System.out.println("  4 = Recomp (tabe fedt og bygge muskel samtidig)");
        System.out.print("Choose: ");
        int c = readInt();

        if (c == 2) {
            return FitnessGoal.GoalType.LOSE_WEIGHT;
        } else if (c == 3) {
            return FitnessGoal.GoalType.MAINTAIN;
        } else if (c == 4) {
            return FitnessGoal.GoalType.RECOMP;
        } else {
            return FitnessGoal.GoalType.BULK_UP;
        }
    }

    // Helper til at læse en dato i format yyyy-MM-dd - falder tilbage til dagens dato hvis input er forkert
    private Date readDate() {
        System.out.print("Deadline (yyyy-MM-dd): ");
        String input = scanner.nextLine().trim();
        try {
            return DATE_FORMAT.parse(input);
        } catch (ParseException e) {
            System.out.println("Invalid date format, using today as deadline.");
            return new Date();
        }
    }



    // Training Preference flow - kalder bare TrainingPreference.save/update


    // Spørger om alle træningspræferencer og laver objektet
    private void createTrainingPreference() {
        System.out.println("\n=== Set Training Preferences ===");

        TrainingPreference.ProgramFocus focus = readProgramFocus();

        // Strength og cardio sættes som default ud fra fokus - så slipper brugeren for at svare på
        // et åbenlyst spørgsmål (en powerlifter vil selvfølgelig have strength = true)
        // Men de bliver alligevel spurgt så de kan vælge begge dele hvis de vil
        boolean defaultStrength = focus != TrainingPreference.ProgramFocus.CARDIO;
        boolean defaultCardio = focus == TrainingPreference.ProgramFocus.CARDIO
                || focus == TrainingPreference.ProgramFocus.GENERAL_LIFESTYLE;

        boolean wantsStrength = readYesNo("Do you want strength training included? (default: " + (defaultStrength ? "yes" : "no") + ")", defaultStrength);
        boolean wantsCardio = readYesNo("Do you want cardio included? (default: " + (defaultCardio ? "yes" : "no") + ")", defaultCardio);

        System.out.print("How many training days per week (1-7): ");
        int days = readInt();
        if (days < 1 || days > 7) {
            System.out.println("Out of range - setting to 3 days as default.");
            days = 3;
        }

        System.out.print("Session duration in minutes (fx 60): ");
        int duration = readInt();
        if (duration <= 0) {
            duration = 60;
        }

        trainingPreference = new TrainingPreference(wantsStrength, wantsCardio, focus, days, duration);
        trainingPreference.save(loggedInUser.getId());
        System.out.println("Training preferences saved!");
    }

    private void viewTrainingPreference() {
        if (trainingPreference == null) {
            System.out.println("No training preferences set.");
            return;
        }

        System.out.println("\n" + trainingPreference);
        // Vis også det anbefalede split så det giver lidt mere værdig at se profilen
        System.out.println("Recommended split: " + trainingPreference.getRecommendedSplit());
    }

    // Update menu for preferences - samme struktur som updateFitnessGoal
    private void updateTrainingPreference() {
        if (trainingPreference == null) {
            System.out.println("No preferences yet - let's create them.");
            createTrainingPreference();
            return;
        }

        boolean running = true;
        while (running) {

            System.out.println("\n=== Update Training Preferences ===");
            System.out.println("1. Change program focus (cardio / powerlifting / bodybuilding / general)");
            System.out.println("2. Toggle strength training");
            System.out.println("3. Toggle cardio");
            System.out.println("4. Update training days per week");
            System.out.println("5. Update session duration");
            System.out.println("6. Reset entire preferences");
            System.out.println("7. Back");
            System.out.print("Choose: ");

            int choice = readInt();
            if (choice == 1) {
                trainingPreference.setProgramFocus(readProgramFocus());
                trainingPreference.update(loggedInUser.getId());
                System.out.println("Program focus updated.");
            } else if (choice == 2) {
                trainingPreference.setWantsStrength(!trainingPreference.getWantsStrength());
                trainingPreference.update(loggedInUser.getId());
                System.out.println("Strength training is now: " + trainingPreference.getWantsStrength());
            } else if (choice == 3) {
                trainingPreference.setWantsCardio(!trainingPreference.getWantsCardio());
                trainingPreference.update(loggedInUser.getId());
                System.out.println("Cardio is now: " + trainingPreference.getWantsCardio());
            } else if (choice == 4) {
                System.out.print("New training days per week (1-7): ");
                int d = readInt();
                if (d < 1 || d > 7) {
                    System.out.println("Out of range - keeping old value.");
                } else {
                    trainingPreference.setTrainingDaysPerWeek(d);
                    trainingPreference.update(loggedInUser.getId());
                    System.out.println("Training days updated.");
                }
            } else if (choice == 5) {
                System.out.print("New session duration (minutes): ");
                int m = readInt();
                if (m <= 0) {
                    System.out.println("Must be positive - keeping old value.");
                } else {
                    trainingPreference.setSessionDurationMin(m);
                    trainingPreference.update(loggedInUser.getId());
                    System.out.println("Session duration updated.");
                }
            } else if (choice == 6) {
                createTrainingPreference();
            } else if (choice == 7) {
                running = false;
            } else {
                System.out.println("Invalid choice, try again.");
            }
        }
    }

    // Macro Plan flow - genererer kalorier og makros ud fra 2026 standarder
    // Bruger Mifflin-St Jeor + ISSN protein guidelines (se MacroPlan.calculateFor)


    // Submenu der lader brugeren generere, se og justere sin makroplan
    private void macroPlanMenu() {

        boolean running = true;
        while (running) {

            System.out.println("\n=== Macro Plan ===");

            if (macroPlan != null) {
                // Vis den nuværende plan med det samme så brugeren ved hvor de står
                System.out.println(macroPlan);
                System.out.println("Split: " + macroPlan.getMacroSplit());
            } else {
                System.out.println("No macro plan yet.");
            }

            System.out.println("\n1. Generate new macro plan (based on current profile, goal, prefs)");
            System.out.println("2. Adjust calories up or down");
            System.out.println("3. Show calculation method");
            System.out.println("4. Back");
            System.out.print("Choose: ");

            int choice = readInt();
            if (choice == 1) {
                // Bygger en ny plan ud fra brugerens nuværende data
                // Smider den gamle ud - hvis brugeren havde justeret manuelt, så er det væk
                macroPlan = MacroPlan.calculateFor(userProfile, fitnessGoal, trainingPreference);
                macroPlan.save(loggedInUser.getId());
                System.out.println("New macro plan generated.");

            } else if (choice == 2) {
                if (macroPlan == null) {
                    System.out.println("Generate a plan first.");
                    continue;
                }
                System.out.print("Adjust calories by (+ to add, - to remove, fx -200): ");
                int delta = readInt();
                macroPlan.adjustCalories(delta);
                macroPlan.update(loggedInUser.getId());

            } else if (choice == 3) {
                if (macroPlan == null) {
                    System.out.println("Generate a plan first.");
                    continue;
                }
                System.out.println("\nHow this was calculated:");
                System.out.println(macroPlan.getCalculationMethod());

            } else if (choice == 4) {
                running = false;
            } else {
                System.out.println("Invalid choice, try again.");
            }
        }
    }


    // Spørger om programfokus - de fire valg fra opgaven
    private TrainingPreference.ProgramFocus readProgramFocus() {
        System.out.println("Program focus:");
        System.out.println("  1 = Cardio");
        System.out.println("  2 = Powerlifting");
        System.out.println("  3 = Bodybuilding");
        System.out.println("  4 = General lifestyle (normal aktiv hverdag)");
        System.out.print("Choose: ");
        int c = readInt();

        if (c == 1) {
            return TrainingPreference.ProgramFocus.CARDIO;
        } else if (c == 2) {
            return TrainingPreference.ProgramFocus.POWERLIFTING;
        } else if (c == 3) {
            return TrainingPreference.ProgramFocus.BODYBUILDING;
        } else {
            return TrainingPreference.ProgramFocus.GENERAL_LIFESTYLE;
        }
    }

    // Helper til ja/nej spørgsmål - tom input giver default-værdien
    private boolean readYesNo(String prompt, boolean defaultValue) {
        System.out.print(prompt + " (y/n): ");
        String input = scanner.nextLine().trim().toLowerCase();
        if (input.isEmpty()) {
            return defaultValue;
        }
        if (input.startsWith("y") || input.startsWith("j")) {
            return true;
        }
        if (input.startsWith("n")) {
            return false;
        }
        return defaultValue;
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