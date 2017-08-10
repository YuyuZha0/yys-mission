package com.acc.yys.util;

import com.acc.yys.dao.StaticDataDao;
import com.acc.yys.pojo.Character;
import com.acc.yys.pojo.CharacterDistribution;
import com.acc.yys.service.MyBatisFactory;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by zhaoyy on 2017/8/10.
 */
public final class XmlResolver {

    private XmlResolver() {

    }

    public static void main(String[] args) {
        resolveXml();
        resolveXml1();
    }

    @SuppressWarnings("unchecked")
    private static void resolveXml() {
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
            e.printStackTrace();
        }
        try (SqlSession session = MyBatisFactory.sqlSessionFactory().openSession(ExecutorType.BATCH)) {
            StaticDataDao dao = session.getMapper(StaticDataDao.class);
            for (CharacterDistribution cd : result) {
                dao.insertCharacterDistribution(cd);
            }
            session.commit();
            session.clearCache();
        }
    }


    private static void resolveXml1() {
        List<Character> characters = new ArrayList<>();
        Map<String, String> tips = new HashMap<>();
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
                characters.add(new Character(name, imageName, quality));
            }
            final Iterator<Element> tipIterator = root.elementIterator("tip");
            while (tipIterator.hasNext()) {
                Element e = tipIterator.next();
                String content = e.attributeValue("content");
                String ref = e.attributeValue("ref");
                tips.put(content, ref);
            }

        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        }
        try (SqlSession session = MyBatisFactory.sqlSessionFactory().openSession(ExecutorType.BATCH)) {
            StaticDataDao dao = session.getMapper(StaticDataDao.class);
            for (Character c : characters) {
                dao.insertCharacter(c);
            }
            session.commit();
            session.clearCache();
        }

        try (SqlSession session = MyBatisFactory.sqlSessionFactory().openSession(ExecutorType.BATCH)) {
            StaticDataDao dao = session.getMapper(StaticDataDao.class);
            for (Map.Entry<String, String> entry : tips.entrySet()) {
                dao.insertTip(entry.getKey(), entry.getValue());
            }
            session.commit();
            session.clearCache();
        }

    }
}
