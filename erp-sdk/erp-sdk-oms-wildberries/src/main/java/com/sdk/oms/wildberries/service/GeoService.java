package com.sdk.oms.wildberries.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.sdk.oms.wildberries.dto.GeoResponse;
import com.sdk.oms.wildberries.dto.WildberriesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName GeoService
 * @description: 经纬度获取地址信息
 * @date 2025年09月19日
 * @version: 1.0
 */
@Slf4j
@Component
public class GeoService {
//    String url = "https://api.opencagedata.com/geocode/v1/json?q=52.5432379,+13.4142133&key=d7bb6c7b42ce4fd0852545f333494a7c&language=zh&pretty=1";

    private static final String API_KEY = "d7bb6c7b42ce4fd0852545f333494a7c";
    private static final String BASE_URL = "https://api.opencagedata.com/geocode/v1/json";

    /**
     * 获取经纬度地址
     * @param longitude 经度
     * @param latitude 纬度
     * @return
     */
    public GeoResponse getGeo(BigDecimal longitude, BigDecimal latitude) {
        try {
            // 构建请求URL
            String query = String.format("q=%s,%s",
                    URLEncoder.encode(String.valueOf(latitude), "UTF-8"),
                    URLEncoder.encode(String.valueOf(longitude), "UTF-8"));
            String requestUrl = BASE_URL + "?" + query + "&key=" + API_KEY + "&language=zh&pretty=1";
            // 发送HTTP请求
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // 获取响应
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder bodyStr = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    bodyStr.append(inputLine);
                }
                in.close();
                System.out.println("bodyStr:" + bodyStr.toString());
                // 可以在此处添加JSON解析逻辑
                GeoResponse response = JSON.parseObject(JSONUtil.toJsonStr(bodyStr.toString()),new TypeReference<GeoResponse>() {}.getType());
                return response;
            } else {
                System.out.println("请求失败，HTTP错误码: " + responseCode);
                throw new ServiceException("请求失败，HTTP错误码: {}", responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        }
    }
}
