package com.acc.yys.util;

import com.google.common.collect.ImmutableMap;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by zhaoyy on 2017/5/19.
 */
public final class SimpleTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<String> fragments;
    private final List<String> templateArgs;

    public SimpleTemplate(InputStream is) {
        this(is, StandardCharsets.UTF_8);
    }

    public SimpleTemplate(InputStream is, Charset charset) {
        this(readInputStream(is, charset));
    }

    private static String readInputStream(InputStream is, Charset charset) {
        Scanner scanner = new Scanner(is, charset.name()).useDelimiter("\\A");
        String template = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        return template;
    }

    public SimpleTemplate(String template) {
        if (template == null)
            throw new IllegalArgumentException("illegal template");
        final List<String> fragments = new ArrayList<>();
        final List<String> templateArgs = new ArrayList<>();
        int len = template.length();
        int lookahead = 0, lastBra = 0, lastKet = -1;
        boolean missBra = false;
        while (lookahead < len) {
            if (lookahead < len - 1 && template.charAt(lookahead) == '$' && template.charAt(lookahead + 1) == '{') {
                lastBra = lookahead;
                lookahead += 2;
                missBra = true;
                continue;
            }

            if (missBra && template.charAt(lookahead) == '}') {
                fragments.add(template.substring(lastKet + 1, lastBra));
                templateArgs.add(template.substring(lastBra + 2, lookahead));
                lastKet = lookahead;
                missBra = false;
            }

            lookahead++;
        }
        fragments.add(template.substring(lastKet + 1, len));
        this.fragments = fragments;
        this.templateArgs = templateArgs;
    }

    public String render(Map<String, String> args) {
        int len1 = fragments.size();
        int len2 = templateArgs.size();
        int index = 0;
        StringBuilder builder = new StringBuilder();
        while (index < len1 || index < len2) {
            if (index < len1)
                builder.append(fragments.get(index));
            if (index < len2) {
                String key = templateArgs.get(index);
                String value = args == null ? null : args.get(key);
                if (value == null)
                    value = String.format("${%s}", key);
                builder.append(value);
            }
            index++;
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        String template = "${1}${2}${3}";
        Map<String, String> map = ImmutableMap.of("1", "aaaaaaa", "2", "bbbbbbbbbbb", "3", "kkkk");
        System.out.println(new SimpleTemplate(template).render(map));
    }

}
