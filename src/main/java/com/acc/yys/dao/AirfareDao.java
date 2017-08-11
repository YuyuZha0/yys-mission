package com.acc.yys.dao;

import com.acc.yys.util.TcAirfareUtils;
import org.apache.ibatis.annotations.Insert;

/**
 * Created by zhaoyy on 2017/8/11.
 */
public interface AirfareDao {

    @Insert("insert into tb_tc_airfare_bargain (start_code,end_code,fly_date,price,discount,start_city,end_city,href,update_time) values " +
            "(#{startCode},#{endCode},#{flyDate},#{price},#{discount},#{startCity},#{endCity},#{href},now()) " +
            "on duplicate key update price=values(price),discount=values(discount),start_city=values(start_city)," +
            "end_city=values(end_city),href=values(href),update_time=values(update_time);")
    void insertOrUpdate(TcAirfareUtils.Airfare airfare);
}
