package com.acc.yys.service;

import com.acc.yys.dao.CharacterDao;
import com.acc.yys.util.FastStrings;
import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by zhaoyy on 2017/5/23.
 */
@Service
public final class FuzzyQueryService {

    private final List<QueryIndex> indices;

    private static final Logger logger = LoggerFactory.getLogger(FuzzyQueryService.class);

    @Autowired
    public FuzzyQueryService(CharacterDao characterDao) {
        List<String> nameList = characterDao.queryAllCharacterNames();
        List<QueryIndex> indices = new ArrayList<>(nameList.size());
        for (String name : nameList) {
            try {
                String pinyin = PinyinHelper.convertToPinyinString(name, "", PinyinFormat.WITHOUT_TONE);
                String shortPinyin = PinyinHelper.getShortPinyin(name);
                indices.add(new QueryIndex(name, pinyin, shortPinyin));
            } catch (PinyinException e) {
                logger.error(e.getMessage(), e);
            }
        }
        this.indices = ImmutableList.copyOf(indices);
    }

    public List<QueryRanker> guessFuzzy(String fuzzy, int limit) {
        return guessFuzzy(fuzzy, limit, indices);
    }

    private static List<QueryRanker> guessFuzzy(String fuzzy, int limit, List<QueryIndex> indices) {
        if (fuzzy == null || fuzzy.length() == 0)
            return Collections.emptyList();
        List<QueryRanker> rankerList = new ArrayList<>();
        if (CharMatcher.javaLetter().matchesAllOf(fuzzy)) {
            fuzzy = fuzzy.toLowerCase();
            if (CharMatcher.anyOf("aeiou").matchesNoneOf(fuzzy)) {
                for (QueryIndex index : indices) {
                    rankerList.add(new QueryRanker(index.cn, getScore(fuzzy, index.shortPinyin)));
                }
            } else {
                for (QueryIndex index : indices) {
                    rankerList.add(new QueryRanker(index.cn, getScore(fuzzy, index.pinyin)));
                }
            }
        } else {
            for (QueryIndex index : indices) {
                rankerList.add(new QueryRanker(index.cn, getScore(fuzzy, index.cn)));
            }
        }
        Collections.sort(rankerList);
        return ImmutableList.copyOf(rankerList.subList(0, Math.min(limit, rankerList.size())));
    }

    private static int getScore(String source, String target) {
        int n = FastStrings.editDistance(source, target);
        return source.length() - target.length() - n;
    }


    private static final class QueryIndex {
        private final String cn;
        private final String pinyin;
        private final String shortPinyin;

        public QueryIndex(String cn, String pinyin, String shortPinyin) {
            this.cn = cn;
            this.pinyin = pinyin;
            this.shortPinyin = shortPinyin;
        }
    }

    public static final class QueryRanker implements Serializable, Comparable<QueryRanker> {


        private static final long serialVersionUID = 1L;
        private final String query;
        private final int score;

        public QueryRanker(String query, int score) {
            this.query = query;
            this.score = score;
        }

        @Override
        public int compareTo(QueryRanker o) {
            return Integer.compare(o.score, score);
        }

        public String getQuery() {
            return query;
        }

        public int getScore() {
            return score;
        }
    }

    public static void main(String[] args) throws PinyinException {
        System.out.println(PinyinHelper.convertToPinyinString("中国", "", PinyinFormat.WITHOUT_TONE));
    }
}
