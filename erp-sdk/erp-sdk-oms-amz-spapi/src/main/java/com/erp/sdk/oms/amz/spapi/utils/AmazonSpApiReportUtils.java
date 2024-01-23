package com.erp.sdk.oms.amz.spapi.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.erp.sdk.oms.amz.spapi.documents.*;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * 亚马逊SP-API报告工具类
 *
 * @author Jim
 * @date 2023/10/16 12:13
 */
@Slf4j
public class AmazonSpApiReportUtils {


    /**
     * 下载并转换
     */
    public static <T> List<T> downloadAndParse(String url, Class<T> tClass, Map<String, String> columnMap, String recordType) throws IOException {
        JSONArray jsonArray = downloadAndParse(url, columnMap, recordType);
        // 转换bean
        return JSONUtil.toList(jsonArray, tClass);
    }

    /**
     * 解析csv内容
     */
    public static CSVReader parseCSV(String csvContent) {
        CSVParser parser = new CSVParserBuilder()
                .withSeparator('\t')
                .withIgnoreQuotations(true)
                .build();
        return new CSVReaderBuilder(new StringReader(csvContent))
                .withCSVParser(parser)
                .build();
    }

    /**
     * 转换成指定class对象
     */
    public static <T> List<T> toBean(CSVReader reader, Class<T> clazz) {
        // 创建CsvToBean对象并指定映射到的类
        CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                .withType(clazz)
                .withIgnoreLeadingWhiteSpace(true)
                .build();
        // 通过CsvToBean的parse方法解析CSV并将数据映射到对象列表
        return csvToBean.parse();
    }

    /**
     * Download and optionally decompress the document retrieved from the given url.
     *
     * @param url        the url pointing to a document
     * @param reportType
     * @throws IOException              when there is an error reading the response
     * @throws IllegalArgumentException when the charset is missing
     */
    public static JSONArray downloadAndParse(String url, Map<String, String> columnMap, String reportType) {
        DownloadHandler obj = new DownloadHandler();
        try {
            return obj.downloadAndParse(url, columnMap, reportType);
        } catch (Exception e) {
            //Handle exception here.
            throw new RuntimeException("下载并解析亚马逊报告异常：url=" + url + " error="+ e.getMessage());
        }
    }

    public static String downloadAndUploadFastDFS(String url, String compressionAlgorithm, String filePath) {
        DownloadHandler obj = new DownloadHandler();
        try {
           return obj.downloadAndUploadFastDFS(url, compressionAlgorithm, filePath);
        } catch (Exception e) {
            //Handle exception here.
            throw new RuntimeException("下载亚马逊报告到FastDFS异常：url=" + url + " error="+ e.getMessage());
        }
    }


    /**
     * 将输入流转换为字符串
     */
    private static String convertStreamToString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
