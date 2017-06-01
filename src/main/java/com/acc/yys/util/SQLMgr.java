package com.acc.yys.util;

import com.google.common.collect.ImmutableMap;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by zhaoyy on 2017/5/23.
 */
public final class SQLMgr {

    private static final Logger logger = LoggerFactory.getLogger(SQLMgr.class);

    private SQLMgr() {

    }

    private static final Map<String, String> SQL_MAP = readSqlFromXml("sql.xml");

    public static String getSqlById(String id) {
        return SQL_MAP.get(id);
    }

    private static Map<String, String> readSqlFromXml(String fileName) {
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(fileName)) {
            final SAXReader reader = new SAXReader();
            final Document document = reader.read(inputStream);
            final Element root = document.getRootElement();
            @SuppressWarnings("unchecked")
            final Iterator<Element> iterator = root.elementIterator("sql");
            ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
            while (iterator.hasNext()) {
                Element element = iterator.next();
                String sqlId = element.attributeValue("id");
                String sql = element.getText();
                logger.info("resolve sql with id:#{}", sqlId);
                builder.put(sqlId, sql);
            }
            return builder.build();
        } catch (IOException | DocumentException e) {
            logger.error(e.getMessage(), e);
        }
        return ImmutableMap.of();
    }

}
