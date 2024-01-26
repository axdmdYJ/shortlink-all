package com.tjut.zjone.controller;


import com.tjut.zjone.common.convention.result.Result;
import com.tjut.zjone.common.convention.result.Results;
import com.tjut.zjone.dto.req.ShortLinkCreateReqDTO;
import com.tjut.zjone.dto.resp.ShortLinkCreateRespDTO;
import com.tjut.zjone.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final LinkService linkService;

    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        return Results.success(linkService.createShortLink(requestParam));
    }

}
