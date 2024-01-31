package com.tjut.zjone.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tjut.zjone.common.convention.result.Result;
import com.tjut.zjone.common.convention.result.Results;
import com.tjut.zjone.dto.req.ShortLinkUpdateReqDTO;
import com.tjut.zjone.remote.ShortLinkRemoteService;
import com.tjut.zjone.remote.dto.req.ShortLinkCreateReqDTO;
import com.tjut.zjone.remote.dto.req.ShortLinkPageReqDTO;
import com.tjut.zjone.remote.dto.resp.GroupLinkCountRespDTO;
import com.tjut.zjone.remote.dto.resp.ShortLinkCreateRespDTO;
import com.tjut.zjone.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ShortLinkController {
    /**
     * 后续改为feign调用
     */
    private final ShortLinkRemoteService remoteService = new ShortLinkRemoteService(){};
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        return remoteService.createShortLink(requestParam);
    }

    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        return remoteService.pageShortLink(requestParam);
    }

    @GetMapping("/api/short-link/v1/count")
    public  Result<List<GroupLinkCountRespDTO>> groupShortLinkCount(@RequestParam("requestParam") List<String> requestParam){
        return remoteService.groupShortLinkCount(requestParam);
    }

    /**
     * 修改短链接
     */
    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        remoteService.updateShortLink(requestParam);
        return Results.success();
    }



}
