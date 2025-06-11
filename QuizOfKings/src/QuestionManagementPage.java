import java.util.Scanner;

public class QuestionManagementPage {
    private final String email;
    private final DatabaseAction da;
    private final Scanner s = new Scanner(System.in);

    public QuestionManagementPage(String email, DatabaseAction da) {
        this.email = email;
        this.da = da;
    }

    public void showQuestionManagementPage () {
        HomePage homePage = new HomePage(da, email);
        System.out.println("Choose an option");
        System.out.println("1. See unapproved questions");
        System.out.println("2. See approved questions");
        System.out.println("3. Home page");
        int option = s.nextInt();
        if (option == 1) {
            System.out.println(da.getQuestions(false));
            editApprovalState(true);
            homePage.showHomePage();
        }
        else if (option == 2) {
            System.out.println(da.getQuestions(true));
            editApprovalState(false);
            homePage.showHomePage();
        }
        else if (option == 3) {
            homePage.showHomePage();
        }
        else {
            System.out.println("Invalid input. Please try again");
            showQuestionManagementPage();
        }
    }

    public void editApprovalState(boolean newApprovalState) {
        System.out.println("🔵 Inter the ID of the question you want to change its approval state");
        int id = s.nextInt();
        if (da.changeApprovalState(newApprovalState, id)) {
            System.out.println("Approval updated successfully");
        }
        else {
            System.out.println("Error! Please try later");
        }
    }

}
