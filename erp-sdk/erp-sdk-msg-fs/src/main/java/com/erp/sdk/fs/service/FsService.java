package com.erp.sdk.fs.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.constant.ThirdConstants;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.sys.dto.FindThirdUserDTO;
import com.erp.model.sys.vo.FsBatchSendMessageDTO;
import com.erp.model.workflow.enums.CfgApproveSyncViewerTypeEnum;
import com.erp.sdk.fs.config.FsProperties;
import com.erp.sdk.fs.dto.LarkResultDTO;
import com.erp.sdk.fs.enmu.DepartmentIdTypeEnum;
import com.erp.sdk.fs.enmu.UserIdTypeEnum;
import com.google.gson.JsonParser;
import com.lark.oapi.Client;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.approval.v4.model.*;
import com.lark.oapi.service.contact.v3.model.*;
import com.lark.oapi.service.contact.v3.model.User;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.erp.model.sys.vo.FsBatchSendMessageDTO.getTextMessageMap;

/**
 * @Classname FsService

 * @Date 2022-08-22 9:22
 * @Created by yl
 */
@Slf4j
@Component
public class FsService {

    private final FsProperties fsProperties;

    @Value("${third.fs.appUrl}")
    private String fsAppUrl;
    @Autowired
    public FsService(FsProperties fsProperties) {
        this.fsProperties = fsProperties;
    }
    private static final String FS_AUTHORIZATION = "Bearer ";

    private static final String TAG = "tag";
    private static final String TEXT = "text";
    private static final String TITLE = "title";
    private static final String CONTENT = "content";
    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT_TYPE = "Content-Type";

    /**
     * 根据code 获取飞书用户信息
     * https://open.feishu.cn/document/server-docs/authentication-management/login-state-management/get?appId=cli_a2c644b09af9500d
     * @param dto
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author yl
     * @date 2022-07-20 18:27
     */
    public Map<String, Object> getFsUser(FindThirdUserDTO dto) {

        String redirectUri = fsProperties.getRedirectLoginUri();
        String thirdType = dto.getThirdType();
        if (StringUtils.isNotBlank(thirdType) && ThirdConstants.THIRD_BINDING_TYPE.equals(thirdType)) {
            redirectUri = fsProperties.getRedirectBindingUri();
        }
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("grant_type", ThirdConstants.FS_GRANT_TYPE);
        paramsMap.put("code", dto.getCode());
        paramsMap.put("client_secret", fsProperties.getClientSecret());
        paramsMap.put("client_id", fsProperties.getClientId());
        paramsMap.put("redirect_uri", redirectUri);
        String bodyStr = OkHttpUtils.doPost(ThirdConstants.FS_TOKEN_URL, paramsMap, null);
        try {
            if (StringUtils.isNotBlank(bodyStr)) {
                Map<String, Object> tokenMap = JSON.parseObject(bodyStr, Map.class);
                if (tokenMap.containsKey("access_token")) {
                    String accessToken = tokenMap.get("access_token").toString();
                    String authorization = FS_AUTHORIZATION + accessToken;
                    Map<String, String> headerMap = new HashMap<>();
                    headerMap.put(AUTHORIZATION, authorization);
                    headerMap.put(CONTENT_TYPE, ThirdConstants.CONTENT_TYPE);
                    String userStr = OkHttpUtils.doGet(ThirdConstants.FS_USER_URL, null, headerMap);
                    Map<String, Object> userMap = JSON.parseObject(userStr, Map.class);
                    return userMap;
                }
            }
        }catch (Exception e){
            log.error("扫码获取飞书信息出错>>>>>{}",e);
            log.info("bodyStr >>>>>>{}",bodyStr);
        }

        return Collections.emptyMap();
    }

    /**
     * 根据code 获取飞书用户信息
     *
     * @param code
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author yl
     * @date 2022-07-20 18:27
     */
    public Map<String, Object> getFsUserByCode(String code) {
        String tenantAccessToken = getFsTenantAccessToken();
        if (StringUtils.isNotBlank(tenantAccessToken)) {
            return getUserAccessInfo(tenantAccessToken, code);
        }
        return Collections.emptyMap();
    }


