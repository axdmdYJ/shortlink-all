package com.tjut.zjone.controller;

import com.tjut.zjone.common.convention.result.Result;
import com.tjut.zjone.common.convention.result.Results;
import com.tjut.zjone.dto.req.GroupSaveNameReqDTO;
import com.tjut.zjone.dto.req.ShortLinkGroupUpdateReqDTO;
import com.tjut.zjone.dto.resp.ShortLinkGroupListRespDTO;
import com.tjut.zjone.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupController {
    public final GroupService groupService;

    /**
     * 新增短链接分组
     */
    @PostMapping("/api/short-link/admin/v1/group")
    public Result<Void> saveGroupName(@RequestBody GroupSaveNameReqDTO requestParam){
        groupService.saveGroupName(requestParam);
        return Results.success();
    }
    @GetMapping("/api/short-link/admin/v1/group")
    public Result<List<ShortLinkGroupListRespDTO>> listGroup(){
        List<ShortLinkGroupListRespDTO> groupList = groupService.getGroupList();
        return Results.success(groupList);
    }

    @PutMapping("/api/short-link/admin/v1/group")
    public Result<Void> updateGroup(@RequestBody ShortLinkGroupUpdateReqDTO requestParam){
        groupService.updateGroup(requestParam);
        return Results.success();
    }

}
