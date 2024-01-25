package com.tjut.zjone.dto.req;


import lombok.Data;

/**
 * 修改短链接分组参数
 */
@Data
public class ShortLinkGroupUpdateReqDTO {
    /**
     * 短链接分组标识
     */
    private String gid;


    /**
     * 短链接组名
     */
    private String name;

}
