import db.DatabaseManager;

public class Main {

    // Starter hele fitness-appen ved at lave en ny Menu og kalde start()
    public static void main(String[] args) {
        DatabaseManager.initialize();
        Menu menu = new Menu();
        menu.start();
    }
}