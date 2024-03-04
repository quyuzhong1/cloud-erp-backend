package apiTest;

import cn.hutool.json.JSONUtil;
import com.sdk.third.lingxing.core.HttpMethod;
import com.sdk.third.lingxing.core.HttpRequest;
import com.sdk.third.lingxing.core.HttpResponse;
import com.sdk.third.lingxing.utils.HttpExecutor;
import com.sdk.third.lingxing.utils.LingxingApiSignUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GetDemo {

    public static void main(String[] args) throws Exception {
        String endpoint = "xxxx";
        String appId = "xxxx";
        String accessToken = "xxxx";

        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("timestamp", System.currentTimeMillis()/1000);
        queryParam.put("access_token", accessToken);
        queryParam.put("app_key", appId);
//        queryParam.put("offset", 0);
//        queryParam.put("length", 20);

        String sign = LingxingApiSignUtils.sign(queryParam, appId);
        queryParam.put("sign", sign);
        log.info("sign:{}", sign);

        HttpRequest<Object> build = HttpRequest.builder(Object.class)
                .method(HttpMethod.GET)
                .endpoint(endpoint)
                .path("erp/sc/data/seller/lists")
                .queryParams(queryParam)
                .build();
        HttpResponse execute = HttpExecutor.create().execute(build);
        log.info("execute:{}", JSONUtil.toJsonStr(execute.readEntity(Object.class)));

    }

}
