package pkgauth;

import pkg.SQLiteJDBC;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        SQLiteJDBC.creatDB();
        // SQLiteJDBC.test();
        SQLiteJDBC.checkUser("oussama","azerty");



    }}
