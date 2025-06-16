import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class DatabaseAction {
    private final Connection connection;

    public DatabaseAction() {
        this.connection = DatabaseConnection.getConnection();
    }

    public HashMap<Character, Integer> playerNewGame(int category, String email, String opponent) {
        HashMap<Character, Integer> mqr = new HashMap<>();
        if (opponent == null) {
            int m_id = gameWithOnePlayer(category, email);
            if (m_id < 0) {
                int mID = designNewMatch(email, category);
                int rID = Integer.parseInt(getDataAboutMatch(mID, 1, "r_id"));
                int qID = Integer.parseInt(getDataAboutMatch(mID, 1, "q_id"));
                System.out.println(getQuestionsByID(qID));
                mqr.put('m', mID);
                mqr.put('q', qID);
                mqr.put('r', rID);
            } else {
                insertSecondPlayerToMatch(email, m_id);
                int rID = Integer.parseInt(getDataAboutMatch(m_id, 1, "r_id"));
                int qID = Integer.parseInt(getDataAboutMatch(m_id, 1, "q_id"));
                System.out.println(getQuestionsByID(qID));
                mqr.put('m', m_id);
                mqr.put('q', qID);
                mqr.put('r', rID);
                ArrayList<Integer> questionID = getQuestionsByCategory(category);
                // Choose a random question of that category
                int randomIndex = ThreadLocalRandom.current().nextInt(questionID.size());
                int qId2 = questionID.get(randomIndex);
                int rID2 = createNewRound(2);
                createNewR_Q_M(qId2, rID2, m_id);
            }
        }
        else {
            int mID = designNewMatch(email, category);
            insertSecondPlayerToMatch(opponent, mID);
            int rID = Integer.parseInt(getDataAboutMatch(mID, 1, "r_id"));
            int qID = Integer.parseInt(getDataAboutMatch(mID, 1, "q_id"));
            System.out.println(getQuestionsByID(qID));
            mqr.put('m', mID);
            mqr.put('q', qID);
            mqr.put('r', rID);
        }
        return mqr;
    }

    public HashMap<Character, Integer> continuingGame(int mID, String email) {
        HashMap<Character, Integer> mqr = new HashMap<>();
        int player_num = isPlayerOne(email, mID) ? 1 : 2;
        int r_num, r_id;
        String r1 = getDataAboutMatch(mID, 1, "r_id");
        String r2 = getDataAboutMatch(mID, 2, "r_id");
        String r3 = getDataAboutMatch(mID, 3, "r_id");
        if (r2 == null) {
            r_num = 1;
            r_id = Integer.parseInt(r1);
        }
        else if (r3 == null) {
            r_num = 2;
            r_id = Integer.parseInt(r2);
        }
        else {
            r_num = 3;
            r_id = Integer.parseInt(r3);
        }


        if (!isPlayerTurn(r_id, player_num)) {
            return null;
        }
        else {
            if (r2 == null) {
                ArrayList<Integer> questionID = getQuestionsByCategory(Integer.parseInt(getDataAboutMatch(mID, 1, "c_id")));
                // Choose a random question of that category
                int randomIndex = ThreadLocalRandom.current().nextInt(questionID.size());
                int qId = questionID.get(randomIndex);
                int rID = createNewRound(2);
                createNewR_Q_M(qId, rID, mID);
            }
            else if (r3 == null) {
                ArrayList<Integer> questionID = getQuestionsByCategory(Integer.parseInt(getDataAboutMatch(mID, 1, "c_id")));
                // Choose a random question of that category
                int randomIndex = ThreadLocalRandom.current().nextInt(questionID.size());
                int qId = questionID.get(randomIndex);
                int rID = createNewRound(3);
                createNewR_Q_M(qId, rID, mID);
            }
            int qID = Integer.parseInt(getDataAboutMatch(mID, r_num, "q_id"));
            System.out.println(getQuestionsByID(qID));
            mqr.put('m', mID);
            mqr.put('q', qID);
            mqr.put('r', r_id);
            return mqr;
        }
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

    public String getDataAboutMatch (int m_id, int r_num, String attribute) {
        String query = "SELECT * FROM (matches NATURAL JOIN r_q_m NATURAL JOIN question) JOIN round USING (r_id) " +
                "WHERE matches.m_id = ? AND round_num = ? ;";
        try {
            PreparedStatement statement = connection.prepareStatement(query);// this converts our query to sql code
            statement.setInt(1, m_id);
            statement.setInt(2, r_num);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()){
                return resultSet.getString(attribute);
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public String getQuestionsByApprovalState(boolean approval_state1) {
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

    public boolean answerQuestion(String email, String option, int mid, int rid) {
        String query = "SELECT answer_question(?, get_player_id_by_email(?), ?, ?) AS a";
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, mid);
            stmt.setString(2, email);
            stmt.setInt(3, rid);
            stmt.setString(4, option);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()){
                return resultSet.getBoolean("a");
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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

    public void insertSecondPlayerToMatch (String email, int m_id) {
        String query = "UPDATE matches SET p2_id = get_player_id_by_email(?) WHERE M_ID = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setInt(2, m_id);

            int affectedRows = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int designNewMatch(String email, int categoryID) {
        ArrayList<Integer> questionID = getQuestionsByCategory(categoryID);
        // Choose a random question of that category
        int randomIndex = ThreadLocalRandom.current().nextInt(questionID.size());
        int qId = questionID.get(randomIndex);
        int rID = createNewRound(1);
        int mID = createNewMatch(email);
        createNewR_Q_M(qId, rID, mID);
        return mID;
    }

    public ArrayList<Integer> getQuestionsByCategory (int category) {
        ArrayList<Integer> id = new ArrayList<>();
        String query = "SELECT q_id FROM question WHERE c_id = ? AND approval_state = true";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, category);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int q_id = resultSet.getInt("q_id");
                id.add(q_id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;

    }
    public String getQuestionsByID (int id) {
        String query = "SELECT * FROM category NATURAL JOIN question WHERE q_id = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            StringBuilder result = new StringBuilder();

            while (resultSet.next()) {
                String category = resultSet.getString("Title");
                String question = resultSet.getString("question_text");
                String A = resultSet.getString("option_A");
                String B = resultSet.getString("option_B");
                String C = resultSet.getString("option_C");
                String D = resultSet.getString("option_D");
                String correct = resultSet.getString("correct_option");
                String difficulty = resultSet.getString("difficulty");

                String row = String.format("Category: " + category + "| Question: " + question + "| A: " + A + "| B: " + B + "| C: " + C + "| D: " + D + "| Correct Option: " + correct + "| Defficulty: " + difficulty);
                result.append(row).append("\n");
            }
            return result.toString();
        } catch (SQLException e) {
            return "An error occurred while fetching questions";
        }

    }

    public void createNewR_Q_M (int q_id, int r_id, int m_id) {
        String query = "INSERT INTO r_q_m (q_id, r_id, m_id) VALUES (?, ?, ?);";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, q_id);
            statement.setInt(2, r_id);
            statement.setInt(3, m_id);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int createNewRound (int roundNum) {
        int id = 0;
        String query = "INSERT INTO round (round_num, start_time) VALUES (?, CURRENT_TIMESTAMP)";
        try {
            id = insertAndGetId(connection, query, roundNum);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return id;
    }

    public int createNewMatch(String email) {
        int id = 0;
        String query = "INSERT INTO matches (p1_id, start_time) VALUES (get_player_id_by_email(?), CURRENT_TIMESTAMP)";
        try {
            id = insertAndGetId(connection, query, email);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return id;
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

    public String seeMatches(String email, boolean isActive) {
        String query =
                "SELECT " +
                        "  m_id AS ID, " +
                        "  get_player_username_by_id(matches.p1_id) AS player1, " +
                        "  get_player_username_by_id(matches.p2_id) AS player2, " +
                        "  get_player_username_by_id(matches.winner_id) AS winner, " +
                        "  calculate_player_score_in_match(matches.m_id, player.p_id) AS your_score " +
                        "FROM matches JOIN player ON (matches.p1_id = player.p_id OR matches.p2_id = player.p_id) " +
                        "WHERE matches.Match_Active = ? AND player.email = ?;";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setBoolean(1, isActive);
            statement.setString(2, email);
            ResultSet resultSet = statement.executeQuery();

            StringBuilder result = new StringBuilder();

            // header
            String header = String.format("%-4s %-15s %-15s %-15s %-10s", "ID", "Player1", "Player2", "Winner", "Your score");
            result.append(header).append("\n");
            result.append("-------------------------------------------------------------\n");

            boolean hasResults = false;

            while (resultSet.next()) {
                hasResults = true;
                int id = resultSet.getInt("ID");
                String player1 = resultSet.getString("player1");
                String player2 = resultSet.getString("player2");
                String winner = resultSet.getString("winner");
                String score = resultSet.getString("your_score");

                String row = String.format("%-4d %-15s %-15s %-15s %-10s", id, player1, player2 == null ? "_" : player2, winner == null ? "_" : winner, score);
                result.append(row).append("\n");
            }

            if (hasResults) {
                return result.toString();
            } else {
                return "No match was found";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "An error occurred while fetching matches.";
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

    public boolean getUserBanStateByEmail(String email){
        String query = "SELECT user_banned FROM player WHERE email = ?;";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("user_banned");
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

    public int gameWithOnePlayer (int categoryID, String email) {
        String query = "SELECT m_id FROM (matches NATURAL JOIN r_q_m NATURAL JOIN question) JOIN round USING (r_id) " +
                "WHERE matches.p2_id IS NULL AND C_ID = ?  AND p1_id != get_player_id_by_email(?);";
        try {
            PreparedStatement statement = connection.prepareStatement(query);// this converts our query to sql code
            statement.setInt(1, categoryID);
            statement.setString(2, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()){
                return resultSet.getInt("m_id");
            }

        }
        catch (SQLException e){
            return -1;
        }
        return -1;
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

    public boolean isPlayerOne (String email, int mID) {
        String query = "SELECT (p1_id = get_player_id_by_email(?)) AS a FROM matches WHERE m_id = ?;";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            statement.setInt(2, mID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()){
                return resultSet.getBoolean("a");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isPlayerTurn (int r_id, int player_num) {
        String query;
        if (player_num == 1) {
            query = "SELECT p1_answer IS NULL AS a FROM round WHERE r_id = ?;";
        }
        else {
            query = "SELECT p2_answer IS NULL AS a FROM round WHERE r_id = ?;";
        }
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, r_id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()){
                return resultSet.getBoolean("a");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
