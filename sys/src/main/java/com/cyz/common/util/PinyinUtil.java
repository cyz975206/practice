package com.cyz.common.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 中文转拼音工具类
 */
public class PinyinUtil {

    private static final HanyuPinyinOutputFormat FORMAT;

    static {
        FORMAT = new HanyuPinyinOutputFormat();
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    /**
     * 中文字符串转拼音（无空格，无分隔符）
     */
    public static String toPinyin(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            try {
                String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
                if (pinyin != null && pinyin.length > 0) {
                    sb.append(pinyin[0]);
                } else {
                    sb.append(c);
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
