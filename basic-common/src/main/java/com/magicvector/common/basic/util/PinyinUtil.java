package com.magicvector.common.basic.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinUtil {

    public static String getPingYin(String src) {
        if (src == null) {
            return src;
        }
        char[] inputChar = null;
        inputChar = src.toCharArray();
        int inputCharLength = inputChar.length;
        String[] piword = new String[inputCharLength];
        HanyuPinyinOutputFormat hPinyinOutputFormat = new HanyuPinyinOutputFormat();
        //大小写
        hPinyinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        //带拼音状态(toneType和charType必须成对使用，否则会异常)
        hPinyinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        hPinyinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
        String piStr = "";
        try {
            for (int i = 0; i < inputCharLength; i++) {
                // 判断是否为汉字字符
                if (Character.toString(inputChar[i]).matches("[\\u4E00-\\u9FA5]+")) {
                    piword = PinyinHelper.toHanyuPinyinStringArray(inputChar[i], hPinyinOutputFormat);
                    piStr += piword[0];
                } else
                    piStr += Character.toString(inputChar[i]);
            }

            return piStr;
        } catch (BadHanyuPinyinOutputFormatCombination e1) {
            e1.printStackTrace();
        }
        return piStr;
    }

    /**
     * 获取汉字首字母
     */
    public static String getPinyinInitials(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            String[] s = PinyinHelper.toHanyuPinyinStringArray(ch);
            if (s != null) {
                sb.append(s[0].charAt(0));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

}
