package com.erp.server.msg.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.MessageChannelEnum;
import com.erp.model.msg.enums.NoticeMessageTypeEnum;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.sys.enums.ThirdPlatformEnums;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.msg.config.properties.FsProperties;
import com.erp.server.msg.constant.FeishuConstant;
import com.erp.server.msg.enums.ChannelSendMsgTypeEnum;
import com.erp.server.msg.enums.FeishuMessageTypeEnum;
import com.erp.server.msg.enums.MessageChannelAppEnum;
import com.erp.server.msg.model.*;
import com.erp.server.msg.service.BaseMessageSendService;
import com.erp.server.msg.utils.MsgConvertUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.erp.server.msg.enums.FeishuMessageTypeEnum.INTERACTIVE;

/**
 * @Classname: FeishuServiceImpl
 * @Description: 飞书发送消息业务类
 * @CreateTime: 2023-04-19  11:19
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class FeishuSendServiceImpl extends BaseMessageSendService {

    public static final String MSG_TYPE = "msg_type";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE = "Content-Type";
    @Resource
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
    public String getFsTenantAccessToken(String channelAppCode) {
        Map<String, Object> paramsMap = new HashMap<>();
        Map<String, FeishuConfigParam> configs = fsProperties.getConfigs();
        // 如果没有找到应用，需使用默认值
        if(StrUtils.isEmpty(channelAppCode)) {
            for (Map.Entry<String, FeishuConfigParam> configParamEntry : configs.entrySet()) {
               if(Objects.equals(configParamEntry.getValue().getIsDefault(),Boolean.TRUE)) {
                   channelAppCode = configParamEntry.getKey();
                   break;
               }
            }

        }
        FeishuConfigParam feishuConfigParam = configs.get(channelAppCode);
        if(Objects.isNull(feishuConfigParam)) {
            log.error("Nacos未配置飞书应用或者未配置默认应用或系统配置的应用代码错误，应用代码：{}，不发生消息",channelAppCode);
            return null;
        }
        paramsMap.put("app_id", feishuConfigParam.getClientId());
        paramsMap.put("app_secret", feishuConfigParam.getClientSecret());

        String bodyStr = OkHttpUtils.doPost(FeishuConstant.FS_TENANT_ACCESS_TOKEN, paramsMap, null);
        if (StringUtils.isNotBlank(bodyStr)) {
            Map<String, Object> tokenMap = JSON.parseObject(bodyStr, Map.class);
            if (tokenMap.containsKey("code") && Integer.valueOf(StrUtils.null2EmptyWithTrim(tokenMap.get("code"))) == 0) {
                return StrUtils.null2EmptyWithTrim(tokenMap.get("tenant_access_token"));
            }
        }
        return null;
    }

    /**
     * 飞书发送消息实现方法
     * @return
     */
    @Override
    public MsgResultVO sendMsg(MsgSendChannelWrapParam noticeMsgInfo) {
        NoticeMsgWrapInfoDTO noticeMsgWrapInfoDTO = noticeMsgInfo.getNoticeMsgWrapInfoDTO();
        if(CollUtil.isEmpty(noticeMsgWrapInfoDTO.getReceiverUserIds())) {
            log.error("飞书接收人为空，本次不发生消息");
            return null;
        }

        MsgResultVO<T> msgResult;
        List<String> receiverUserIds = noticeMsgWrapInfoDTO.getReceiverUserIds();
        noticeMsgWrapInfoDTO.setReceiverUserIds(receiverUserIds.stream().distinct().collect(Collectors.toList()));
        Boolean isBatch = noticeMsgWrapInfoDTO.getReceiverUserIds().size() >  1;
        if(!isBatch) { // 单条消息
            msgResult = sendSingleMsg(noticeMsgInfo);
        } else { // 批量消息
            msgResult = sendBatchMsg(noticeMsgInfo);
        }
        return msgResult;
    }

    @Override
    public void doSendWarnMsg(WarnMsgInfoDTO msgInfo) {
        this.sendWebhookMessage(msgInfo);
    }

    /**
     * 发送单条消息
     * @return
     */
    private MsgResultVO<T> sendSingleMsg(MsgSendChannelWrapParam noticeMsgInfo) {
        //获取飞书的应用token
        MessageChannelAppEnum messageChannelAppEnum = noticeMsgInfo.getChannelApp();
        String msgChannelAppCode = Objects.isNull(messageChannelAppEnum) ? "" : messageChannelAppEnum.getCode();
        String tenantAccessToken = getFsTenantAccessToken(msgChannelAppCode);
        MsgResultVO<T> msgResult = new MsgResultVO<T>();
        if(Objects.isNull(tenantAccessToken)) {
            msgResult.setCode(ApiError.ERROR_LARK_TOKEN_IS_NULL.code);
            msgResult.setMsg(ApiError.ERROR_LARK_TOKEN_IS_NULL.msg);
            msgResult.setNeedReSend(false);
            return msgResult;
        }
        if (StringUtils.isBlank(tenantAccessToken)) {
            log.error("发送飞书消息失败 token为空");
            msgResult.setCode(ApiError.ERROR_LARK_TOKEN_IS_NULL.code);
            msgResult.setMsg(ApiError.ERROR_LARK_TOKEN_IS_NULL.msg);
            msgResult.setNeedReSend(true);
            return msgResult;
        }
        NoticeMsgWrapInfoDTO noticeMsgWrapInfoDTO = noticeMsgInfo.getNoticeMsgWrapInfoDTO();
        String userId = noticeMsgWrapInfoDTO.getReceiverUserIds().get(0);
        List<ThirdUnionDTO> thirdUnionDTOs = sysUserFeign.getThirdUnionIdsByUserIds(ThirdPlatformEnums.FS.code, CollUtil.newArrayList(userId));
        if(CollUtil.isEmpty(thirdUnionDTOs)){
            return msgResult;
        }
        if(StrUtils.isEmpty(thirdUnionDTOs.get(0).getThirdUnionId())) {
            log.warn("用户【{}】未找到绑定的飞书信息",thirdUnionDTOs.get(0).getUserName());
            msgResult.setCode(ApiError.ERROR_LARK_TOKEN_IS_NULL.code);
            msgResult.setMsg(ApiError.ERROR_LARK_TOKEN_IS_NULL.msg);
            msgResult.setNeedReSend(false);
            return msgResult;
        }
        String unionId = thirdUnionDTOs.get(0).getThirdUnionId();

        // 消息类型，文本或卡片
        NoticeMessageTypeEnum msgTypeEnum = noticeMsgWrapInfoDTO.getNoticeMessageTypeEnum();
        // 平台在外面判断，否则不可能进来
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(AUTHORIZATION, FeishuConstant.FS_AUTHORIZATION + tenantAccessToken);
        headerMap.put(CONTENT_TYPE, FeishuConstant.CONTENT_TYPE);
        Map<String, Object> bodyMap = new HashMap<>();
        FeishuMessageTypeEnum feishuMessageTypeEnum = ChannelSendMsgTypeEnum.getByCode(msgTypeEnum.getCode()).getFeishuMsgType();
        bodyMap.put(MSG_TYPE, feishuMessageTypeEnum.getCode());
        //用户的unionIds
        bodyMap.put("receive_id", unionId);
        FeiShuSendBaseParam param = wrapParam(noticeMsgWrapInfoDTO, feishuMessageTypeEnum);
        bodyMap.put("content", JSONUtil.toJsonStr(param.getContent()));
        log.info("开始发送飞书消息，请求内容体参数=【{}】", JSONUtil.toJsonStr(bodyMap));
        msgResult.setRequestBody(JSONUtil.toJsonStr(bodyMap));
        String resultStr = OkHttpUtils.doPostJson(FeishuConstant.LARK_SEND_MESSAGE_URL, bodyMap, headerMap);
        log.info("结束发送飞书消息，请求内容体参数=【{}】，响应内容=【{}】", JSONUtil.toJsonStr(bodyMap), resultStr);
        LarkResultVO<T> result = JSON.parseObject(resultStr, LarkResultVO.class);

        if(null == result || 0 != result.getCode()){
            log.error("发送飞书消息失败，请求内容体参数=【{}】，响应内容=【{}】", JSONUtil.toJsonStr(bodyMap), JSONUtil.toJsonStr(result));
            msgResult.setCode(ApiError.ERROR_LARK_SEND_MSG_FAIL.code);
            msgResult.setMsg(ApiError.ERROR_LARK_SEND_MSG_FAIL.msg);
            msgResult.setNeedReSend(true);

            WarnMsgInfoDTO warnMsgInfoDTO = new WarnMsgInfoDTO();
            warnMsgInfoDTO.setTitle("消息通知发送失败");
            warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_MSG);
            warnMsgInfoDTO.setBizName("飞书发送消息通知");
            warnMsgInfoDTO.setTableName("");
            warnMsgInfoDTO.setTableId("");
            warnMsgInfoDTO.setKeyInfo(CharSequenceUtil.format("飞书响应结果【{}】", JSON.toJSONString(result)));
            warnMsgInfoDTO.setHappenTime(LocalDateTime.now());
            sendWebhookMessage(warnMsgInfoDTO);
            return msgResult;
        } else {
            msgResult.setCode(200);
            msgResult.setMsg("发送成功");
        }
        if(Objects.equals(noticeMsgWrapInfoDTO.getUrgent(),Boolean.TRUE)) {
            FeishuSingleMsgResultVO singleMsgResultVO = JSON.parseObject(StrUtils.null2EmptyWithTrim(result.getData()), FeishuSingleMsgResultVO.class);
            LarkResultVO<T> larkResultVO = pressMessage(singleMsgResultVO.getMessageId(), CollUtil.newArrayList(unionId),msgChannelAppCode);
            if(null == larkResultVO || 0 != larkResultVO.getCode()) {
                log.error("发送飞书加急消息失败，消息id:{}",singleMsgResultVO.getMessageId());
            }
        }
        return msgResult;
    }

    /**
     * 发送批量消息
     * @param noticeMsgInfo
     * @return
     */
    private MsgResultVO<T> sendBatchMsg(MsgSendChannelWrapParam noticeMsgInfo) {
        //获取飞书的应用token
        MessageChannelAppEnum messageChannelAppEnum = noticeMsgInfo.getChannelApp();
        String msgChannelAppCode = Objects.isNull(messageChannelAppEnum) ? "" : messageChannelAppEnum.getCode();
        String tenantAccessToken = getFsTenantAccessToken(msgChannelAppCode);
        MsgResultVO<T> msgResult = new MsgResultVO<T>();
        if(Objects.isNull(tenantAccessToken)) {
            msgResult.setCode(ApiError.ERROR_LARK_TOKEN_IS_NULL.code);
            msgResult.setMsg(ApiError.ERROR_LARK_TOKEN_IS_NULL.msg);
            msgResult.setNeedReSend(false);
            return msgResult;
        }
        if (StringUtils.isBlank(tenantAccessToken)) {
            log.error("发送飞书消息失败 token为空");
            msgResult.setCode(ApiError.ERROR_LARK_TOKEN_IS_NULL.code);
            msgResult.setMsg(ApiError.ERROR_LARK_TOKEN_IS_NULL.msg);
            msgResult.setNeedReSend(true);
            return msgResult;
        }
        NoticeMsgWrapInfoDTO noticeMsgWrapInfoDTO = noticeMsgInfo.getNoticeMsgWrapInfoDTO();
        List<ThirdUnionDTO> thirdUnionDTOs = sysUserFeign.getThirdUnionIdsByUserIds(ThirdPlatformEnums.FS.code, noticeMsgWrapInfoDTO.getReceiverUserIds());
        if(CollUtil.isEmpty(thirdUnionDTOs)) {
            log.warn("批量发送消息用户【{}】未绑定飞书信息", JSON.toJSONString(thirdUnionDTOs));
            msgResult.setCode(ApiError.ERROR_LARK_TOKEN_IS_NULL.code);
            msgResult.setMsg(ApiError.ERROR_LARK_TOKEN_IS_NULL.msg);
            msgResult.setNeedReSend(false);
            return msgResult;
        }
        List<String> unionIds = thirdUnionDTOs.stream().map(ThirdUnionDTO::getThirdUnionId).collect(Collectors.toList());
        // 消息类型，文本或卡片
        NoticeMessageTypeEnum msgTypeEnum = noticeMsgWrapInfoDTO.getNoticeMessageTypeEnum();

        Map<String, String> headerMap = new HashMap<>();
        String authorization = FeishuConstant.FS_AUTHORIZATION + tenantAccessToken;
        headerMap.put(AUTHORIZATION, authorization);
        headerMap.put(CONTENT_TYPE, FeishuConstant.CONTENT_TYPE);

        Map<String, Object> bodyMap = new HashMap<>();
        FeishuMessageTypeEnum feishuMessageTypeEnum = ChannelSendMsgTypeEnum.getByCode(msgTypeEnum.getCode()).getFeishuMsgType();
        bodyMap.put(MSG_TYPE, feishuMessageTypeEnum.getCode());
        //用户的unionIds
        bodyMap.put("union_ids", unionIds);
        FeiShuSendBaseParam param = wrapParam(noticeMsgWrapInfoDTO, feishuMessageTypeEnum);
        // 特别注意，此处的card不能序列化成json
        bodyMap.put("card", param.getContent());
        log.info("开始发送批量飞书消息，请求内容体参数=【{}】", JSONUtil.toJsonStr(bodyMap));
        msgResult.setRequestBody(JSONUtil.toJsonStr(bodyMap));
        String resultStr = OkHttpUtils.doPostJson(FeishuConstant.FS_BATCH_SEND_MESSAGE_URL, bodyMap, headerMap);
        log.info("结束批量发送飞书消息，请求内容体参数=【{}】，响应内容=【{}】", JSONUtil.toJsonStr(bodyMap), resultStr);
        LarkResultVO<T> result = JSON.parseObject(resultStr, LarkResultVO.class);
        if(null == result || 0 != result.getCode()){
            log.error("批量发送飞书消息失败，请求内容体参数=【{}】，响应内容=【{}】", JSONUtil.toJsonStr(bodyMap), JSONUtil.toJsonStr(result));
            msgResult.setCode(ApiError.ERROR_LARK_SEND_MSG_FAIL.code);
            msgResult.setMsg(ApiError.ERROR_LARK_SEND_MSG_FAIL.msg);
            msgResult.setNeedReSend(true);

            WarnMsgInfoDTO warnMsgInfoDTO = new WarnMsgInfoDTO();
            warnMsgInfoDTO.setTitle("消息通知发送失败");
            warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_MSG);
            warnMsgInfoDTO.setBizName("飞书发送批量消息通知");
            warnMsgInfoDTO.setTableName("");
            warnMsgInfoDTO.setTableId("");
            warnMsgInfoDTO.setKeyInfo(CharSequenceUtil.format("飞书响应结果【{}】", JSON.toJSONString(result)));
            warnMsgInfoDTO.setHappenTime(LocalDateTime.now());
            sendWebhookMessage(warnMsgInfoDTO);
            return msgResult;
        } else {
            msgResult.setCode(200);
            msgResult.setMsg("发送成功");
        }
        return msgResult;
    }

    /**
     * 发送加急消息
     * @param messageId 消息id
     * @param unionIds
     * @return
     */
    private LarkResultVO<T> pressMessage(String messageId, List<String> unionIds,String channelAppCode) {
        if(CharSequenceUtil.isBlank(messageId) || CollUtil.isEmpty(unionIds)){
            throw new ServiceException(ApiError.ERROR_MSG_ID_OR_UNION_ID_IS_NULL);
        }
        //获取飞书的应用token

        String tenantAccessToken = getFsTenantAccessToken(channelAppCode);
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
                .url(CharSequenceUtil.format(FeishuConstant.LARK_PRESS_URL,messageId))
                .method("PATCH", body)
                .addHeader(CONTENT_TYPE, FeishuConstant.CONTENT_TYPE)
                .addHeader(AUTHORIZATION, authorization)
                .build();
        LarkResultVO<T> result = null;
        try {
            Response response = client.newCall(request).execute();
            String resultStr = response.body().string();
            result = JSON.parseObject(resultStr, LarkResultVO.class);
            log.info("结束发送飞书加急消息，请求内容体参数=【{}】，响应内容=【{}】", JSONUtil.toJsonStr(paramMap), resultStr);
            if(null == result || 0 != result.getCode()){
                log.error(CharSequenceUtil.format("发送应用内加急失败！param={}, messageId={},返回数据larkResultDTO={}",JSONUtil.toJsonStr(paramMap), messageId, JSONUtil.toJsonStr(result)));
            }
        } catch (IOException e) {
            log.error("发送飞书加急消息失败，消息id=【{}】，请求内容体参数=【{}】，响应内容=【{}】", messageId, JSONUtil.toJsonStr(paramMap), JSONUtil.toJsonStr(result));
        }
        return result;
    }

    private FeiShuSendBaseParam wrapParam(NoticeMsgWrapInfoDTO noticeMsgInfo,FeishuMessageTypeEnum feishuMessageTypeEnum) {
        FeiShuSendBaseParam feiShuSendSingleParam = new FeiShuSendBaseParam();

        FeiShuSendBaseParam.ContentDTO contentDTO;
        if (feishuMessageTypeEnum.equals(INTERACTIVE)) {
            contentDTO = MsgConvertUtil.wrapTypicalCard(noticeMsgInfo);
        }else {
            contentDTO = new  FeiShuSendBaseParam.ContentDTO();
            contentDTO.setText(noticeMsgInfo.getContent());
        }
        feiShuSendSingleParam.setContent(contentDTO);
        return feiShuSendSingleParam;
    }

    /**
     * 发送系统异常信息至飞书群
     */
    public void sendWebhookMessage(WarnMsgInfoDTO warnMsgInfo) {
        log.info("接收到系统异常预警消息：【{}】",JSON.toJSONString(warnMsgInfo));
        Boolean warnSend = fsProperties.getWarnSend();
        if(Objects.isNull(warnSend) || !warnSend) {
            log.error("nacos配置飞书预警关闭，不发送预警通知");
            return;
        }
        try {
            WarnMsgTypeEnum warnMsgTypeEnum = warnMsgInfo.getWarnMsgTypeEnum();
            Map<String, String> warns = fsProperties.getWarns();
            if(!warns.containsKey(warnMsgTypeEnum.getCode())) {
                log.error("nacos未配置飞书预警配置【{}】，不发送预警通知", warnMsgTypeEnum.getName());
                return;
            }
            WarnMsgContentDTO warnMsgContentDTO = new WarnMsgContentDTO();
            // 由于采用关键字（系统预警）
            String activeProfile = SpringUtil.getActiveProfile();
            warnMsgContentDTO.setTitle(activeProfile + "-" + warnMsgTypeEnum.getName() + "：" + warnMsgInfo.getTitle());
            // 组装预警内容
            String msgContent = CharSequenceUtil.format("所属项目：{}\n业务名称：{}\n异常日志表名及表id：{} {}\n关键信息：{}\n发生时间：{}",
                    warnMsgInfo.getErpServerModuleEnum().getCode(), StrUtils.null2EmptyWithTrim(warnMsgInfo.getBizName()),
                    StrUtils.null2EmptyWithTrim(warnMsgInfo.getTableName()), StrUtils.null2EmptyWithTrim(warnMsgInfo.getTableId()),
                    StrUtils.null2EmptyWithTrim(warnMsgInfo.getKeyInfo()), LocalDateTimeUtil.format(warnMsgInfo.getHappenTime(), "yyyy-MM-dd HH:mm:ss"));
            warnMsgContentDTO.setContent(msgContent);

            String fsToken = warns.get(warnMsgTypeEnum.getCode());
            String requestUrl = CharSequenceUtil.format(FeishuConstant.FS_WARN_HOOK_URL, fsToken);
            FeiShuSendBaseParam.ContentDTO param = MsgConvertUtil.wrapTypicalCard(warnMsgContentDTO);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put(MSG_TYPE, INTERACTIVE.getCode());
            bodyMap.put("card", param);
            log.info("开始发送飞书预警消息，请求内容体参数=【{}】", JSONUtil.toJsonStr(bodyMap));
            String resultStr = OkHttpUtils.doPostJson(requestUrl, bodyMap, null);
            log.info("结束批量发送飞书预警消息，请求内容体参数=【{}】，响应内容=【{}】", JSONUtil.toJsonStr(bodyMap), resultStr);
        } catch (Exception e) {
            log.info("飞书发送预警信息异常", e);
        }
    }

}
