package pkg;

import protocol.Message;

import java.sql.*;

public class SQLiteJDBC {

    public static void creatDB() throws SQLException, ClassNotFoundException {
        Connection c = null;
        Statement stmt = null;
        try {

            //Establish connection
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:Fcrypt.db");
            System.out.println("Opened database successfully");

            stmt = c.createStatement();

            String sql = "DROP TABLE IF EXISTS users";
            stmt.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS policies";
            stmt.executeUpdate(sql);

            //Create Tables
            sql = "CREATE TABLE IF NOT EXISTS users " +
                    "(ID INT PRIMARY KEY     NOT NULL," +
                    " USERNAME       TEXT    NOT NULL, " +
                    " PASSWORD       CHAR(50))";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS policies " +
                    "(ID INT PRIMARY KEY     NOT NULL," +
                    "ID_USERS INT NOT NULL ," +
                    "POLICY         TEXT    NOT NULL )";


            stmt.executeUpdate(sql);
            System.out.println("policies table created");
            //c = DriverManager.getConnection("jdbc:sqlite:Fcrypt.db");
            c.setAutoCommit(false);
            System.out.println("table created");

            sql = "INSERT INTO users (ID,USERNAME,PASSWORD) " +
                    "VALUES (1, 'ilyas',123456);";
            stmt.executeUpdate(sql);
            System.out.println("table created");
            sql = "INSERT INTO users (ID,USERNAME,PASSWORD) " +
                    "VALUES (2, 'oussama','azerty');";
            stmt.executeUpdate(sql);


            sql = "INSERT INTO users (ID,USERNAME,PASSWORD) " +
                    "VALUES (3, 'karim','karim123');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO users (ID,USERNAME,PASSWORD) " +
                    "VALUES (4, 'anas','123456');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO users (ID,USERNAME,PASSWORD) " +
                    "VALUES (5, 'abdelouahed','123@123');";
            stmt.executeUpdate(sql);


            sql = "INSERT INTO policies (ID,ID_USERS,POLICY) " +
                    "VALUES (1, 1, 'PhdStudent');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO policies (ID,ID_USERS,POLICY) " +
                    "VALUES (2, 1, 'Professor');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO policies (ID,ID_USERS,POLICY) " +
                    "VALUES (3, 2, 'engineer');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO policies (ID,ID_USERS,POLICY) " +
                    "VALUES (4, 2, 'Phd');";
            stmt.executeUpdate(sql);
            sql = "INSERT INTO policies (ID,ID_USERS,POLICY) " +
                    "VALUES (5, 3, 'Director');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO policies (ID,ID_USERS,POLICY) " +
                    "VALUES (6, 3, 'Doctor');";
            stmt.executeUpdate(sql);
            sql = "INSERT INTO policies (ID,ID_USERS,POLICY) " +
                    "VALUES (7, 4, 'professor');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO policies (ID,ID_USERS,POLICY) " +
                    "VALUES (8, 4, 'PhdStudent');";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO policies (ID,ID_USERS,POLICY) " +
                    "VALUES (9, 5, 'Datascientist');";
            stmt.executeUpdate(sql);

            System.out.println("Records created successfully");

            stmt.close();
            c.commit();
            c.close();

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table created successfully");
    }







    public static void test() throws ClassNotFoundException, SQLException {


        Message log = new Message(2, "oussama@um6p.ma", "azerty");
       // boolean ok = checkUser(log.getUsername(), log.getPassword());

            Connection c = null;
            Statement stmt = null;
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:Fcrypt.db");
            stmt = c.createStatement();
            c.setAutoCommit(false);
            ResultSet rs = stmt.executeQuery( "SELECT ID, USERNAME FROM users;" );
            while ( rs.next() ) {
                int id = rs.getInt("ID");
                String login = rs.getString("USERNAME");
                System.out.println("ID = " + id+"  NAME =" + login);
               // System.out.println();

            }
            rs.close();
            stmt.close();
            c.close();

        }






    public static boolean checkUser(String login, String pass) throws ClassNotFoundException, SQLException {
        String sql = "SELECT ID, USERNAME "
                + "FROM users WHERE USERNAME = ? AND PASSWORD = ?";
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:Fcrypt.db");

            PreparedStatement pstmt = c.prepareStatement(sql);

            // set the value
            pstmt.setString(1, login);
            pstmt.setString(2, pass);
            //
            ResultSet rs = pstmt.executeQuery();

            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getInt("ID") + "\t" +
                        rs.getString("USERNAME") + "\t");

                if (rs.next()) {
                    return true;
                } else
                    return false;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


        return false;
    }
    }








