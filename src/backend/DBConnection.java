package backend;

import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:mysql://shortline.proxy.rlwy.net:22222/railway?useSSL=true&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "MRziKFBLUqrLhWPrttfZvRVPWvIPiEsG";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
