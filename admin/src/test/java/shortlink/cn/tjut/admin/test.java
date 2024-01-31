package shortlink.cn.tjut.admin;

public class test {
    public static void main(String[] args) {
        String SQL = "CREATE TABLE `t_link_goto_%d` (\n" +
                "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
                "  `full_short_url` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '完整短链接',\n" +
                "  `gid` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '分组标识',\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;";
        for (int i = 0; i < 16; i++) {
            System.out.printf((SQL) + "%n",i);
        }
    }
}
