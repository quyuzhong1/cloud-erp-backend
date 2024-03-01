package apiTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sdk.third.lingxing.core.Config;
import com.sdk.third.lingxing.core.HttpMethod;
import com.sdk.third.lingxing.core.HttpRequest;
import com.sdk.third.lingxing.core.HttpResponse;
import com.sdk.third.lingxing.utils.HttpExecutor;
import com.sdk.third.lingxing.utils.LingxingApiSignUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SetCategoryDemo {
    public static void main(String[] args) throws Exception {
        String endpoint = "xxxx";
        String appId = "xxxx";
        String accessToken = "xxxx";

        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("timestamp", System.currentTimeMillis() / 1000 + "");
        queryParam.put("app_key", appId);
        queryParam.put("access_token", accessToken);
        Map<String, Object> body = new HashMap<>();
        List<Map<String, String>> bodyParam = new ArrayList<>();
        Map<String, String> body_content = new HashMap<>();
        body_content.put("title","Weaving Loom 织机产品线");
        bodyParam.add(body_content);
        body.put("data", JSONObject.toJSONString(bodyParam));
        Map<String, Object> signMap = new HashMap<>();
        signMap.putAll(queryParam);
        signMap.putAll(body);
        body.put("data", bodyParam);
        String sign = LingxingApiSignUtils.sign(signMap, appId);
        queryParam.put("sign", sign);
        log.info("sign:{}", sign);
        HttpRequest<Object> build = HttpRequest.builder(Object.class)
                .method(HttpMethod.POST)
                .endpoint(endpoint)
                .path("erp/sc/routing/storage/category/set")
                .queryParams(queryParam)
                .json(JSON.toJSONString(body))
                .build();
        HttpResponse execute = HttpExecutor.create().execute(build);
        log.info("execute:{}", execute.readEntity(Object.class));
    }
}
