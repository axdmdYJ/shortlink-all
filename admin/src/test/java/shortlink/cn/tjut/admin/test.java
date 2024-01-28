package shortlink.cn.tjut.admin;

public class test {
    public static void main(String[] args) {
        String SQL = "CREATE TABLE `t_group_%d` (\n" +
                "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
                "  `gid` varchar(32) NOT NULL COMMENT '分组标识',\n" +
                "  `name` varchar(256) NOT NULL COMMENT '分组名称',\n" +
                "  `username` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户名',\n" +
                "  `sort_order` int NOT NULL DEFAULT '0' COMMENT '分组排序',\n" +
                "  `create_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
                "  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',\n" +
                "  `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标识 0：未删除 1：已删除',\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  UNIQUE KEY `idx_unique_gid_username` (`gid`,`username`) USING BTREE COMMENT 'gid,username索引'\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;\n";
        for (int i = 0; i < 16; i++) {
            System.out.printf((SQL) + "%n",i);
        }
    }
}
