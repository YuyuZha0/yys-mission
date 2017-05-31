package com.acc.yys.service.impl;

import com.acc.yys.pojo.Character;
import com.acc.yys.pojo.CharacterDistribution;
import com.acc.yys.service.QueryService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import java.util.stream.Collectors;

/**
 * Created by zhaoyy on 2017/5/23.
 */
@Service
public final class QueryServiceImpl implements QueryService {


    private static final Logger logger = LoggerFactory.getLogger(QueryServiceImpl.class);
    private final Map<String, List<QueryResult>> cache;

    public QueryServiceImpl() {
        this.cache = ImmutableMap.copyOf(
                resolveXml().stream()
                        .collect(Collectors.groupingBy(CharacterDistribution::getCharacterName, Collectors.toList()))
                        .entrySet()
                        .stream()
                        .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), mapAndSort(entry.getValue())))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
        logger.info("query cache init successfully with final size[{}].", cache.size());
    }

    @SuppressWarnings("unchecked")
    private static List<CharacterDistribution> resolveXml() {
        List<CharacterDistribution> result = new ArrayList<>();
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("chapters.xml")) {
            SAXReader reader = new SAXReader();
            Document document = reader.read(is);
            Element root = document.getRootElement();
            Iterator<Element> chapters = root.elementIterator("chapter");
            while (chapters.hasNext()) {
                Element chapter = chapters.next();
                String chapterName = chapter.attributeValue("title");
                String sub = chapter.attributeValue("sub-title");
                if (sub != null && !sub.isEmpty())
                    chapterName = chapterName + "-" + sub;
                Iterator<Element> battles = chapter.elementIterator("battle");
                //System.out.println(chapterName);
                while (battles.hasNext()) {
                    Element battle = battles.next();
                    String battleName = battle.attributeValue("name");
                    if (battleName == null || battleName.isEmpty())
                        battleName = " - ";
                    Iterator<Element> rounds = battle.elementIterator("round");
                    int roundCount = 0;
                    //System.out.println("\t" + battleName);
                    while (rounds.hasNext()) {
                        ++roundCount;
                        Element round = rounds.next();
                        String roundName = round.attributeValue("name");
                        boolean hardMode = "true".equalsIgnoreCase(round.attributeValue("hard-mode"));
                        if (roundName == null || roundName.isEmpty())
                            roundName = String.format("第%s回合", roundCount);
                        if (hardMode)
                            roundName += "(困难模式)";
                        Iterator<Element> characters = round.elementIterator("character");
                        //System.out.println("\t\t" + roundName);
                        while (characters.hasNext()) {
                            Element character = characters.next();
                            String characterName = character.attributeValue("name");
                            int count = Integer.parseInt(character.attributeValue("count"));
                            result.add(new CharacterDistribution(chapterName, battleName, roundName, characterName, count));
                        }
                    }
                }
            }
        } catch (IOException | DocumentException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

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
