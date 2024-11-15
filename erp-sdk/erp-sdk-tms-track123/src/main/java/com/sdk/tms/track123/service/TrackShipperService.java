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
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String TRACK_123_API_SECRET = "Track123-Api-Secret";
    public static final String CHARSET_UTF_8 = "application/json;charset=utf-8";
    public static final String TIMESTAMP = "timestamp";
    static String url = "https://api.track123.com/gateway/open-api/tk/v2/track/query";
    static String token = "579cf53f55694d89aef0887d81886aec";

    /**
     * 获取快递物流商列表
     */
    public TrackResponse getCourierList(String token) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(CONTENT_TYPE, CHARSET_UTF_8);
        headers.put(TRACK_123_API_SECRET, token);
        headers.put(TIMESTAMP, String.valueOf(timestamp));

        String result = OkHttpUtils.doGet(PathConstants.BASE_URL + PathConstants.GET_COURIER_URL, new LinkedHashMap<>(), headers);
        return JSONUtil.toBean(result, TrackResponse.class);
    }

    public TrackResponse getTrack(String token, TrackRequest trackRequest) {
        long timestamp = System.currentTimeMillis();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(CONTENT_TYPE, CHARSET_UTF_8);
        headers.put(TRACK_123_API_SECRET, token);
        headers.put(TIMESTAMP, String.valueOf(timestamp));
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
        headers.put(CONTENT_TYPE, CHARSET_UTF_8);
        headers.put(TRACK_123_API_SECRET, token);
        headers.put(TIMESTAMP, String.valueOf(timestamp));
        String result = OkHttpUtils.doPostJsonObject(PathConstants.BASE_URL + PathConstants.REGISTER_LOGISTICS_NUMBER, registerRequests, headers);
        return JSONUtil.toBean(result, RegisterResult.class);
    }

}
