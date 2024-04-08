package com.sdk.oms.tictok.handler;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import java.io.IOException;


public class testtt {
    public static void main(String[] args) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // 创建一个空的 JSON 请求体
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{}");

        // 创建 GET 请求，设置请求体为空的 JSON 对象
        Request request = new Request.Builder()
                .url("https://open-api.tiktokglobalshop.com/authorization/202309/shops?access_token=ROW_GzpNXQAAAACj-JAAAriAWjVtF2MrUIFdsyKbmgpFdy5ZKZU7_DcW_ahRpfbkhswwJKnrzfpPj19EPf6OyL_uhPFuQitb3TvgTVppCInACVvzpgcRGcI3K9Zp0hN8V0cL4vJXwjXCOrBuHT-kXLvYPhzjmDJYZA3KcjPmeDpH4EsgaB-YkuAstQ&app_key=6buinkjt3hmld&shop_id=&sign=f44819f738b0d80270a4c95862848e0b7db99405b28ab3a29a23b239b913992f&timestamp=1712560785&version=202309")
                .header("x-tts-access-token", "ROW_GzpNXQAAAACj-JAAAriAWjVtF2MrUIFdsyKbmgpFdy5ZKZU7_DcW_ahRpfbkhswwJKnrzfpPj19EPf6OyL_uhPFuQitb3TvgTVppCInACVvzpgcRGcI3K9Zp0hN8V0cL4vJXwjXCOrBuHT-kXLvYPhzjmDJYZA3KcjPmeDpH4EsgaB-YkuAstQ")
                .get()
                .build();

        // 发送请求
        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.body().string());
        }
    }
}
