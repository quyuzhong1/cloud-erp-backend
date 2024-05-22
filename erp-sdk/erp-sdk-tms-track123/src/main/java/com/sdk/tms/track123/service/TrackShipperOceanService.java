package com.sdk.tms.track123.service;

import cn.hutool.json.JSONUtil;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.tms.dto.LogisticsTrackBaseDTO;
import com.sdk.tms.track123.constant.PathConstants;
import com.sdk.tms.track123.model.request.OceanRegisterRequest;
import com.sdk.tms.track123.model.response.OceanRegisterResult;
import com.sdk.tms.track123.model.response.RegisterResult;
import com.sdk.tms.track123.model.response.TrackOceanResponse;
import com.sdk.tms.track123.model.response.TrackResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author zdy
 * @ClassName TrackShipperService
 * @description: 物流商对接服务
 * @date 2023年10月31日
 * @version: 1.0
 */
@Slf4j
@Component
public class TrackShipperOceanService {
    static String url = "https://api.track123.com/gateway/open-api/tk/v2/track/query";
    static String token = "9fa500686633410a84ff0b00daed555e";

    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        TrackShipperOceanService trackShipperService = new TrackShipperOceanService();
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
        trackNos.add("304071414818");
        trackNos.add("620372231752");

        LogisticsTrackBaseDTO.OceanTrackRequestDTO orderRequest = LogisticsTrackBaseDTO.OceanTrackRequestDTO.builder()
                .trackingNo("MATS5217756000")
                .type(3)
                .orderNo("matson")
                .build();
        TrackOceanResponse track = trackShipperService.getTrack(token, Arrays.asList(orderRequest));
        System.out.println(track);
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

        String result = OkHttpUtils.doGet(PathConstants.BASE_URL + PathConstants.OCEAN_GET_COURIER_URL, new LinkedHashMap<>(), headers);
        return JSONUtil.toBean(result, TrackResponse.class);
    }

    public TrackOceanResponse getTrack(String token, List<LogisticsTrackBaseDTO.OceanTrackRequestDTO> oceanTrackRequestList) {
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json;charset=utf-8");
        headers.put("Track123-Api-Secret", token);
        headers.put("timestamp", String.valueOf(timestamp));
        String result = OkHttpUtils.doPostJsonObject(PathConstants.BASE_URL + PathConstants.OCEAN_GET_TRACK_URL, oceanTrackRequestList, headers);
        return JSONUtil.toBean(result, TrackOceanResponse.class);
    }

    public OceanRegisterResult registerLogisticsNumber(String token, List<OceanRegisterRequest> registerRequests){
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json;charset=utf-8");
        headers.put("Track123-Api-Secret", token);
        headers.put("timestamp", String.valueOf(timestamp));
        String result = OkHttpUtils.doPostJsonObject(PathConstants.BASE_URL + PathConstants.OCEAN_REGISTER_LOGISTICS_NUMBER, registerRequests, headers);
        System.out.println("注册结果");
        System.out.println(result);
        return JSONUtil.toBean(result, OceanRegisterResult.class);
    }
}
