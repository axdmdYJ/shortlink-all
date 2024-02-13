package com.tjut.shortlink.project.dao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tjut.shortlink.project.dao.entity.LinkDO;
import com.tjut.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
* @author a0000
* @description 针对表【t_link】的数据库操作Mapper
*/
public interface LinkMapper extends BaseMapper<LinkDO> {



    /**
     * 短链接访问统计自增
     */
    @Update("update t_link " +
            "set " +
            "total_pv = total_pv + #{totalPv}," + " total_uv = total_uv + #{totalUv}, total_uip = total_uip + #{totalUip}" +
            " where " +
            "gid = #{gid} and full_short_url = #{fullShortUrl}")
    void incrementStats(
            @Param("gid") String gid,
            @Param("fullShortUrl") String fullShortUrl,
            @Param("totalPv") Integer totalPv,
            @Param("totalUv") Integer totalUv,
            @Param("totalUip") Integer totalUip
    );




    /**
     * 分页统计短链接
     */
    IPage<LinkDO> pageLink(ShortLinkPageReqDTO requestParam);
}




