
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Centralized Database Connection Manager.
 * Loads configuration from config/db.properties and manages JDBC connections.
 * Supports dynamic LAN IP configuration without code recompilation.
 */
public class DBConnection {

    private static final String CONFIG_FILE_PATH = "config/db.properties";
    private static Properties properties = new Properties();

    private static String dbHost = "localhost";
    private static int dbPort = 3306;
    private static String dbName = "society_voting";
    private static String dbUsername = "root";
    private static String dbPassword = "";
    private static boolean useSSL = false;
    private static boolean allowPublicKeyRetrieval = true;
    private static String serverTimezone = "UTC";

    static {
        loadConfig();
        try {
            // Explicitly load MySQL Connector driver class
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * Loads connection settings from config/db.properties file.
     */
    public static synchronized void loadConfig() {
        File configFile = new File(CONFIG_FILE_PATH);
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                properties.load(input);
                dbHost = properties.getProperty("DB_HOST", "localhost").trim();
                dbPort = Integer.parseInt(properties.getProperty("DB_PORT", "3306").trim());
                dbName = properties.getProperty("DB_NAME", "society_voting").trim();
                dbUsername = properties.getProperty("DB_USERNAME", "root").trim();
                dbPassword = properties.getProperty("DB_PASSWORD", "").trim();
                useSSL = Boolean.parseBoolean(properties.getProperty("DB_USE_SSL", "false").trim());
                allowPublicKeyRetrieval = Boolean.parseBoolean(properties.getProperty("DB_ALLOW_PUBLIC_KEY_RETRIEVAL", "true").trim());
                serverTimezone = properties.getProperty("DB_SERVER_TIMEZONE", "UTC").trim();
            } catch (Exception e) {
                System.err.println("[DBConnection] Could not read " + CONFIG_FILE_PATH + ", using defaults. " + e.getMessage());
            }
        } else {
            saveConfig(dbHost, dbPort, dbName, dbUsername, dbPassword);
        }
    }

    /**
     * Saves updated database connection configuration to config/db.properties.
     */
    public static synchronized void saveConfig(String host, int port, String name, String username, String password) {
        dbHost = host.trim();
        dbPort = port;
        dbName = name.trim();
        dbUsername = username.trim();
        dbPassword = password != null ? password : "";

        properties.setProperty("DB_HOST", dbHost);
        properties.setProperty("DB_PORT", String.valueOf(dbPort));
        properties.setProperty("DB_NAME", dbName);
        properties.setProperty("DB_USERNAME", dbUsername);
        properties.setProperty("DB_PASSWORD", dbPassword);
        properties.setProperty("DB_USE_SSL", String.valueOf(useSSL));
        properties.setProperty("DB_ALLOW_PUBLIC_KEY_RETRIEVAL", String.valueOf(allowPublicKeyRetrieval));
        properties.setProperty("DB_SERVER_TIMEZONE", serverTimezone);

        File dir = new File("config");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE_PATH)) {
            properties.store(out, "Society Voting & Election Management System - Database Configuration");
        } catch (IOException e) {
            System.err.println("[DBConnection] Failed to save database config: " + e.getMessage());
        }
    }

    /**
     * Constructs the standard MySQL JDBC URL.
     * @param includeDb Whether to append database name in the connection string.
     * @return Formatted JDBC URL.
     */
    public static String getJdbcUrl(boolean includeDb) {
        String base = "jdbc:mysql://" + dbHost + ":" + dbPort + "/";
        if (includeDb && dbName != null && !dbName.isEmpty()) {
            base += dbName;
        }
        return base + "?useSSL=" + useSSL 
                    + "&allowPublicKeyRetrieval=" + allowPublicKeyRetrieval 
                    + "&serverTimezone=" + serverTimezone
                    + "&connectTimeout=5000&socketTimeout=10000";
    }

    /**
     * Obtains a new connection to the society_voting database.
     * @return java.sql.Connection
     * @throws SQLException If connection fails.
     */
    public static Connection getConnection() throws SQLException {
        String url = getJdbcUrl(true);
        return DriverManager.getConnection(url, dbUsername, dbPassword);
    }

    /**
     * Tests connection to the MySQL server and database.
     * @return null if successful, or error message string if failed.
     */
    public static String testConnection(String host, int port, String name, String user, String pass) {
        String testUrl = "jdbc:mysql://" + host.trim() + ":" + port + "/" + name.trim() 
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=4000";
        try (Connection conn = DriverManager.getConnection(testUrl, user.trim(), pass != null ? pass : "")) {
            return null; // success
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    /**
     * Tests the currently configured connection.
     * @return null if success, or error message.
     */
    public static String testCurrentConnection() {
        return testConnection(dbHost, dbPort, dbName, dbUsername, dbPassword);
    }

    /**
     * Executes the SQL schema script if the user requests or on first setup.
     * @param sqlFilePath Path to schema.sql
     * @throws Exception If script execution fails.
     */
    public static void executeSqlScript(String sqlFilePath) throws Exception {
        String rawContent = new String(Files.readAllBytes(Paths.get(sqlFilePath)));
        StringBuilder cleanSql = new StringBuilder();
        for (String line : rawContent.split("\\r?\\n")) {
            String trimmedLine = line.trim();
            if (!trimmedLine.startsWith("--") && !trimmedLine.startsWith("/*") && !trimmedLine.isEmpty()) {
                cleanSql.append(line).append("\n");
            }
        }

        // Connect to server without specific DB first to create database
        String serverUrl = getJdbcUrl(false);
        try (Connection conn = DriverManager.getConnection(serverUrl, dbUsername, dbPassword);
             Statement stmt = conn.createStatement()) {
            String[] queries = cleanSql.toString().split(";");
            for (String query : queries) {
                String trimmed = query.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }

    // Getters
    public static String getDbHost() { return dbHost; }
    public static int getDbPort() { return dbPort; }
    public static String getDbName() { return dbName; }
    public static String getDbUsername() { return dbUsername; }
    public static String getDbPassword() { return dbPassword; }
}
