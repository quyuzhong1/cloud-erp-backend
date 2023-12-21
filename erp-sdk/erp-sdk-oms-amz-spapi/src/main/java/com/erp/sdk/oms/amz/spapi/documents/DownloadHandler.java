package com.erp.sdk.oms.amz.spapi.documents;// DownloadExample.java
// This example is for use with the Selling Partner API for Reports, Version: 2021-06-30
// and the Selling Partner API for Feeds, Version: 2021-06-30

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.rmi.ServerException;
import java.util.*;
import java.util.zip.GZIPInputStream;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Example that downloads a document.
 */
@Slf4j
public class DownloadHandler {


    /**
     * Download and optionally decompress the document retrieved from the given url.
     *
     * @param url                  the url pointing to a document
     * @param compressionAlgorithm the compressionAlgorithm used for the document
     * @throws IOException              when there is an error reading the response
     * @throws IllegalArgumentException when the charset is missing
     */
    public JSONArray download(String url, String compressionAlgorithm, Map<String, String> excelConfig) throws IOException, IllegalArgumentException {
        Response response = sendRequest(url);

        try (ResponseBody responseBody = response.body()) {
            MediaType mediaType = MediaType.parse(response.header("Content-Type"));
            Charset charset = mediaType.charset();
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
                if ("text".equals(mediaType.type()) && "plain".equals(mediaType.subtype())) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);
                    closeThis = inputStreamReader;

                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    closeThis = reader;

                    return parseToJSONArray(excelConfig, reader);
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

//    /**
//     * url:访问的网络地址
//     */
//    public static JSONArray downLoadFileToLocalGzip(String uri, Map<String, String> excelConfig) {
//        URLConnection conn = null;
//        InputStream inStream = null;
//        GZIPInputStream gzip = null;
//        InputStreamReader reader = null;
//        BufferedReader in = null;
//        try {
//            URL url = new URL(uri);
//            conn = url.openConnection();
//
//            inStream = conn.getInputStream();
//            gzip = new GZIPInputStream(inStream);
//            reader = new InputStreamReader(gzip);
//            in = new BufferedReader(reader);
//            return parseToJSONArray(excelConfig, in);
//        } catch (IOException e) {
//            throw new ServiceException("解析错误:" + e.getMessage());
//        } finally {
//            try {
//                assert inStream != null;
//                inStream.close();
//                assert gzip != null;
//                gzip.close();
//                assert reader != null;
//                reader.close();
//                assert in != null;
//                in.close();
//            } catch (Exception e) {
//                throw new RuntimeException("解析关闭资源错误", e);
//            }
//        }
//    }

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

    private static JSONArray parseToJSONArray(Map<String, String> excelConfig, BufferedReader in) throws IOException {
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
                    r = getExcelConfig(p, excelConfig);
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

//    public static JSONArray downLoadFileToLocal(String uri, Map<String, String> excelConfig) {
//        URLConnection conn = null;
//        InputStream inStream = null;
//        InputStream gzip = null;
//        InputStreamReader reader = null;
//        BufferedReader in = null;
//        try {
//            URL url = new URL(uri);
//            conn = url.openConnection();
//            inStream = conn.getInputStream();
//            reader = new InputStreamReader(inStream);
//            in = new BufferedReader(reader);
//            return parseToJSONArray(excelConfig, in);
//        } catch (IOException e) {
//            throw new ServiceException("解析错误:" + e.getMessage());
//        } finally {
//            try {
//                assert inStream != null;
//                inStream.close();
//                assert reader != null;
//                reader.close();
//                assert in != null;
//                in.close();
//            } catch (Exception e) {
//                throw new RuntimeException("解析关闭资源错误", e);
//            }
//        }
//    }


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
            p.put(str[k], k);
        }
        return p;
    }

    /**
     * url:获取报告列名称
     */
    public static Set<String> getReportTitleFromUrl(String url, String compressionAlgorithm) throws Exception {
        Response response = sendRequest(url);

        try (ResponseBody responseBody = response.body()) {
            MediaType mediaType = MediaType.parse(response.header("Content-Type"));
            Charset charset = mediaType.charset();
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
                if ("text".equals(mediaType.type()) && "plain".equals(mediaType.subtype())) {
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

    private static Response sendRequest(String url) throws IOException {
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


    private static Map<Integer, String> getExcelConfig(Map<String, Integer> p, Map<String, String> excelConfig) {
        Map<Integer, String> r = new HashMap<>();
        for (Map.Entry<String, Integer> entry : p.entrySet()) {
            if (excelConfig.get(entry.getKey()) != null) {
                r.put(entry.getValue(), excelConfig.get(entry.getKey()));
            }
        }
        if (r.isEmpty()) {
            return null;
        }
        return r;
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
        return val;
    }

}