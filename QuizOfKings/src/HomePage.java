import java.sql.SQLException;
import java.util.Scanner;

public class HomePage {
    private DatabaseAction da;
    private String email;

    public HomePage(DatabaseAction da, String email) {
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
            System.out.println("3. Player statics"); // done
            System.out.println("4. Player finished matches"); // done
            System.out.println("5. Global ranking");
            System.out.println("6. Create new question"); // done
            System.out.println("7. Main menu");  //done
            System.out.println("8. Exit");  // done
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
                UserBanPage userBanPage = new UserBanPage(da, email);
                userBanPage.showBanPage();
            }
            else if ((option == 9 && questionManagementAuthority) || (option == 10 && questionManagementAuthority && userBanAuthority)) {
                QuestionManagementPage qmp = new QuestionManagementPage(email, da);
                qmp.showQuestionManagementPage();
            }
            else if (option == 1){
                StartNewMatch newMatch = new StartNewMatch(email, da);
                newMatch.startMatch();
            }
            else if (option == 2){
                ActiveGame activeGame =new ActiveGame(email, da);
                activeGame.seeActiveGames();
            }
            else if (option == 3){
                System.out.println(da.seeStatistics(email));
                System.out.println("1. Home");
                System.out.println("2. Exit");
                int option1 = s.nextInt();
                if (option1 == 1) {
                    this.showHomePage();
                }
                else if (option1 == 2) {
                    System.out.println("Good bye!");
                    System.exit(7);
                }
                else {
                    System.out.println("Invalid input. Back to home");
                    this.showHomePage();
                }
            }
            else if (option == 4){
                System.out.println(da.seeFinishedMatches(email));
                System.out.println("1. Home");
                System.out.println("2. Exit");
                int option1 = s.nextInt();
                if (option1 == 1) {
                    this.showHomePage();
                }
                else if (option1 == 2) {
                    System.out.println("Good bye!");
                    System.exit(7);
                }
                else {
                    System.out.println("Invalid input. Back to home");
                    this.showHomePage();
                }
            }
            else if (option == 5){
                RankingPage rankingPage = new RankingPage(da);
                rankingPage.showRankingPage();
            }
            else if (option == 6){
                NewQuestionPage newQuestionPage = new NewQuestionPage(email, da);
                newQuestionPage.showNewQuestionPage();
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
