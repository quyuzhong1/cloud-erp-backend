package apiTest;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.sdk.third.lingxing.core.Config;
import com.sdk.third.lingxing.core.HttpMethod;
import com.sdk.third.lingxing.core.HttpRequest;
import com.sdk.third.lingxing.core.HttpResponse;
import com.sdk.third.lingxing.utils.HttpExecutor;
import com.sdk.third.lingxing.utils.LingxingApiSignUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class FbaDemo {

    public static void main1(String[] args) throws Exception {
        String endpoint = "xxxx";
        String appId = "xxxx";
        String accessToken = "xxxx";

        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("timestamp", System.currentTimeMillis() / 1000 + "");
        queryParam.put("access_token", accessToken);
        queryParam.put("app_key", appId);

        Map<String, Object> body = new HashMap<>();
        body.put("sid", "140");
        body.put("start_date", "2023-12-01");
        body.put("end_date", "2024-02-05");
        body.put("shipment_status", "CLOSED");

        Map<String, Object> signMap = new HashMap<>();
        signMap.putAll(queryParam);
        signMap.putAll(body);

        String sign = LingxingApiSignUtils.sign(signMap, appId);
        queryParam.put("sign", sign);
        log.info("sign:{}", sign);

        HttpRequest<Object> build = HttpRequest.builder(Object.class)
                .method(HttpMethod.POST)
                .endpoint(endpoint)
                .path("erp/sc/data/fba_report/shipmentList")
                .queryParams(queryParam)
                .json(JSON.toJSONString(body))
                .config(Config.DEFAULT.withConnectionTimeout(30000).withReadTimeout(30000))
                .build();
        try (HttpResponse execute = HttpExecutor.create().execute(build)) {
            log.info("execute:{}", JSONUtil.toJsonStr(execute.readEntity(Object.class)));
        }
    }


    public static void main(String[] args) throws Exception {
        String endpoint = "xxxx";
        String appId = "xxxx";
        String accessToken = "xxxx";

        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("timestamp", System.currentTimeMillis() / 1000 + "");
        queryParam.put("access_token", accessToken);
        queryParam.put("app_key", appId);

        Map<String, Object> body = new HashMap<>();
        body.put("sid", 126);
        body.put("event_date", "2024-01-20");

        Map<String, Object> signMap = new HashMap<>();
        signMap.putAll(queryParam);
        signMap.putAll(body);

        String sign = LingxingApiSignUtils.sign(signMap, appId);
        queryParam.put("sign", sign);
        log.info("sign:{}", sign);

        HttpRequest<Object> build = HttpRequest.builder(Object.class)
                .method(HttpMethod.POST)
                .endpoint(endpoint)
                .path("erp/sc/data/fba_report/receivedInventory")
                .queryParams(queryParam)
                .json(JSON.toJSONString(body))
                .config(Config.DEFAULT.withConnectionTimeout(30000).withReadTimeout(30000))
                .build();
        try (HttpResponse execute = HttpExecutor.create().execute(build)) {
            log.info("execute:{}", JSONUtil.toJsonStr(execute.readEntity(Object.class)));
        }
    }

}
