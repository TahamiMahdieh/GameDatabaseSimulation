-- CREATE DATABASE QuizGameDB;
USE QuizGameDB;

CREATE TABLE Authorities (
	A_ID INT AUTO_INCREMENT PRIMARY KEY,
    Question_Management BOOLEAN NOT NULL,
    Users_Ban BOOLEAN NOT NULL
);
INSERT INTO Authorities (Question_Management, Users_Ban) VALUES (false, false);
INSERT INTO Authorities (Question_Management, Users_Ban) VALUES (true, false);
INSERT INTO Authorities (Question_Management, Users_Ban) VALUES (false, true);
INSERT INTO Authorities (Question_Management, Users_Ban) VALUES (true, true);


CREATE TABLE Player (
    P_ID INT AUTO_INCREMENT PRIMARY KEY,
    A_ID INT NOT NULL DEFAULT 1,
    Username VARCHAR(50) NOT NULL,
    Email VARCHAR(100) NOT NULL UNIQUE,
    Pass VARCHAR(255) NOT NULL,
    User_Banned BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (A_ID) REFERENCES Authorities(A_ID),
    Sign_In_Date DATE
);

CREATE TABLE Category (
	C_ID INT AUTO_INCREMENT PRIMARY KEY,
    Title VARCHAR(50) NOT NULL UNIQUE
);


CREATE TABLE Question (
    Q_ID INT AUTO_INCREMENT PRIMARY KEY,
    C_ID INT NOT NULL,
    Question_Text TEXT NOT NULL,
    Option_A VARCHAR(100) NOT NULL,
    Option_B VARCHAR(100) NOT NULL,
    Option_C VARCHAR(100) NOT NULL,
    Option_D VARCHAR(100) NOT NULL,
    Correct_Option CHAR NOT NULL,
    Creator_ID INT NOT NULL,
    Difficulty VARCHAR(10) NOT NULL,
    Approval_State BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (Creator_ID) REFERENCES player (P_ID),
    FOREIGN KEY (C_ID) REFERENCES Category (C_ID),
    CHECK (Correct_Option IN ('A', 'B', 'C', 'D')),
    CHECK (Difficulty IN ('Easy', 'Medium', 'Hard'))
);


CREATE TABLE Round (
	R_ID INT AUTO_INCREMENT PRIMARY KEY,
    Round_Num INT NOT NULL,
    P1_Answer CHAR,
    P2_Answer CHAR,
    Start_Time TIMESTAMP NOT NULL,
    End_Time TIMESTAMP, -- this can be null. if it has been more than a day since the start of the round, the match gets expired.
    CHECK (P1_Answer IN ('A', 'B', 'C', 'D')), 
    CHECK (P2_Answer IN ('A', 'B', 'C', 'D')),
    CHECK (Round_Num > 0 AND Round_Num < 7)
);

CREATE TABLE Matches (
	M_ID INT AUTO_INCREMENT PRIMARY KEY,
    P1_ID INT NOT NULL,
    P2_ID INT,
    winner_ID INT DEFAULT NULL,
    Start_Time TIMESTAMP NOT NULL, 
    End_Time TIMESTAMP, 
    Match_Active BOOLEAN NOT NULL DEFAULT TRUE,
    CHECK (P1_ID != P2_ID),
    FOREIGN KEY (P1_ID) REFERENCES player (P_ID),
    FOREIGN KEY (P2_ID) REFERENCES player (P_ID),
    FOREIGN KEY (winner_ID) REFERENCES player (P_ID)
);


CREATE TABLE R_Q_M (
	Q_ID INT,
	R_ID INT,
    M_ID INT,
	PRIMARY KEY (Q_ID, M_ID, R_ID),
	FOREIGN KEY (Q_ID) REFERENCES Question (Q_ID),
	FOREIGN KEY (R_ID) REFERENCES Round (R_ID),
    FOREIGN KEY (M_ID) REFERENCES Matches (M_ID)
);


CREATE TABLE Statistics (
	S_ID INT AUTO_INCREMENT PRIMARY KEY,
    P_ID INT UNIQUE NOT NULL, 
    Total_Matches_Count INT NOT NULL DEFAULT 0, 
    Won_Matches_Count INT NOT NULL DEFAULT 0, 
	Accuracy FLOAT NOT NULL DEFAULT 1,
	XP INT NOT NULL DEFAULT 50,
    FOREIGN KEY (P_ID) REFERENCES Player (P_ID) ON DELETE CASCADE
);


