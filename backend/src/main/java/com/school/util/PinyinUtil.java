package com.school.util;

public class PinyinUtil {

    private static final int[] PINYIN_TABLE = new int[20902];
    private static final char[] LETTER_TABLE = {
        'A','B','C','D','E','F','G','H','J','K','L','M','N','O','P','Q','R','S','T','W','X','Y','Z'
    };
    private static final int[] BOUNDARIES = {
        45217,45253,45761,46318,46826,47010,47297,47614,48119,48119,49062,49324,
        49896,50371,50614,50622,50906,51387,51446,52218,52218,52698,52980,53689
    };

    static {
        for (int i = 0; i < 20902; i++) {
            PINYIN_TABLE[i] = i + 19968;
        }
    }

    public static String getFirstLetter(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "#";
        }
        char firstChar = chinese.charAt(0);
        if (firstChar >= 'a' && firstChar <= 'z') {
            return String.valueOf((char)(firstChar - 32));
        }
        if (firstChar >= 'A' && firstChar <= 'Z') {
            return String.valueOf(firstChar);
        }
        if (firstChar >= '0' && firstChar <= '9') {
            return "#";
        }
        int gbCode;
        try {
            byte[] bytes = String.valueOf(firstChar).getBytes("GBK");
            if (bytes.length < 2) {
                return "#";
            }
            gbCode = (bytes[0] & 0xff) * 256 + (bytes[1] & 0xff);
        } catch (Exception e) {
            return "#";
        }
        if (gbCode < BOUNDARIES[0] || gbCode > BOUNDARIES[BOUNDARIES.length - 1]) {
            return "#";
        }
        for (int i = 0; i < BOUNDARIES.length - 1; i++) {
            if (gbCode >= BOUNDARIES[i] && gbCode < BOUNDARIES[i + 1]) {
                return String.valueOf(LETTER_TABLE[i]);
            }
        }
        return "#";
    }

    public static String getPinyinInitials(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chinese.length(); i++) {
            sb.append(getFirstLetter(String.valueOf(chinese.charAt(i))));
        }
        return sb.toString().replace("#", "");
    }

    public static String extractExpertise(String description) {
        if (description == null || description.isEmpty()) {
            return "未设置";
        }
        String[] keywords = {"擅长", "专研", "研究", "方向", "领域", "专家"};
        for (String keyword : keywords) {
            int idx = description.indexOf(keyword);
            if (idx >= 0) {
                int start = idx + keyword.length();
                int end = Math.min(start + 15, description.length());
                String expertise = description.substring(start, end).trim();
                if (expertise.length() > 2) {
                    expertise = expertise.replaceAll("[，。；,.!！?？]$", "");
                    return expertise.length() > 15 ? expertise.substring(0, 15) + "..." : expertise;
                }
            }
        }
        if (description.length() > 15) {
            return description.substring(0, 15) + "...";
        }
        return description;
    }
}
