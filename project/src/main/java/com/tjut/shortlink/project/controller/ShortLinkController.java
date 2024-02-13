package com.tjut.shortlink.project.controller;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tjut.shortlink.project.common.convention.result.Result;
import com.tjut.shortlink.project.common.convention.result.Results;
import com.tjut.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.tjut.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.tjut.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.tjut.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.tjut.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.tjut.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.tjut.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.tjut.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.tjut.shortlink.project.handler.CustomBlockHandler;
import com.tjut.shortlink.project.service.LinkService;
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
    @SentinelResource(
            value = "create_short-link", // 标识资源的名称
            blockHandler = "createShortLinkBlockHandlerMethod", // 指定熔断降级处理的方法名
            blockHandlerClass = CustomBlockHandler.class // 指定熔断降级处理方法所在的类
    )
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        return Results.success(linkService.createShortLink(requestParam));
    }

    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        return Results.success(linkService.pageShortLink(requestParam));
    }

    @GetMapping("/api/short-link/v1/count")
    public  Result<List<ShortLinkGroupCountQueryRespDTO>> groupShortLinkCount(@RequestParam("requestParam") List<String> requestParam){
        return Results.success(linkService.listGroupShortLinkCount(requestParam));
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
