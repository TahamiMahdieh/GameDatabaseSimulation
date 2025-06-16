import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;

public class ActiveMatchesPage {
    private final String email;
    private final DatabaseAction da;

    public ActiveMatchesPage(String email, DatabaseAction da) {
        this.email = email;
        this.da = da;
    }

    public void seeActiveMatchesPage () {
        HomePage homePage = new HomePage(da, email);
        Scanner s = new Scanner(System.in);
        if (da.getUserBanStateByEmail(email)) {
            System.out.println("❌ You are Banned by Manager.");
            homePage.showHomePage();
        }
        else {
            String res = da.seeMatches(email, true);
            System.out.println(res);
            if (res.equals("No match was found")) {
                homePage.showHomePage();
            }
            else {
                int mID;
                System.out.println("🔵 Inter ID of the match that you are willing to continue: ");
                mID = s.nextInt();

                HashMap<Character, Integer> mqr = da.continuingGame(mID, email);
                if (mqr == null) {
                    System.out.println("🔵 It's not your turn");
                    homePage.showHomePage();
                } else {
                    System.out.println("Inter your option:");
                    String option2;
                    while (true) {
                        option2 = s.next();
                        if (option2.equals("A") || option2.equals("B") || option2.equals("C") || option2.equals("D")) {
                            break;
                        } else {
                            System.out.println("invalid input! please try again");
                        }
                    }
                    if (da.answerQuestion(email, option2, mqr.get('m'), mqr.get('r'))) {
                        System.out.println("answer saved successfully");
                        homePage.showHomePage();
                    } else {
                        System.out.println("Error! Please try again");
                        homePage.showHomePage();
                    }
                }
            }
        }
    }
}
