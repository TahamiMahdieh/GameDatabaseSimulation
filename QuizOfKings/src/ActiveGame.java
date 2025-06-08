public class ActiveGame {
    private String email;
    private DatabaseActions da;

    public ActiveGame(String email, DatabaseActions da) {
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
    public DatabaseActions getDa() {
        return da;
    }
    public void setDa(DatabaseActions da) {
        this.da = da;
    }
}
