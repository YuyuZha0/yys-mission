package com.acc.yys.dao;

import com.acc.yys.util.SQLMgr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

/**
 * Created by zhaoyy on 2017/5/27.
 */
@Repository
public class MessageDao {

    private final JdbcTemplate template;

    @Autowired
    public MessageDao(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    public int insertMsg(String username, String email, String msg) {
        return template.update(SQLMgr.getSqlById("insertMsg"), username, email, msg);
    }
}
