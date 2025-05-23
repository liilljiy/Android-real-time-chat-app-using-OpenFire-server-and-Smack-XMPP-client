package com.example.lj.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

public class MyPinyinHelper {

    private static final HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();

    static {
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    public static String toPinyin(String chinese) {
        try {
            StringBuilder sb = new StringBuilder();
            for (char c : chinese.toCharArray()) {
                String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (pinyins != null && pinyins.length > 0) {
                    sb.append(pinyins[0]);
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return chinese;
        }
    }
}
