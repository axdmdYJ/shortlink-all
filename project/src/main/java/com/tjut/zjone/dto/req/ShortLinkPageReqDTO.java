package com.tjut.zjone.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tjut.zjone.dao.entity.LinkDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ShortLinkPageReqDTO extends Page<LinkDO> {

    /**
     * 分组标识
     */
    private String gid;


}
