package com.tjut.zjone.common.enums;

import com.tjut.zjone.common.convention.errorcode.IErrorCode;

public enum UserErrorCodeEnum implements IErrorCode {

    USER_NULL("B000200","用户不存在"),
    USER_EXISTS("B000201","用户已存在");
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