-- ------------------------------------------------------------------

DELIMITER $$
CREATE TRIGGER update_match_status BEFORE UPDATE ON Matches 
FOR EACH ROW 
BEGIN 
    IF NEW.End_Time IS NOT NULL THEN
        SET NEW.Match_Active = false;
    END IF;
END $$
DELIMITER ; 


DELIMITER $$
CREATE TRIGGER update_round_status BEFORE UPDATE ON round 
FOR EACH ROW 
BEGIN 
    IF NEW.p2_answer IS NOT NULL AND NEW.p1_answer IS NOT NULL THEN
        SET NEW.end_time = NOW();
    END IF;
END $$
DELIMITER ; 


DELIMITER $$
CREATE TRIGGER update_match_status_by_round AFTER UPDATE ON round
FOR EACH ROW
BEGIN
    DECLARE matchId INT;
    DECLARE p1 INT;
    DECLARE p2 INT;

    IF NEW.round_num = 3 AND NEW.p1_answer IS NOT NULL AND NEW.p2_answer IS NOT NULL THEN
        SELECT m_id INTO matchId FROM r_q_m WHERE r_id = NEW.r_id LIMIT 1;

        IF matchId IS NOT NULL THEN
            SELECT p1_id, p2_id INTO p1, p2 FROM matches
            WHERE m_id = matchId LIMIT 1;

		
            UPDATE matches
            SET match_active = FALSE, end_time = NOW()
            WHERE m_id = matchId;

		
            UPDATE matches
            SET winner_id = CASE 
                WHEN calculate_player_score_in_match(matchId, p1) > calculate_player_score_in_match(matchId, p2) THEN p1
                WHEN calculate_player_score_in_match(matchId, p1) < calculate_player_score_in_match(matchId, p2) THEN p2
                ELSE NULL 
            END
            WHERE m_id = matchId;
        END IF;

    END IF;
END $$

DELIMITER ;

 


DELIMITER $$
CREATE TRIGGER update_statistics 
BEFORE UPDATE ON Matches 
FOR EACH ROW 
BEGIN 
  IF NEW.End_Time IS NOT NULL THEN

    -- player 1
    UPDATE statistics SET 
      xp = calculate_player_xp(NEW.P1_ID),
      Won_Matches_Count = calculate_won_matches_count(NEW.P1_ID),
      Total_Matches_Count = calculate_total_matches_count(NEW.P1_ID),
      Accuracy = CASE 
                   WHEN calculate_total_matches_count(NEW.P1_ID) != 0 
                   THEN CAST(calculate_won_matches_count(NEW.P1_ID) AS FLOAT) / calculate_total_matches_count(NEW.P1_ID)
                   ELSE 1.0 
                 END
    WHERE p_id = NEW.P1_ID;

    -- player 2
    UPDATE statistics SET 
      xp = calculate_player_xp(NEW.P2_ID),
      Won_Matches_Count = calculate_won_matches_count(NEW.P2_ID),
      Total_Matches_Count = calculate_total_matches_count(NEW.P2_ID),
      Accuracy = CASE 
                   WHEN calculate_total_matches_count(NEW.P2_ID) != 0 
                   THEN CAST(calculate_won_matches_count(NEW.P2_ID) AS FLOAT) / calculate_total_matches_count(NEW.P2_ID)
                   ELSE 1.0 
                 END
    WHERE p_id = NEW.P2_ID;

  END IF;
END $$

DELIMITER ;



DELIMITER $$
CREATE TRIGGER new_player_status AFTER INSERT ON Player 
FOR EACH ROW 
BEGIN 
    INSERT INTO Statistics(P_ID) VALUES (NEW.P_ID);
END $$
DELIMITER ; 


DELIMITER $$
CREATE FUNCTION get_player_username_by_id (id INT) 
RETURNS VARCHAR(50)
DETERMINISTIC
BEGIN
    DECLARE username1 VARCHAR(50);
    SELECT username INTO username1 FROM player WHERE p_id = id;
    RETURN username1;
END$$
DELIMITER ;


DELIMITER $$
CREATE FUNCTION get_player_id_by_email (email1 VARCHAR (100)) 
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE id INT;
    SELECT p_id INTO id FROM player WHERE email = email1;
    RETURN id;
