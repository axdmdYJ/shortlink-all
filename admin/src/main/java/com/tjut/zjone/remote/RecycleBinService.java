package com.tjut.zjone.remote;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tjut.zjone.common.convention.result.Result;
import com.tjut.zjone.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.tjut.zjone.remote.dto.resp.ShortLinkPageRespDTO;

/**
 * URL 回收站接口层
 */
public interface RecycleBinService {

    /**
     * 分页查询回收站短链接
     *
     * @param requestParam 请求参数
     * @return 返回参数包装
     */
    Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam);
}