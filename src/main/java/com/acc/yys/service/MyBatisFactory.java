package com.acc.yys.service;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhaoyy on 2017/8/10.
 */
public final class MyBatisFactory {

    private static final Logger logger = LoggerFactory.getLogger(MyBatisFactory.class);

    private MyBatisFactory() {

    }

    public static SqlSessionFactory sqlSessionFactory() {
        return MyBatisHolder.FACTORY;
    }

    private static SqlSessionFactory createSqlSessionFactory() {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("mybatis-config.xml")) {
            return new SqlSessionFactoryBuilder()
                    .build(is);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        throw new IllegalArgumentException("cannot create sqlSessionFactory");
    }

    private static final class MyBatisHolder {
        static final SqlSessionFactory FACTORY = createSqlSessionFactory();
    }
}
