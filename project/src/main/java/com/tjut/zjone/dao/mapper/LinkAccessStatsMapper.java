package com.tjut.zjone.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tjut.zjone.dao.entity.LinkAccessStatsDO;
import com.tjut.zjone.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @description 针对表【t_link_access_stats】的数据库操作Mapper
*/
public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStatsDO> {

    /**
     * 记录基础访问监控数据
     */
    @Insert("INSERT INTO t_link_access_stats (full_short_url, gid, date, pv, uv, uip, hour, weekday, create_time, update_time, del_flag) " +
            "VALUES( #{linkAccessStats.fullShortUrl}, #{linkAccessStats.gid}, #{linkAccessStats.date}, #{linkAccessStats.pv}, #{linkAccessStats.uv}, #{linkAccessStats.uip}, #{linkAccessStats.hour}, #{linkAccessStats.weekday}, NOW(), NOW(), 0) ON DUPLICATE KEY UPDATE pv = pv +  #{linkAccessStats.pv}, " +
            "uv = uv + #{linkAccessStats.uv}, " +
            " uip = uip + #{linkAccessStats.uip};")
    void shortLinkStats(@Param("linkAccessStats") LinkAccessStatsDO linkAccessStatsDO);//@Param("linkAccessStats") 表示将 linkAccessStatsDO 参数命名为 "linkAccessStats"，以便在 SQL 语句中引用它。




    /**
     * 根据短链接获取指定日期内基础监控数据
     */
    @Select("SELECT " +
            "    date, " +
            "    SUM(pv) AS pv, " +
            "    SUM(uv) AS uv, " +
            "    SUM(uip) AS uip " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, date;")
    List<LinkAccessStatsDO> listStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内小时基础监控数据
     */
    @Select("SELECT " +
            "    hour, " +
            "    SUM(pv) AS pv " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, hour;")
    List<LinkAccessStatsDO> listHourStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内小时基础监控数据
     */
    @Select("SELECT " +
            "    weekday, " +
            "    SUM(pv) AS pv " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, weekday;")
    List<LinkAccessStatsDO> listWeekdayStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);
}