    /**
     * 获取飞书自建应用的 tenant_access_token
     *
     * @param
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author yl
     * @date 2022-11-11 17:00
     */
    public String getFsTenantAccessToken() {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("app_id", fsProperties.getClientId());
        paramsMap.put("app_secret", fsProperties.getClientSecret());
        String bodyStr = OkHttpUtils.doPost(ThirdConstants.FS_TENANT_ACCESS_TOKEN, paramsMap, null);
        if (StringUtils.isNotBlank(bodyStr)) {
            Map<String, Object> tokenMap = JSON.parseObject(bodyStr, Map.class);
            if (tokenMap.containsKey("code") && Integer.valueOf(tokenMap.get("code").toString()) == 0) {
                return tokenMap.get("tenant_access_token").toString();
            }
        }
        return "";
    }

    /**
     * 获取用户的 飞书accessToken
     *
     * @param
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author yl
     * @date 2022-11-11 17:47
     */
    public Map<String, Object> getUserAccessInfo(String appAccessToken, String code) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("grant_type", ThirdConstants.FS_GRANT_TYPE);
        paramsMap.put("code", code);
        Map<String, String> headerMap = new HashMap<>();
        String authorization = FS_AUTHORIZATION + appAccessToken;
        headerMap.put(AUTHORIZATION, authorization);
        headerMap.put(CONTENT_TYPE, ThirdConstants.CONTENT_TYPE);
        String userStr = OkHttpUtils.doPostJson(ThirdConstants.FS_USER_ACCESS_TOKEN, paramsMap, headerMap);
        Map<String, Object> userMap = JSON.parseObject(userStr, Map.class);
        return userMap;

    }

    /**
     * 获取飞书的 client id
     *
     * @return
     */
    public String getFsClientId() {
        return fsProperties.getClientId();
    }

    /**
     * 批量发送消息
     *
     * @param
     * @return
     * @author yl
     * @date 2022-11-15 10:27
     */
    public Boolean sendMessage(FsBatchSendMessageDTO dto) {
        //获取飞书的应用token
        String tenantAccessToken = getFsTenantAccessToken();
        if (StringUtils.isNotBlank(tenantAccessToken)) {
            Map<String, String> headerMap = new HashMap<>();
            String authorization = FS_AUTHORIZATION + tenantAccessToken;
            headerMap.put(AUTHORIZATION, authorization);
            headerMap.put(CONTENT_TYPE, ThirdConstants.CONTENT_TYPE);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("msg_type", ThirdConstants.FS_MESSAGE_INTERACTIVE);
            //用户的unionIds
            bodyMap.put("union_ids", dto.getUnionIds());
            bodyMap.put("card", dto.getContentMap());
            String resultStr = OkHttpUtils.doPostJson(ThirdConstants.FS_BATCH_SEND_MESSAGE_URL, bodyMap, headerMap);
            Map<String, Object> resultMap = JSON.parseObject(resultStr, Map.class);
            if (resultMap != null && resultMap.containsKey("code")) {
                Integer code = (Integer) resultMap.get("code");
                int succeedCode = 0;
                if (succeedCode == code) {
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * 发送消息
     * @param unionId
     * @param titleContent
     * @param textContent
     * @param msgType
     * @return
     */
    public LarkResultDTO<T> sendMessage(String unionId, String titleContent, String textContent,String msgType) {
        //获取飞书的应用token
        String tenantAccessToken = getFsTenantAccessToken();
        if (StringUtils.isBlank(tenantAccessToken)) {
            log.error("批量发送飞书消息失败 token为空 ={}", JSONUtil.toJsonStr(tenantAccessToken));
            throw new ServiceException(ApiError.ERROR_LARK_TOKEN_IS_NULL);
        }
        Map<String, String> headerMap = new HashMap<>();
        String authorization = FS_AUTHORIZATION + tenantAccessToken;
        headerMap.put(AUTHORIZATION, authorization);
        headerMap.put(CONTENT_TYPE, ThirdConstants.CONTENT_TYPE);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("msg_type", msgType);
        //用户的unionIds
        bodyMap.put("receive_id", unionId);
        String content;
        switch (msgType){
            case ThirdConstants.FS_MESSAGE_INTERACTIVE:
                content = JSONUtil.toJsonStr(FsBatchSendMessageDTO.getCardMessageMap(titleContent, textContent, fsAppUrl));
                break;
            default:
                content = JSONUtil.toJsonStr(getTextMessageMap(textContent));
        }
        bodyMap.put(CONTENT, content);
        String resultStr = OkHttpUtils.doPostJson(ThirdConstants.LARK_SEND_MESSAGE_URL, bodyMap, headerMap);
        LarkResultDTO<T> resultMap = JSONUtil.toBean(resultStr, LarkResultDTO.class);

        if(null == resultMap || 0 != resultMap.getCode()){
            log.error("批量发送飞书消息失败 result ={}", JSONUtil.toJsonStr(resultMap));
            throw new ServiceException(ApiError.ERROR_LARK_SEND_MSG_FAIL);
        }
        return resultMap;
    }

    public void pressMessage(String messageId, List<String> unionIds) {
        if(CharSequenceUtil.isBlank(messageId) || CollUtil.isEmpty(unionIds)){
            throw new ServiceException(ApiError.ERROR_MSG_ID_OR_UNION_ID_IS_NULL);
        }
        //获取飞书的应用token
        String tenantAccessToken = getFsTenantAccessToken();
        if (StringUtils.isBlank(tenantAccessToken)) {
            log.error("批量加急飞书消息失败 token为空 ={}", JSONUtil.toJsonStr(tenantAccessToken));
            throw new ServiceException(ApiError.ERROR_LARK_TOKEN_IS_NULL);
        }
        String authorization = FS_AUTHORIZATION + tenantAccessToken;
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("user_id_list", unionIds);
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(paramMap));
        Request request = new Request.Builder()
                .url(CharSequenceUtil.format(ThirdConstants.LARK_PRESS_URL,messageId))
                .method("PATCH", body)
                .addHeader(CONTENT_TYPE, ThirdConstants.CONTENT_TYPE)
                .addHeader(AUTHORIZATION, authorization)
                .build();
        try {
            Response response = client.newCall(request).execute();
            LarkResultDTO<T> larkResultDTO = JSON.parseObject(response.body().string(), LarkResultDTO.class);
            if(0 != larkResultDTO.getCode()){
                log.error(CharSequenceUtil.format("发送应用内加急失败！param={}, messageId={},返回数据larkResultDTO={}",JSONUtil.toJsonStr(paramMap), messageId, JSONUtil.toJsonStr(larkResultDTO)));
                throw new IllegalArgumentException(CharSequenceUtil.format("发送应用内加急失败！param={}, messageId={},返回数据larkResultDTO={}",JSONUtil.toJsonStr(paramMap), messageId, JSONUtil.toJsonStr(larkResultDTO)));
            }
        } catch (IOException e) {
            log.error("调用发送应急消息方法失败url={}", CharSequenceUtil.format(ThirdConstants.LARK_PRESS_URL,messageId), e);
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * 获取飞书消息卡片信息
     *
     * @param
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author yl
     * @date 2022-11-18 12:31
     */
    public Map<String, Object> getCardMessageMap(String messageContent, String productContent, String url,boolean isPress) {
        Map<String, Object> cardMap = new LinkedHashMap<>();
        Map<String, Boolean> configMap = new HashMap<>();
        configMap.put("wide_screen_mode", true);
        cardMap.put("config", configMap);
        Map<String, Object> headerMap = new HashMap<>();
        Map<String, String> titleMap = new HashMap<>();
        titleMap.put("tag", "plain_text");
        titleMap.put(CONTENT, messageContent);
        headerMap.put(TITLE, titleMap);
        cardMap.put("header", headerMap);
        List<Map<String, Object>> elements = new ArrayList<>();
        Map<String, Object> fieldAllMap = new LinkedHashMap<>();
        fieldAllMap.put("tag", "div");
        List<Map<String, Object>> fieldMapList = new ArrayList<>();
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("is_short", true);
        Map<String, Object> textMap = new HashMap<>();
        textMap.put(TAG, "lark_md");
        textMap.put(CONTENT, productContent);
        fieldMap.put(TEXT, textMap);
        fieldMapList.add(fieldMap);
        fieldAllMap.put("fields", fieldMapList);
        elements.add(fieldAllMap);
        if(isPress){
            Map<String, Object> actionAllMap = new LinkedHashMap<>();
            actionAllMap.put( TAG, "action");
            actionAllMap.put("layout", "bisected");
            List<Map<String, Object>> actionList = new ArrayList<>();
            Map<String, Object> actionMap = new LinkedHashMap<>();
            actionMap.put( TAG, "button");
            actionMap.put("url", url);
            actionMap.put("type", "primary");
            Map<String, Object> actionTextMap = new HashMap<>();
            actionTextMap.put( TAG, "plain_text");
            actionTextMap.put(CONTENT, "查看详情");
            actionMap.put("text", actionTextMap);
            Map<String, Object> actionValueMap = new HashMap<>();
            actionValueMap.put("chosen", "approve");
            actionMap.put("value", actionValueMap);
            actionList.add(actionMap);
            actionAllMap.put("actions", actionList);
            elements.add(actionAllMap);
        }
        cardMap.put("elements", elements);
        return cardMap;
    }

    /**
     * 获取Client实例的方法
     * 该方法用于构建并返回一个配置了必要参数的Client实例，以便与飞书API进行交互
     * @return Client 实例，用于发送网络请求
     */
    public Client getClient() {
        // 构建client
        Client client = Client.newBuilder(fsProperties.getAppId(), fsProperties.getAppSecret())
                .requestTimeout(3, TimeUnit.SECONDS) // 设置httpclient 超时时间，默认永不超时
                .logReqAtDebug(true) // 在 debug 模式下会打印 http 请求和响应的 headers、body 等信息。.build();
                .build();
        //SDK 使用 client. 业务域.版本.资源 .方法名称 来定位具体的 API 方法
        return client;
    }


    /**
     * 批量通过手机号或邮箱获取用户
     * https://open.feishu.cn/document/server-docs/contact-v3/user/batch_get_id
     * @author jack
     * @date 2025-05-13
     */
    public BatchGetIdUserResp getBatchFsUserByMobileOrEmail(FindThirdUserDTO.UserParamsDTO dto) {
        try {
            // 构建client
            Client client =getClient();
//            飞书生产
            // 创建请求对象
            BatchGetIdUserReq req = BatchGetIdUserReq.newBuilder()
                    .userIdType(dto.getUserIdType())
                    .batchGetIdUserReqBody(BatchGetIdUserReqBody.newBuilder()
                            .mobiles(dto.getMobiles())
                            .includeResigned(Boolean.TRUE)
                            .build())
                    .build();
            // 发起请求
            BatchGetIdUserResp resp = client.contact().v3().user().batchGetId(req);
            // 处理服务端错误
            if (!resp.success()) {
                String msg = String.format("code:%s,msg:%s,reqId:%s",resp.getCode(), resp.getMsg(), resp.getRequestId());
                log.error("批量通过手机号或邮箱获取用户getBatchFsUserByMob;ileOrEmail失败>>>>>{}", resp.getMsg() );
                throw new ServiceException("获取子部门列表>>>>>{}",msg);
            }
            return resp;
        } catch (Exception e) {
            log.error("批量通过手机号或邮箱获取用户getBatchFsUserByMobileOrEmail出错>>>>>{}", e);
            throw new ServiceException("获取子部门列表>>>>>{}",e);
        }
    }

    /**
     * 批量获取用户信息
     * https://open.feishu.cn/document/contact-v3/user/batch
     * @author jack
     * @date 2025-05-13
     */
    public User[] getBatchFsUser(FindThirdUserDTO.UserParamsDTO dto) {
        // 构建client
        Client client = getClient();
        // 创建请求对象
        BatchUserReq req = BatchUserReq.newBuilder()
                .userIdType(dto.getUserIdType())
                .departmentIdType(dto.getDepartmenetIdType())
                .userIds(dto.getUserIds())
                .build();
        try {
            // 发起请求
            BatchUserResp resp = client.contact().v3().user().batch(req);
            // 处理服务端错误
            if (!resp.success()) {
                String msg = String.format("code:%s,msg:%s,reqId:%s",resp.getCode(), resp.getMsg(), resp.getRequestId());
                return null;
            }
            return resp.getData().getItems();
        } catch (Exception e) {
            log.error("批量获取用户信息getBatchFsUser出错>>>>>{}",e);
        }
        return null;
    }


    /**
     * 创建三方审批定义
     * https://open.feishu.cn/document/server-docs/approval-v4/external_approval/create
     * @author jack
     * @date 2025-05-14
     */
    public CreateExternalApprovalResp externalApprovalsCreate(CreateExternalApprovalReq req) {
        try {
            // 构建client
            Client client = getClient();

            // 发起请求
            CreateExternalApprovalResp resp = client.approval().v4().externalApproval().create(req);

            // 处理服务端错误
            if (!resp.success()) {
                String msg = String.format("code:%s,msg:%s,reqId:%s",resp.getCode(), resp.getMsg(), resp.getRequestId());
                throw new ServiceException("创建飞书三方审批定义失败>>>>>{}",msg);
            }
            return resp;
        } catch (Exception e) {
            throw new ServiceException("创建飞书三方审批定义出错>>>>>{}",e);
        }
    }

    /**
     * 获取子部门列表
     * https://open.feishu.cn/document/server-docs/contact-v3/department/children
     * @author jack
     * @date 2025-05-14
     */
    public ChildrenDepartmentResp getChildrenDepartments(ChildrenDepartmentReq req) {
        try {
            // 构建client
            Client client = getClient();
            // 创建请求对象
            if(Objects.isNull(req)){
                req = ChildrenDepartmentReq.newBuilder()
                        .departmentId("0")
                        .userIdType(UserIdTypeEnum.UNIONID.getCode())
                        .departmentIdType(DepartmentIdTypeEnum.OPENDEPARTMENTID.getCode())
                        .fetchChild(true)
                        .pageSize(50)
                        .build();
            }
            // 发起请求
            ChildrenDepartmentResp resp = client.contact().v3().department().children(req);

            // 处理服务端错误
            if (!resp.success()) {
                String msg = String.format("code:%s,msg:%s,reqId:%s, resp:%s",
                        resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8))));
                throw new ServiceException("获取子部门列表>>>>>{}",msg);
            }
            return resp;
        } catch (Exception e) {
            throw new ServiceException("获取子部门列表>>>>>{}",e);
        }
    }


    /**
     * 同步三方审批实例
     * https://open.feishu.cn/document/server-docs/approval-v4/external_instance/create
     * @author jack
     * @date 2025-05-16
     */
    public CreateExternalInstanceResp createExternalInstance(CreateExternalInstanceReq req) {
        try {
            // 构建client
            Client client = getClient();

            // 发起请求
            CreateExternalInstanceResp resp = client.approval().v4().externalInstance().create(req);
            // 处理服务端错误
            if (!resp.success()) {
                String msg = String.format("code:%s,msg:%s,reqId:%s", resp.getCode(), resp.getMsg(), resp.getRequestId());
                log.error("同步三方审批实例失败>>>>>{}",msg);
            }
            return resp;
        } catch (Exception e) {
            throw new ServiceException("同步三方审批实例出错>>>>>{}", e);
        }
    }

    /**
     * 校验三方审批实例
     * https://open.feishu.cn/document/server-docs/approval-v4/external_instance/check
     * @author jack
     * @date 2025-05-19
     */
    public CheckExternalInstanceResp checkExternalInstance(CheckExternalInstanceReq req) {
        try {
            // 构建client
            Client client = getClient();
            // 创建请求对象
            req = CheckExternalInstanceReq.newBuilder()
                    .checkExternalInstanceReqBody(CheckExternalInstanceReqBody.newBuilder()
                            .instances(new ExteranlInstanceCheck[]{
                                    ExteranlInstanceCheck.newBuilder()
                                            .instanceId("1234234234242423")
                                            .updateTime("1591603040000")
                                            .tasks(new ExternalInstanceTask[]{
                                                    ExternalInstanceTask.newBuilder()
                                                            .taskId("112253")
                                                            .updateTime("1591603040000")
                                                            .build()
                                            })
                                            .build()
                            })
                            .build())
                    .build();

            // 发起请求
            CheckExternalInstanceResp resp = client.approval().v4().externalInstance().check(req);

            // 处理服务端错误
            if (!resp.success()) {
                String msg = String.format("code:%s,msg:%s,reqId:%s", resp.getCode(), resp.getMsg(), resp.getRequestId());
                log.error("校验三方审批实例失败>>>>>{}",msg);
            }
            return resp;
        } catch (Exception e) {
            throw new ServiceException("校验三方审批实例出错>>>>>{}", e);
        }
    }



    /**
     * 发送审批 Bot 消息
     * https://open.feishu.cn/document/server-docs/approval-v4/message/send-bot-messages
     * @author jack
     * @date 2025-05-19
     */
    public Boolean sendApproveMessage() {
        //获取飞书的应用token
        String tenantAccessToken = getFsTenantAccessToken();
        if (StringUtils.isNotBlank(tenantAccessToken)) {
            Map<String, String> headerMap = new HashMap<>();
            String authorization = FS_AUTHORIZATION + tenantAccessToken;
            headerMap.put(AUTHORIZATION, authorization);
            headerMap.put(CONTENT_TYPE, ThirdConstants.CONTENT_TYPE);
            Map<String, Object> bodyMap = new HashMap<>();
//            bodyMap.put("template_id", 1008);
//            bodyMap.put("user_id", );
//            bodyMap.put("approval_name", "@i18n@approvalName");
//            bodyMap.put("title_user_id", );
//            bodyMap.put("title_user_id_type ", UserIdTypeEnum.USERID.getCode());
//
//            Map<String, Object> contentMap = new HashMap<>();
//            List<String> summaries = new ArrayList<>();
//            contentMap.put("user_id", );
//            contentMap.put("user_id_type", UserIdTypeEnum.USERID.getCode());
//            contentMap.put("summaries",summaries);
//            bodyMap.put("content ",contentMap );

            String resultStr = OkHttpUtils.doPostJson(ThirdConstants.FS_APPROVE_MESSAGE_SEND_URL, bodyMap, headerMap);
            Map<String, Object> resultMap = JSON.parseObject(resultStr, Map.class);
            if (resultMap != null && resultMap.containsKey("code")) {
                Integer code = (Integer) resultMap.get("code");
                int succeedCode = 0;
                if (succeedCode == code) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 查看指定三方审批定义
     * https://open.feishu.cn/document/approval-v4/external_approval/get
     * @author jack
     * @date 2025-05-20
     */
    public GetExternalApprovalResp getExternalApprovalResp(String approveCode) {
        try {
            // 构建client
            Client client = getClient();

            // 创建请求对象
            GetExternalApprovalReq req = GetExternalApprovalReq.newBuilder()
                    .approvalCode(approveCode)
                    .userIdType(UserIdTypeEnum.USERID.getCode())
                    .build();

            // 发起请求
            GetExternalApprovalResp resp = client.approval().v4().externalApproval().get(req);

            // 处理服务端错误
            if (!resp.success()) {
                String msg = String.format("code:%s,msg:%s,reqId:%s", resp.getCode(), resp.getMsg(), resp.getRequestId());
                log.error("查看指定三方审批定义失败>>>>>{}", msg);
                throw new ServiceException("查看指定三方审批定义失败>>>>>{}", msg);
            }
            return resp;
        } catch (Exception e) {
            throw new ServiceException("查看指定三方审批定义失败>>>>>{}", e);
        }
    }

    /**
     * 更新审批 Bot 消息
     * https://open.feishu.cn/document/server-docs/approval-v4/message/update-bot-messages
     * @author jack
     * @date 2025-05-19
     */
    public Boolean updateApproveMessage() {
        //获取飞书的应用token
        String tenantAccessToken = getFsTenantAccessToken();
        if (StringUtils.isNotBlank(tenantAccessToken)) {
            Map<String, String> headerMap = new HashMap<>();
            String authorization = FS_AUTHORIZATION + tenantAccessToken;
            headerMap.put(AUTHORIZATION, authorization);
            headerMap.put(CONTENT_TYPE, ThirdConstants.CONTENT_TYPE);

            Map<String, Object> bodyMap = new HashMap<>();
//            bodyMap.put("template_id", 1008);
//            bodyMap.put("user_id", );
//            bodyMap.put("approval_name", "@i18n@approvalName");
//            bodyMap.put("title_user_id", );
//            bodyMap.put("title_user_id_type ", UserIdTypeEnum.USERID.getCode());
//
//            Map<String, Object> contentMap = new HashMap<>();
//            List<String> summaries = new ArrayList<>();
//            contentMap.put("user_id", );
//            contentMap.put("user_id_type", UserIdTypeEnum.USERID.getCode());
//            contentMap.put("summaries",summaries);
//            bodyMap.put("content ",contentMap );

            String resultStr = OkHttpUtils.doPostJson(ThirdConstants.FS_APPROVE_MESSAGE_UPDATE_URL, bodyMap, headerMap);
            Map<String, Object> resultMap = JSON.parseObject(resultStr, Map.class);
            if (resultMap != null && resultMap.containsKey("code")) {
                Integer code = (Integer) resultMap.get("code");
                int succeedCode = 0;
                if (succeedCode == code) {
                    return true;
                }
            }
        }
        return false;
    }


    public static void main(String[] args) throws Exception {
        //测试
        Client client = Client.newBuilder("cli_a885904d16b5500e","U3pYsRdP7HTIpkolUjJol5cHtl12eoLu")
                .requestTimeout(3, TimeUnit.SECONDS) // 设置httpclient 超时时间，默认永不超时
                .logReqAtDebug(true) // 在 debug 模式下会打印 http 请求和响应的 headers、body 等信息。.build();
                .build();
        //生产
//        Client client = Client.newBuilder("cli_a2c644b09af9500d","VJJKhsIg05R8HgO2JJgbteYvwDb5325z")
//                .requestTimeout(3, TimeUnit.SECONDS) // 设置httpclient 超时时间，默认永不超时
//                .logReqAtDebug(true) // 在 debug 模式下会打印 http 请求和响应的 headers、body 等信息。.build();
//                .build();

    }

    /**
     * 获取指定飞书审批定义
     */
    public GetApprovalResp getApproval(String code) throws Exception {
        // 构建client
        Client client = Client.newBuilder("cli_a8858e6f51b95013", "dMU3PHMMoC172dOxFdn8agJeQvYpKYd3").build();

        // 创建请求对象
        GetApprovalReq req = GetApprovalReq.newBuilder()
                .approvalCode(code)
                .locale("zh-CN")
                .withAdminId(false)
                .userIdType("open_id")
                .build();

        // 发起请求
        GetApprovalResp resp = client.approval().v4().approval().get(req);

        // 处理服务端错误
        if (!resp.success()) {
            System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
                    resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JsonParser.parseString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
            throw new ServiceException(resp.getMsg());
        }

        // 业务数据处理
        System.out.println(Jsons.DEFAULT.toJson(resp.getData()));
        return resp;
    }

}
