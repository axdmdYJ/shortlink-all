package com.tjut.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tjut.shortlink.project.dao.entity.LinkDO;
import com.tjut.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.tjut.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.tjut.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.tjut.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.tjut.shortlink.project.dto.resp.ShortLinkPageRespDTO;

/**
 * 回收站管理接口层
 */
public interface RecycleBinService extends IService<LinkDO> {

    /**
     * 保存回收站
     *
     * @param requestParam 请求参数
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);

    /**
     * 分页查询短链接
     *
     * @param requestParam 分页查询短链接请求参数
     * @return 短链接分页返回结果
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam);

    void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam);

    void removeRecycleBin(RecycleBinRemoveReqDTO requestParam);
}