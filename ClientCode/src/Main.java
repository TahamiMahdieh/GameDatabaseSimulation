
public class Main {
    public static void main(String[] args) {
        DatabaseAction da = new DatabaseAction();
        MainMenu mm = new MainMenu(da);
        mm.showMainMenu();
    }
}