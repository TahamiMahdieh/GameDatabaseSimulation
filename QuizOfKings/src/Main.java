
public class Main {
    public static void main(String[] args) {
        DatabaseActions da = new DatabaseActions();
        MainMenu mm = new MainMenu(da);
        mm.showMainMenu();
    }
}