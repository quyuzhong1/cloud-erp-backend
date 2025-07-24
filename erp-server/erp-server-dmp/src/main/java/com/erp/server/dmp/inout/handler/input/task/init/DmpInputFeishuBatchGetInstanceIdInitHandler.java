package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
                throw new ServiceException("调用飞书失败");
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
}
