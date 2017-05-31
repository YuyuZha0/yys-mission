package com.acc.yys.util;

import com.google.common.base.Splitter;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhaoyy on 2017/5/26.
 */
public final class ChapterXmlGenerator {
    private static final Pattern wordPtn = Pattern.compile("[^\u4e00-\u9fa5\\d]+");
    private static final Pattern countPtn = Pattern.compile("[\u4e00-\u9fa5]+×\\d+");
    private static final Pattern prefixPtn = Pattern.compile("[\u4e00-\u9fa5：·（）]+\\d?");
    private static final char mul = '×';

    private ChapterXmlGenerator() {

    }


    public static void generate(String filePath) throws IOException {
        Document document = DocumentHelper.createDocument();
        document.setXMLEncoding("UTF-8");
        Element root = document.addElement("chapters");
        addChapterElement(root);
        addChapterElement1(root);
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        writeFormattedXml(document, writer);
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        generate("D:\\temp\\chapters.xml");
    }

    private static void writeFormattedXml(final Document document, final Writer writer) {
        OutputFormat format = new OutputFormat();
        format.setIndent(true);
        format.setTrimText(true);
        format.setNewlines(true);
        XMLWriter xmlWriter = new XMLWriter(writer, format);
        try {
            xmlWriter.write(document);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readText(String fileName) {
        String text = null;
        try (InputStream is = Thread
                .currentThread()
                .getContextClassLoader()
                .getResourceAsStream(fileName)) {

            Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
            text = scanner.hasNext() ? scanner.next() : "";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

    private static String getPrefix(String line) {
        Matcher prefixMatcher = prefixPtn.matcher(line);
        return prefixMatcher.find() ? prefixMatcher.group() : "";
    }

    private static void addCharacter(String line, Element round) {
        Matcher countMatcher = countPtn.matcher(line);
        while (countMatcher.find()) {
            String s = countMatcher.group();
            String[] ss = FastStrings.split(s, mul);
            Element character = round.addElement("character");
            character.addAttribute("name", ss[0]);
            character.addAttribute("count", ss[1]);
        }
    }

    private static void addChapterElement(Element root) {
        Element currentChapter = null;
        for (String line : readText("chapter.txt").split("\n")) {
            if (line.startsWith("第") || line.startsWith("番")) {
                currentChapter = root.addElement("chapter");
                List<String> list = Splitter.on(wordPtn).splitToList(line);
                currentChapter.addAttribute("title", list.get(0));
                currentChapter.addAttribute("sub-title", list.get(1));
                continue;
            }
            Element battle = currentChapter.addElement("battle");
            String name = getPrefix(line);
            battle.addAttribute("name", name);
            if (name.contains("首领"))
                battle.addAttribute("boss", "true");
            for (String sub : line.split("回合")) {
                Element round = battle.addElement("round");
                if (line.contains("困难模式"))
                    round.addAttribute("hard-mode", "true");
                addCharacter(sub, round);
            }

        }
    }

    ////////////////////////////////////////////////////////////////////////////////


    private static void addChapterElement1(Element root) {
        Element currentChapter = null;
        Element currentBattle = null;
        String lastName = null;
        for (String line : readText("chapter1.txt").split("\n")) {
            String name = getPrefix(line);
            if (line.indexOf(mul) < 0) {
                if (lastName != null) {
                    currentChapter = root.addElement("chapter");
                    currentChapter.addAttribute("title", lastName);
                }
                lastName = name;
                continue;
            }
            if (lastName != null) {
                currentBattle = currentChapter.addElement("battle");
                currentBattle.addAttribute("name", lastName);
            }
            Element round = currentBattle.addElement("round");
            round.addAttribute("name", name);
            addCharacter(line, round);
            lastName = null;
        }
    }
}
