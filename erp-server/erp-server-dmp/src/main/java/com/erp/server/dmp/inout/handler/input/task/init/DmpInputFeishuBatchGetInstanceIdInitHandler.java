package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.entity.ThirdProcessDefinitionEntity;
import com.erp.model.workflow.enums.ThirdProcessDefinitionStatusEnum;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.lark.oapi.Client;
import com.lark.oapi.service.approval.v4.model.ListInstanceReq;
import com.lark.oapi.service.approval.v4.model.ListInstanceResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputFeishuBatchGetInstanceIdInitHandler extends DmpInputInitHandler {

    @Resource
    private FsService fsService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        LocalDateTime startTime = dmpInputTaskEntity.getStartTime();
        LocalDateTime endTime = dmpInputTaskEntity.getEndTime();

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
        //获取第三方审批定义
        List<ThirdProcessDefinitionEntity> cfgThirdProcessList = listCfgThirdProcess();

        List<String> approvalCodeList = cfgThirdProcessList.stream().map(ThirdProcessDefinitionEntity::getApprovalCode).distinct().collect(Collectors.toList());
        JSONArray result = new JSONArray();
        for (String approvalCode : approvalCodeList) {
            try {
                List<String> ids = fsService.batchGetInstanceId(approvalCode, startTime, endTime);
                for (String id : ids) {
                    JSONObject object = new JSONObject();
                    object.put("instance_id", id);
                    object.put("ulanzi_approval_code", approvalCode);
                    result.add(object);
                }
            } catch (Exception e) {
                log.error("调用飞书失败,e= {}",e.getMessage());
                continue;
            }
        }
        dmpInputTaskInitDTOList.add(DmpInputTaskInitDTO.initMsg(result.toJSONString()));
        return dmpInputTaskInitDTOList;
    }

    /**
     * 获取飞书审批定义
     * @author will
     * @date 2025/7/14 16:30
     * @return List<ThirdProcessDefinitionEntity>
     */
    private List<ThirdProcessDefinitionEntity> listCfgThirdProcess () {

        List<ThirdProcessDefinitionEntity> thirdProcessDefinitionList = FeignQuery.create(ThirdProcessDefinitionEntity.class)
                .eq(ThirdProcessDefinitionEntity::getStatus, ThirdProcessDefinitionStatusEnum.ACTIVE.getCode())
                .eq(ThirdProcessDefinitionEntity::getEnableStatus, Boolean.TRUE)
                .list();
        if (CollUtil.isEmpty(thirdProcessDefinitionList)) {
            log.warn("无可用的飞书审批定义，handler: {}", this.getClass().getSimpleName());
            return Collections.emptyList();
        }
        return thirdProcessDefinitionList;
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
