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
    FOREIGN KEY (A_ID) REFERENCES Authorities(A_ID),
    Sign_In_Date DATE
);

CREATE TABLE Category (
	C_ID INT AUTO_INCREMENT PRIMARY KEY,
    Title VARCHAR(50) NOT NULL UNIQUE
);


CREATE TABLE Question (
    Q_ID INT AUTO_INCREMENT PRIMARY KEY,
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
    P2_ID INT NOT NULL,
    winner_ID INT DEFAULT NULL,
    Start_Time TIMESTAMP NOT NULL, 
    End_Time TIMESTAMP, 
    Match_Active BOOLEAN NOT NULL DEFAULT TRUE,
    CHECK (P1_ID != P2_ID),
    FOREIGN KEY (P1_ID) REFERENCES player (P_ID),
    FOREIGN KEY (P2_ID) REFERENCES player (P_ID),
    FOREIGN KEY (winner_ID) REFERENCES player (P_ID)
);


CREATE TABLE Qstion_Ctgry (
	Q_ID INT,
	C_ID INT,
	PRIMARY KEY (Q_ID, C_ID),
	FOREIGN KEY (Q_ID) REFERENCES Question (Q_ID),
	FOREIGN KEY (C_ID) REFERENCES Category (C_ID)
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
('Mahdieh', 'mahdieh@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2025-11-02'),
('Bahar', 'bahar@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2025-11-04'),
('Ali', 'ali@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2025-11-05'),
('HasanGholi', 'hasan@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2025-11-11'),
('Reza', 'reza@gmail.com', '77bb75ad6dd160a47b68d3b0182cf1589c953cdeee2720359def0c6f470490bb', '2025-10-18');
-- --------------------------------------------------------------------
INSERT INTO question (question_text, option_a, option_b, option_c, option_d, correct_option, Creator_ID, difficulty, approval_state) VALUES
			('234 + 123 = ?', '567', '357', '412', '337', 'B', 1, 'Easy', true),
			('تعداد اعداد اول کوچکتر از 100؟', '22', '23', '24', '25', 'D', 2,  'Medium', true),
			('1 + 19 + 83 - 12 + 8 + 56 - 28 = ?', '135', '128', '127', '102', 'C', 1, 'Easy', true),
            ('دارنده بیشترین گل ملی؟', 'علی کریمی', 'علی دایی', 'فرهاد مجیدی', 'جواد نکونام', 'B', 3, 'Easy', true),
            ('دروازه بان ایران در جام جهانی 1978؟', 'ناصر حجازی', 'منصور رشیدی', 'بهروز سلطانی', 'وحید قلیچ', 'A', 4, 'Hard', true),
            ('کدام یک تنیسور است؟', 'کیمیا علیزاده', 'مهسا جاور', 'مهتا خانلو', 'مریم نیک زاد', 'C', 5,  'Hard', true),
            ('نام خدای زمان در ایران باستان؟', 'اهورا مزدا', 'میترا', 'اناهیتا', 'زروان', 'D', 3, 'Hard', true),
            ('چه کسی مجلس هفدهم را منحل کرد؟', 'مصدق', 'مفضل الله نوری', 'امیرکبیر', 'ناصرالدین شاه', 'A', 1, 'Medium', true),
            ('نام اولین مدرسه تاسیس شده در ایران؟', 'دارالفنون', 'سعادت', 'گلستان', 'امیرکبیر', 'A', 2, 'Easy', true),
            ('محل بعثت پیامبر؟', 'مدینه', 'دمشق', 'مکه', 'نجف', 'C', 4, 'Easy', true),
			('پنجمین سیاره منظومه شمسی؟', 'مریخ', 'مشتری', 'زحل', 'زمین', 'B', 5, 'Easy', true),
			('عداد استان های ایران؟', '30', '31', '32', '33', 'C', 1, 'Medium', true),
            ('کشور سازنده سریال سالهای دور از خانه', 'چین', 'کره', 'ژاپن', 'ویتنام', 'C', 1, 'Medium', true),
            ('بازیگر نقش گلادیاتور؟', 'راسل کرو', 'جانی دپ', 'ابرد پیت', 'جورج کلونی', 'A', 1, 'Easy', true),
            ('کدلم فیلم به کارگردانی مجید مجیدی نیست؟', 'بچه های آسمان', 'بدوک', 'رنگ خدا', 'جهان با من برقص', 'D', 2, 'Easy', true),
            ('کدام یک از بازیگران فیلم یک تکه نان نیست؟', 'هومن سیدی', 'هومن برق نورد', 'رویا نونهالی', 'ژاله علو', 'D', 3, 'Hard', true),
            ('کدام یک سوغات مشهد نیست؟', 'گز', 'عطر مشهدی', 'زعفران', 'نبات', 'A', 1, 'Easy', true),
			('نام قدیم بندر انزلی؟', 'بندر آزاد', 'بندر پهلوی', 'خرمشاه', 'بندر زاهه', 'B', 2, 'Hard', true),
            ('اولین زن برنده مدال المپیک؟', 'شارلوت کوپر', 'باربارا اسکات', 'کیمیا علیزاده', 'ناهید کیانی', 'A', 3, 'Hard', true),
            ('2012 - 234 + 12345 * 12', '149917', '149918', '146918', '200008', 'B', 4, 'Hard', true),
            ('کدام یک جزو ورزش های رزمی است؟', 'سپک تاکرا', 'کنگ فو', 'بسکتبال', 'والیبال', 'B', 5, 'Easy', true),
            ('کدام یک از شاهان هخامنشی نیست؟', 'کمبوجیه', 'اردشیر دوم', 'ارشک', 'طهماسب یکم', 'D', 1, 'Hard', true);
            
INSERT INTO qstion_ctgry (Q_ID, C_ID) VALUES
			(1, 1), (2, 1), (3, 1), (4, 2),
            (5, 2), (6, 2), (7, 3), (8, 3),
            (9, 3), (10, 4), (11, 4), (12, 4),
            (13, 5), (14, 5), (15, 5), (16, 5),
            (17, 4), (18, 3), (19, 2), (20, 1),
            (21, 2), (22, 3);
            
INSERT INTO round (Round_num, p1_answer, p2_answer, start_time, end_time) VALUES 
			(1, 'B', 'B', '2025-06-08 14:30:45', '2025-06-08 14:35:45'),
            (2, 'B', 'D', '2025-06-09 14:30:45', '2025-06-08 14:35:45'),
            (3, 'A', 'C', '2025-06-10 14:30:45', '2025-06-08 14:35:45');
INSERT INTO matches (p1_id, p2_id, winner_id, start_time, end_time) VALUES
			(1, 2, 2, '2025-06-08 14:30:45', NULL);
UPDATE matches SET end_time = '2025-06-08 14:35:45' WHERE m_id = 1;
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES
			(1, 1, 1), (2, 2, 1), (3, 3, 1);
            
INSERT INTO round (Round_num, p1_answer, p2_answer, start_time, end_time) VALUES 
			(1, 'B', 'A', '2026-06-08 14:30:45', '2026-06-08 14:35:45'),
            (2, 'A', 'D', '2026-06-09 14:30:45', '2026-06-08 14:35:45'),
            (3, 'C', 'A', '2026-06-10 14:30:45', '2026-06-08 14:35:45');
INSERT INTO matches (p1_id, p2_id, winner_id, start_time, end_time) VALUES
			(2, 1, 2, '2026-06-08 14:30:45', NULL);
UPDATE matches SET end_time = '2026-06-08 14:35:45' WHERE m_id = 2;
INSERT INTO r_q_m (q_id, r_id, m_id) VALUES
			(4, 4, 2), (5, 5, 2), (6, 6, 2);
-- ------------------------------------------------------------------
UPDATE player SET A_ID = 4 WHERE P_ID = 1;
UPDATE player SET A_ID = 3 WHERE P_ID = 2;
UPDATE player SET A_ID = 2 WHERE P_ID = 3;
-- ------------------------------------------------------------------
CREATE INDEX email_index ON Player(email);
CREATE INDEX id_index ON Question(Q_ID);