END$$
DELIMITER ;


DELIMITER $$
CREATE FUNCTION get_category_by_id (id INT) 
RETURNS VARCHAR (50)
DETERMINISTIC
BEGIN
    DECLARE category1 VARCHAR(50);
    SELECT Title INTO category1 FROM category WHERE c_id = id;
    RETURN category1;
END$$
DELIMITER ;


DELIMITER $$
CREATE FUNCTION calculate_player_score_in_match (m_id1 INT, p_id1 INT) 
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE score INT DEFAULT 0;
    DECLARE is_p1 BOOLEAN;

    SELECT (p1_id = p_id1) INTO is_p1
    FROM matches
    WHERE m_id = m_id1;

    IF is_p1 THEN
        SELECT COUNT(*) INTO score
        FROM matches NATURAL JOIN r_q_m NATURAL JOIN question
        JOIN round USING (r_id)
        WHERE m_id = m_id1 AND p1_answer = correct_option;
    ELSE
        SELECT COUNT(*) INTO score
        FROM matches NATURAL JOIN r_q_m NATURAL JOIN question
        JOIN round USING (r_id)
        WHERE m_id = m_id1 AND p2_answer = correct_option;
    END IF;

    RETURN score;
END$$
DELIMITER ;


DELIMITER $$
CREATE FUNCTION answer_question(m_id1 INT, p_id1 INT, r_id1 INT, p_option CHAR) 
RETURNS BOOLEAN
DETERMINISTIC
BEGIN
    DECLARE is_p1 BOOLEAN;
    DECLARE already_answered BOOLEAN;

    SELECT (p1_id = p_id1) INTO is_p1
    FROM matches
    WHERE m_id = m_id1;

    IF is_p1 THEN
        SELECT p1_answer IS NOT NULL INTO already_answered
        FROM round
        WHERE r_id = r_id1;

        IF already_answered THEN
            RETURN FALSE;
        END IF;

        UPDATE round 
        SET p1_answer = p_option 
        WHERE r_id = r_id1;

    ELSE
        SELECT p2_answer IS NOT NULL INTO already_answered
        FROM round
        WHERE r_id = r_id1;

        IF already_answered THEN
            RETURN FALSE;
        END IF;

        UPDATE round 
        SET p2_answer = p_option 
        WHERE r_id = r_id1;
    END IF;

    RETURN TRUE;
END$$
DELIMITER ;




DELIMITER $$
CREATE FUNCTION calculate_player_xp (id1 INT) 
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE xp INT;
    SELECT (SELECT COUNT(*) FROM matches WHERE winner_id = id1 ) * 10 + 
		   (SELECT COUNT(*) FROM matches
			WHERE (p1_id = id1 OR p2_id = id1) AND winner_id IS NOT NULL AND winner_id != id1 ) * 5 + 50 INTO xp;
    RETURN xp;
END$$
DELIMITER ;


DELIMITER $$
CREATE FUNCTION calculate_won_matches_count (id1 INT) 
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE won_count INT;
    SELECT COUNT(*) FROM matches WHERE winner_id = id1 INTO won_count;
    RETURN won_count;
END$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION calculate_total_matches_count (id1 INT) 
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE total_count INT;
    SELECT COUNT(*) FROM matches WHERE (p1_id = id1 OR p2_id = id1) AND winner_id IS NOT NULL INTO total_count;
    RETURN total_count;
END$$
DELIMITER ;


