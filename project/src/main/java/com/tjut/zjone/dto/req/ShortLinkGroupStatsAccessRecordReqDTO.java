package com.tjut.zjone.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tjut.zjone.dao.entity.LinkAccessLogsDO;
import lombok.Data;

@Data
public class ShortLinkGroupStatsAccessRecordReqDTO extends Page<LinkAccessLogsDO> {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;
}