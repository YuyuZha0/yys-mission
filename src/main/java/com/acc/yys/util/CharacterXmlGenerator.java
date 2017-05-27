package com.acc.yys.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by zhaoyy on 2017/5/26.
 */
public final class CharacterXmlGenerator {

    private static final String DEST_URL1 = "http://news.4399.com/yyssy/shishenlu";
    private static final String DEST_URL2 = "http://www.87g.com/yys/38101.html";

    private static final Logger logger = LoggerFactory.getLogger(CharacterXmlGenerator.class);

    private CharacterXmlGenerator() {

    }

    public static void generate(String filePath, String imgDir) throws IOException {
        Document document = DocumentHelper.createDocument();
        org.dom4j.Element root = document.addElement("characters");
        Map<String, Map<String, String>> map1 = getFromUrl1()
                .stream().collect(Collectors.toMap(map -> map.get("name"), map -> map));
        Map<String, Map<String, String>> map2 = getFromUrl2()
                .stream().collect(Collectors.toMap(map -> map.get("name"), map -> map));
        for (Map.Entry<String, Map<String, String>> entry : map2.entrySet()) {
            if (map1.get(entry.getKey()) != null)
                continue;
            map1.put(entry.getKey(), entry.getValue());
        }
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final Set<String> nameSet = new HashSet<>();
        for (Map<String, String> map : map1.values()) {
            String name = map.get("name");
            nameSet.add(name);
            String quality = map.get("quality");
            String imgsrc = map.get("imgsrc");
            org.dom4j.Element character = root.addElement("character");
            character.addAttribute("name", name);
            character.addAttribute("quality", quality);
            String imageName = getImageName(name, imgsrc);
            asyncDownloadImage(imageName, imgsrc, imgDir, executorService);
            character.addAttribute("image-name", imageName);
        }
        executorService.shutdown();
        addTips(root, nameSet);
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
        generate("D:\\temp\\characters.xml", "D:\\temp\\character_images");
    }

    private static Set<Map<String, String>> getFromUrl1() throws IOException {
        return Jsoup.connect(DEST_URL1)
                .timeout(10 * 1000)
                .get()
                .select("body > div.area.wp.mt10.slide_wp > ul > li")
                .stream()
                .map(CharacterXmlGenerator::parseElement1)
                .filter(map -> map != null)
                .collect(Collectors.toSet());
    }

    private static Map<String, String> parseElement1(final org.jsoup.nodes.Element element) {
        String name = element.text().trim();
        if (name.isEmpty())
            return null;
        String url = element.select("a").attr("href");
        String quality = FastStrings.split(url, '/')[4];
        Elements img = element.select("img");
        String imgsrc = img.attr("src");
        if (imgsrc == null || imgsrc.isEmpty())
            imgsrc = img.attr("lz_src");
        return ImmutableMap.of("name", name,
                "quality", quality,
                "imgsrc", imgsrc
        );
    }

    private static Set<Map<String, String>> getFromUrl2() throws IOException {
        return Jsoup.connect(DEST_URL2)
                .timeout(10 * 1000)
                .get()
                .select("#pic")
                .stream()
                .map(CharacterXmlGenerator::parseElement2)
                .filter(map -> map != null)
                .collect(Collectors.toSet());
    }

    private static Map<String, String> parseElement2(final org.jsoup.nodes.Element element) {
        String name = element.attr("alt").trim();
        if (name.isEmpty() || "廉鼬".equals(name) || "莹草".equals(name) || "青灯行".equals(name))
            return null;
        String imgsrc = element.attr("src");
        return ImmutableMap.of("name", name,
                "imgsrc", imgsrc,
                "quality", "n"
        );
    }

    private static void asyncDownloadImage(final String fileName, final String url, final String dir, final ExecutorService service) {
        String format = fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length());
        service.submit(() -> {
            try {
                BufferedImage image = ImageIO.read(new URL(url));
                File file = new File(dir + File.separator + fileName);
                if (file.exists())
                    file.delete();
                ImageIO.write(image, format, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static String getImageName(String name, String url) {
        String format = url.substring(url.lastIndexOf('.') + 1, url.length());
        return Hashing.md5().newHasher()
                .putString(name, Charsets.UTF_8)
                .hash()
                .toString() + "." + format;
    }

    private static void addTips(final org.dom4j.Element root, Set<String> nameSet) {
        for (String line : readText("tips.txt").split("\n")) {
            List<String> ss = Splitter.on(CharMatcher.whitespace()).splitToList(line);
            StringBuilder builder = new StringBuilder();
            for (String s : ss) {
                if (nameSet.contains(s)) {
                    org.dom4j.Element tip = root.addElement("tip");
                    tip.addAttribute("content", builder.toString().trim());
                    tip.addAttribute("ref", s);
                    break;
                }
                builder.append(s);
                builder.append(' ');
            }
        }
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
}
