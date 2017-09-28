package com.acc.yys.service.impl;

import com.acc.yys.dao.StaticDataDao;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public FuzzyQueryServiceImpl(@Autowired StaticDataDao dao) {
        List<QueryIndex> indices = new ArrayList<>();
        Map<String, String> tipsMap = new HashMap<>();
        Map<String, Character> characterMap = new HashMap<>();
        try {
            for (Map<String, Object> m : dao.queryCharacterList()) {
                String name = (String) m.get("character_name");
                String imageName = (String) m.get("image_name");
                String quality = (String) m.get("quality");
                characterMap.put(name, new Character(name, imageName, quality));
                String pinyin = PinyinHelper.convertToPinyinString(name, "", PinyinFormat.WITHOUT_TONE);
                String shortPinyin = PinyinHelper.getShortPinyin(name);
                indices.add(new QueryIndex(name, pinyin, shortPinyin));
            }
            for (Map<String, Object> m : dao.queryTipList()) {
                String content = (String) m.get("content");
                String ref = (String) m.get("ref");
                tipsMap.put(content, ref);
                String pinyin = PinyinHelper.convertToPinyinString(content, "", PinyinFormat.WITHOUT_TONE);
                String shortPinyin = PinyinHelper.getShortPinyin(content);
                indices.add(new QueryIndex(content, pinyin, shortPinyin));
            }

        } catch (PinyinException e) {
            logger.error(e.getMessage(), e);
        }
        this.indices = ImmutableList.copyOf(indices);
        logger.info("create indices on [{}] vocabs.", this.indices.size());
        this.tipsMap = ImmutableMap.copyOf(tipsMap);
        logger.info("find [{}] tips.", tipsMap.size());
        this.characterMap = ImmutableMap.copyOf(characterMap);
        logger.info("find [{}] character map.", characterMap.size());
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
        return CharMatcher.ascii().matchesAllOf(query);
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
        return ImmutableList.copyOf(result.subList(0, Math.min(5, result.size())));
    }

    private static float getMatchScore(String source, String target) {
        int d = FastStrings.editDistance(source, target);
        return 1 - (d + 0.0f) / Math.max(source.length(), target.length());
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
        if (characterName == null || characterName.isEmpty())
            return null;
        Character character = characterMap.get(characterName);
        if (character != null)
            return character;
        String tip = tipsMap.get(characterName);
        if (tip != null) {
            return characterMap.get(tip);
        }
        List<String> guess = guessRealMeaning(characterName, 1);
        if (guess.isEmpty())
            return null;
        return queryCharacter(guess.get(0));
    }

    @FunctionalInterface
    private interface Scorer {
        String getScoreFiled(QueryIndex index);
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
            if (o.score == score)
                return o.realName.compareTo(realName);
            return Floats.compare(o.score, score);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            QueryScore score1 = (QueryScore) o;

            if (Float.compare(score1.score, score) != 0) return false;
            return realName.equals(score1.realName);

        }

        @Override
        public int hashCode() {
            int result = realName.hashCode();
            result = 31 * result + (score != +0.0f ? Float.floatToIntBits(score) : 0);
            return result;
        }
    }
}
