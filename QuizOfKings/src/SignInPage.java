import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignInPage {
    private final DatabaseActions da;
    private final Scanner s = new Scanner(System.in);

    public SignInPage(DatabaseActions da) {
        this.da = da;
    }

    public void showSignInPage () {
        String password;
        String email;
        System.out.println("Email :");
        email = s.next();
        System.out.println("Password :");
        password = s.next();
        if (da.signIn(email, password)) {
            HomePage hp = new HomePage(da, email);
            hp.showHomePage();
        }
        else {
            System.out.println("Wrong password or email. please try again.");
            System.out.println("1. Try again");
            System.out.println("2. Main menu");
            System.out.println("3. Exit");

            int option = s.nextInt();
            if (option == 1) {
                this.showSignInPage();
            }
            else if (option == 2) {
                MainMenu mm = new MainMenu(da);
                mm.showMainMenu();
            }
            else if (option == 3) {
                System.out.println("Good bye!");
                System.exit(2);
            }
            else {
                System.out.println("Invalid input! Try to sign in again");
                this.showSignInPage();
            }
        }

    }


}
