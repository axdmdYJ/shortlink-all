package com.tjut.zjone.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjut.zjone.dao.entity.LinkAccessStatsDO;
import com.tjut.zjone.service.LinkAccessStatsService;
import com.tjut.zjone.dao.mapper.LinkAccessStatsMapper;
import org.springframework.stereotype.Service;

/**
* @description 针对表【t_link_access_stats】的数据库操作Service实现
*/
@Service
public class LinkAccessStatsServiceImpl extends ServiceImpl<LinkAccessStatsMapper, LinkAccessStatsDO>
    implements LinkAccessStatsService{

}




