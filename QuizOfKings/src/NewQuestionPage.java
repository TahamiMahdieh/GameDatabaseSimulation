import java.util.Scanner;

public class NewQuestionPage {
    private String email;
    private DatabaseAction da;

    public NewQuestionPage(String email, DatabaseAction da) {
        this.email = email;
        this.da = da;
    }

    public void showNewQuestionPage () {
        Scanner s = new Scanner(System.in);
        int option;
        System.out.println("Choose the category");
        System.out.println("1. Math");
        System.out.println("2. Sport");
        System.out.println("3. History");
        System.out.println("4. Common knowledge");
        System.out.println("5. Cinema");
        System.out.println("6. Back to home page");
        option = s.nextInt();
        if (option != 1 && option != 2 && option != 3 && option != 4 && option != 5 && option != 6) {
            System.out.println("Invalid input! please try again");
            this.showNewQuestionPage();
        }
        else if (option == 6) {
            HomePage homePage = new HomePage(da, email);
            homePage.showHomePage();
        }
        else {
            s.nextLine();
            System.out.println("Question text: ");
            String questionText = s.nextLine();
            System.out.println("Option A: ");
            String optionA = s.nextLine();
            System.out.println("Option B: ");
            String optionB = s.nextLine();
            System.out.println("Option C: ");
            String optionC = s.nextLine();
            System.out.println("Option D: ");
            String optionD = s.nextLine();
            System.out.println("Correct option: ");
            char correctOption = s.next().charAt(0);
            System.out.println("Difficulty: 1.Easy  2.Medium  3.Hard");
            int difficulty = s.nextInt();
            while (difficulty != 1 && difficulty != 2 && difficulty != 3) {
                System.out.println("Invalid input. Please try again");
                System.out.println("Difficulty: 1.Easy  2.Medium  3.Hard");
                difficulty = s.nextInt();
            }
            if (da.insertNewQuestion(option, questionText, optionA, optionB, optionC, optionD, correctOption, email, difficulty)){
                System.out.println("Your question is sent successfully");
            }
            else {
                System.out.println("Process failed. Please try again");
            }
            HomePage homePage = new HomePage(da, email);
            homePage.showHomePage();
        }

    }
}
