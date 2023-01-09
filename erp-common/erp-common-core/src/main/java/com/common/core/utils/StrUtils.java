package com.common.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtils {
    public static Pattern compile = Pattern.compile("[A-Z]");
    /**
     * 判断字符串是否只包含数字和字母
     * @Author Luo_WG
     * @Date 2022/10/24 11:09
     * @param str str
     * @return boolean
     **/
    public static boolean isLetterDigit(String str) {
        String regex = "^[a-z0-9A-Z]+$";
        return str.matches(regex);
    }

    /**
     *  判断字符串是否只包含数字、字母、-
     */
    public static boolean isLetterDigitBar(String str) {
        String regex = "^[a-z0-9A-Z\\-]+$";
        return str.matches(regex);
    }

    /**
     * 判断是否为数字
     */
    public static boolean isDigit(String str) {
        String regex = "^[+-]?(0|([1-9]\\d*))(\\.\\d+)?$";
        return str.matches(regex);
    }

    /**
     * 判断是否为百分比
     */
    public static boolean isPercentage(String str) {
        String regex = "^([0-9.]+)[ ]*%$";
        return str.matches(regex);
    }

    /**
     * @author Luo_WG
     * @Description 将驼峰转为下划线，正则式
     * @param str  例如：helloWord
     * @return java.lang.String
     * underline:下划线，驼峰：hump
     */
    public static String underlineByhump(String str) {
        Matcher matcher = compile.matcher(str);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()) {
            matcher.appendReplacement(sb,  "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 将下划线转为驼峰
     * @Author Luo_WG
     * @Date 2022/10/24 20:46
     * @param line 源字符串
     * @param smallCamel 大小驼峰,是否为小驼峰
     * @return 转换后的字符串
     * @return java.lang.String
     **/
    public static String underlineToCamel(String line,boolean smallCamel){
        if(line==null||"".equals(line)){
            return "";
        }
        StringBuffer sb=new StringBuffer();
        Pattern pattern=Pattern.compile("([A-Za-z\\d]+)(_)?");
        Matcher matcher=pattern.matcher(line);
        while(matcher.find()){
            String word=matcher.group();
            sb.append(smallCamel&&matcher.start()==0?Character.toLowerCase(word.charAt(0)):Character.toUpperCase(word.charAt(0)));
            int index=word.lastIndexOf('_');
            if(index>0){
                sb.append(word.substring(1, index).toLowerCase());
            }else{
                sb.append(word.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }
}
