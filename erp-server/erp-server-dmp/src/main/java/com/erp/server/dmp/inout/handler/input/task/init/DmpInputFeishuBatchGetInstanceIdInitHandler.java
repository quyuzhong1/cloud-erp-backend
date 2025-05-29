package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.entity.ThirdProcessDefinitionEntity;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.lark.oapi.service.approval.v4.model.GetApprovalResp;
import com.lark.oapi.service.approval.v4.model.GetApprovalRespBody;
import com.lark.oapi.service.approval.v4.model.ListInstanceResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        List<ThirdProcessDefinitionEntity> thirdProcessDefinitionEntityList = FeignQuery.create(ThirdProcessDefinitionEntity.class)
                .isNotNull(ThirdProcessDefinitionEntity::getApprovalCode)
                .ne(ThirdProcessDefinitionEntity::getApprovalCode, "")
                .list();
        JSONArray result = new JSONArray();
        if (CollUtil.isNotEmpty(thirdProcessDefinitionEntityList)) {
            for (ThirdProcessDefinitionEntity thirdProcessDefinitionEntity : thirdProcessDefinitionEntityList) {
                String approvalCode = thirdProcessDefinitionEntity.getApprovalCode();
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
        }
        dmpInputTaskInitDTOList.add(DmpInputTaskInitDTO.initMsg(result.toJSONString()));
        return dmpInputTaskInitDTOList;
    }

}
