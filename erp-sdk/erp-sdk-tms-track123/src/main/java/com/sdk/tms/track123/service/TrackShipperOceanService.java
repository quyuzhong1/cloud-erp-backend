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
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String TRACK_123_API_SECRET = "Track123-Api-Secret";
    public static final String CHARSET_UTF_8 = "application/json;charset=utf-8";
    public static final String TIMESTAMP = "timestamp";
    static String url = "https://api.track123.com/gateway/open-api/tk/v2/track/query";
    static String token = "9fa500686633410a84ff0b00daed555e";

    /**
     * 获取快递物流商列表
     */
    public TrackResponse getCourierList(String token) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(CONTENT_TYPE, CHARSET_UTF_8);
        headers.put(TRACK_123_API_SECRET, token);
        headers.put(TIMESTAMP, String.valueOf(timestamp));

        String result = OkHttpUtils.doGet(PathConstants.BASE_URL + PathConstants.OCEAN_GET_COURIER_URL, new LinkedHashMap<>(), headers);
        return JSONUtil.toBean(result, TrackResponse.class);
    }

    public TrackOceanResponse getTrack(String token, List<LogisticsTrackBaseDTO.OceanTrackRequestDTO> oceanTrackRequestList) {
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(CONTENT_TYPE, CHARSET_UTF_8);
        headers.put(TRACK_123_API_SECRET, token);
        headers.put(TIMESTAMP, String.valueOf(timestamp));
        String result = OkHttpUtils.doPostJsonObject(PathConstants.BASE_URL + PathConstants.OCEAN_GET_TRACK_URL, oceanTrackRequestList, headers);
        return JSONUtil.toBean(result, TrackOceanResponse.class);
    }

    public OceanRegisterResult registerLogisticsNumber(String token, List<OceanRegisterRequest> registerRequests){
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(CONTENT_TYPE, CHARSET_UTF_8);
        headers.put(TRACK_123_API_SECRET, token);
        headers.put(TIMESTAMP, String.valueOf(timestamp));
        String result = OkHttpUtils.doPostJsonObject(PathConstants.BASE_URL + PathConstants.OCEAN_REGISTER_LOGISTICS_NUMBER, registerRequests, headers);

        return JSONUtil.toBean(result, OceanRegisterResult.class);
    }
}
