package com.acc.yys.service.impl;

import com.acc.yys.util.TcAirfareUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Created by zhaoyy on 2017/8/11.
 */
//@Component
public final class TcAirfareScheduler {

    @Autowired
    private SqlSession sqlSession;

    @Scheduled(cron = "${tc.crawler.cron}")
    public void getDataSilently() {
        TcAirfareUtils.startDumpingData(sqlSession);
    }
}
