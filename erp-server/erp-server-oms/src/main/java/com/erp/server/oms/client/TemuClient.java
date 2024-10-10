package com.erp.server.oms.client;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.UrlContant;
import com.common.core.utils.HttpCommonUtil;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Slf4j
@Data
public class TemuClient {

    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";

    //https://seller.kuajingmaihuo.com/sop/view/867739977041685428#r2WKrz
    private static final String TEMU_US_URL = "http://40.118.250.12:7000/openapi/router";
//    private static final String TEMU_US_URL = "http://openapi-b-us.temudemo.com/openapi/router";
    private static final String ORDER_LIST = "bg.order.list.get";

    //https://seller.kuajingmaihuo.com/sop/view/750197804480663142#SjadVR
    private static final String TEMU_GOOD_US_URL = "http://40.118.250.12:7100/openapi/router";
//    private static final String TEMU_GOOD_US_URL = "https://openapi.kuajingmaihuo.com/openapi/router";
//    private static final String TEMU_GOOD_US_URL = "https://kj-openapi.temudemo.com/openapi/router";
    private static final String GOODS_LIST = "bg.goods.list.get";


//    private static final String APP_KEY = "73f83507e4244c78a5b83e1174bcbb19";
//    private static final String APP_SECRET = "e1557f76a9db2b8e5b335c58b6b5c01b698708ed";
//    private static final String ACCESS_TOKEN = "dnkpkcbcevl2xnflubbhdtyfftgcgkqsbozcwdoxgtdcaiysmdcqbln9";

//美区
    private static final String APP_KEY = "ab3a401ed6c265793776aa3d4c48bd6f";
    private static final String APP_SECRET = "a05e0902cf9c1b3c372680e084f1d424332284fb";
    private static final String ACCESS_TOKEN = "upsfmg4urc2tqviee1smfl2hk68fuunhf82lz4dn9ydqqwkmzubcvjjxzyo";

    private WebClient webClient;

    @Resource
    private HttpClient httpClient;


    public static void main(String[] args) {
        TemuEntity entity = new TemuEntity();
        TemuClient temuClient = new TemuClient();
        //非美区可以
//        temuClient.getGoodList(entity);

        //美区可以
        temuClient.getOrderList(entity);
    }

    public void getGoodList(TemuEntity entity ){
        entity.setData_type("JSON");
        entity.setAccess_token(ACCESS_TOKEN);
        entity.setApp_key(APP_KEY);
        entity.setTimestamp((int) (System.currentTimeMillis()/1000));
        entity.setType(GOODS_LIST);

        String jsonParameters = JSONObject.toJSONString(entity);
        JSONObject jsonObject = JSONObject.parseObject(jsonParameters);
        List<String> sortedParams = new ArrayList<>();
        addParameters(jsonObject, sortedParams);
        Collections.sort(sortedParams);
        String sign = this.generateSignature(sortedParams);
        entity.setSign(sign);

        jsonParameters = JSONObject.toJSONString(entity);
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("Content-Type", "application/json");
        JSONObject result = HttpCommonUtil.sendOkhttp(TEMU_GOOD_US_URL, jsonParameters, null, headerMap, RequestMethod.POST);
        System.out.println("===="+result);
        System.out.println("curl -X POST -H 'content-type: application/json' -d '"+jsonParameters+"' "+TEMU_GOOD_US_URL);
    }

    public void getOrderList(TemuEntity entity ){
        entity.setData_type("JSON");
        entity.setAccess_token(ACCESS_TOKEN);
        entity.setApp_key(APP_KEY);
        entity.setTimestamp((int) (System.currentTimeMillis()/1000));
        entity.setType(ORDER_LIST);

        String jsonParameters = JSONObject.toJSONString(entity);
        JSONObject jsonObject = JSONObject.parseObject(jsonParameters);
        List<String> sortedParams = new ArrayList<>();
        addParameters(jsonObject, sortedParams);
        Collections.sort(sortedParams);
        String sign = this.generateSignature(sortedParams);
        entity.setSign(sign);

        jsonParameters = JSONObject.toJSONString(entity);

        System.out.println(jsonParameters);
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("Content-Type", "application/json");
        JSONObject result = HttpCommonUtil.sendOkhttp(TEMU_US_URL, jsonParameters, null, headerMap, RequestMethod.POST);
//        String result = webClient.post().header(CONTENT_TYPE_HEADER_NAME, MediaType.APPLICATION_JSON_VALUE).bodyValue(entity).retrieve().bodyToMono(String.class).block();
        System.out.println("===="+result);
        System.out.println("curl -X POST -H 'content-type: application/json' -d '"+jsonParameters+"' "+TEMU_US_URL);

    }

    // 生成签名的方法
    public String generateSignature(List<String> sortedParams) {
        StringBuilder sb = new StringBuilder();
        for (String param : sortedParams) {
            sb.append(param);
        }
        String toBeSigned = APP_SECRET + sb.toString() + APP_SECRET;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(toBeSigned.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex.toUpperCase());
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addParameters(JSONObject obj, List<String> sortedParams) {
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            if (entry.getValue() instanceof JSONObject) {
                addParameters((JSONObject) entry.getValue(), sortedParams);
            } else if (entry.getValue() instanceof java.util.List) {
                java.util.List list = (java.util.List) entry.getValue();
                for (Object item : list) {
                    if (item instanceof JSONObject) {
                        addParameters((JSONObject) item, sortedParams);
                    }
                }
            } else {
                sortedParams.add(entry.getKey() + entry.getValue());
            }
        }
    }


}
