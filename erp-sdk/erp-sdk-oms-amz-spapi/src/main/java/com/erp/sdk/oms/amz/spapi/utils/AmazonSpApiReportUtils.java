package com.erp.sdk.oms.amz.spapi.utils;

import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import com.erp.sdk.oms.amz.spapi.csv.FbaMyiAllInventoryCsvReportEntity;
import com.erp.sdk.oms.amz.spapi.csv.ListingCsvReportEntity;
import com.erp.sdk.oms.amz.spapi.documents.DownloadHelper;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.springframework.beans.factory.xml.DocumentLoader;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * 亚马逊SP-API报告工具类
 *
 * @author Jim
 * @date 2023/10/16 12:13
 */
@Slf4j
public class AmazonSpApiReportUtils {

    /**
     * 下载并转换库存报告
     */
    public static List<ListingCsvReportEntity> downloadAndParseListing(String url) throws IOException {
        String csvContent  = download(url);
        CSVReader reader = parseCSV(csvContent);
        // 转换bean
        return toBean(reader, ListingCsvReportEntity.class);
    }

    /**
     * 下载并转换（亚马逊物流管理库存 - 已存档）
     */
    public static List<FbaMyiAllInventoryCsvReportEntity> downloadAndParseFbaAllInventory(String url) throws IOException {
        String csvContent  = download(url);
        CSVReader reader = parseCSV(csvContent);
        // 转换bean
        return toBean(reader, FbaMyiAllInventoryCsvReportEntity.class);
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
     * @param url the url pointing to a document
     * @throws IOException              when there is an error reading the response
     * @throws IllegalArgumentException when the charset is missing
     */
    public static String download(String url) throws IOException, IllegalArgumentException {
        OkHttpClient httpclient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpclient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorMsg = String.format("[Amazon SP-APi] report download Call to download content was unsuccessful with response code: %d and message: %s", response.code(), response.message());
                throw new ServiceException(errorMsg);
            }
            // 获取响应头中的 Content-Type
            String contentType = response.header("Content-Type");
            if (contentType.contains("gzip")) {
                // 如果 Content-Type 包含 "gzip"，则解压缩响应体
                GZIPInputStream gzipInputStream = new GZIPInputStream(response.body().byteStream());
                return convertStreamToString(gzipInputStream);
            }
            return response.body().string();
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

    public static void main(String[] args) throws Exception{
//        String url = "https://d34o8swod1owfl.cloudfront.net/Report_47700__GET_MERCHANT_LISTINGS_ALL_DATA_.txt";
//        List<ListingCsvReportEntity> list = downloadAndParseListing(url);
//        System.out.println(JSONUtil.toJsonStr(list));
        // 亚马逊物流管理库存 - 已存档
//        String url = "https://tortuga-prod-na.s3-external-1.amazonaws.com/2a2d3258-3b23-4f23-8eb6-c019f45bab25.amzn1.tortuga.4.na.T1507HA60E8SWN?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20231103T004815Z&X-Amz-SignedHeaders=host&X-Amz-Expires=300&X-Amz-Credential=AKIA5U6MO6RAETTDXOQT%2F20231103%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=c3e2ede4089507b180769c7d0459665988e9cf4f4f5730c01a1a2468094de64d";
//        List<FbaMyiAllInventoryCsvReportEntity> list = downloadAndParseFbaAllInventory(url);
//        System.out.println(JSONUtil.toJsonStr(list));

        // 亚马逊物流管理库存状况报告


        // 亚马逊物流预留库存报告
        String url = "https://tortuga-prod-na.s3-external-1.amazonaws.com/f36c2976-2253-451b-bb3d-e6e47a43ffca.amzn1.tortuga.4.na.TUE3ZIC1NB3GN?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20231103T014513Z&X-Amz-SignedHeaders=host&X-Amz-Expires=300&X-Amz-Credential=AKIA5U6MO6RAETTDXOQT%2F20231103%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=4d70013cd08b5ef7b76a2fa0fad6fd05667e4f2e5a99aeab70502d2cec59085c";
        String cvsContent = download(url);
        System.out.println(cvsContent);


    }
}
