import java.util.Scanner;

public class MainMenu {
    private final DatabaseActions da;
    private final Scanner s = new Scanner(System.in);

    public MainMenu(DatabaseActions da) {
        this.da = da;
    }

    public void showMainMenu () {
        while (true) {
            System.out.println("Welcome to Quiz of kings! please choose one option");
            System.out.println("1. Sign in");
            System.out.println("2. Sign up");
            System.out.println("3. Exit");
            int option1 = s.nextInt();
            if (option1 == 1) {
                SignInPage sip = new SignInPage(da);
                sip.showSignInPage();
            } else if (option1 == 2) {
                SignUpPage sup = new SignUpPage(da);
                sup.showSignUpPage();
            } else if (option1 == 3) {
                System.out.println("Farewell!");
                System.exit(1);
            } else {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }
}
