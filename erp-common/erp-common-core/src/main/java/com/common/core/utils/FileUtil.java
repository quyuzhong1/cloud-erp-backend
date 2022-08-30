package com.common.core.utils;


import ma.glasnost.orika.impl.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件工具，用于读取或写入文件
 */
public class FileUtil {

    /**
     * readText:将指定路径的文件读出里面的内容，返回一个字符串
     *
     * @param filePath
     * @return
     */
    public static String readText(String filePath) {

        String s;
        StringBuffer sb = new StringBuffer();

        try {
            File f = new File(filePath);
            if (!f.exists()) {
                f.createNewFile();
            }
            BufferedReader input = new BufferedReader(new FileReader(f));
            while ((s = input.readLine()) != null) {
                sb.append(s);
            }
            input.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return sb.toString();
    }


    /**
     * append:将一个字符写入一个已有的文件中，通过追加的方法追加到内容的末尾；
     *
     * @param filePath
     * @param data
     */
    public static synchronized void append(String filePath, String data) {

        String s;
        StringBuffer sb = new StringBuffer();

        try {
            String dirPath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
            File dir = new File(dirPath);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File f = new File(filePath);
            if (!f.exists()) {
                f.createNewFile();
            }
            BufferedReader input = new BufferedReader(new FileReader(f));


            while ((s = input.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }
            input.close();
            sb.append(data);
            BufferedWriter output = new BufferedWriter(new FileWriter(f));
            output.write(sb.toString());
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * append:将一个字符写入一个已有的文件中，通过追加的方法追加到内容的末尾；
     *
     * @param filePath
     * @param data
     */
    public static synchronized void write(String filePath, String data) {

        StringBuffer sb = new StringBuffer(data);

        try {

            //创建文件夹
            String dirPath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
            File dir = new File(dirPath);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File f = new File(filePath);
            if (!f.exists()) {
                f.createNewFile();
            }

            BufferedWriter output = new BufferedWriter(new FileWriter(f));
            output.write(sb.toString());
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void write(String filePath,byte[] fileBytes){
        try {
            String dirPath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File f = new File(filePath);
            if (!f.exists()) {
                f.createNewFile();
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("a.xls"));
            bos.write(fileBytes);
            bos.flush();
            bos.close();

        }catch (Exception e){

        }

    }
    /**
     * 读入TXT文件
     */
    public static String readFile(InputStream is) {

        StringBuffer sb = new StringBuffer();
        try (InputStreamReader reader = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(reader)
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    private static Pattern humpPattern = Pattern.compile("[A-Z]");


    /**
     * 驼峰转换下划线
     *
     * @param str
     * @return
     */
    public static String humpToUnderLine(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 去除HTML标签
     *
     * @param str
     * @return
     */
    public static String delHtmlTag(String str) {
        return str.replaceAll("<[.[^>]]*>", "").replaceAll(" ", "");
    }

    /**
     * HTML中提取图片
     *
     * @param s
     * @return 获得图片
     */
    public static List<String> extractImageFromHtml(String s) {

        if (StringUtils.isBlank(s)) {
            return null;
        }

        String regex;
        List<String> list = new ArrayList<>();
        regex = "src=\"(.*?)\"";
        Pattern pa = Pattern.compile(regex, Pattern.DOTALL);
        Matcher ma = pa.matcher(s);
        while (ma.find()) {
            String srcAll = ma.group();
            srcAll = srcAll.substring(srcAll.indexOf("\"") + 1, srcAll.lastIndexOf("\""));
            list.add(srcAll);
        }
        return list;
    }


    /**
     * 扩展名
     *
     * @param fileName
     * @return
     */
    public static String getFileExtension(String fileName) {
        if (StringUtils.isNotEmpty(fileName) && fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 文件类型
     *
     * @param fileName
     * @return
     */
    public static Integer fileType(String fileName) {
        String postfix = getFileExtension(fileName);
        Integer result = 0;
        if ("bmp,jpg,jpeg,png,tif,gif,pcx,tga,exif,fpx,svg,psd,cdr,pcd,dxf,ufo,eps,ai,raw,wmf,webp,sketch".contains(postfix)) {
            result = 1;
        } else if ("vob,mpg,avi,mp4,mkv,mov".contains(postfix)) {
            result = 2;
        } else if ("pdf,doc,docx".contains(postfix)) {
            result = 3;
        } else if ("zip,7z,rar".contains(postfix)) {
            result = 4;
        }
        return result;
    }


}