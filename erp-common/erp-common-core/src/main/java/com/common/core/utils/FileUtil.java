package com.common.core.utils;


import cn.hutool.core.codec.Base64;
import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件工具，用于读取或写入文件
 */
@Slf4j
public class FileUtil {

    /**
     * readText:将指定路径的文件读出里面的内容，返回一个字符串
     *
     * @param filePath
     * @return
     */
    public static String readText(String filePath) {
        StringBuilder sb = new StringBuilder();
        // 使用 try-with-resources 确保资源关闭
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                boolean newFile = f.createNewFile();
                if (!newFile) {
                    log.warn("createNewFile 文件已存在");
                }
            }
            try (BufferedReader input = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = input.readLine()) != null) {
                    sb.append(line);
                }
            }
        } catch (IOException ex) {
            log.error("文件读取失败", ex);
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
        try {
            // 创建目录（如果不存在）
            String dirPath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 创建文件（如果不存在）
            File f = new File(filePath);
            if (!f.exists()) {
                boolean newFile = f.createNewFile();
                if (!newFile) {
                    log.warn("createNewFile 文件已存在");
                }
            }
            // 使用 try-with-resources 进行文件写入（追加模式）
            try (BufferedWriter output = new BufferedWriter(new FileWriter(f, true))) {
                output.write(data);
                output.newLine(); // 换行以保持追加内容清晰
            }
        } catch (IOException e) {
            log.error("文件追加失败: {}", filePath, e);
        }
    }

    /**
     * append:将一个字符写入一个已有的文件中，通过追加的方法追加到内容的末尾；
     *
     * @param filePath
     * @param data
     */
    public static synchronized void write(String filePath, String data) {
        try {
            // 创建文件夹
            String dirPath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
            File dir = new File(dirPath);
            if (!dir.exists() && !dir.mkdirs()) {
                log.error("创建目录失败: {}", dirPath);
                return;
            }

            // 创建文件（如果不存在）
            File f = new File(filePath);
            if (!f.exists() && !f.createNewFile()) {
                log.error("创建文件失败: {}", filePath);
                return;
            }

            // 使用 try-with-resources 自动关闭 BufferedWriter
            try (BufferedWriter output = new BufferedWriter(new FileWriter(f))) {
                output.write(data);
            }
        } catch (IOException e) {
            log.error("文件写入失败: {}", filePath, e);
        }
    }



    public static void write(String filePath, byte[] fileBytes) {
        try {
            // 创建文件夹
            String dirPath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
            File dir = new File(dirPath);
            if (!dir.exists() && !dir.mkdirs()) {
                log.error("创建目录失败: {}", dirPath);
                return;
            }
            // 创建文件（如果不存在）
            File f = new File(filePath);
            if (!f.exists() && !f.createNewFile()) {
                log.error("创建文件失败: {}", filePath);
                return;
            }
            // 使用 try-with-resources 确保 BufferedOutputStream 自动关闭
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f))) {
                bos.write(fileBytes);
                bos.flush(); // 可选，但加上更安全
            }
        } catch (IOException e) {
            log.error("写入文件失败: {}", filePath, e);
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


    private static final Pattern humpPattern = Pattern.compile("[A-Z]");


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
        int result = 0;
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
        // 创建文件目录
        File dir = new File(filePath);
        if (!dir.exists() && !dir.isDirectory()) {
            dir.mkdirs();
        }

        // 解析 Base64 数据并创建文件
        byte[] bytes = Base64.decode(base64); // 使用标准库的 Base64 解码
        File file = new File(filePath + "\\" + fileName); // 更规范的路径拼接方式

        // 使用 try-with-resources 自动关闭流
        try (FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(bytes);
            bos.flush(); // 确保数据完全写入文件
        } catch (Exception e) {
            log.error("写入文件失败: {}", file.getAbsolutePath(), e);
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
            HttpURLConnection conn = null;
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
            HttpURLConnection conn = null;
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
    public  static MultipartFile filePathToMultipartFile(String filePath) {
        try {
            // 打开 URL 连接
            URL url = new URL(filePath);
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
     * 传文件名
     * @author will
     * @date 2025/4/25 15:42
     * @param filePath
     * @param fileName
     * @return MultipartFile
     */
    public static MultipartFile toMultipartFile(String filePath, String fileName, String defaultSuffix) {
        try {
            // 处理文件名：无后缀时添加默认后缀
            String processedFileName = processFileName(fileName, defaultSuffix);

            // 处理文件路径（替换空格和反斜杠）
            String fileUrl = filePath.replace(" ", "%20").replace("\\", "/");

            // 打开 URL 连接
            URL url = new URL(fileUrl);
            URLConnection conn = url.openConnection();

            // 读取文件内容到字节数组
            try (BufferedInputStream inputStream = new BufferedInputStream(conn.getInputStream());
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                byte[] bytes = outputStream.toByteArray();

                // 创建 MultipartFile（动态设置 MIME 类型）
                return new MockMultipartFile(
                        "file",
                        processedFileName,
                        getMimeType(processedFileName),
                        new ByteArrayInputStream(bytes)
                );
            }
        } catch (IOException e) {
            throw new ServiceException("文件处理失败: " + e.getMessage());
        }
    }



    /**
     * 处理文件名：无后缀时添加默认后缀
     */
    private static String processFileName(String fileName, String defaultSuffix) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == 0) {
            // 无后缀 或 后缀在开头（如 .gitignore）
            return fileName + "." + defaultSuffix;
        } else {
            // 已有合法后缀，保留原名称
            return fileName;
        }
    }

    /**
     * 根据后缀推断 MIME 类型
     */
    private static String getMimeType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "pdf": return "application/pdf";
            case "xml": return "text/xml";
            default: return "application/octet-stream";
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
    /**
     * 获取文件名的后缀名
     * @param fileName 文件名
     * @return 后缀名（不含点），如果没有后缀则返回空字符串
     */
    public static String getFileSuffix(String fileName) {
        // 处理空值或空字符串
        if (fileName == null || fileName.trim().isEmpty()) {
            return "";
        }

        // 查找最后一个点的位置
        int lastDotIndex = fileName.lastIndexOf('.');

        // 没有点或者点是最后一个字符的情况
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }

        // 从点的下一个字符开始截取到结尾
        return fileName.substring(lastDotIndex + 1);
    }

    public static String convertToBase64AndCheckIfPdf(MultipartFile multipartFile) throws IOException {
        // 检查文件是否为空
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new ServiceException("文件不能为空");
        }

        // 将文件内容转换为Base64编码的字符串
        byte[] fileContent = multipartFile.getBytes();
        String base64Encoded = Base64.encode(fileContent);
        // 返回结果
        return base64Encoded;
    }


    // 文件下载方法（带超时和重试）
    public static byte[] downloadFile(String url) {
        int retry = 3;
        while (retry-- > 0) {
            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionTimeToLive(10, TimeUnit.SECONDS)
                    .build()) {

                HttpGet httpGet = new HttpGet(url);
                try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    if (response.getStatusLine().getStatusCode() == 200) {
                        return EntityUtils.toByteArray(response.getEntity());
                    }
                }
            } catch (Exception e) {
                if (retry == 0) throw new ServiceException("下载失败: " + url, e);
            }
        }
        throw new ServiceException("无法下载文件: " + url);
    }

    // 文件名处理（防止非法字符）
    public static String getFileNameFromUrl(String url) {
        try {
            String path = new URI(url).getPath();
            String rawName = path.substring(path.lastIndexOf('/') + 1);
            return rawName.replaceAll("[\\\\/:*?\"<>|]", "_"); // 替换非法字符
        } catch (URISyntaxException e) {
            return "file_" + DigestUtils.md5Hex(url) + ".xml";
        }
    }



    /**
     * 订货通根据url下载图片
     * @author will
     * @date 2025/10/13 10:55
     * @param fileUrl
     * @return MultipartFile
     */
    public static MultipartFile dhtFileUrlToMultipartFile(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 方法1：从 URL 查询参数中提取 Fn 参数
            String fileName = extractFileNameFromUrl(fileUrl);

            // 方法2：如果方法1失败，使用默认文件名但确保扩展名正确
            if (fileName == null || fileName.isEmpty()) {
                fileName = "file.png"; // 根据实际情况设置默认扩展名
            }

            // 根据文件扩展名设置 Content-Type
            String contentType = getContentTypeByFileName(fileName);

            InputStream inputStream = connection.getInputStream();
            byte[] bytes = IOUtils.toByteArray(inputStream);

            return new MockMultipartFile(
                    "file",
                    fileName,
                    contentType,
                    new ByteArrayInputStream(bytes)
            );
        } catch (Exception e) {
            throw new RuntimeException("文件转换失败", e);
        }
    }

    // 从 URL 查询参数中提取文件名
    private static String extractFileNameFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String query = url.getQuery();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    if (idx > 0 && "Fn".equals(pair.substring(0, idx))) {
                        String fileName = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                        // 确保文件名有正确的扩展名
                        if (!fileName.contains(".")) {
                            // 如果没有扩展名，根据内容或默认添加
                            fileName += ".png"; // 根据实际情况调整
                        }
                        return fileName;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 根据文件名获取 Content-Type
    private static String getContentTypeByFileName(String fileName) {
        String extension = getFileExtension(fileName);
        return getContentTypeByExtension(extension);
    }

    private static String getContentTypeByExtension(String extension) {
        switch (extension) {
            case "png": return "image/png";
            case "jpg": case "jpeg": return "image/jpeg";
            case "gif": return "image/gif";
            case "bmp": return "image/bmp";
            case "webp": return "image/webp";
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default: return "application/octet-stream";
        }
    }
}