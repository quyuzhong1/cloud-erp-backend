package com.sdk.oms.tictok.handler;

import cn.hutool.json.JSONUtil;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractOrderHandler;
import com.common.business.utils.RedisUtil;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.sdk.oms.tictok.dto.TikTokOrderDTO;
import com.sdk.oms.tictok.service.TikTokSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.TIK_TOK)
@BusinessType(BusinessTypeEnum.ORDER)
public class TikTokOrderHandler extends AbstractOrderHandler<TikTokOrderDTO, PlatformOrderDTO> {
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private TikTokSdkClientService mercadoSdkClientService;


    public static void main(String[] args) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("access_token", "ROW_GzpNXQAAAACj-JAAAriAWjVtF2MrUIFdsyKbmgpFdy5ZKZU7_DcW_ahRpfbkhswwJKnrzfpPj19EPf6OyL_uhPFuQitb3TvgTVppCInACVvzpgcRGcI3K9Zp0hN8V0cL4vJXwjXCOrBuHT-kXLvYPhzjmDJYZA3KcjPmeDpH4EsgaB-YkuAstQ");
        queryParams.put("app_key", "6buinkjt3hmld");
        queryParams.put("sign", "6b35dab19933ca6ba538c33f04cde9a5171dfb678a05ea8aa75dfb67fcad0c48");
        queryParams.put("timestamp", "1712474227");
        queryParams.put("version", "202309");
        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("content-type", "application/json");
        headerMap.put("x-tts-access-token", "ROW_GzpNXQAAAACj-JAAAriAWjVtF2MrUIFdsyKbmgpFdy5ZKZU7_DcW_ahRpfbkhswwJKnrzfpPj19EPf6OyL_uhPFuQitb3TvgTVppCInACVvzpgcRGcI3K9Zp0hN8V0cL4vJXwjXCOrBuHT-kXLvYPhzjmDJYZA3KcjPmeDpH4EsgaB-YkuAstQ");
        String secret = "8ff628de24faf70c24855de4d967fb6a17a47e3f";
        // 生成签名
        String generatedSign = urlParamsSort(queryParams, "/authorization/202309/shops", headerMap, secret);
        System.out.println("Generated signature: " + generatedSign);
        String sign = generateSHA256(generatedSign, secret);
        System.out.println(sign);

    }

    /**
     * 第一步提取除sign和access_token之外的所有查询参数。按字母顺序对参数的键重新排序
     * @param queries
     * @return
     */
    private static String urlParamsSort(Map<String, String> queries, String path, Map<String, String> headerMap, String secret) {
        // 提取除 "sign" 和 "access_token" 之外的所有查询参数
        List<String> keys = new ArrayList<>();
        for (String k : queries.keySet()) {
            if (!"sign".equals(k) && !"access_token".equals(k)) {
                keys.add(k);
            }
        }

        // 按字母顺序对参数的键重新排序
        Collections.sort(keys);

        // 生成重新排序的查询键字符串
        StringBuilder input = new StringBuilder();
        for (String key : keys) {
            input.append(key).append(queries.get(key));
        }

        input.insert(0, path);
        // 如果请求标头 content_type 不是 multipart/form-data，则追加到末尾 body
        if (!"multipart/form-data".equals(headerMap.get("content_type"))) {
            input.append(JSONUtil.toJsonStr(queries));
        }
        String finalString = secret + input.toString() + secret;
        return finalString;
    }

    public static String generateSHA256(String input, String secret) {
        try {
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSHA256.init(secretKey);
            byte[] hmacBytes = hmacSHA256.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            // Handle the exception
            e.printStackTrace();
            return "";
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    @Override
    public List<TikTokOrderDTO> download(JobTaskDTO task) {
        return null;
    }

    @Override
    public List<PlatformOrderDTO> convert(List<TikTokOrderDTO> sourceDataList) {
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return sourceDataList.stream()
                // 组装
                .map(TikTokOrderDTO::convertDTO).collect(Collectors.toList());
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }


}

