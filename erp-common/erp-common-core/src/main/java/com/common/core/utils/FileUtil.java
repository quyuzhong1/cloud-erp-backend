package com.common.core.utils;


import cn.hutool.core.codec.Base64;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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


    /**
     * 将MultipartFile 转化成 file
     *
     * @param
     * @return java.io.File
     * @author yl
     * @date 2022-08-24 10:28
     */
    public static File multiToFile(MultipartFile multipartFile) {
        //选择用缓冲区来实现这个转换即使用java 创建的临时文件 使用 MultipartFile.transferto()方法 。
        File file = null;
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            String[] filename = originalFilename.split("\\.");
            UUID uuid = UUID.randomUUID();
            file = File.createTempFile(uuid.toString(), filename[filename.length-1]);
            multipartFile.transferTo(file);
            file.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static void base64ToFile(String base64, String fileName, String filePath) {
        File file = null;
        //创建文件目录
        File dir = new File(filePath);
        if (!dir.exists() && !dir.isDirectory()) {
            dir.mkdirs();
        }
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        byte[] bytes = Base64.decode(base64);
        file = new File(filePath + "\\" + fileName);
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static String convertPdfUrlToBase64(String pdfUrl) throws IOException {
        if(StringUtils.isBlank(pdfUrl)){
            return null;
        }
        InputStream inStream = null;
        String base64 = "";
        String prefix = "data:application/pdf;base64,";
        try {
            URL url = new URL(pdfUrl);
            //打开链接
            HttpURLConnection conn = null;;
            conn = (HttpURLConnection) url.openConnection();
            //设置请求方式为"GET"
            conn.setRequestMethod("GET");
            //超时响应时间为5秒
            conn.setConnectTimeout(5 * 1000);
            //通过输入流获取图片数据
            inStream = conn.getInputStream();
            //对字节数组Base64编码
            base64 = Base64.encode(inStream);
            base64 = base64.replaceAll("\r\n","");
            return prefix + base64;
        } finally {
            //关闭输入流
            if (inStream != null) {
                inStream.close();
            }
        }
    }

    public static String convertPdfUrlToBase64(String pdfUrl,String token) throws IOException {
        if(StringUtils.isBlank(pdfUrl)){
            return null;
        }
        InputStream inStream = null;
        String base64 = "";
        String prefix = "data:application/pdf;base64,";
        try {
            URL url = new URL(pdfUrl);
            //打开链接
            HttpURLConnection conn = null;;
            conn = (HttpURLConnection) url.openConnection();
            //设置请求方式为"GET"
            conn.setRequestMethod("GET");
            //顺丰接口调用授权
            conn.setRequestProperty("X-Auth-token", token);
            //超时响应时间为5秒
            conn.setConnectTimeout(5 * 1000);
            //通过输入流获取图片数据
            inStream = conn.getInputStream();
            //对字节数组Base64编码
            base64 = Base64.encode(inStream);
            base64 = base64.replaceAll("\r\n","");
            return prefix + base64;
        } finally {
            //关闭输入流
            if (inStream != null) {
                inStream.close();
            }
        }
    }
    /**
     * @description: 转换MultipartFile
     * @author Will
     * @date: 2024/4/3 15:03
     * @param filePath
     * @return MultipartFile
     */
    public  static MultipartFile toMultipartFile(String filePath) {
        try {
            String fileUrl = filePath.replace(" ","%20");

            // 打开 URL 连接
            URL url = new URL(fileUrl);
            URLConnection conn = url.openConnection();
            // 从连接获取输入流
            BufferedInputStream inputStream = new BufferedInputStream(conn.getInputStream());

            // 读取输入流中的数据并存储到 ByteArrayOutputStream 中
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // 关闭输入流
            inputStream.close();

            // 从 ByteArrayOutputStream 中获取 byte 数组
            byte[] bytes = outputStream.toByteArray();

            // 关闭 ByteArrayOutputStream
            outputStream.close();

            // 从文件路径中提取文件名
            String fileName = "";
            if (filePath.contains("?")) {
                fileName = filePath.substring(filePath.lastIndexOf("/")+1,filePath.lastIndexOf("?"));
            } else {
                fileName = filePath.substring(filePath.lastIndexOf("/")+1);
            }

            // 创建 MockMultipartFile 对象
            return new MockMultipartFile(fileName, new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            // 捕获异常并抛出自定义的 ServiceException
            throw new ServiceException("未能获取文件");
        }
    }

    /**
     * 去除文件后缀名
     * @param fileName
     * @return
     */
    public static String removeExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return fileName;
        }

        // 获取最后一个点的位置
        int lastDotIndex = fileName.lastIndexOf(".");

        // 如果没有点或点是第一个字符，返回原文件名
        if (lastDotIndex == -1 || lastDotIndex == 0) {
            return fileName;
        }

        // 截取文件名的部分（去掉后缀）
        return fileName.substring(0, lastDotIndex);
    }
}