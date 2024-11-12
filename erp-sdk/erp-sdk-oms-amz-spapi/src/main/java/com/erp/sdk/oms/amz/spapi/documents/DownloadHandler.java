package com.erp.sdk.oms.amz.spapi.documents;// DownloadExample.java
// This example is for use with the Selling Partner API for Reports, Version: 2021-06-30
// and the Selling Partner API for Feeds, Version: 2021-06-30

import java.io.*;

import java.nio.charset.Charset;
import java.rmi.ServerException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;

/**
 * Example that downloads a document.
 */
@Slf4j
public class DownloadHandler {


    /**
     * Download and optionally decompress the document retrieved from the given url.
     *
     * @param filePath FastDFS文件路径
     * @throws IOException              when there is an error reading the response
     * @throws IllegalArgumentException when the charset is missing
     */
    public JSONArray downloadFromFastDFSAndParse(String filePath, Map<String, String> excelConfig, String recordType) throws IOException, IllegalArgumentException {
        byte[] fileByte = FastDFSClientUtil.getFileByte(filePath);
        if (null == fileByte) {
            throw new ServerException("获取FastFDFS文件流失败：" + filePath);
        }
        // 文件元数据信息
        Map<String, String> fileMetadata = checkAndGetFileMetadata(filePath);
        // 报告压缩算法：GZIP或空
        String compressionAlgorithm = fileMetadata.get("compressionAlgorithm");
        // 报告内容类型
        MediaType mediaType = checkAndParseMediaTypeFromFileMetadata(filePath, fileMetadata);
        //        Charset charset = mediaType.charset();
        Charset charset = mediaType.getCharset();
//        Charset charset = StandardCharsets.ISO_8859_1;

        Closeable closeThis = null;
        BufferedReader reader = null;
        try {

            if ("GZIP".equalsIgnoreCase(compressionAlgorithm)) {
                GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(fileByte));
                InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
                reader = new BufferedReader(inputStreamReader);
                closeThis = gzipInputStream;
            } else {
                // This example assumes that the download content has a charset in the content-type header, e.g.
                // text/plain; charset=UTF-8
//            if ("text".equals(mediaType.type()) && "plain".equals(mediaType.subtype())) {
                if ("text".equals(mediaType.getType()) && "plain".equals(mediaType.getSubtype())) {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(fileByte);
                    InputStreamReader inputStreamReader;
                    if (null == charset) {
                        inputStreamReader = new InputStreamReader(inputStream);
                    } else {
                        inputStreamReader = new InputStreamReader(inputStream, charset);
                    }
                    // fastDFS保存过的文件统一UTF-8
//                 inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    closeThis = inputStreamReader;
                    reader = new BufferedReader(inputStreamReader);
                    closeThis = reader;
                }
            }

            if (null == reader) {
                String msg = StrUtil.format("下载失败:无法解析获取到流:url={}, mediaType={}", filePath, mediaType);
                throw new ServerException(msg);
            }
            return parseToJSONArray(excelConfig, reader, recordType, filePath);
        } finally {
            if (null != closeThis) {
                closeThis.close();
            }
        }
    }


    /**
     * Download and optionally decompress the document retrieved from the given url.
     *
     * @param url                  the url pointing to a document
     * @param compressionAlgorithm the compressionAlgorithm used for the document
     * @throws IOException              when there is an error reading the response
     * @throws IllegalArgumentException when the charset is missing
     */
    public String downloadAndUploadFastDFS(String url,
                                           String compressionAlgorithm,
                                           String fileName,
                                           String reportDocumentId,
                                           String recordType
    ) throws IOException, IllegalArgumentException {
        Response response = sendRequest(url);

        try (ResponseBody responseBody = response.body()) {
            String mediaTypeHeader = response.header("Content-Type");
//            MediaType mediaType = MediaType.parse(mediaTypeHeader);
            //        Charset charset = mediaType.charset();
            MediaType mediaType = MediaType.parseMediaType(mediaTypeHeader);
            Charset charset = mediaType.getCharset();
            if (charset == null && StringUtils.isBlank(compressionAlgorithm)) {
                throw new IllegalArgumentException(String.format("Could not parse character set from '%s'", mediaType));
            }
            // 解析文件信息
            Map<String, String> nameValuePair = new HashMap<>();
            nameValuePair.put("Content-Type", mediaTypeHeader);
            nameValuePair.put("compressionAlgorithm", compressionAlgorithm);
            nameValuePair.put("reportDocumentId", reportDocumentId);
            nameValuePair.put("recordType", recordType);
            if ("GZIP".equalsIgnoreCase(compressionAlgorithm)) {
                fileName = fileName.concat(".gz");
                try (InputStream inputStream = responseBody.byteStream()) {
                    if (null == inputStream) {
                        String msg = StrUtil.format("下载失败:无法解析获取到流:url={}, mediaType={}", url, mediaType);
                        throw new ServerException(msg);
                    }
                    // 保存到FastDFS
                    return FastDFSClientUtil.uploadFile(inputStream, fileName, nameValuePair);
                }
            }

            if (null == charset) {
                // 无字符集
                try (InputStream inputStream = responseBody.byteStream()) {
                    if (null == inputStream) {
                        String msg = StrUtil.format("下载失败:无法解析获取到流:url={}, mediaType={}", url, mediaType);
                        throw new ServerException(msg);
                    }
                    // 保存到FastDFS
                    return FastDFSClientUtil.uploadFile(inputStream, fileName, nameValuePair);
                }
            } else {
                // 有字符集
                InputStream inputStream = null;
                InputStreamReader inputStreamReader = null;
                try {
                    inputStream = responseBody.byteStream();
                    if (null == inputStream) {
                        String msg = StrUtil.format("下载失败:无法解析获取到流:url={}, mediaType={}", url, mediaType);
                        throw new ServerException(msg);
                    }
                    inputStreamReader = new InputStreamReader(inputStream, charset);
                    // 保存到FastDFS
                    return FastDFSClientUtil.uploadFile(inputStreamReader, charset, fileName, nameValuePair);
                } finally {
                    if (null != inputStream) {
                        inputStream.close();
                    }
                    if (null != inputStreamReader) {
                        inputStreamReader.close();
                    }
                }
            }
        }
    }


    private static Set<String> parseToTitle(BufferedReader in) throws IOException {
        String line;
        LinkedHashMap<String, Integer> p;
        do {
            line = in.readLine();
            if (!ObjectUtils.isEmpty(line)) {
                p = getReportTitle(line);
                if (p == null) {
                    break;
                }
                return p.keySet();
            }
        } while (line != null);
        throw new ServerException("获取异常");
    }

    private static JSONArray parseToJSONArray(Map<String, String> excelConfig, BufferedReader in, String recordType, String filePath) throws IOException {
        String line;
        int k = 1;
        JSONArray jl = new JSONArray();
        Map<String, Integer> p = null;
        Map<Integer, String> r = null;
        do {
            line = in.readLine();
            if (!ObjectUtils.isEmpty(line)) {
                if (k == 1) {
                    p = getReportTitle(line);
                    if (p == null) {
                        break;
                    }
                    r = getExcelConfig(p, excelConfig, recordType, filePath);
                    if (r == null) {
                        break;
                    }
                } else {
                    JSONObject jo = getReportData(line, r);
                    if (jo != null) {
                        jl.add(jo);
                    }
                }
                k++;
            }
        } while (line != null);

        if (jl.isEmpty()) {
            return null;
        }
        return jl;
    }


    private static LinkedHashMap<String, Integer> getReportTitle(String title) {
        if (ObjectUtils.isEmpty(title)) {
            return null;
        }
        String[] str = title.split("\t");
        if (str.length < 2) {
            return null;
        }
        LinkedHashMap<String, Integer> p = new LinkedHashMap<>();
        for (int k = 0; k < str.length; k++) {
            // 移除字段首尾"", 移除零宽不断空格（ZWNBSP）字符（Unicode U+FEFF）
            String currentStr = str[k].replaceAll("^\"|\"$", "").replaceAll("\uFEFF", "");
            p.put(currentStr, k);
        }
        return p;
    }

    /**
     * url:获取报告列名称
     */
    public static Set<String> getReportTitleFromUrl(String url, String compressionAlgorithm) throws Exception {
        Response response = sendRequest(url);

        try (ResponseBody responseBody = response.body()) {
//            MediaType mediaType = MediaType.parse(response.header("Content-Type"));
//            Charset charset = mediaType.charset();
            MediaType mediaType = MediaType.parseMediaType(response.header("Content-Type"));
            Charset charset = mediaType.getCharset();
            if (charset == null) {
                throw new IllegalArgumentException(String.format("Could not parse character set from '%s'", mediaType));
            }

            Closeable closeThis = null;
            try {
                InputStream inputStream = responseBody.byteStream();
                closeThis = inputStream;

                if ("GZIP".equals(compressionAlgorithm)) {
                    inputStream = new GZIPInputStream(inputStream);
                    closeThis = inputStream;
                }

                // This example assumes that the download content has a charset in the content-type header, e.g.
                // text/plain; charset=UTF-8
//                if ("text".equals(mediaType.type()) && "plain".equals(mediaType.subtype())) {
                if ("text".equals(mediaType.getType()) && "plain".equals(mediaType.getSubtype())) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);
                    closeThis = inputStreamReader;

                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    closeThis = reader;

                    return parseToTitle(reader);
                    //Handle content with binary data/other media types here.
                }
            } finally {
                if (closeThis != null) {
                    closeThis.close();
                }
            }
        }
        throw new ServerException("下载失败未找到有效mediaType, url=" + url);
    }

    /**
     * url:获取报告列名称
     */
    public static Set<String> getReportTitleFromFilePath(String filePath) throws Exception {
        InputStream inputStream = FastDFSClientUtil.getInputStream(filePath);
        if (null == inputStream) {
            throw new ServerException("获取FastFDFS文件流失败：" + filePath);
        }
        // 文件元数据信息
        Map<String, String> fileMetadata = checkAndGetFileMetadata(filePath);

        // 报告内容类型
        MediaType mediaType = checkAndParseMediaTypeFromFileMetadata(filePath, fileMetadata);
//        Charset charset = mediaType.charset();
        Charset charset = mediaType.getCharset();
        BufferedReader reader = null;
        try {
            if ("GZIP".equalsIgnoreCase(fileMetadata.get("compressionAlgorithm"))) {
                inputStream = new GZIPInputStream(inputStream);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                reader = new BufferedReader(inputStreamReader);
            }

            // This example assumes that the download content has a charset in the content-type header, e.g.
            // text/plain; charset=UTF-8
//            if ("text".equals(mediaType.type()) && "plain".equals(mediaType.subtype())) {
            if ("text".equals(mediaType.getType()) && "plain".equals(mediaType.getSubtype())) {
                InputStreamReader inputStreamReader;
                if (null == charset) {
                    inputStreamReader = new InputStreamReader(inputStream);
                } else {
                    inputStreamReader = new InputStreamReader(inputStream, charset);
                }
                reader = new BufferedReader(inputStreamReader);
            }
            if (null == reader) {
                String msg = StrUtil.format("下载失败:无法解析获取到流:url={}, mediaType={}", filePath, mediaType);
                throw new ServerException(msg);
            }
            return parseToTitle(reader);
        } finally {
            inputStream.close();
            if (null != reader) {
                reader.close();
            }
        }
    }

    public static Map<String, String> checkAndGetFileMetadata(String filePath) throws ServerException {
        Map<String, String> fileMetadata = FastDFSClientUtil.getFileMetadata(filePath);
        if (CollectionUtils.isEmpty(fileMetadata)) {
            throw new ServerException("文件元数据为空:filePath=" + filePath);
        }
        return fileMetadata;
    }

    /**
     * 从文件原数据解析出MediaType
     *
     * @param filePath
     * @param fileMetadata
     * @return
     */
    private static MediaType checkAndParseMediaTypeFromFileMetadata(String filePath, Map<String, String> fileMetadata) {
        String mediaTypeStr = fileMetadata.get("Content-Type");
        // 报告压缩算法：GZIP或空
        String compressionAlgorithm = fileMetadata.get("compressionAlgorithm");
        MediaType mediaType;
        if (StringUtils.isBlank(mediaTypeStr)) {
            throw new IllegalArgumentException(StrUtil.format("Content-Type为空：filePath={}", filePath));
        } else {
//            mediaType = MediaType.parse(mediaTypeStr);
//            Charset charset = mediaType.charset();
            mediaType = MediaType.parseMediaType(mediaTypeStr);
            Charset charset = mediaType.getCharset();
            if (charset == null && StringUtils.isBlank(compressionAlgorithm)) {
                throw new IllegalArgumentException(String.format("Could not parse character set from '%s'", mediaType));
            }
        }
        return mediaType;
    }


    public static Response sendRequest(String url) throws IOException {
        OkHttpClient httpclient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response = httpclient.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.error("Call to download content was unsuccessful with response code: {} and message: {}", response.code(), response.message());
            throw new ServerException("Call to download content was unsuccessful with response code: " + response.code() + " and message: " + response.message());
        }
        return response;
    }


    private static Map<Integer, String> getExcelConfig(Map<String, Integer> p, Map<String, String> excelConfig, String recordType, String filePath) {
        Map<Integer, String> r = new HashMap<>();
        Map<Integer, String> notExistMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : p.entrySet()) {
            if (excelConfig.get(entry.getKey()) != null) {
                r.put(entry.getValue(), excelConfig.get(entry.getKey()));
            } else {
                // 不存在配置的字段：原生返回
                notExistMap.put(entry.getValue(), entry.getKey());
            }
        }
        if (r.isEmpty()) {
            throw new ServiceException("未找到字段配置");
        }
        if (CollectionUtil.isEmpty(notExistMap)) {
            return r;
        }
        log.warn("cfg_amz_report_field报告类型【{}】, filePath={},存在未配置的字段：{}", recordType, filePath, notExistMap.values());
        r.putAll(notExistMap);
        return r;
        // 查找不存在的键
//        List<String> notExistKeyList = p.keySet().stream()
//                .filter(key -> !excelConfig.containsKey(key))
//                .collect(Collectors.toList());
//        if (CollectionUtil.isEmpty(notExistKeyList)) {
//            return r;
//        }
//        String msg = StrUtil.format("cfg_amz_report_field报告类型【{}】, filePath={},存在未配置的字段：{}", recordType, filePath, notExistKeyList);
//        throw new ServiceException(msg);
    }

    private static JSONObject getReportData(String str, Map<Integer, String> r) {
        if (ObjectUtils.isEmpty(str)) {
            return null;
        }
        if (r == null || r.isEmpty()) {
            return null;
        }
        String[] strArray = str.split("\t");
        JSONObject jo = new JSONObject();
        for (Integer k = 0; k < strArray.length; k++) {
            if (ObjectUtils.isEmpty(r.get(k))) {
                continue;
            }
            jo.set(r.get(k), checkStr(strArray[k]));
        }
        return jo;
    }

    public static String checkStr(String val) {
        if (StringUtils.isEmpty(val)) {
            return "";
        }
        // 移除首尾""
        return val.replaceAll("^\"|\"$", "");
    }
}