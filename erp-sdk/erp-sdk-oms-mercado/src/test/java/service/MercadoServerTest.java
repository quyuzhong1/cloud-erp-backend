package service;

import com.alibaba.fastjson.JSONObject;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.utils.UUID;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import okhttp3.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= MercadoSdkClientService.class)
public class MercadoServerTest {

    @Resource
    private MercadoSdkClientService imlServer;

    public MercadoServerTest(){
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appToken","44ac3ae1211d416a080858e57833cc14");
        authMap.put("appKey","fa0c90d7dbb434fa2160209756db677c");
        ThirdWarehouseContext.setAuthMap(authMap);
    }


    @Test
    public void createUserTest() {
        String baseUrl = "https://api.mercadolibre.com/users/global_selling_test_user";

        //组装刷新token请求的url
//        String baseUrl = "https://api.mercadolibre.com/oauth/token?grant_type=refresh_token&client_id=3457166802805723&client_secret=F1L9EUIhsIlUc6yRGyzMhwFVweBZKIJ7&refresh_token=TG-65df03258fea2f0001855d7d-1509269799";

        //入参（无）
        Map<String, Object> param = new HashMap<>();
        param.put("site_id", "CBT");
        param.put("country_id", "CN");
        //请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "Bearer APP_USR-3457166802805723-022921-2ac16f3485aa65c12e00c2cb6e0f04cb-1509269799");
        String paramStr = JSONObject.toJSONString(param);
        //发起POST请求
        Map<String, Object> map = sendPost(baseUrl, paramStr);
        System.out.println(map);
    }
    @Test
    public void listingTest() {
        String baseUrl = "https://api.mercadolibre.com/marketplace/products/search?status=active&product_identifier=0123456789";

        //组装刷新token请求的url
//        String baseUrl = "https://api.mercadolibre.com/oauth/token?grant_type=refresh_token&client_id=3457166802805723&client_secret=F1L9EUIhsIlUc6yRGyzMhwFVweBZKIJ7&refresh_token=TG-65df03258fea2f0001855d7d-1509269799";

        //入参（无）
        Map<String, Object> param = new HashMap<>();
        //请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("Authorization", "APP_USR-3457166802805723-022921-2ac16f3485aa65c12e00c2cb6e0f04cb-1509269799");
        String paramStr = JSONObject.toJSONString(param);
        //发起POST请求
        Map<String, Object> map = sendPost(baseUrl, paramStr);
        System.out.println(map);
    }


    public Map<String, Object> sendPost(String baseUrl, String param) {
        Response response = null;
        Long start = System.currentTimeMillis();
        String responseString = "";
        String consumerId = UUID.randomUUID().toString();
        Map<String, Object> ressultMap = new HashMap<>();
        MediaType mediaType = MediaType.parse("application/json");
        try {
                response = this.doSend(
                        new Request.Builder()
                                .header("Authorization", "Bearer APP_USR-3457166802805723-022921-2ac16f3485aa65c12e00c2cb6e0f04cb-1509269799")
                                .url(baseUrl)
                                .post(RequestBody.create(mediaType, param))
                                .build()
                );

            responseString = response.body().string();
            ressultMap.put("code", String.valueOf(response.code()));
            ressultMap.put("msg", response.message());
            ressultMap.put("data", responseString);
            return ressultMap;
        } catch (Exception e) {
            if (e.getMessage().contains("connect timed out")) {
                ressultMap.put("code", "411");
            }else{
                ressultMap.put("code", "500");
            }
            ressultMap.put("msg", e.getMessage());
            return ressultMap;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private Response doSend(Request request) throws Exception {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(100, TimeUnit.SECONDS)//设置读取超时时间
                .build();
        Response response = okHttpClient.newCall(request).execute();
        return response;
    }
}