package com.acc.yys.util;

import com.acc.yys.dao.AirfareDao;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.session.SqlSession;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhaoyy on 2017/8/11.
 */
public final class TcAirfareUtils {

    private static final Logger logger = LoggerFactory.getLogger(TcAirfareUtils.class);

    private static final String URL = "https://www.ly.com/Flight/FlightBookAjax.aspx?Type=GETFEWSELECTCITYFLIGHT&_dAjax=callback&&code=%s&callback=%s";

    private TcAirfareUtils() {

    }

    private static String getResponseBody(String code, String callback) {
        final String url = String.format(URL, code, callback);
        try {
            return Jsoup.connect(url)
                    .header("accept-encoding", "gzip, deflate, br")
                    .header("accept-language", "zh-CN,zh;q=0.8")
                    .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36")
                    .ignoreContentType(true)
                    .execute()
                    .body();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    private static String extractJson(String s, String callback) {
        if (s == null || s.isEmpty())
            return "[]";
        String regex = String.format("(?<=%s\\(\\{\"state\":\"100\",\"list\":).+(?=\\}\\))", callback);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        if (matcher.find())
            return matcher.group();
        return "[]";
    }

    private static List<Airfare> parseJson(String content, ObjectMapper mapper) {
        try {
            return Arrays.asList(mapper.readValue(content, Airfare[].class));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private static void saveToMysql(List<Airfare> list, SqlSession session) {
        AirfareDao dao = session.getMapper(AirfareDao.class);
        for (Airfare airfare : list)
            dao.insertOrUpdate(airfare);
        session.commit();
        session.clearCache();
    }

    public static void startDumpingData(SqlSession session) {
        final ObjectMapper mapper = new ObjectMapper();
        List<Airfare> list = new ArrayList<>();
        Arrays.asList("PEK", "SHA", "NKG")
                .forEach(code -> {
                    String callback = "tc" + System.currentTimeMillis();
                    String body = getResponseBody(code, callback);
                    String content = extractJson(body, callback);
                    list.addAll(parseJson(content, mapper));
                });
        saveToMysql(list, session);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Airfare implements Serializable {

        private final String startCode;
        private final String endCode;
        private final String flyDate;
        private final Integer price;
        private final Integer discount;
        private final String startCity;
        private final String endCity;
        private final String href;

        @JsonCreator
        public Airfare(@JsonProperty("Start") String startCode,
                       @JsonProperty("End") String endCode,
                       @JsonProperty("Date") String flyDate,
                       @JsonProperty("Price") Integer price,
                       @JsonProperty("Discount") Integer discount,
                       @JsonProperty("StartCity") String startCity,
                       @JsonProperty("EndCity") String endCity,
                       @JsonProperty("Href") String href) {
            this.startCode = startCode;
            this.endCode = endCode;
            this.flyDate = flyDate;
            this.price = price;
            this.discount = discount;
            this.startCity = startCity;
            this.endCity = endCity;
            this.href = href;
        }

        public String getStartCode() {
            return startCode;
        }

        public String getEndCode() {
            return endCode;
        }

        public String getFlyDate() {
            return flyDate;
        }

        public Integer getPrice() {
            return price;
        }

        public Integer getDiscount() {
            return discount;
        }

        public String getStartCity() {
            return startCity;
        }

        public String getEndCity() {
            return endCity;
        }

        public String getHref() {
            return href;
        }

        @Override
        public String toString() {
            return "Airfare{" +
                    "startCode='" + startCode + '\'' +
                    ", endCode='" + endCode + '\'' +
                    ", flyDate='" + flyDate + '\'' +
                    ", price=" + price +
                    ", discount=" + discount +
                    ", startCity='" + startCity + '\'' +
                    ", endCity='" + endCity + '\'' +
                    ", href='" + href + '\'' +
                    '}';
        }


    }

}
