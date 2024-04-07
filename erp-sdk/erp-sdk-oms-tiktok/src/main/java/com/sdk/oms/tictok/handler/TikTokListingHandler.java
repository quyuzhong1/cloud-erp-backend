package com.sdk.oms.tictok.handler;

import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractProductHandler;
import com.common.business.utils.RedisUtil;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.sdk.oms.tictok.dto.TikTokListingDTO;
import com.sdk.oms.tictok.service.TikTokSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
/**
 * 沃尔玛商品信息
 * @Author Luo_WG
 * @Date 2023/10/18 11:24
 **/
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.TIK_TOK)
@BusinessType(BusinessTypeEnum.PRODUCT)
public class TikTokListingHandler extends AbstractProductHandler<TikTokListingDTO, PlatformProductDTO> {
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private TikTokSdkClientService mercadoSdkClientService;

    @Override
    public List<TikTokListingDTO> download(JobTaskDTO data) {
        return null;
    }

    @Override
    public List<PlatformProductDTO> convert(List<TikTokListingDTO> sourceDataList) {
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return sourceDataList.stream()
                // 组装
                .map(TikTokListingDTO::convertDTO).collect(Collectors.toList());
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }





    public static String calSign(HttpRequest req, String secret) {
        Map<String, List<String>> queries = req.getQueryParams();

        List<String> keys = queries.keySet().stream()
                .filter(k -> !"sign".equals(k) && !"access_token".equals(k))
                .collect(Collectors.toList());

        Collections.sort(keys);

        StringBuilder inputBuilder = new StringBuilder();
        for (String key : keys) {
            List<String> values = queries.get(key);
            for (String value : values) {
                inputBuilder.append(key).append(value);
            }
        }

        String input = inputBuilder.toString();

        input = req.getPath() + input;

        String contentType = req.getHeader("Content-type");
        if (contentType != null && !contentType.equals("multipart/form-data")) {
            try (InputStream inputStream = req.getBody()) {
                byte[] bodyBytes = readAllBytes(inputStream);
                String body = new String(bodyBytes, StandardCharsets.UTF_8);
                input += body;
                req.setBody(bodyBytes);
            } catch (IOException e) {
                // Handle error
                e.printStackTrace();
            }
        }

        input = secret + input + secret;

        return generateSHA256(input, secret);
    }

    private static String generateSHA256(String input, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] digest = sha256Hmac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            // Handle error
            e.printStackTrace();
            return "";
        }
    }

    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }
}

class HttpRequest {
    private String path;
    private Map<String, List<String>> queryParams;
    private Map<String, String> headers;
    private InputStream body;

    public HttpRequest(String path, Map<String, List<String>> queryParams, Map<String, String> headers, InputStream body) {
        this.path = path;
        this.queryParams = queryParams;
        this.headers = headers;
        this.body = body;
    }

    public String getPath() {
        return path;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public InputStream getBody() {
        return body;
    }

    public void setBody(byte[] bodyBytes) {
        this.body = new InputStream() {
            private int index = 0;

            @Override
            public int read() throws IOException {
                if (index < bodyBytes.length) {
                    return bodyBytes[index++];
                } else {
                    return -1;
                }
            }
        };
    }

    public static void main(String[] args) {
        // 模拟 HTTP 请求
        String path = "/authorization/202309/shops";
        Map<String, List<String>> queryParams = new HashMap<>();
        queryParams.put("access_token", Arrays.asList("ROW_GzpNXQAAAACj-JAAAriAWjVtF2MrUIFdsyKbmgpFdy5ZKZU7_DcW_ahRpfbkhswwJKnrzfpPj19EPf6OyL_uhPFuQitb3TvgTVppCInACVvzpgcRGcI3K9Zp0hN8V0cL4vJXwjXCOrBuHT-kXLvYPhzjmDJYZA3KcjPmeDpH4EsgaB-YkuAstQ"));
        queryParams.put("app_key", Arrays.asList("6buinkjt3hmld"));
        queryParams.put("shop_id", Arrays.asList(""));
        queryParams.put("sign", Arrays.asList("6b35dab19933ca6ba538c33f04cde9a5171dfb678a05ea8aa75dfb67fcad0c48"));
        queryParams.put("timestamp", Arrays.asList("1712474227"));
        queryParams.put("version", Arrays.asList("202309"));

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        headers.put("x-tts-access-token", "ROW_GzpNXQAAAACj-JAAAriAWjVtF2MrUIFdsyKbmgpFdy5ZKZU7_DcW_ahRpfbkhswwJKnrzfpPj19EPf6OyL_uhPFuQitb3TvgTVppCInACVvzpgcRGcI3K9Zp0hN8V0cL4vJXwjXCOrBuHT-kXLvYPhzjmDJYZA3KcjPmeDpH4EsgaB-YkuAstQ");

        String body = "{}"; // 空 JSON 作为示例请求体
        ByteArrayInputStream bodyStream = new ByteArrayInputStream(body.getBytes());

        HttpRequest request = new HttpRequest(path, queryParams, headers, bodyStream);

        // 设置 secret
        String secret = "8ff628de24faf70c24855de4d967fb6a17a47e3f";

        // 调用 calSign 方法计算签名
        String signature = TikTokListingHandler.calSign(request, secret);

        // 打印签名结果
        System.out.println("Signature: " + signature);
    }

}
