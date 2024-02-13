package com.tjut.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ShortLinkPageReqDTO extends Page{

    /**
     * 分组标识
     */
    private String gid;


    /**
     * 排序标识
     */
    private String orderTag;
}
