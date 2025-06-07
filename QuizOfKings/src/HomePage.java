import java.sql.SQLException;
import java.util.Scanner;

public class HomePage {
    private DatabaseActions da;
    private String email;

    public HomePage(DatabaseActions da, String email) {
        this.da = da;
        this.email = email;
    }

    public void showHomePage () {
        Scanner s = new Scanner(System.in);
        String username;
        boolean userBanAuthority;
        boolean questionManagementAuthority;
        int option;
        try {
            userBanAuthority = da.getUserBanAuthorityByEmail(email);
            questionManagementAuthority = da.getQuestionManagementAuthorityByEmail(email);
            username = da.getUsernameByEmail(email);
            System.out.println("Welcome " + username);
            System.out.println("1. Start a new match");
            System.out.println("2. Continue active matches");
            System.out.println("3. Player statics");
            System.out.println("4. Player Matches");
            System.out.println("5. Global ranking");
            System.out.println("6. Create new question");
            System.out.println("7. Main menu");
            System.out.println("8. Exit");
            int i = 9; // just in case that the number of options will not miss number 9
            if (userBanAuthority) {
                System.out.println(i + ". Banning users");
                i ++;
            }
            if (questionManagementAuthority) {
                System.out.println(i + ". Managing questions");
            }
            option = s.nextInt();
            if (option == 8) {
                System.out.println("Good bye!");
                System.exit(6);
            }
            else if (option == 7) {
                MainMenu mm = new MainMenu(da);
                mm.showMainMenu();
            }
            else if (option == 9 && userBanAuthority) {

            }
            else if (option == 9 && questionManagementAuthority) {

            }
            else if (option == 10 && questionManagementAuthority && userBanAuthority) {

            }
            else if (option == 1){

            }
            else if (option == 2){

            }
            else if (option == 3){

            }
            else if (option == 4){

            }
            else if (option == 5){

            }
            else if (option == 6){

            }
            else {
                System.out.println("Invalid input! please try again.");
                this.showHomePage();
            }


        }
        catch (SQLException e) {
            System.out.println("Error !!!!");
            System.exit(5);
        }


    }
}
