import java.sql.*;
import java.util.*;

class AddTableComments {
    static final Map<String, String> COMMENTS = new LinkedHashMap<>();
    static {
        COMMENTS.put("app_user", "用户账户");
        COMMENTS.put("pet", "用户宠物");
        COMMENTS.put("pet_breed", "宠物品种目录");
        COMMENTS.put("pet_category", "宠物分类目录");
        COMMENTS.put("pet_follow", "用户关注关系");
        COMMENTS.put("pet_friend", "好友关系");
        COMMENTS.put("pet_gift_log", "好友赠送记录（真实赠送/领取）");
        COMMENTS.put("pet_interact_log", "每日互动次数");
        COMMENTS.put("pet_knowledge", "养宠知识文章");
        COMMENTS.put("pet_message", "用户私信消息");
        COMMENTS.put("pet_post", "社区帖子");
        COMMENTS.put("pet_post_comment", "帖子评论");
        COMMENTS.put("pet_post_favorite", "帖子收藏");
        COMMENTS.put("pet_post_like", "帖子点赞");
        COMMENTS.put("pet_post_media", "帖子媒体（图片/视频）");
        COMMENTS.put("pet_post_report", "帖子举报记录");
        COMMENTS.put("pet_species", "宠物种类目录");
        COMMENTS.put("pet_task_progress", "每日任务进度");
        COMMENTS.put("pet_user_resource", "用户养成资源");
    }

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/aurora?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
        String dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword == null || dbPassword.isEmpty()) {
            System.err.println("请先设置环境变量 DB_PASSWORD");
            return;
        }
        try (Connection conn = DriverManager.getConnection(url, "root", dbPassword)) {
            System.out.println("=== 修改前 ===");
            Map<String, String> before = readComments(conn);
            for (String t : COMMENTS.keySet()) {
                String c = before.getOrDefault(t, "<表不存在>");
                System.out.println(pad(t) + " -> " + (c.isEmpty() ? "(空)" : c));
            }

            System.out.println("=== 执行 ALTER（仅注释为空的表）===");
            int changed = 0;
            try (Statement st = conn.createStatement()) {
                for (Map.Entry<String, String> e : COMMENTS.entrySet()) {
                    String t = e.getKey();
                    String cur = before.get(t);
                    if (cur == null || !cur.isEmpty()) {
                        System.out.println(pad(t) + " : 已有注释，跳过");
                        continue;
                    }
                    String sql = "ALTER TABLE `" + t + "` COMMENT = '" + e.getValue() + "'";
                    st.executeUpdate(sql);
                    changed++;
                    System.out.println(pad(t) + " : 已添加 -> " + e.getValue());
                }
            }
            System.out.println("共修改 " + changed + " 张表");

            System.out.println("=== 修改后 ===");
            Map<String, String> after = readComments(conn);
            for (String t : COMMENTS.keySet()) {
                String c = after.getOrDefault(t, "<表不存在>");
                System.out.println(pad(t) + " -> " + (c.isEmpty() ? "(空)" : c));
            }
        }
    }

    static Map<String, String> readComments(Connection conn) throws Exception {
        Map<String, String> m = new HashMap<>();
        String sql = "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA='aurora'";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) m.put(rs.getString(1), rs.getString(2));
        }
        return m;
    }

    static String pad(String s) {
        StringBuilder b = new StringBuilder(s);
        while (b.length() < 20) b.append(' ');
        return b.toString();
    }
}
