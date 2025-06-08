import java.util.Scanner;

public class StartNewMatch {
    private String email;
    private DatabaseActions da;

    public StartNewMatch(String email, DatabaseActions da) {
        this.email = email;
        this.da = da;
    }


    public void startMatch () {
        Scanner s = new Scanner(System.in);
        System.out.println("Choose the category");
        System.out.println("1. Math");
        System.out.println("2. Sport");
        System.out.println("3. History");
        System.out.println("4. Common knowledge");
        System.out.println("5. Cinema");
        int option = s.nextInt();
        if (option < 1 || option > 5) {
            System.out.println("Invalid input. Please try again");
            this.startMatch();
        }
        else {
            da.startNewGame(option, email);
        }
    }


    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public DatabaseActions getDa() {
        return da;
    }
    public void setDa(DatabaseActions da) {
        this.da = da;
    }
}
