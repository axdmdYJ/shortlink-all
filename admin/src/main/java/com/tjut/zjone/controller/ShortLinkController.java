package com.tjut.zjone.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tjut.zjone.common.convention.result.Result;
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


    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        ShortLinkRemoteService remoteService = new ShortLinkRemoteService(){};
        return remoteService.createShortLink(requestParam);
    }

    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        ShortLinkRemoteService remoteService = new ShortLinkRemoteService(){};
        return remoteService.pageShortLink(requestParam);
    }

    @GetMapping("/api/short-link/v1/count")
    public  Result<List<GroupLinkCountRespDTO>> groupShortLinkCount(@RequestParam("requestParam") List<String> requestParam){
        ShortLinkRemoteService remoteService = new ShortLinkRemoteService(){};
        return remoteService.groupShortLinkCount(requestParam);
    }
}
