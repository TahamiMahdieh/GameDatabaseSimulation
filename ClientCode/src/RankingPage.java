public class RankingPage {
    private DatabaseAction da;
    private String email;

    public RankingPage(DatabaseAction da, String email) {
        this.email = email;
        this.da = da;
    }

    public void showRankingPage(){
        da.printTopPlayers("Past Week", getWeeklyQuery());
        da.printTopPlayers("Past Month", getMonthlyQuery());
        da.printTopPlayers("All Time", getOverallQuery());
        HomePage homePage = new HomePage(da, email);
        homePage.showHomePage();
    }



    private String getWeeklyQuery() {
        return " SELECT get_player_username_by_id(winner_ID) AS username, COUNT(*) AS win_count FROM Matches " +
                "WHERE (Match_Active = FALSE OR End_Time IS NOT NULL) AND End_Time >= NOW() - INTERVAL 7 DAY " +
                "GROUP BY winner_ID ORDER BY win_count DESC LIMIT 10;";
    }

    private String getMonthlyQuery() {
        return " SELECT get_player_username_by_id(winner_ID) AS username, COUNT(*) AS win_count FROM Matches WHERE (Match_Active = FALSE OR End_Time IS NOT NULL) " +
                "AND End_Time >= NOW() - INTERVAL 1 MONTH GROUP BY winner_ID ORDER BY win_count DESC LIMIT 10;";
    }

    private String getOverallQuery() {
        return " SELECT get_player_username_by_id(winner_ID) AS username, COUNT(*) AS win_count FROM Matches WHERE (Match_Active = FALSE OR End_Time IS NOT NULL) " +
                "GROUP BY winner_ID ORDER BY win_count DESC LIMIT 10; ";
    }

}
