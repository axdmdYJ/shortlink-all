package com.tjut.zjone.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjut.zjone.dao.entity.LinkStatsTodayDO;
import com.tjut.zjone.dao.mapper.LinkStatsTodayMapper;
import com.tjut.zjone.service.LinkStatsTodayService;
import org.springframework.stereotype.Service;

/**
 * 短链接今日统计接口实现层
 */
@Service
public class LinkStatsTodayServiceImpl extends ServiceImpl<LinkStatsTodayMapper, LinkStatsTodayDO>
        implements LinkStatsTodayService {

}