-- ---------------------------------------------------------
INSERT INTO category (Title) VALUES ('math'), ('sport'), ('history'), ('common knowledge'), ('cinema');
-- ------------------------------------------------------------------
INSERT INTO player (username, email, pass, sign_in_date) VALUES 
('Mahdieh', 'mahdieh@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2024-11-02'),
('Bahar', 'bahar@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2024-11-04'),
('Ali', 'ali@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2024-11-05'),
('HasanGholi', 'hasan@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2025-01-11'),
('Zahra', 'zahra@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2025-03-11'),
('Asqar', 'asqar@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2025-03-17'),
('Maryam', 'maryam@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2025-03-17'),
('Javad', 'javad@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2025-03-17'),
('Hosein', 'hosein@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2025-03-17'),
('Goli', 'goli@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2025-03-17'),
('Mina', 'mina@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2025-03-17'),
('Reza', 'reza@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2023-10-18'),
('Akbar', 'akbar@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2025-03-17');
-- --------------------------------------------------------------------
INSERT INTO question (C_ID, question_text, option_a, option_b, option_c, option_d, correct_option, Creator_ID, difficulty, approval_state) VALUES
			(1, '234 + 123 = ?', '567', '357', '412', '337', 'B', 1, 'Easy', true),
			(1, 'تعداد اعداد اول کوچکتر از 100؟', '22', '23', '24', '25', 'D', 2,  'Medium', true),
			(1, '1 + 19 + 83 - 12 + 8 + 56 - 28 = ?', '135', '128', '127', '102', 'C', 1, 'Easy', true),
            (2, 'دارنده بیشترین گل ملی؟', 'علی کریمی', 'علی دایی', 'فرهاد مجیدی', 'جواد نکونام', 'B', 3, 'Easy', true),
            (2, 'دروازه بان ایران در جام جهانی 1978؟', 'ناصر حجازی', 'منصور رشیدی', 'بهروز سلطانی', 'وحید قلیچ', 'A', 4, 'Hard', true),
            (2, 'کدام یک تنیسور است؟', 'کیمیا علیزاده', 'مهسا جاور', 'مهتا خانلو', 'مریم نیک زاد', 'C', 5,  'Hard', true),
            (3, 'نام خدای زمان در ایران باستان؟', 'اهورا مزدا', 'میترا', 'اناهیتا', 'زروان', 'D', 3, 'Hard', true),
            (3, 'چه کسی مجلس هفدهم را منحل کرد؟', 'مصدق', 'مفضل الله نوری', 'امیرکبیر', 'ناصرالدین شاه', 'A', 1, 'Medium', true),
            (3, 'نام اولین مدرسه تاسیس شده در ایران؟', 'دارالفنون', 'سعادت', 'گلستان', 'امیرکبیر', 'A', 2, 'Easy', true),
            (4, 'محل بعثت پیامبر؟', 'مدینه', 'دمشق', 'مکه', 'نجف', 'C', 4, 'Easy', true),
			(4, 'پنجمین سیاره منظومه شمسی؟', 'مریخ', 'مشتری', 'زحل', 'زمین', 'B', 5, 'Easy', true),
			(4, 'عداد استان های ایران؟', '30', '31', '32', '33', 'C', 1, 'Medium', true),
            (5, 'کشور سازنده سریال سالهای دور از خانه', 'چین', 'کره', 'ژاپن', 'ویتنام', 'C', 1, 'Medium', true),
            (5, 'بازیگر نقش گلادیاتور؟', 'راسل کرو', 'جانی دپ', 'ابرد پیت', 'جورج کلونی', 'A', 1, 'Easy', true),
            (5, 'کدلم فیلم به کارگردانی مجید مجیدی نیست؟', 'بچه های آسمان', 'بدوک', 'رنگ خدا', 'جهان با من برقص', 'D', 2, 'Easy', true),
            (5,'کدام یک از بازیگران فیلم یک تکه نان نیست؟', 'هومن سیدی', 'هومن برق نورد', 'رویا نونهالی', 'ژاله علو', 'D', 3, 'Hard', true),
            (4, 'کدام یک سوغات مشهد نیست؟', 'گز', 'عطر مشهدی', 'زعفران', 'نبات', 'A', 1, 'Easy', true),
			(3, 'نام قدیم بندر انزلی؟', 'بندر آزاد', 'بندر پهلوی', 'خرمشاه', 'بندر زاهه', 'B', 2, 'Hard', true),
            (2, 'اولین زن برنده مدال المپیک؟', 'شارلوت کوپر', 'باربارا اسکات', 'کیمیا علیزاده', 'ناهید کیانی', 'A', 3, 'Hard', true),
            (1, '2012 - 234 + 12345 * 12', '149917', '149918', '146918', '200008', 'B', 4, 'Hard', true),
            (2, 'کدام یک جزو ورزش های رزمی است؟', 'سپک تاکرا', 'کنگ فو', 'بسکتبال', 'والیبال', 'B', 5, 'Easy', true),
            (3, 'کدام یک از شاهان هخامنشی نیست؟', 'کمبوجیه', 'اردشیر دوم', 'ارشک', 'طهماسب یکم', 'D', 1, 'Hard', true),
            (1, 'نخستین زن برنده مدال فیلدز در ریاضیات؟', 'مارینا ویازوفسکا', 'مریم میرزاخانی', 'جون هو', 'عاطفه مقدسی', 'B', 2, 'Easy', true),
            (4, 'شهر معروف به نصف جهان؟', 'بندرعباس', 'شیراز', 'تبریز', 'اصفهان', 'D', 3, 'Easy', true),
            (5, 'فیلمنامه کدام فیلم بر اساس واقعیت نوشته شده است؟', 'ارقام پنهان', 'بچه های آسمان', 'راندده', 'پس از نیمه شب', 'A', 3, 'Hard', true),
            (1, '12 * 12 = 4 * ?', '36', '16', '24', '18', 'A', 3, 'Easy', false),
            (1, '1192 - 11 + 561 * 2', '2300', '2223', '2303', '2103', 'C', 1, 'Easy', false),
            (4, 'واحد استاندارد اندازه گیری طول؟', 'اینچ', 'سانتی متر', 'متر', 'فوت', 'C', 11, 'Easy', false),
            (4, 'هورمونی که باعث ایجاد حس لذت میشود؟ ', 'دوپامین', 'کورتیزول', 'سروتونین', 'اندوفین', 'A', 11, 'Hard', false);
