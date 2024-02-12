package com.tjut.zjone.common.constant;

public class RedisCacheConstant {
    public static final String LOCK_USER_REGISTER_KEY = "short-link_lock_user-register";

    /**
     * 分组创建分布式锁
     */
    public static final String LOCK_GROUP_CREATE_KEY = "short-link:lock_group-create:%s";



    /**
     * 用户登录缓存标识
     */
    public static final String USER_LOGIN_KEY = "short-link:login:";
}
