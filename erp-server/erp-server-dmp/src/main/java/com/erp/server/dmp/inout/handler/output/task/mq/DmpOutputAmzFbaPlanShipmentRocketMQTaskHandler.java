package com.erp.server.dmp.inout.handler.output.task.mq;

import com.common.business.dto.PlatformFbaShipmentDTO;
import com.erp.model.dmp.entity.DmpFbaShipmentDetailEntity;
import com.erp.model.dmp.entity.DmpFbaShipmentEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.service.DmpInputTaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * FBA InboundPlan 货件输出（沿用 FBA 货件 MQ 结构）
 */
@Service
@Scope("prototype")
public class DmpOutputAmzFbaPlanShipmentRocketMQTaskHandler extends DmpOutputAmzFbaShipmentRocketMQTaskHandler {

    @Resource
    private DmpInputTaskService dmpInputTaskService;

    @Override
    public PlatformFbaShipmentDTO convert(DmpFbaShipmentEntity dmpMainEntity,
                                          List<DmpFbaShipmentDetailEntity> dmpDetailEntityList,
                                          String cfgOutputId) {
        PlatformFbaShipmentDTO dto = super.convert(dmpMainEntity, dmpDetailEntityList, cfgOutputId);
        if (dto == null) {
            return null;
        }
        dto.setShopId(resolveRootTaskShopId(dmpMainEntity));
        return dto;
    }

    private String resolveRootTaskShopId(DmpFbaShipmentEntity dmpMainEntity) {
        if (dmpMainEntity == null || StringUtils.isBlank(dmpMainEntity.getInputTaskId())) {
            return StringUtils.defaultString(dmpMainEntity == null ? "" : dmpMainEntity.getNextLevelId());
        }
        DmpInputTaskEntity currentTask = dmpInputTaskService.getById(dmpMainEntity.getInputTaskId());
        int guard = 0;
        while (currentTask != null
                && StringUtils.isNotBlank(currentTask.getParentTaskId())
                && guard++ < 20) {
            DmpInputTaskEntity parentTask = dmpInputTaskService.getById(currentTask.getParentTaskId());
            if (parentTask == null) {
                break;
            }
            currentTask = parentTask;
        }
        if (currentTask == null) {
            return StringUtils.defaultString(dmpMainEntity.getNextLevelId());
        }
        return StringUtils.defaultString(currentTask.getNextLevelId());
    }
}