-- -----------------------------------------------------------------------------------------------------------------                      
INSERT INTO round (Round_num, p1_answer, p2_answer, start_time, end_time) VALUES 
			(1, 'B', 'B', '2025-06-05 14:30:45', '2025-06-05 15:35:45'),
            (2, 'B', 'D', '2025-06-06 14:30:45', '2025-06-06 15:35:45'),
            (3, 'A', 'C', '2025-06-07 14:30:45', '2025-06-07 15:35:45');
INSERT INTO matches (p1_id, p2_id, winner_id, start_time, end_time) VALUES
			(1, 2, 2, '2025-06-05 14:35:45', NULL);
UPDATE matches SET end_time = '2025-06-07 15:35:45' WHERE m_id = 1;
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES
			(1, 1, 1), (2, 2, 1), (3, 3, 1);
            
INSERT INTO round (Round_num, p1_answer, p2_answer, start_time, end_time) VALUES 
			(1, 'B', 'A', '2024-06-08 14:30:45', '2024-06-08 14:35:45'),
            (2, 'A', 'D', '2024-06-09 14:30:45', '2024-06-08 14:35:45'),
            (3, 'C', 'A', '2024-06-10 14:30:45', '2024-06-08 14:35:45');
INSERT INTO matches (p1_id, p2_id, winner_id, start_time, end_time) VALUES
			(8, 7, 8, '2024-06-08 14:30:45', NULL);
UPDATE matches SET end_time = '2024-06-08 14:35:45' WHERE m_id = 2;
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES
			(4, 4, 2), (5, 5, 2), (6, 6, 2);
            
            
INSERT INTO round (Round_num, p1_answer, p2_answer, start_time, end_time) VALUES 
			(1, 'D', 'A', '2025-05-29 14:30:45', '2025-05-29 14:35:45'),
            (2, 'A', 'D', '2025-05-29 15:30:45', '2025-06-29 15:50:45'),
            (3, 'C', 'A', '2025-05-29 16:30:45', '2025-06-29 17:30:12');
INSERT INTO matches (p1_id, p2_id, winner_id, start_time, end_time) VALUES
			(1, 6, 1, '2025-05-29 14:30:45', NULL);
UPDATE matches SET end_time = '2025-06-29 17:30:12' WHERE m_id = 3;
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES
			(7, 7, 3), (8, 8, 3), (9, 9, 3);
            

INSERT INTO round (Round_num, p1_answer, p2_answer, start_time, end_time) VALUES 
			(1, 'D', 'A', '2025-05-29 14:30:45', '2025-05-29 14:35:45'),
            (2, 'A', 'B', '2025-05-29 15:30:45', '2025-06-29 15:50:45'),
            (3, 'A', 'A', '2025-05-29 16:30:45', '2025-06-29 17:30:12');
INSERT INTO matches (p1_id, p2_id, winner_id, start_time, end_time) VALUES
			(5, 8, 5, '2025-05-29 14:30:45', NULL);
