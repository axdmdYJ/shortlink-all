package com.tjut.zjone.dto.req;

import lombok.Data;

@Data
public class ShortLinkGroupSortDTO {


    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组排序
     */
    private Integer sortOrder;

}
