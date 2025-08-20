package com.sdk.tms.track123.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.sdk.tms.track123.constant.PathConstants;
import com.sdk.tms.track123.model.request.RegisterRequest;
import com.sdk.tms.track123.model.request.TrackRequest;
import com.sdk.tms.track123.model.response.RegisterResult;
import com.sdk.tms.track123.model.response.TrackResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zdy
 * @ClassName TrackShipperService
 * @description: 物流商对接服务
 * @date 2023年10月31日
 * @version: 1.0
 */
@Slf4j
@Component
public class TrackShipperService {
    static String url = "https://api.track123.com/gateway/open-api/tk/v2/track/query";
    static String token = "579cf53f55694d89aef0887d81886aec";

    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        TrackShipperService trackShipperService = new TrackShipperService();
//        trackShipperService.getCourierList(token);

//        String message = "Hello, World!";
//        String secretKey = "mySecretKey";
//
//        try {
//            byte[] hmacSha256Bytes = calculateHmacSHA256(message, secretKey);
//            String hmacSha256Hex = bytesToHex(hmacSha256Bytes);
//            System.out.println("HmacSHA256: " + hmacSha256Hex);
//        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        List<String> trackNos = new ArrayList<>();
        trackNos.add("WSHBR1064163742YQ");
        trackNos.add("WSHBR1064205439YQ");

        TrackRequest orderRequest = TrackRequest.builder()
                .trackNos(trackNos)
//                .createTimeStart("2021-08-01 00:00:00")
//                .createTimeEnd("2021-09-28 00:00:00")
                .cursor("")
                .queryPageSize(100)
                .build();
        TrackResponse track = trackShipperService.getTrack(token, orderRequest);
        System.out.println(JSONObject.toJSONString(track));
    }

    /**
     * 获取快递物流商列表
     */
    public TrackResponse getCourierList(String token) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json;charset=utf-8");
        headers.put("Track123-Api-Secret", token);
        headers.put("timestamp", String.valueOf(timestamp));

        String result = OkHttpUtils.doGet(PathConstants.BASE_URL + PathConstants.GET_COURIER_URL, new LinkedHashMap<>(), headers);
        return JSONUtil.toBean(result, TrackResponse.class);
    }

    public TrackResponse getTrack(String token, TrackRequest trackRequest) {
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json;charset=utf-8");
        headers.put("Track123-Api-Secret", token);
        headers.put("timestamp", String.valueOf(timestamp));
        String result = OkHttpUtils.doPostJsonObject(PathConstants.BASE_URL + PathConstants.GET_TRACK_URL, trackRequest, headers);
        TrackResponse response = null;
        if (StringUtils.isBlank(result)){
            return null;
        }
        try {
            response = JSONUtil.toBean(result, TrackResponse.class);
        }catch (Exception e){
            throw new ServiceException("解析返回数据异常："+ result);
        }
        return response;
    }

    public RegisterResult registerLogisticsNumber(String token, List<RegisterRequest> registerRequests){
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json;charset=utf-8");
        headers.put("Track123-Api-Secret", token);
        headers.put("timestamp", String.valueOf(timestamp));
        String result = OkHttpUtils.doPostJsonObject(PathConstants.BASE_URL + PathConstants.REGISTER_LOGISTICS_NUMBER, registerRequests, headers);
        System.out.println(result);
        try {
            return JSONUtil.toBean(result, RegisterResult.class);
        }catch (Exception e){
            throw new ServiceException("解析返回数据异常："+ result);
        }
    }
    public RegisterResult updateTrack(String token, List<RegisterRequest> registerRequests){
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json;charset=utf-8");
        headers.put("Track123-Api-Secret", token);
        headers.put("timestamp", String.valueOf(timestamp));
        String result = OkHttpUtils.doPostJsonObject(PathConstants.BASE_URL + PathConstants.UPDATE_LOGISTICS_NUMBER, registerRequests, headers);
        System.out.println(result);
        try {
            return JSONUtil.toBean(result, RegisterResult.class);
        }catch (Exception e){
            throw new ServiceException("解析返回数据异常："+ result);
        }
    }

    private static byte[] calculateHmacSHA256(String message, String secretKey)
            throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        mac.init(secretKeySpec);
        return mac.doFinal(message.getBytes("UTF-8"));
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
