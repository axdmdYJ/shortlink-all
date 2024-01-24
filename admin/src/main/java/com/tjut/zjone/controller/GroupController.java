package com.tjut.zjone.controller;

import com.tjut.zjone.common.convention.result.Result;
import com.tjut.zjone.common.convention.result.Results;
import com.tjut.zjone.dto.resq.GroupSaveNameReqDTO;
import com.tjut.zjone.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GroupController {
    public final GroupService groupService;

    @PostMapping("/api/short-link/admin/v1/group")
    public Result<Void> saveGroupName(@RequestBody GroupSaveNameReqDTO requestParam){
        groupService.saveGroupName(requestParam);
        return Results.success();
    }

}
