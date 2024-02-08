package com.tjut.zjone.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tjut.zjone.common.convention.result.Result;
import com.tjut.zjone.common.convention.result.Results;
import com.tjut.zjone.dto.req.ShortLinkBatchCreateReqDTO;
import com.tjut.zjone.dto.req.ShortLinkCreateReqDTO;
import com.tjut.zjone.dto.req.ShortLinkPageReqDTO;
import com.tjut.zjone.dto.req.ShortLinkUpdateReqDTO;
import com.tjut.zjone.dto.resp.GroupLinkCountRespDTO;
import com.tjut.zjone.dto.resp.ShortLinkBatchCreateRespDTO;
import com.tjut.zjone.dto.resp.ShortLinkCreateRespDTO;
import com.tjut.zjone.dto.resp.ShortLinkPageRespDTO;
import com.tjut.zjone.service.LinkService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final LinkService linkService;

    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        return Results.success(linkService.createShortLink(requestParam));
    }

    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        return Results.success(linkService.pageShortLink(requestParam));
    }

    @GetMapping("/api/short-link/v1/count")
    public  Result<List<GroupLinkCountRespDTO>> groupShortLinkCount(@RequestParam("requestParam") List<String> requestParam){
        return Results.success(linkService.groupLinkCount(requestParam));
    }

    /**
     * 更新短链接
     */
    @PostMapping("/api/short-link/v1/update")
    public Result<Void> shortLinkUpdate(@RequestBody ShortLinkUpdateReqDTO requestParam){
        linkService.updateShortLink(requestParam);
        return Results.success();
    }


    @GetMapping("/{short-uri}")
    public void restoreUrl(@PathVariable("short-uri") String shortUri, ServletRequest request, ServletResponse response) {
        linkService.restoreUrl(shortUri, request, response);
    }

    /**
     * 批量创建短链接
     */
    @PostMapping("/api/short-link/v1/create/batch")
    public Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam) {
        return Results.success(linkService.batchCreateShortLink(requestParam));
    }


}
