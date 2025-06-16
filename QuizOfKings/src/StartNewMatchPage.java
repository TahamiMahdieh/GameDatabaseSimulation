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
        if (da.getUserBanStateByEmail(email)) {
            System.out.println("❌ You are Banned by Manager.");
            HomePage homePage = new HomePage(da, email);
            homePage.showHomePage();
        }
        else {
            System.out.println("Choose an option:");
            System.out.println("1. Random opponent");
            System.out.println("2. Chosen opponent");
            System.out.println("3. Home page");
            System.out.println("4. Exit");
            Scanner s = new Scanner(System.in);
            int option = s.nextInt();
            if (option == 1) {
                System.out.println("Choose the category");
                System.out.println("1. Math");
                System.out.println("2. Sport");
                System.out.println("3. History");
                System.out.println("4. Common knowledge");
                System.out.println("5. Cinema");
                System.out.println("6. Home page");
                System.out.println("7. Exit");
                int option1 = s.nextInt();
                if (option1 < 1 || option1 > 7) {
                    System.out.println("Invalid input. Please try again");
                    startMatch();
                } else {
                    if (option1 == 7) {
                        System.out.println("Good bye!");
                        System.exit(8);
                    } else if (option1 == 6) {
                        HomePage homePage = new HomePage(da, email);
                        homePage.showHomePage();
                    } else {
                        HashMap<Character, Integer> mqr = da.playerNewGame(option1, email, null);
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
                            HomePage homePage = new HomePage(da, email);
                            homePage.showHomePage();
                        } else {
                            System.out.println("Error! Please try again");
                            HomePage homePage = new HomePage(da, email);
                            homePage.showHomePage();
                        }
                    }
                }
            }
            else if (option == 2) {
                System.out.println("Enter the opponent's email:");
                String opponentEmail;
                while (true) {
                    opponentEmail = s.next();
                    if (da.doesEmailExist(opponentEmail) && !opponentEmail.equals(email)) {
                        break;
                    }
                    else if (opponentEmail.equals(email)) {
                        System.out.println("You can't start a match with yourself! Please enter another email.");
                    }
                    else {
                        System.out.println("This email doesn't exist. Please try again.");
                    }
                }
                System.out.println("Choose the category");
                System.out.println("1. Math");
                System.out.println("2. Sport");
                System.out.println("3. History");
                System.out.println("4. Common knowledge");
                System.out.println("5. Cinema");
                System.out.println("6. Home page");
                System.out.println("7. Exit");
                int option1 = s.nextInt();
                if (option1 < 1 || option1 > 7) {
                    System.out.println("Invalid input. Please try again");
                    startMatch();
                } else {
                    if (option1 == 7) {
                        System.out.println("Good bye!");
                        System.exit(8);
                    } else if (option1 == 6) {
                        HomePage homePage = new HomePage(da, email);
                        homePage.showHomePage();
                    } else {
                        HashMap<Character, Integer> mqr = da.playerNewGame(option1, email, opponentEmail);
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
                            HomePage homePage = new HomePage(da, email);
                            homePage.showHomePage();
                        } else {
                            System.out.println("Error! Please try again");
                            HomePage homePage = new HomePage(da, email);
                            homePage.showHomePage();
                        }
                    }
                }
            }
            else if (option == 3) {
                HomePage homePage = new HomePage(da, email);
                homePage.showHomePage();
            }
            else if (option == 4) {
                System.out.println("Good bye!");
                System.exit(9);
            }
            else {
                System.out.println("Invalid input! Please try again");
                startMatch();
            }
        }
    }

}
