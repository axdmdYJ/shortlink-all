package com.tjut.zjone.remote.dto.req;

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


}
