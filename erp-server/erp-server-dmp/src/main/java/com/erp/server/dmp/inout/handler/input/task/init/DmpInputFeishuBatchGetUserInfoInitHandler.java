package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.FindThirdUserDTO;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.lark.oapi.Client;
import com.lark.oapi.service.approval.v4.model.ListInstanceReq;
import com.lark.oapi.service.approval.v4.model.ListInstanceResp;
import com.lark.oapi.service.contact.v3.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputFeishuBatchGetUserInfoInitHandler extends DmpInputInitHandler {

    @Resource
    private FsService fsService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        log.error("调用飞书批量获取用户信息");
        //优先取扩展json中的实例id
        List<String> userIds = new ArrayList<>();
        String extendJson = dmpInputTaskEntity.getExtendJson();
        String eventId = "";
        String eventType = "";
        if (CharSequenceUtil.isNotBlank(extendJson)) {
            JSONObject jsonObject = JSON.parseObject(extendJson);
            String userId = jsonObject.getString("userId");
            eventId = jsonObject.getString("eventId");
            eventType = jsonObject.getString("eventType");
            if (CharSequenceUtil.isNotBlank(userId)) {
                userIds.add(userId);
            }
        }
        JSONArray result = new JSONArray();
        try {
            FindThirdUserDTO.UserParamsDTO dto = new FindThirdUserDTO.UserParamsDTO();
            dto.setUserIds( userIds.toArray(new String[0]));
            dto.setUserIdType("user_id");
            dto.setDepartmenetIdType("open_department_id");
            User[] users = fsService.batchGetFsUser(dto);
            if(Objects.nonNull(users) && users.length > 0){
                for (User user : users) {
                    JSONObject object = new JSONObject();
                    object.put("eventId", eventId);
                    object.put("eventType", eventType);
                    object.put("unionId", user.getUnionId());
                    object.put("userId", user.getUserId());
                    object.put("openId", user.getOpenId());
                    object.put("name", user.getName());
                    object.put("email", user.getEmail());
                    object.put("mobile", user.getMobile().replace("+86",""));
                    object.put("gender", user.getGender());
                    object.put("isResigned", user.getStatus().getIsResigned());
                    object.put("dataJson", JSON.toJSONString(user));
                    result.add(object);
                }
            }
        } catch (Exception e) {
            log.error("调用飞书失败,e= {}",e.getMessage());
            throw new ServiceException("调用飞书失败,msg= {}",e.getMessage());
        }
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(result.toJSONString()));
    }

    public static void main(String[] args) {
        // 转换为毫秒时间戳
        long startMillis = LocalDateTime.of(2025,9,16,11,10,10).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMillis = LocalDateTime.of(2025,9,16,12,10,10).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        // 构建client
        Client client = new Client().newBuilder("cli_a2c644b09af9500d", "VJJKhsIg05R8HgO2JJgbteYvwDb5325z").build();

        // 创建请求对象
        ListInstanceReq req = ListInstanceReq.newBuilder()
                .pageSize(100)
                .approvalCode("316A1122-D878-4453-9881-161C916DC1A2")
                //startTime转毫秒
                .startTime(String.valueOf(startMillis))
                .endTime(String.valueOf(endMillis))
                .build();
        List<String> allInstanceCodes = new ArrayList<>();

        try {
            ListInstanceResp resp = null;
            do {

                if (ObjectUtil.isNotEmpty(resp) && CharSequenceUtil.isNotBlank(resp.getData().getPageToken())) {
                    // 添加延迟，控制请求频率
                    Thread.sleep(200);
                }

                resp = client.approval().v4().instance().list(req);

                if (resp.success()) {
                    String[] instanceCodeList = resp.getData().getInstanceCodeList();
                    if (instanceCodeList != null && instanceCodeList.length > 0) {
                        allInstanceCodes.addAll(Arrays.asList(instanceCodeList));
                    }

                    // 设置下一页 token
                    String pageToken = resp.getData().getPageToken();
                    if (StrUtil.isNotBlank(pageToken)) {
                        req.setPageToken(pageToken);
                    }
                } else {
                    log.error("调用飞书API失败：code={}, msg={}, reqId={}", resp.getCode(), resp.getMsg(), resp.getRequestId());
                    throw new ServiceException("调用飞书API失败：" + resp.getMsg());
                }
            } while (StrUtil.isNotBlank(resp.getData().getPageToken()));
        } catch (Exception e) {
            log.error("获取审批实例ID异常", e);
            Thread.currentThread().interrupt();
            throw new ServiceException("获取审批实例ID异常", e);
        }
    }

}