UPDATE matches SET end_time = '2025-06-29 17:30:12' WHERE m_id = 4;
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES
			(7, 10, 4), (8, 11, 4), (9, 12, 4);


INSERT INTO round (Round_num, p1_answer, p2_answer, start_time, end_time) VALUES 
			(1, 'C', 'C', '2025-03-29 14:30:45', '2025-03-29 14:35:45'),
            (2, 'B', 'D', '2025-03-29 15:30:45', '2025-03-29 15:50:45'),
            (3, 'C', 'A', '2025-03-29 16:30:45', '2025-03-29 17:30:12');
INSERT INTO matches (p1_id, p2_id, winner_id, start_time, end_time) VALUES
			(4, 5, 4, '2025-03-29 14:30:45', NULL);
UPDATE matches SET end_time = '2025-03-29 17:30:12' WHERE m_id = 5;
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES
			(10, 13, 5), (11, 14, 5), (12, 15, 5);
            
            
INSERT INTO round (Round_num, p1_answer, p2_answer, start_time, end_time) VALUES 
			(1, 'C', 'C', '2025-03-29 14:30:45', '2025-03-29 14:35:45'),
            (2, 'B', 'D', '2025-03-29 15:30:45', '2025-03-29 15:50:45'),
            (3, 'C', 'A', '2025-03-29 16:30:45', '2025-03-29 17:30:12');
INSERT INTO matches (p1_id, p2_id, winner_id, start_time, end_time) VALUES
			(9, 11, 9, '2025-03-29 14:30:45', NULL);
UPDATE matches SET end_time = '2025-03-29 17:30:12' WHERE m_id = 6;
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES
			(10, 16, 6), (11, 17, 6), (12, 18, 6);
            
            
INSERT INTO round (Round_num, p1_answer, p2_answer, start_time, end_time) VALUES 
			(1, 'C', 'C', '2025-04-19 14:30:45', '2025-04-19 14:35:45'),
            (2, 'A', 'D', '2025-04-19 15:30:45', '2025-04-19 15:50:45'),
            (3, 'C', 'A', '2025-04-19 16:30:45', '2025-04-20 17:30:12');
INSERT INTO matches (p1_id, p2_id, winner_id, start_time, end_time) VALUES
			(11, 7, 11, '2025-04-19 14:30:45', NULL);
UPDATE matches SET end_time = '2025-04-20 17:30:12' WHERE m_id = 7;
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES
			(10, 19, 7), (11, 20, 7), (12, 21, 7);
            

INSERT INTO round (Round_num, p1_answer, p2_answer, start_time, end_time) VALUES 
			(1, 'C', 'B', '2023-03-29 14:30:45', '2023-03-29 14:35:45'),
            (2, 'A', 'D', '2023-03-29 15:30:45', '2023-03-29 15:50:45'),
            (3, 'B', 'A', '2023-03-29 16:30:45', '2023-03-29 17:30:12');
INSERT INTO matches (p1_id, p2_id, winner_id, start_time, end_time) VALUES
			(3, 5, 3, '2023-03-29 14:30:45', NULL);
UPDATE matches SET end_time = '2023-03-29 17:30:12' WHERE m_id = 8;
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES
			(13, 22, 8), (14, 23, 8), (15, 24, 8);
            
            
INSERT INTO round (Round_num, p1_answer, p2_answer, start_time, end_time) VALUES 
			(1, 'A', 'C', '2025-05-09 10:30:45', '2025-05-09 14:35:45'),
            (2, 'D', 'D', '2025-05-09 15:30:45', '2025-05-09 15:50:45'),
            (3, 'C', 'A', '2025-05-09 16:30:45', '2025-05-09 18:30:12');
INSERT INTO matches (p1_id, p2_id, winner_id, start_time, end_time) VALUES
			(7, 2, 7, '2025-05-09 10:30:45', NULL);
UPDATE matches SET end_time = '2025-05-09 18:30:12' WHERE m_id = 9;
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES
			(1, 25, 9), (2, 26, 9), (3, 27, 9);
            
INSERT INTO round (Round_num, p1_answer, p2_answer, start_time, end_time) VALUES 
			(1, 'C', 'C', '2025-01-11 07:30:45', '2025-01-11 14:35:45'),
            (2, 'B', 'D', '2025-01-11 15:30:45', '2025-01-11 15:50:45'),
            (3, 'C', 'A', '2025-01-11 16:30:45', '2025-01-11 20:30:12');
