package com.tjut.zjone.remote.dto.resp;


import lombok.Data;

@Data
public class GroupLinkCountRespDTO {


    /**
     * 短链接标识
     */
    private String gid;
    /**
     * 链接数量
     */
    private Integer shortLinkCount;
}
