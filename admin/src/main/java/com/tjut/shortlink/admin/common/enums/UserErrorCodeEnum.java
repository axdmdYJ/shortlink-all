package com.tjut.shortlink.admin.common.enums;

import com.tjut.shortlink.admin.common.convention.errorcode.IErrorCode;

public enum UserErrorCodeEnum implements IErrorCode {

    USER_TOKEN_FAIL("A000201", "用户token验证失败"),
    USER_NULL("B000200","用户不存在"),
    USER_NAME_EXISTS("B000201","用户名已存在"),
    USER_EXISTS("B000202", "用户记录已存在"),
    USER_SAVE_FAILE("B000203", "用户记录新增失败");
    public final String code;
    public final String message;

    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
