package com.acc.yys.service.impl;

import com.acc.yys.dao.StaticDataDao;
import com.acc.yys.pojo.Character;
import com.acc.yys.pojo.CharacterDistribution;
import com.acc.yys.service.QueryService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by zhaoyy on 2017/5/23.
 */
@Service
public final class QueryServiceImpl implements QueryService {


    private static final Logger logger = LoggerFactory.getLogger(QueryServiceImpl.class);
    private final Map<String, List<QueryResult>> cache;

    public QueryServiceImpl(@Autowired StaticDataDao staticDataDao) {
        Map<String, List<QueryResult>> temp = staticDataDao
                .queryCharacterDistributionList()
                .stream()
                .map(map -> {
                    String chapterName = (String) map.get("chapter_name");
                    String battleName = (String) map.get("battle_name");
                    String roundName = (String) map.get("round_name");
                    String characterName = (String) map.get("character_name");
                    int count = (Integer) map.get("character_count");
                    return new CharacterDistribution(chapterName, battleName, roundName, characterName, count);
                })
                .collect(Collectors.groupingBy(CharacterDistribution::getCharacterName, Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), mapAndSort(entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.cache = ImmutableMap.copyOf(temp);
        logger.info("query cache init successfully with final size[{}].", temp.size());
    }

    @SuppressWarnings("unchecked")


    private static List<QueryResult> mapAndSort(List<CharacterDistribution> list) {
        List<QueryResult> resultList = list.stream()
                .collect(Collectors.groupingBy(CharacterDistribution::getChapterName, Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> new QueryResult(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        Collections.sort(resultList);
        return ImmutableList.copyOf(resultList);
    }

    @Override
    public List<QueryResult> queryCharacterLocation(Character character) {
        return cache.get(character.getName());
    }

}
