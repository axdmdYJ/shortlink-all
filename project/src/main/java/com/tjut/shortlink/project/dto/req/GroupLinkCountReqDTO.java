package com.tjut.shortlink.project.dto.req;


import lombok.Data;

@Data
public class GroupLinkCountReqDTO {

    /**
     * 分组标识
     */
    private String gid;


    /**
     * 用户名
     */
    private String username;
}
