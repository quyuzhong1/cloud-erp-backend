package com.sdk.tms.kuaidi100.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.sdk.tms.kuaidi100.model.request.Kuaidi100QueryParam;
import com.sdk.tms.kuaidi100.model.response.Kuaidi100QueryResponse;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述：快递100实时查询服务类
 *
 * @author jack
 * @date 2026-03-31
 */
@Slf4j
@Service
public class Kuaidi100Service {

    private static final String QUERY_URL = "https://poll.kuaidi100.com/poll/query.do";

    /**
     * 实时查询快递轨迹
     *
     * @param customer 授权码
     * @param key      授权密钥
     * @param param    查询参数
     * @return 查询结果
     * @author jack
     * @date 2026-03-31
     */
    public Kuaidi100QueryResponse getTrack(String customer, String key, Kuaidi100QueryParam param) {
        try {
            String paramJson = JSONObject.toJSONString(param);
            // 签名规则：MD5(param + key + customer).toUpperCase()
            String sign = DigestUtil.md5Hex(paramJson + key + customer).toUpperCase();

            Map<String, Object> formParams = new HashMap<>();
            formParams.put("customer", customer);
            formParams.put("sign", sign);
            formParams.put("param", paramJson);

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/x-www-form-urlencoded");

            log.warn("快递100实时查询请求参数：{}", formParams);
            String response = OkHttpUtils.doPost(QUERY_URL, formParams, headers);
            log.warn("快递100实时查询响应结果：{}", response);

            return JSONObject.parseObject(response, Kuaidi100QueryResponse.class);
        } catch (Exception e) {
            log.error("快递100实时查询异常", e);
            return null;
        }
    }

    /**
     * 构建快递100请求参数
     */
    public Kuaidi100QueryParam buildKuaidi100QueryParam(String companyCode,String trackNo,Boolean isPushMobile,String mobile){
        // 构建查询参数
        Kuaidi100QueryParam param = Kuaidi100QueryParam.builder()
                .com(companyCode.toLowerCase()) // 快递100要求小写
                .num(trackNo)
                .resultv2("1")
                .build();
        if(isPushMobile && StringUtils.isNotBlank(mobile)){
            //判断mobile如果小于4位数字则报错
            if(mobile.length()>=4){
                //只取后四位
                param.setPhone(StrUtil.subSuf(mobile,mobile.length()-4));
            }else {
                param.setPhone(mobile);
            }
        }
        return param;
    }

    /**
     * 快递100 状态码转换
     * 0:在途, 1:揽收, 2:疑难, 3:签收, 4:退签, 5:派件, 6:退回, 10:待清关, 11:清关中, 12:已清关, 13:清关异常, 14:收件人拒签
     */
    public String convertTrackStatus(String state) {
        if (StringUtils.isBlank(state)) {
            return LogisticTrackStatusEnum.NOT_FIND.getCode();
        }
        switch (state) {
            case "0":
            case "10":
            case "11":
            case "12":
                return LogisticTrackStatusEnum.TRACK_ING.getCode();
            case "1":
                return LogisticTrackStatusEnum.WAIT_COLLECT.getCode();
            case "2":
            case "13":
                return LogisticTrackStatusEnum.MAYBE_EXCEPTION.getCode();
            case "3":
                return LogisticTrackStatusEnum.SIGN.getCode();
            case "4":
            case "6":
                return LogisticTrackStatusEnum.RETURNED.getCode();
            case "5":
                return LogisticTrackStatusEnum.DELIVERY_ING.getCode();
            case "14":
                return LogisticTrackStatusEnum.DELIVERY_FAIL.getCode();
            default:
                return LogisticTrackStatusEnum.NOT_FIND.getCode();
        }
    }
}
