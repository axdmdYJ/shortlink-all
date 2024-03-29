package com.tjut.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tjut.shortlink.project.dao.entity.LinkDO;
import com.tjut.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.tjut.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.tjut.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.tjut.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.tjut.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.tjut.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.tjut.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.tjut.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.tjut.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.util.List;

/**
* @author a0000
* @description 针对表【t_link】的数据库操作Service
* @createDate 2024-01-26 19:17:33
*/
public interface LinkService extends IService<LinkDO> {

    /**
     * 创建短链接
     * @param requestParam 创建短链接参数
     * @return 组标识 完整短链接
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);

    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String>  requestParam);

    void updateShortLink(ShortLinkUpdateReqDTO requestParam);

    void restoreUrl(String shortUri, ServletRequest request, ServletResponse response);

    /**
     * 批量创建短链接
     *
     * @param requestParam 批量创建短链接请求参数
     * @return 批量创建短链接返回参数
     */
    ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam);


    /**
     * 短链接统计
     *
     * @param fullShortUrl         完整短链接
     * @param gid                  分组标识
     * @param shortLinkStatsRecord 短链接统计实体参数
     */
    void shortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO shortLinkStatsRecord);

}
