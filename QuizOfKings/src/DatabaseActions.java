import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;

public class DatabaseActions {
    private final Connection connection;

    public DatabaseActions() {
        this.connection = DatabaseConnection.getConnection();
    }

    public boolean startNewGame (int category, String email) {
        return false;
    }

    public String seeStatistics(String email) {
        String query = "SELECT Total_Matches_Count, Won_Matches_Count, Average_Accuracy, XP FROM statistics NATURAL JOIN player WHERE Email = ?";
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

            // هدر جدول
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

    public void updatePlayerXP(String email) {
        String query = "UPDATE statistics SET xp = calculate_player_xp(p_id) WHERE p_id = get_player_id_by_email(?);";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
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
