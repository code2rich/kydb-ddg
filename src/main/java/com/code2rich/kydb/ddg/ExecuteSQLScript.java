package com.code2rich.kydb.ddg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExecuteSQLScript {
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:oracle:thin:@10.20.32.192:1521:orcl";
        String username = "kyp_poc30";
        String password = "hundsun";
        String scriptPath = "/Users/code2rich/home/project_repo/kyp-allinone/kyp-others/mykyp/db/oracle/script.sql";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = connection.createStatement()) {

            // Read the SQL script
            String sql = new String(Files.readAllBytes(Paths.get(scriptPath)));

            // Execute the SQL script
            statement.execute(sql);

            System.out.println("Script executed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}