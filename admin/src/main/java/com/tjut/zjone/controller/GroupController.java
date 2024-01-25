package com.tjut.zjone.controller;

import com.tjut.zjone.common.convention.result.Result;
import com.tjut.zjone.common.convention.result.Results;
import com.tjut.zjone.dto.req.GroupSaveNameReqDTO;
import com.tjut.zjone.dto.req.ShortLinkGroupSortDTO;
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

    /**
     * 查询短链接列表
     */
    @GetMapping("/api/short-link/admin/v1/group")
    public Result<List<ShortLinkGroupListRespDTO>> listGroup(){
        List<ShortLinkGroupListRespDTO> groupList = groupService.getGroupList();
        return Results.success(groupList);
    }

    /**
     * 更新短链接组
     * @param requestParam 分组标识和短链接组名
     */
    @PutMapping("/api/short-link/admin/v1/group")
    public Result<Void> updateGroup(@RequestBody ShortLinkGroupUpdateReqDTO requestParam){
        groupService.updateGroup(requestParam);
        return Results.success();
    }

    /**
     * 删除短链接组
     * @param gid 短链接组标识
     */
    @DeleteMapping("/api/short-link/admin/v1/group")
    public Result<Void> deleteGroup(@RequestParam("gid") String gid){
        groupService.deleteGroup(gid);
        return Results.success();
    }

    /**
     * 短链接分组排序
     * @param requestParam 分组标识和排序码
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/group/sort")
    public Result<Void> sortGroup(@RequestBody List<ShortLinkGroupSortDTO> requestParam){
        groupService.sortGroup(requestParam);
        return Results.success();
    }

}
