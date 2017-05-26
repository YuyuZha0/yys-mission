package com.acc.yys.service.impl;

import com.acc.yys.pojo.Chapter;
import com.acc.yys.pojo.Character;
import com.acc.yys.service.QueryService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaoyy on 2017/5/23.
 */
@Service
public final class QueryServiceImpl implements QueryService{



    private final LoadingCache<String, List<Chapter>> cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build(new CacheLoader<String, List<Chapter>>() {
                @Override
                public List<Chapter> load(String key) throws Exception {
                    //TODO
                    return null;
                }
            });

    public List<Chapter> queryChapterByCharacterName(String characterName) {
        if (characterName == null || characterName.isEmpty())
            return Collections.emptyList();
        return cache.getUnchecked(characterName);
    }

    @Override
    public List<QueryResult> queryChapter(Character character) {
        return null;
    }
}
