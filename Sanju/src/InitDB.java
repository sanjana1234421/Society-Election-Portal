
public class InitDB {
    public static void main(String[] args) {
        System.out.println("Initializing MySQL database 'society_voting' from database/schema.sql...");
        try {
            DBConnection.executeSqlScript("database/schema.sql");
            System.out.println("[SUCCESS] Database 'society_voting' and sample seed data created successfully!");
            String status = DBConnection.testCurrentConnection();
            if (status == null) {
                System.out.println("[VERIFIED] Connected to 'society_voting' on " + DBConnection.getDbHost() + " successfully!");
            } else {
                System.err.println("[WARNING] Status: " + status);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
