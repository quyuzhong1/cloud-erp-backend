package com.erp.server.dmp.inout.handler.output.create;

import java.util.Collections;
import java.util.List;

import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.enums.DmpCfgInputExecSystemEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputHotfixCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erp.model.dmp.entity.DmpOutputTaskEntity;
import com.erp.model.dmp.enums.DmpOutputTaskStatusEnum;
import com.erp.model.dmp.enums.DmpOutputTaskTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputCreateResponse;
import com.erp.server.dmp.service.DmpOutputTaskService;

import lombok.extern.slf4j.Slf4j;

/**
 * dmp输出任务创建处理器
 *
 * @author Administrator
 *
 */
@Slf4j
@Service
public class DmpOutputHotfixCreateHandler extends DmpOutputBaseCreateHandler {

    @Autowired
    private DmpOutputTaskService dmpOutputTaskService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<DmpOutputTaskEntity> createOutputTask(DmpOutputCreateRequest dmpRequest,
                                                      DmpOutputCreateResponse dmpResponse) {
        DmpCfgOutputEntity dmpCfgOutputEntity = dmpResponse.getDmpCfgOutputEntity();
        if (DmpCfgInputExecSystemEnum.REST_CLOUD.getCode().equals(dmpCfgOutputEntity.getExecSystem())) {
            // RestCloud创建任务
            DmpOutputHotfixCreateRequest hotfixDmpRequest = (DmpOutputHotfixCreateRequest) dmpRequest;
            DmpOutputTaskEntity dmpOutputTaskEntity = new DmpOutputTaskEntity();
            dmpOutputTaskEntity.setCfgOutputId(hotfixDmpRequest.getCfgOutputId());
            dmpOutputTaskEntity.setStatus(DmpOutputTaskStatusEnum.INIT.getCode());
            dmpOutputTaskEntity.setTaskType(hotfixDmpRequest.getTaskType());
            dmpOutputTaskEntity.setStartTime(hotfixDmpRequest.getStartTime());
            dmpOutputTaskEntity.setEndTime(hotfixDmpRequest.getEndTime());
            dmpOutputTaskEntity.setExecTimeout(hotfixDmpRequest.getExecTimeout());
            dmpOutputTaskEntity.setExecSystem(dmpCfgOutputEntity.getExecSystem());

            dmpOutputTaskService.save(dmpOutputTaskEntity);
            return Collections.singletonList(dmpOutputTaskEntity);
        } else {
            // 查询中台数据重推
            DmpOutputTaskEntity dmpOutputTaskEntity = new DmpOutputTaskEntity();
            dmpOutputTaskEntity.setCfgOutputId(dmpRequest.getCfgOutputId());
            dmpOutputTaskEntity.setStatus(DmpOutputTaskStatusEnum.INIT.getCode());
            dmpOutputTaskEntity.setTaskType(DmpOutputTaskTypeEnum.HOTFIX.getCode());

            dmpOutputTaskService.save(dmpOutputTaskEntity);
            return Collections.singletonList(dmpOutputTaskEntity);
        }
    }
}
