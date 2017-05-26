package com.acc.yys.service.impl;

import com.acc.yys.pojo.Character;
import com.acc.yys.service.FuzzyQueryService;
import com.acc.yys.util.FastStrings;
import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.google.common.base.CharMatcher;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Floats;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhaoyy on 2017/5/23.
 */
@Service
public final class FuzzyQueryServiceImpl implements FuzzyQueryService {

    private static final Logger logger = LoggerFactory.getLogger(FuzzyQueryServiceImpl.class);
    private static final Pattern notQueryPtn = Pattern.compile("[^\u4e00-\u9fa5a-zA-Z]+");

    private final List<QueryIndex> indices;
    private final Map<String, String> tipsMap;
    private final Map<String, Character> characterMap;
    private final LoadingCache<String, List<String>> queryCache = CacheBuilder
            .newBuilder()
            .maximumSize(10000)
            .build(new CacheLoader<String, List<String>>() {
                @Override
                public List<String> load(String key) throws Exception {
                    return getRankedNameList(key, indices);
                }
            });

    @SuppressWarnings("unchecked")
    public FuzzyQueryServiceImpl() {
        List<QueryIndex> indices = new ArrayList<>();
        Map<String, String> tipsMap = new HashMap<>();
        Map<String, Character> characterMap = new HashMap<>();
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("characters.xml")) {
            SAXReader reader = new SAXReader();
            Document document = reader.read(is);
            Element root = document.getRootElement();
            final Iterator<Element> characterIterator = root.elementIterator("character");
            while (characterIterator.hasNext()) {
                Element e = characterIterator.next();
                String name = e.attributeValue("name");
                String imageName = e.attributeValue("image-name");
                String quality = e.attributeValue("quality");
                characterMap.put(name, new Character(name, imageName, quality));
                String pinyin = PinyinHelper.convertToPinyinString(name, "", PinyinFormat.WITHOUT_TONE);
                String shortPinyin = PinyinHelper.getShortPinyin(name);
                indices.add(new QueryIndex(name, pinyin, shortPinyin));
            }
            final Iterator<Element> tipIterator = root.elementIterator("tip");
            while (tipIterator.hasNext()) {
                Element e = tipIterator.next();
                String content = e.attributeValue("content");
                String ref = e.attributeValue("ref");
                tipsMap.put(content, ref);
                String pinyin = PinyinHelper.convertToPinyinString(content, "", PinyinFormat.WITHOUT_TONE);
                String shortPinyin = PinyinHelper.getShortPinyin(content);
                indices.add(new QueryIndex(content, pinyin, shortPinyin));
            }

        } catch (IOException | DocumentException | PinyinException e) {
            logger.error(e.getMessage(), e);
        }
        this.indices = ImmutableList.copyOf(indices);
        this.tipsMap = ImmutableMap.copyOf(tipsMap);
        this.characterMap = ImmutableMap.copyOf(characterMap);
    }

    @Override
    public List<String> guessRealMeaning(String query, int limit) {
        if (query == null || query.isEmpty())
            return Collections.emptyList();
        List<String> result = queryCache.getUnchecked(query);
        if (limit < 0)
            return ImmutableList.copyOf(result);
        return ImmutableList.copyOf(result.subList(0, Math.min(limit, result.size())));
    }

    @Override
    public Character queryCharacter(String characterName) {
        return null;
    }

    private static List<String> getRankedNameList(String query, List<QueryIndex> indices) {
        query = trimQuery(query);
        if (query.isEmpty())
            return Collections.emptyList();
        if (isAllEnglishLetter(query)) {
            query = query.toLowerCase();
            if (isShortPinyinPattern(query)) {
                return queryAndRank(query, indices, index -> index.shortPinyin);
            } else {
                return queryAndRank(query, indices, index -> index.pinyin);
            }
        }
        return queryAndRank(query, indices, index -> index.realName);
    }


    private static String trimQuery(String query) {
        Matcher matcher = notQueryPtn.matcher(query);
        return matcher.replaceAll("");
    }

    private static boolean isAllEnglishLetter(String query) {
        return CharMatcher.javaLetter().matchesAllOf(query);
    }

    private static boolean isShortPinyinPattern(String query) {
        return CharMatcher.anyOf("aeiou").matchesNoneOf(query);
    }

    private static List<String> queryAndRank(String query, List<QueryIndex> indices, Scorer scorer) {
        List<QueryScore> scoreList = new ArrayList<>();
        for (QueryIndex index : indices) {
            String field = scorer.getScoreFiled(index);
            float score = getMatchScore(query, field);
            if (score == 0)
                continue;
            scoreList.add(new QueryScore(index.realName, score));
        }
        Collections.sort(scoreList);
        List<String> result = new ArrayList<>();
        for (QueryScore score : scoreList) {
            //System.out.println(score.realName + ":" + score.score);
            result.add(score.realName);
        }
        return ImmutableList.copyOf(result);
    }

    private static final class QueryIndex {

        private final String realName;
        private final String pinyin;
        private final String shortPinyin;

        QueryIndex(String realName, String pinyin, String shortPinyin) {
            //System.out.println(realName + ":" + pinyin + ":" + shortPinyin);
            this.realName = realName;
            this.pinyin = pinyin;
            this.shortPinyin = shortPinyin;
        }
    }

    private static final class QueryScore implements Comparable<QueryScore> {

        private final String realName;
        private final float score;

        QueryScore(String realName, float score) {
            this.realName = realName;
            this.score = score;
        }


        @Override
        public int compareTo(QueryScore o) {
            return Floats.compare(o.score, score);
        }
    }

    @FunctionalInterface
    private interface Scorer {
        String getScoreFiled(QueryIndex index);
    }


    private static float getMatchScore(String source, String target) {
        int d = FastStrings.editDistance(source, target);
        return 1 - (d + 0.0f) / Math.max(source.length(), target.length());
    }

    public static void main(String[] args) {
        FuzzyQueryServiceImpl service = new FuzzyQueryServiceImpl();
        System.out.println(service.guessRealMeaning("txg", -1));
    }
}
