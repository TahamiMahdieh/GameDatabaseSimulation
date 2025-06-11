import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;

public class DatabaseAction {
    private final Connection connection;

    public DatabaseAction() {
        this.connection = DatabaseConnection.getConnection();
    }

    public boolean startNewGame (int category, String email) {
        return false;
    }

    public boolean insertNewQuestion (int category, String Question_Text, String option_A, String option_B, String option_C, String option_D, char correct_option, String email, int difficulty) {
        String query = "INSERT INTO question (C_ID, Question_Text, option_A, option_B, option_C, option_d, correct_option, creator_id, difficulty)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, get_player_id_by_email(?),?)";
        try {
            String difficultyString;
            switch (difficulty) {
                case 1 :
                    difficultyString = "Easy";
                    break;
                case 2 :
                    difficultyString = "Medium";
                    break;
                default:
                    difficultyString = "Hard";
            }
            insertAndGetId(connection, query, category, Question_Text, option_A, option_B, option_C, option_D, String.valueOf(correct_option), email, difficultyString);
            return true;
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public String getQuestions (boolean approval_state1) {
        String query = "SELECT * FROM category NATURAL JOIN question WHERE approval_state = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setBoolean(1, approval_state1);
            ResultSet resultSet = statement.executeQuery();

            StringBuilder result = new StringBuilder();
            boolean hasResults = false;

            while (resultSet.next()) {
                hasResults = true;
                String id = resultSet.getString("Q_ID");
                String category = resultSet.getString("Title");
                String question = resultSet.getString("question_text");
                String A = resultSet.getString("option_A");
                String B = resultSet.getString("option_B");
                String C = resultSet.getString("option_C");
                String D = resultSet.getString("option_D");
                String correct = resultSet.getString("correct_option");
                String difficulty = resultSet.getString("difficulty");

                String row = String.format("ID: " +  id + "| Category: " + category + "| Question: " + question + "| A: " + A + "| B: " + B + "| C: " + C + "| D: " + D + "| Correct Option: " + correct + "| Defficulty: " + difficulty);
                result.append(row).append("\n");
            }

            if (hasResults) {
                return result.toString();
            } else {
                return "No question found";
            }
        } catch (SQLException e) {
            return "An error occurred while fetching questions";
        }
    }

    public String getUsers (boolean banState) {
        String query = "SELECT * FROM player WHERE User_Banned = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setBoolean(1, banState);
            ResultSet resultSet = statement.executeQuery();

            StringBuilder result = new StringBuilder();
            boolean hasResults = false;

            String header = String.format("%-4s %-30s %-60s %-15s", "ID", "Username", "Email", "Sign in date");
            result.append(header).append("\n");
            result.append("----------------------------------------------------------------------------------------------------------------------\n");


            while (resultSet.next()) {
                hasResults = true;
                String id = resultSet.getString("P_ID");
                String username = resultSet.getString("Username");
                String player_email = resultSet.getString("email");
                String signInDate = resultSet.getString("sign_in_date");

                String row = String.format("%-4s %-30s %-60s %-15s", id, username, player_email, signInDate);
                result.append(row).append("\n");
            }

            if (hasResults) {
                return result.toString();
            } else {
                return "No user found";
            }
        } catch (SQLException e) {
            return "An error occurred while fetching users";
        }
    }

