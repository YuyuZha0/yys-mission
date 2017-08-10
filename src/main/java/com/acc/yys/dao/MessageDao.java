package com.acc.yys.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * Created by zhaoyy on 2017/5/27.
 */
public interface MessageDao {

    @Insert("insert into tb_msg(username,email,msg) values (#{username},#{email},#{msg});")
    void insertMsg(@Param("username") String username, @Param("email") String email, @Param("msg") String msg);
}
