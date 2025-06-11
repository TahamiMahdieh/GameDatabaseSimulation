import java.util.Scanner;

public class UserBanPage {
    private final DatabaseAction da;
    private final String email;
    private final Scanner s = new Scanner(System.in);

    public UserBanPage(DatabaseAction da, String email) {
        this.da = da;
        this.email = email;
    }

    public void showBanPage () {
        HomePage homePage = new HomePage(da, email);
        System.out.println("Choose an option");
        System.out.println("1. Banned users");
        System.out.println("2. Allowed users");
        System.out.println("3. Home page");
        int option = s.nextInt();
        if (option == 1) {
            System.out.println(da.getUsers(true));
            editBanState(false);
            homePage.showHomePage();
        }
        else if (option == 2) {
            System.out.println(da.getUsers(false));
            editBanState(true);
            homePage.showHomePage();
        }
        else if (option == 3) {
            homePage.showHomePage();
        }
        else {

        }
    }

    public void editBanState(boolean newBanState) {
        System.out.println("🔵 Inter the ID of the player you want to change their ban state");
        int id = s.nextInt();
        if (da.changeBanState(newBanState, id)) {
            System.out.println("Ban state updated successfully");
        }
        else {
            System.out.println("Error! Please try later");
        }
    }
}










