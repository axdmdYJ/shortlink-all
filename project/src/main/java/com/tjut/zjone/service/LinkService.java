package com.tjut.zjone.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjut.zjone.dao.entity.LinkDO;
import com.tjut.zjone.dto.req.ShortLinkCreateReqDTO;
import com.tjut.zjone.dto.resp.ShortLinkCreateRespDTO;

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
}