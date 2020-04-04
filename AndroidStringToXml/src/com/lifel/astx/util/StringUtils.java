package com.lifel.astx.util;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 针对不同项目单独开发的工具
 * <p>
 * Created by lifel on 2016/12/9.
 */

public class StringUtils {
    private static DecimalFormat decimalFormat = new DecimalFormat("#.#");


    /**
     * 判断是否为null,如果为null则返回--.不为null则原样返回
     *
     * @param str 需要判断的字符串
     * @return
     */
    public static String ifNUll(String str) {
        return ifNUll(str, "--");
    }

    /**
     * 判断是否为null,如果为null则返回--.不为null则原样返回
     *
     * @param str 需要判断的字符串
     * @return
     */
    public static String ifNUll(String str, String defaultStr) {
        if (null == str || 0 == str.length()) {
            return defaultStr;
        } else {
            return str;
        }
    }


    /**
     * 判断字符串是否为空
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {

        if (null == str || 0 == str.length()) {
            return true;//为空,返回true
        }
        return false;//不为空,返回false;
    }

    /**
     * 格式化数字,自动返回万/亿的数据.保留一位小数
     *
     * @param number
     * @return
     */
    public static String getFormatNumber(int number) {
        if (number < 10000) {
            return decimalFormat.format(number);
        } else if (number < 100000000) {
            return decimalFormat.format(number / 10000) + "万";
        } else {
            return decimalFormat.format(number / 100000000) + "亿";
        }
    }

    /**
     * 格式化数字,自动返回万/亿的数据.保留一位小数
     *
     * @param number
     * @return
     */
    public static String getFormatNumber(long number) {
        if (number < 10000) {
            return decimalFormat.format(number);
        } else if (number < 100000000) {
            return decimalFormat.format(number / 10000) + "万";
        } else {
            return decimalFormat.format(number / 100000000) + "亿";
        }
    }

    /**
     * 格式化数字,自动返回万/亿的数据.保留一位小数
     *
     * @param number
     * @return
     */
    public static String getFormatNumber(double number) {
        if (number < 10000) {
            return decimalFormat.format(number);
        } else if (number < 100000000) {
            return decimalFormat.format(number / 10000) + "万";
        } else {
            return decimalFormat.format(number / 100000000) + "亿";
        }
    }


    /**
     * 判断字符串中是否包含中文
     * @param str
     * 待校验字符串
     * @return 是否为中文
     * @warn 不能校验是否为中文标点符号
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
}
