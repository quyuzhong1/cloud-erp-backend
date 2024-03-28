package com.erp.server.sys.rocketmq.sync.kingdee.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeSysDeptService;
import com.erp.server.sys.service.KingdeeDepartmentService;
import com.erp.server.sys.service.SysDepartmentService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2023/4/4 12:25
 */
@Slf4j
@Service
public class SyncKingdeeSysDeptServiceImpl implements SyncKingdeeSysDeptService {

    @Resource
    private KingdeeDepartmentService kingdeeDepartmentService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void syncDataToKingdee(KingdeeDepartmentEntity entity, String operate) {

        if (ObjectUtils.isEmpty(entity)) {
            return;
        }

        Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id",entity.getId());

        //名称
        resultMap.put("name",entity.getKingdeeDeptName());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getKingdeeId());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        String useOrgCode = entity.getUseOrgCode();
        resultMap.put("createOrgCode", useOrgCode);
        resultMap.put("useOrgCode",useOrgCode);
        //上级负责部门编码
        KingdeeDepartmentEntity parent = kingdeeDepartmentService.getById(entity.getParentId());
        if (ObjectUtils.isNotEmpty(parent)) {
            resultMap.put("parentCode", parent.getKingdeeDeptCode());
        }
        //生成任务
        sendMqAndSaveTask(entity,operate,resultMap);
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private void sendMqAndSaveTask (KingdeeDepartmentEntity entity, String operate, Map<String, Object> resultMap) {
        //添加推送任务
        DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
        dmpSyncTaskDTO.setSourceId(entity.getId());
        dmpSyncTaskDTO.setSourceCode(entity.getId());
        dmpSyncTaskDTO.setSourceType(SourceTypeEnum.SYS_DEPARTMENT.getCode());
        dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
        dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_SYS_DEPARTMENT_TAG.getName());
        dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
        dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
        dmpSyncTaskDTO.setSyncOperate(operate);
        dmpMqFeign.sendMqAndSaveTask(dmpSyncTaskDTO);
    }
}
