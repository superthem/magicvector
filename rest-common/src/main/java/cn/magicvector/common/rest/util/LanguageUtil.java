package cn.magicvector.common.rest.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class LanguageUtil {



    static HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();

    static {
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
    }


    public static String toPinyin(String chinese) {
        StringBuilder pinyin = new StringBuilder();
        for(char khar : chinese.toCharArray()) {
            String[] pinyinArray = null;
            try {
                pinyinArray = PinyinHelper.toHanyuPinyinStringArray(khar, defaultFormat);
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
            if (pinyinArray != null) {
                pinyin.append(pinyinArray[0]);
            } else if ( khar != ' ') {
                pinyin.append(khar);
            }
        }
        return pinyin.toString();
    }
}
