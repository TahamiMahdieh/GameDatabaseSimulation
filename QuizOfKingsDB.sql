CREATE DATABASE QuizGameDB;
USE QuizGameDB;
CREATE TABLE Player (
    P_ID INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50) NOT NULL,
    Email VARCHAR(100) NOT NULL UNIQUE,
    Password VARCHAR(255) NOT NULL,
    Sign_In_Date DATE
);

CREATE TABLE Category (
	C_ID INT AUTO_INCREMENT PRIMARY KEY,
    Title VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE Authorities (
	A_ID INT AUTO_INCREMENT PRIMARY KEY,
    Question_Management BOOLEAN NOT NULL,
    Users_Ban BOOLEAN NOT NULL
);

CREATE TABLE Abilities (
		A_ID INT,
        P_ID INT,
        PRIMARY KEY (A_ID, P_ID),
        FOREIGN KEY (A_ID) REFERENCES Authorities (A_ID),
        FOREIGN KEY (P_ID) REFERENCES Player (P_ID)
);
CREATE TABLE Question (
    Q_ID INT AUTO_INCREMENT PRIMARY KEY,
    Question_Text TEXT NOT NULL,
    Option_A VARCHAR(100) NOT NULL,
    Option_B VARCHAR(100) NOT NULL,
    Option_C VARCHAR(100) NOT NULL,
    Option_D VARCHAR(100) NOT NULL,
    Correct_Option CHAR NOT NULL,
    Difficulty VARCHAR(10) NOT NULL,
    Approval_State BOOLEAN NOT NULL,
    CHECK (Correct_Option IN ('A', 'B', 'C', 'D')),
    CHECK (Difficulty IN ('Easy', 'Medium', 'Hard'))
);
CREATE TABLE Creates (
	Q_ID INT,
	P_ID INT,
	PRIMARY KEY (Q_ID, P_ID),
	FOREIGN KEY (Q_ID) REFERENCES Question (Q_ID),
	FOREIGN KEY (P_ID) REFERENCES Player (P_ID)
);

CREATE TABLE Round (
	R_ID INT AUTO_INCREMENT PRIMARY KEY,
    Round_Num INT NOT NULL,
    Player1_Answer CHAR,
    Player2_Answer CHAR,
    Start_Time TIMESTAMP NOT NULL,
    End_Time TIMESTAMP, -- this can be null. if it has been more than a day since the start of the round, the match gets expired.
    CHECK (Player1_Answer IN ('A', 'B', 'C', 'D')), 
    CHECK (Player2_Answer IN ('A', 'B', 'C', 'D')),
    CHECK (Round_Num > 0 AND Round_Num < 7)
);

CREATE TABLE Matches (
	M_ID INT AUTO_INCREMENT PRIMARY KEY,
    Start_Time TIMESTAMP NOT NULL, 
    End_Time TIMESTAMP, 
    Match_Status VARCHAR(10) NOT NULL,
    CHECK (Match_Status IN ('Done', 'Active'))
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
);*/

CREATE TABLE Player1 (
	P_ID INT,
    M_ID INT PRIMARY KEY,
    FOREIGN KEY (M_ID) REFERENCES Matches (M_ID),
	FOREIGN KEY (P_ID) REFERENCES Player (P_ID)
)

CREATE TABLE Player2 (
	P_ID INT,
    M_ID INT PRIMARY KEY,
    FOREIGN KEY (M_ID) REFERENCES Matches (M_ID),
	FOREIGN KEY (P_ID) REFERENCES Player (P_ID)
);

CREATE TABLE Winner (
	P_ID INT,
    M_ID INT PRIMARY KEY,
    Win_Date TIMESTAMP,
    FOREIGN KEY (M_ID) REFERENCES Matches (M_ID),
	FOREIGN KEY (P_ID) REFERENCES Player (P_ID)
);

CREATE TABLE Statistics (
	S_ID INT AUTO_INCREMENT PRIMARY KEY,
    P_ID INT, 
    Total_Matches_Count INT NOT NULL DEFAULT 0, 
    Won_Matcplayer2hes_Count INT NOT NULL DEFAULT 0, 
	Average_Accuracy FLOAT NOT NULL DEFAULT 1, 
    FOREIGN KEY (P_ID) REFERENCES Player (P_ID) 
);

------------------------------------------------------------------

DELIMITER $$
CREATE TRIGGER update_match_status BEFORE UPDATE ON Matches 
FOR EACH ROW 
BEGIN 
    IF OLD.End_Time IS NULL AND NEW.End_Time IS NOT NULL THEN
        SET NEW.Match_Status = 'Done';
    END IF;
END $$
DELIMITER ; 


