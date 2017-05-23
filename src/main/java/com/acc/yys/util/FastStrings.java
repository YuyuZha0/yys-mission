package com.acc.yys.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoyy on 2017/5/23.
 */
public final class FastStrings {

    private FastStrings() {

    }

    public static float similarity(CharSequence source, CharSequence target) {
        if (source == null || target == null)
            throw new IllegalArgumentException("empty sequence is illegal");
        int len = Math.max(source.length(), target.length());
        return 1 - (float) editDistance(source, target) / len;
    }

    public static int editDistance(CharSequence source, CharSequence target) {
        int ls = source.length();
        int lt = target.length();
        int[][] m = new int[ls + 1][lt + 1];
        for (int i = 0; i < ls + 1; i++)
            m[i][0] = i;
        for (int j = 0; j < lt + 1; j++)
            m[0][j] = j;
        for (int i = 1; i < ls + 1; i++)
            for (int j = 1; j < lt + 1; j++) {
                if (source.charAt(i - 1) == target.charAt(j - 1)) {
                    m[i][j] = m[i - 1][j - 1];
                } else {
                    int insert = m[i][j - 1] + 1;
                    int delete = m[i - 1][j] + 1;
                    int replace = m[i - 1][j - 1] + 1;
                    m[i][j] = Math.min(Math.min(insert, delete), replace);
                }
            }
        return m[ls][lt];
    }

    public static String[] split(String s, final char separator, int limit) {
        if (s == null) return null;
        int len = s.length();
        if (len == 0)
            return new String[0];
        final List<String> list = limit > 0 ? new ArrayList<>(limit) : new ArrayList<>();
        int n = -1;
        boolean match = false;
        for (int i = 0; i < len; i++) {
            if (limit > 0 && list.size() == limit - 1) {
                break;
            }
            if (separator != s.charAt(i)) {
                match = true;
                continue;
            }
            if (match) list.add(s.substring(n + 1, i));
            match = false;
            n = i;
        }
        if (n < len - 1)
            list.add(s.substring(n + 1, len));
        return list.toArray(new String[list.size()]);
    }

    public static String[] split(String s, final char separator) {
        return split(s, separator, -1);
    }
}
