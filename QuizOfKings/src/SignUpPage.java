import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpPage {
    private DatabaseActions da = new DatabaseActions();
    private Scanner s = new Scanner(System.in);

    public SignUpPage(DatabaseActions da) {
        this.da = da;
    }

    public void showSignUpPage () {
        String email;
        String password;
        String username;
        System.out.println("Username :");
        username = s.next();
        System.out.println("Password :");
        password = s.next();
        while (!isValidPassword(password)) {
            System.out.println("Unsafe password! please use more than 5 characters and at least one number and one punctuation mark");
            System.out.println("Password : ");
            password = s.next();
        }
        System.out.println("Email :");
        email = s.next();
        while (!isValidEmail(email)) {
            System.out.println("Invalid email! please try again");
            System.out.println("Email : ");
            email = s.next();
        }
        if (da.doesEmailExist(email)) {
            System.out.println("This email is already used");
            System.out.println("1. Try to sign up again");
            System.out.println("2. Main menu");
            System.out.println("3. Exit");
            int option = s.nextInt();
            if (option == 1) {
                this.showSignUpPage();
            }
            else if (option == 2) {
                MainMenu mm = new MainMenu(da);
                mm.showMainMenu();
            }
            else if (option == 3) {
                System.out.println("Good bye!");
                System.exit(3);
            }
            else {
                System.out.println("Invalid input! Try to sign in again");
                this.showSignUpPage();
            }
        }
        else {
            if (da.signUp(username, password, email)) {
                HomePage hp = new HomePage(da, email);
                hp.showHomePage();
            }
            else {
                System.out.println("Error. Back to main menu");
                MainMenu mm = new MainMenu(da);
                mm.showMainMenu();
            }
        }
    }

    public static boolean isValidEmail(String email) {
        String regex = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidPassword(String password) {
        // at least includes a number and a punctuation marks and its length is more than 5 characters
        return password.length() > 5 && password.matches(".*\\d.*") && password.matches(".*\\p{Punct}.*");
    }
}
