package com.tjut.zjone.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tjut.zjone.dao.entity.LinkDO;
import lombok.Data;

import java.util.List;

/**
 * 回收站短链接分页请求参数
 */
@Data
public class ShortLinkRecycleBinPageReqDTO extends Page<LinkDO> {

    /**
     * 分组标识
     */
    private List<String> gidList;

}