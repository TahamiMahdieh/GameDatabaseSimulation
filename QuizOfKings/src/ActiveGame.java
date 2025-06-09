public class ActiveGame {
    private String email;
    private DatabaseAction da;

    public ActiveGame(String email, DatabaseAction da) {
        this.email = email;
        this.da = da;
    }



    public boolean seeActiveGames() {


        return false;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public DatabaseAction getDa() {
        return da;
    }
    public void setDa(DatabaseAction da) {
        this.da = da;
    }
}