INSERT INTO matches (p1_id, p2_id, winner_id, start_time, end_time) VALUES
			(10, 9, 10, '2025-01-11 07:30:45', NULL);
UPDATE matches SET end_time = '2025-01-11 20:30:12' WHERE m_id = 10;
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES
			(1, 28, 10), (2, 29, 10), (3, 30, 10);
            
INSERT INTO round (Round_num, p1_answer, p2_answer, start_time, end_time) VALUES 
			(1, 'C', 'C', '2021-03-29 14:30:45', '2021-03-29 14:35:45'),
            (2, 'B', 'A', '2021-03-29 15:30:45', '2021-03-29 15:50:45'),
            (3, 'C', 'A', '2021-03-29 16:30:45', '2021-03-29 17:30:12');
INSERT INTO matches (p1_id, p2_id, winner_id, start_time, end_time) VALUES
			(4, 12, 12, '2021-03-29 14:30:45', NULL);
UPDATE matches SET end_time = '2021-03-29 17:30:12' WHERE m_id = 11;
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES
			(4, 31, 11), (5, 32, 11), (6, 33, 11);
            
            
INSERT INTO round (Round_num, p1_answer, p2_answer, start_time, end_time) VALUES 
			(1, 'C', 'C', '2025-06-28 14:30:45', '2025-06-28 14:35:45'),
            (2, 'B', 'A', '2025-06-28 15:30:45', '2025-06-28 15:50:45'),
            (3, 'C', 'D', '2025-06-28 16:30:45', '2025-06-28 17:30:12');
INSERT INTO matches (p1_id, p2_id, winner_id, start_time, end_time) VALUES
			(1, 12, 12, '2025-06-28 14:30:45', NULL);
UPDATE matches SET end_time = '2025-06-28 17:30:12' WHERE m_id = 12;
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES
			(4, 34, 12), (5, 35, 12), (6, 36, 12);
            
            
INSERT INTO round (Round_num, p1_answer, p2_answer, start_time, end_time) VALUES 
			(1, 'A', 'C', '2025-06-27 14:30:45', '2025-06-27 14:35:45'),
            (2, 'B', 'A', '2025-06-27 15:30:45', '2025-06-27 15:50:45'),
            (3, 'C', 'A', '2025-06-27 16:30:45', '2025-06-27 17:30:12');
INSERT INTO matches (p1_id, p2_id, winner_id, start_time, end_time) VALUES
			(4, 1, 1, '2025-06-28 14:30:45', NULL);
UPDATE matches SET end_time = '2025-06-28 17:30:12' WHERE m_id = 13;
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES
			(4, 37, 13), (5, 38, 13), (6, 39, 13);
            

INSERT INTO round (Round_num, p1_answer, p2_answer, start_time, end_time) VALUES 
			(1, 'B', 'C', '2025-06-27 14:30:45', '2025-06-27 14:35:45'),
            (2, 'B', 'A', '2025-06-27 15:30:45', '2025-06-27 15:50:45'),
            (3, 'C', 'A', '2025-06-27 16:30:45', '2025-06-27 17:30:12');
INSERT INTO matches (p1_id, p2_id, winner_id, start_time, end_time) VALUES
			(1, 8, 1, '2025-06-27 14:30:45', NULL);
UPDATE matches SET end_time = '2025-06-27 17:30:12' WHERE m_id = 14;
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES
			(20, 40, 14), (23, 41, 14), (1, 42, 14);
            
            
            
INSERT INTO round (Round_num, p1_answer, start_time) VALUES 
			(1, 'B', '2025-06-27 14:30:45');
INSERT INTO matches (p1_id, start_time) VALUES
			(10,'2025-06-27 14:30:45');
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES 
			(20, 43, 15);
-- ------------------------------------------------------------------------------------------------------------------
UPDATE player SET A_ID = 4 WHERE P_ID = 1;
UPDATE player SET A_ID = 3 WHERE P_ID = 2;
UPDATE player SET A_ID = 2 WHERE P_ID = 3;
UPDATE player SEt user_banned = true WHERE email = 'akbar@gmail.com';
-- ------------------------------------------------------------------------------------------------------------------
CREATE INDEX email_index ON Player(email);
CREATE INDEX id_index ON Question(Q_ID);


