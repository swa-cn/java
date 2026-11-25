import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class CleanupTestUser {
    public static void main(String[] args) throws Exception {
        String user = "fishtest01";
        String url = "jdbc:mysql://localhost:3306/aurora?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
        List<String[]> targets = new ArrayList<>();
        String dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword == null || dbPassword.isEmpty()) {
            System.err.println("请先设置环境变量 DB_PASSWORD");
            return;
        }
        try (Connection c = DriverManager.getConnection(url, "root", dbPassword)) {
            String sql = "SELECT TABLE_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS "
                    + "WHERE TABLE_SCHEMA='aurora' "
                    + "AND (TABLE_NAME LIKE 'pet%' OR TABLE_NAME='app_user') "
                    + "AND COLUMN_NAME IN ('username','user_key','owner_key','friend_key','from_key','to_key','author','sender_key','receiver_key','reporter_key','follower_key','following_key') "
                    + "ORDER BY TABLE_NAME, COLUMN_NAME";
            try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) targets.add(new String[]{rs.getString(1), rs.getString(2)});
            }
            int total = 0;
            for (String[] t : targets) {
                if ("app_user".equals(t[0])) continue;
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM `" + t[0] + "` WHERE `" + t[1] + "`=?")) {
                    ps.setString(1, user);
                    int n = ps.executeUpdate();
                    if (n > 0) { System.out.println(t[0] + "." + t[1] + " deleted=" + n); total += n; }
                }
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM `app_user` WHERE username=?")) {
                ps.setString(1, user);
                int n = ps.executeUpdate();
                if (n > 0) { System.out.println("app_user.username deleted=" + n); total += n; }
            }
            System.out.println("TOTAL_DELETED=" + total);
        }
    }
}
