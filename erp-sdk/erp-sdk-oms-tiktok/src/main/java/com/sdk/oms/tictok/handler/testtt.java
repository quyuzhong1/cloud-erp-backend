package com.sdk.oms.tictok.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.HttpCommonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.tictok.constant.TikTokConstant;
import com.sdk.oms.tictok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tictok.dto.tiktok.listing.ListingDTO;
import com.sdk.oms.tictok.dto.tiktok.listing.view.ListingViewDTO;
import jodd.util.StringUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;


public class testtt {
    public static void main(String[] args) throws IOException {
        TikTokShopInfoDTO shopInfoDTO = new TikTokShopInfoDTO();
        shopInfoDTO.setClientId("6buinkjt3hmld");
        shopInfoDTO.setAccessToken("ROW_GzpNXQAAAACj-JAAAriAWjVtF2MrUIFdsyKbmgpFdy5ZKZU7_DcW_ahRpfbkhswwJKnrzfpPj19EPf6OyL_uhPFuQitb3TvgTVppCInACVvzpgcRGcI3K9Zp0hN8V0cL4vJXwjXCOrBuHT-kXLvYPhzjmDJYZA3KcjPmeDpH4EsgaB-YkuAstQ");
        shopInfoDTO.setClientSecret("8ff628de24faf70c24855de4d967fb6a17a47e3f");
        sendMercadoGetListing(shopInfoDTO);
    }

    /**
     * 发送请求获取指定店铺的sku信息
     * @param shopInfoDTO
     * @return
     */
    public static List<ListingViewDTO> sendMercadoGetListing(TikTokShopInfoDTO shopInfoDTO) {

        List<ListingViewDTO> resultsBeanList = new ArrayList<>();

        //每次最多获取100条
        Integer pageSize = 100;
        //平台接口地址
        String url = TikTokConstant.URL;
        //服务密钥
        String secret = shopInfoDTO.getClientSecret();

        String pageToken = "";

        StringBuffer sb = new StringBuffer();
        while(true) {
            //组装授权url
            String path = "/product/"+ TikTokConstant.VERSION+"/products/search";
            // 定义查询参数用于计算签名
            Map<String, Object> params = new HashMap<>();
            params.put("access_token", "ROW_GzpNXQAAAACj-JAAAriAWjVtF2MrUIFdsyKbmgpFdy5ZKZU7_DcW_ahRpfbkhswwJKnrzfpPj19EPf6OyL_uhPFuQitb3TvgTVppCInACVvzpgcRGcI3K9Zp0hN8V0cL4vJXwjXCOrBuHT-kXLvYPhzjmDJYZA3KcjPmeDpH4EsgaB-YkuAstQ");
            params.put("app_key", "6buinkjt3hmld");
            params.put("page_size", 1);
            params.put("shop_cipher", "ROW_lQ9cEwAAAADLHlVuUFi_v-jD4goRMhED");
            params.put("shop_id", "");
            params.put("sign", "");
            String timestamp = System.currentTimeMillis()/1000 +"";
            params.put("timestamp", timestamp);
            params.put("version", TikTokConstant.VERSION);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("content-type", "application/json");
            headerMap.put("x-tts-access-token", shopInfoDTO.getAccessToken());

            //请求body，平台用于计算签名
            Map<String, String> bodyMap = new HashMap<>();

            String input = urlParamsSort(params, path, headerMap, secret, JSONUtil.toJsonStr(bodyMap));
            // 追加请求路径获取签名
            String sign = generateSHA256(input, secret);
            //加入sign签名入参
            params.put("sign", sign);

            //组装授权url
            sb.append(url);
            sb.append(path);
            sb.append("?access_token="+params.get("access_token")+"");
            sb.append("&app_key="+params.get("app_key")+"");
            sb.append("&page_size="+params.get("page_size")+"");
            sb.append("&shop_cipher="+params.get("shop_cipher")+"");
            sb.append("&shop_id="+params.get("shop_id")+"");
            sb.append("&sign="+params.get("sign")+"");
            sb.append("&timestamp="+params.get("timestamp")+"");
            sb.append("&version="+params.get("version")+"");

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, TikTok查询sku数据失败，返回值 responseMap={}",
                        sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
            }


            //解析数据
            ObjectMapper objectMapper = new ObjectMapper();
            ListingDTO listingDTO = null;
            try {
                listingDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), ListingDTO.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            if (StringUtil.isBlank(listingDTO.getData().getNextPageToken())) {
                break;
            }
            pageToken = listingDTO.getData().getNextPageToken();

            //获取到所有客户的产品id
            List<String> productIds = listingDTO.getData().getProducts().stream().map(req -> req.getFid()).distinct().collect(Collectors.toList());

        }

        if (CollectionUtils.isEmpty(resultsBeanList)) {
            return Collections.emptyList();
        }

        return resultsBeanList;
    }

    /**
     * 第一步提取除sign和access_token之外的所有查询参数。按字母顺序对参数的键重新排序
     * @param queries
     * @return
     */
    private static String urlParamsSort(Map<String, Object> queries, String path, Map<String, String> headerMap, String secret, String bodyStr) {
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
            input.append(JSONUtil.toJsonStr(bodyStr));
        }
        String finalString = secret + input.toString() + secret;
        return finalString;
    }

    private static String generateSHA256(String input, String secret) {
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            hmacSha256.init(secretKey);
            byte[] hash = hmacSha256.doFinal(input.getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace(); // 处理异常
            return null;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
