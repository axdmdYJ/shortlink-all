package com.tjut.zjone.remote;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tjut.zjone.common.convention.result.Result;
import com.tjut.zjone.dto.req.ShortLinkUpdateReqDTO;
import com.tjut.zjone.remote.dto.req.ShortLinkCreateReqDTO;
import com.tjut.zjone.remote.dto.req.ShortLinkPageReqDTO;
import com.tjut.zjone.remote.dto.resp.GroupLinkCountRespDTO;
import com.tjut.zjone.remote.dto.resp.ShortLinkCreateRespDTO;
import com.tjut.zjone.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ShortLinkRemoteService {

    default  Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        String resultStr = HttpUtil.post("http://localhost:8001/api/short-link/admin/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultStr, new TypeReference<Result<ShortLinkCreateRespDTO>>() {
        });
    }

    default  Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid",requestParam.getGid());
        requestMap.put("current",requestParam.getCurrent());
        requestMap.put("size",requestParam.getSize());
        String resultStr = HttpUtil.get("http://localhost:8001/api/short-link/admin/v1/page", requestMap);
        return JSON.parseObject(resultStr, new TypeReference<>() {
        });
    }

    default Result<List<GroupLinkCountRespDTO>> groupShortLinkCount(List<String> requestParam){
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestParam", requestParam);
        String resultStr = HttpUtil.get("http://localhost:8001/api/short-link/v1/count", requestMap);
        return JSON.parseObject(resultStr, new TypeReference<>() {
        });
    }

    default void updateShortLink(ShortLinkUpdateReqDTO requestParam){
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/update", JSON.toJSONString(requestParam));
    }
}
