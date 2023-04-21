package com.erp.server.msg.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.msg.dto.NoticeMsgInfoDTO;
import com.erp.model.msg.enums.MessageChannelEnum;
import com.erp.model.msg.enums.NoticeMessageTypeEnum;
import com.erp.model.msg.enums.NoticeTypeEnum;
import com.erp.model.sys.enums.ThirdPlatformEnums;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.msg.config.FsProperties;
import com.erp.server.msg.constant.FeishuConstant;
import com.erp.server.msg.enums.ChannelSendMsgTypeEnum;
import com.erp.server.msg.enums.FeishuMessageTypeEnum;
import com.erp.server.msg.model.FeiShuSendBaseParam;
import com.erp.server.msg.model.FeishuSingleMsgResultVO;
import com.erp.server.msg.model.LarkResultVO;
import com.erp.server.msg.service.BaseMessageSendService;
import com.erp.server.msg.utils.FeishuUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname: FeishuServiceImpl
 * @Description: 飞书发送消息业务类
 * @CreateTime: 2023-04-19  11:19
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class FeishuSendServiceImpl extends BaseMessageSendService {

    @Autowired
    private FsProperties fsProperties;

    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public MessageChannelEnum channel() {
        return MessageChannelEnum.FEISHU;
    }

    /**
     * 获取飞书tenantAccessToken
     * @return
     */
    public String getFsTenantAccessToken() {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("app_id", fsProperties.getClientId());
        paramsMap.put("app_secret", fsProperties.getClientSecret());
        String bodyStr = OkHttpUtils.doPost(FeishuConstant.FS_TENANT_ACCESS_TOKEN, paramsMap, null);
        if (StringUtils.isNotBlank(bodyStr)) {
            Map<String, Object> tokenMap = JSONObject.parseObject(bodyStr, Map.class);
            if (tokenMap.containsKey("code") && Integer.valueOf(StrUtils.null2EmptyWithTrim(tokenMap.get("code"))) == 0) {
                return tokenMap.get("tenant_access_token").toString();
            }
        }
        return "";
    }

    /**
     * 飞书发送消息实现方法
     * @return
     */
    @Override
    public ApiResult sendMsg(NoticeMsgInfoDTO noticeMsgInfo) {
        if(CollUtil.isEmpty(noticeMsgInfo.getReceiverUserIds())) {
            log.error("飞书接收人为空，本次不发生消息");
            return null;
        }
        ApiResult apiResult = new ApiResult();
        List<String> receiverUserIds = noticeMsgInfo.getReceiverUserIds();
        noticeMsgInfo.setReceiverUserIds(receiverUserIds.stream().distinct().collect(Collectors.toList()));
        Boolean isBatch = noticeMsgInfo.getReceiverUserIds().size() >  1;
        LarkResultVO larkResultVO;
        if(!isBatch) { // 单条消息
            larkResultVO = sendSingleMsg(noticeMsgInfo);
        } else { // 批量消息
            larkResultVO = sendBatchMsg(noticeMsgInfo);
        }
        if(Objects.nonNull(larkResultVO)) {
            apiResult.setCode(larkResultVO.getCode() == 0 ? 200 : ApiError.ERROR_LARK_SEND_MSG_FAIL.code);
            apiResult.setMsg(larkResultVO.getMsg());
        }
        return apiResult;
    }

    /**
     * 发送单条消息
     * @return
     */
    private LarkResultVO sendSingleMsg(NoticeMsgInfoDTO noticeMsgInfo) {
        //获取飞书的应用token
        String tenantAccessToken = getFsTenantAccessToken();
        if (StringUtils.isBlank(tenantAccessToken)) {
            log.error("发送飞书消息失败 token为空");
            throw new ServiceException(ApiError.ERROR_LARK_TOKEN_IS_NULL);
        }
        String userId = noticeMsgInfo.getReceiverUserIds().get(0);
        ThirdUnionDTO thirdUnionDTO = sysUserFeign.getThirdUnionIdByUserId(ThirdPlatformEnums.FS.code, userId);
        String unionId = thirdUnionDTO.getThirdUnionId();
        if(Objects.isNull(thirdUnionDTO) || StrUtils.isEmpty(unionId)) {
            log.warn("用户【{}】未找到绑定的飞书信息");
        }
        NoticeTypeEnum noticeTypeEnum = noticeMsgInfo.getNoticeTypeEnum();
        // 消息类型，文本或卡片
        NoticeMessageTypeEnum msgTypeEnum = noticeTypeEnum.getNoticeMessageType();
        // 平台在外面判断，否则不可能进来
        Map<String, String> headerMap = new HashMap<String, String>(){{
            put("Authorization", FeishuConstant.FS_AUTHORIZATION + tenantAccessToken);
            put("Content-Type", FeishuConstant.CONTENT_TYPE);
        }};
        Map<String, Object> bodyMap = new HashMap<>();
        FeishuMessageTypeEnum feishuMessageTypeEnum = ChannelSendMsgTypeEnum.of(msgTypeEnum.getCode()).getFeishuMsgType();
        bodyMap.put("msg_type", feishuMessageTypeEnum.getCode());
        //用户的unionIds
        bodyMap.put("receive_id", unionId);
        FeiShuSendBaseParam param = wrapSingleParam(noticeMsgInfo, feishuMessageTypeEnum);
        bodyMap.put("content", JSONUtil.toJsonStr(param.getContent()));
        log.info("开始发送飞书消息，请求内容体参数=【{}】", JSONUtil.toJsonStr(bodyMap));
        String resultStr = OkHttpUtils.doPostJson(FeishuConstant.LARK_SEND_MESSAGE_URL, bodyMap, headerMap);
        log.info("结束发送飞书消息，请求内容体参数=【{}】，响应内容=【{}】", JSONUtil.toJsonStr(bodyMap), resultStr);
        LarkResultVO result = JSONObject.parseObject(resultStr, LarkResultVO.class);

        if(null == result || 0 != result.getCode()){
            log.error("发送飞书消息失败，请求内容体参数=【{}】，响应内容=【{}】", JSONUtil.toJsonStr(bodyMap), JSONUtil.toJsonStr(result));
            throw new ServiceException(ApiError.ERROR_LARK_SEND_MSG_FAIL);
        }
        if(Objects.equals(noticeMsgInfo.getUrgent(),Boolean.TRUE)) {
            FeishuSingleMsgResultVO singleMsgResultVO = JSONObject.parseObject(StrUtils.null2EmptyWithTrim(result.getData()), FeishuSingleMsgResultVO.class);
            LarkResultVO larkResultVO = pressMessage(singleMsgResultVO.getMessage_id(), CollUtil.newArrayList(unionId));
            if(null == larkResultVO || 0 != larkResultVO.getCode()) {
                log.error("发送飞书加急消息失败，消息id:{}",singleMsgResultVO.getMessage_id());
            }
        }
        return result;
    }

    /**
     * 发送批量消息
     * @param noticeMsgInfo
     * @return
     */
    private LarkResultVO sendBatchMsg(NoticeMsgInfoDTO noticeMsgInfo) {
        LarkResultVO result = null;
        return result;
    }

    private FeiShuSendBaseParam wrapSingleParam(NoticeMsgInfoDTO noticeMsgInfo,FeishuMessageTypeEnum feishuMessageTypeEnum) {
        FeiShuSendBaseParam feiShuSendSingleParam = new FeiShuSendBaseParam();

        FeiShuSendBaseParam.ContentDTO contentDTO;
        switch (feishuMessageTypeEnum) {
            case INTERACTIVE: // 卡片
                contentDTO = FeishuUtil.wrapTypicalCard(noticeMsgInfo);
                break;
            default: // 默认文本
                contentDTO = new  FeiShuSendBaseParam.ContentDTO();
                contentDTO.setText(noticeMsgInfo.getContent());
        }
        feiShuSendSingleParam.setContent(contentDTO);
        return feiShuSendSingleParam;
    }

    /**
     * 发送加急消息
     * @param messageId 消息id
     * @param unionIds
     * @return
     */
    private LarkResultVO pressMessage(String messageId, List<String> unionIds) {
        if(StrUtil.isBlank(messageId) || CollectionUtil.isEmpty(unionIds)){
            throw new ServiceException(ApiError.ERROR_MSG_ID_OR_UNION_ID_IS_NULL);
        }
        //获取飞书的应用token
        String tenantAccessToken = getFsTenantAccessToken();
        if (StringUtils.isBlank(tenantAccessToken)) {
            log.error("发送飞书加急消息失败 token");
            throw new ServiceException(ApiError.ERROR_LARK_TOKEN_IS_NULL);
        }
        String authorization = FeishuConstant.FS_AUTHORIZATION + tenantAccessToken;
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("user_id_list", unionIds);
        log.info("开始发送飞书加急消息，请求内容体参数=【{}】", JSONUtil.toJsonStr(paramMap));
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(paramMap));
        Request request = new Request.Builder()
                .url(StrUtil.format(FeishuConstant.LARK_PRESS_URL,messageId))
                .method("PATCH", body)
                .addHeader("Content-Type", FeishuConstant.CONTENT_TYPE)
                .addHeader("Authorization", authorization)
                .build();
        LarkResultVO result = null;
        try {
            Response response = client.newCall(request).execute();
            String resultStr = response.body().string();
            result = JSONObject.parseObject(resultStr, LarkResultVO.class);
            log.info("结束发送飞书加急消息，请求内容体参数=【{}】，响应内容=【{}】", JSONUtil.toJsonStr(paramMap), resultStr);
            if(null == result || 0 != result.getCode()){
                log.error(StrUtil.format("发送应用内加急失败！param={}, messageId={},返回数据larkResultDTO={}",JSONUtil.toJsonStr(paramMap), messageId, JSONUtil.toJsonStr(result)));
                throw new RuntimeException(StrUtil.format("发送应用内加急失败！param={}, messageId={},返回数据larkResultDTO={}",JSONUtil.toJsonStr(paramMap), messageId, JSONUtil.toJsonStr(result)));
            }
        } catch (IOException e) {
            log.error("发送飞书加急消息失败，消息id=【{}】，请求内容体参数=【{}】，响应内容=【{}】", messageId, JSONUtil.toJsonStr(paramMap), JSONUtil.toJsonStr(result));
            throw new ServiceException(ApiError.ERROR_LARK_SEND_MSG_FAIL);
        }
        return result;
    }

}
