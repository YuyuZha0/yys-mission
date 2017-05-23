package com.acc.yys.dao;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoyy on 2017/5/23.
 */
@Repository
public final class CharacterDao {

    private final JdbcTemplate template;

    @Autowired
    public CharacterDao(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }


    public List<String> queryAllCharacterNames() {
        return template.query("", new ResultSetExtractor<List<String>>() {
            @Override
            public List<String> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                List<String> list = new ArrayList<>();
                while (resultSet.next()) {
                    list.add(resultSet.getString(1));
                }
                return ImmutableList.copyOf(list);
            }
        });
    }
}