    public boolean changeApprovalState(boolean newState, int ID) {
        String query = "UPDATE question SET approval_state = ? WHERE Q_ID = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setBoolean(1, newState);
            stmt.setInt(2, ID);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    public boolean changeBanState(boolean newBanState, int id) {
        String query = "UPDATE player SET user_Banned = ? WHERE P_ID = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setBoolean(1, newBanState);
            stmt.setInt(2, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String seeStatistics(String email) {
        String query = "SELECT Total_Matches_Count, Won_Matches_Count, Accuracy, XP FROM statistics NATURAL JOIN player WHERE Email = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                StringBuilder result = new StringBuilder();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String value = resultSet.getString(i);
                    result.append(columnName).append(": ").append(value);
                    if (i < columnCount) {
                        result.append(", ");
                    }
                }

                return result.toString();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String seeFinishedMatches(String email) {
        String query =
                "SELECT " +
                        "  get_player_username_by_id(matches.p1_id) AS player1, " +
                        "  get_player_username_by_id(matches.p2_id) AS player2, " +
                        "  get_player_username_by_id(matches.winner_id) AS winner, " +
                        "  calculate_player_score_in_match(matches.m_id, player.p_id) AS your_score " +
                        "FROM matches JOIN player ON (matches.p1_id = player.p_id OR matches.p2_id = player.p_id) " +
                        "WHERE matches.Match_Active = false AND player.email = ?;";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            StringBuilder result = new StringBuilder();

            // header
            String header = String.format("%-15s %-15s %-15s %-10s", "Player1", "Player2", "Winner", "Your score");
            result.append(header).append("\n");
            result.append("-------------------------------------------------------------\n");

            boolean hasResults = false;

            while (resultSet.next()) {
                hasResults = true;
                String player1 = resultSet.getString("player1");
                String player2 = resultSet.getString("player2");
                String winner = resultSet.getString("winner");
                String score = resultSet.getString("your_score");

                String row = String.format("%-15s %-15s %-15s %-10s", player1, player2, winner, score);
                result.append(row).append("\n");
            }

            if (hasResults) {
                return result.toString();
            } else {
                return "You haven't finished a match yet";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "An error occurred while fetching finished matches.";
        }
    }

    public boolean signIn (String email, String password){
        String query = "SELECT COUNT(*) FROM player WHERE Email = ? AND Pass = ?";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

            //converting bytes to hex
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            String hashedPassword = sb.toString();


            PreparedStatement statement = connection.prepareStatement(query);// this converts our query to sql code
            statement.setString(1, email);
            statement.setString(2, hashedPassword);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()){
                int count = resultSet.getInt(1);
                return count > 0;
            }
        }
        catch (SQLException | NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean signUp (String username, String password, String email){
        String query = "INSERT INTO player (Username, Email, Pass, Sign_In_Date) VALUES (?, ?, ?, ?)";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());


            //converting bytes to hex
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            String hashedPassword = sb.toString();
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            insertAndGetId(connection, query, username, email, hashedPassword, timestamp);
            return true;
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getUsernameByEmail(String email) throws SQLException {
        String query = "SELECT username FROM player WHERE email = ?;";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("username");
            } else {
                throw new IllegalStateException("No user found with email: " + email);
            }
        }
    }

    public boolean getQuestionManagementAuthorityByEmail(String email){
        String query = "SELECT question_management FROM player NATURAL JOIN authorities WHERE email = ?;";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("question_management");
            } else {
                throw new IllegalStateException("No user found with email: " + email);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void printTopPlayers(String title, String query) {
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\nTop Players - " + title );
            System.out.printf("%-4s | %-15s | %-5s%n", "Rank", "Player ", "Wins");
            System.out.println("------------------------------------------");

            int rank = 1;
            while (rs.next()) {
                String player = rs.getString("username");
                int winCount = rs.getInt("win_count");
                System.out.printf("%-4d | %-15s | %-5d%n", rank++, player, winCount);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean getUserBanAuthorityByEmail(String email){
        String query = "SELECT users_ban FROM player NATURAL JOIN authorities WHERE email = ?;";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("users_ban");
            } else {
                throw new IllegalStateException("No user found with email: " + email);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean doesEmailExist (String email){
        String query = "SELECT COUNT(*) FROM player WHERE email = ?;";
        try {
            PreparedStatement statement = connection.prepareStatement(query);// this converts our query to sql code
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()){
                int count = resultSet.getInt(1);
                return count > 0;
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    private int insertAndGetId(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Insertion failed, no ID obtained.");
                }
            }
        }
    }


}
