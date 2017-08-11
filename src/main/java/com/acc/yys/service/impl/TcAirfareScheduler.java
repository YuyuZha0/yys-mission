package com.acc.yys.service.impl;

import com.acc.yys.util.TcAirfareUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by zhaoyy on 2017/8/11.
 */
@Component
public final class TcAirfareScheduler {


    @Scheduled(cron = "0 0 4,16 * * *")
    public void getDataSilently() {
        TcAirfareUtils.startDumpingData();
    }
}
