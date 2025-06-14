import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class StartNewMatchPage {
    private String email;
    private DatabaseAction da;

    public StartNewMatchPage(String email, DatabaseAction da) {
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
        System.out.println("6. Home page");
        System.out.println("7. Exit");
        int option = s.nextInt();
        if (option < 1 || option > 7) {
            System.out.println("Invalid input. Please try again");
            startMatch();
        }
        else {
            if (option == 7) {
                System.out.println("Good bye!");
                System.exit(8);
            }
            else if (option == 6) {
                HomePage homePage = new HomePage(da, email);
                homePage.showHomePage();
            }
            else {
                HashMap<Character, Integer> mqr = da.playerNewGame(option, email);
                System.out.println("Inter your option:");
                String option1;
                while (true) {
                    option1 = s.next();
                    if (option1.equals("A") || option1.equals("B") || option1.equals("C") || option1.equals("D")) {
                        break;
                    } else {
                        System.out.println("invalid input! please try again");
                    }
                }
                if (da.answerQuestion(email, option1, mqr.get('m'), mqr.get('r'))) {
                    System.out.println("answer saved successfully");
                    HomePage homePage = new HomePage(da, email);
                    homePage.showHomePage();
                }
                else {
                    System.out.println("Error! Please try again");
                    HomePage homePage = new HomePage(da, email);
                    homePage.showHomePage();
                }
            }
        }
    }

}
