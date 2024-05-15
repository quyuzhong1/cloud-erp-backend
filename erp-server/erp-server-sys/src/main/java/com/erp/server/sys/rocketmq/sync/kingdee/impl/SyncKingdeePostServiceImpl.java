package com.erp.server.sys.rocketmq.sync.kingdee.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.sys.entity.KingdeePostEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeePostService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lambda
 * @Classname SyncKingdeePostServiceImpl
 * @Description TODO
 * @Date 2024-03-13 15:50
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeePostServiceImpl implements SyncKingdeePostService {


    @Resource
    private DmpMqFeign dmpMqFeign;



    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public String syncDataToKingdee(KingdeePostEntity entity, String operate) {
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException("金蝶岗位表不存在");
        }
        Map<String, Object> resultMap = new HashMap<>();
        //业务id
        resultMap.put("id",entity.getId());
        //名称
        resultMap.put("name",entity.getName());
        //金蝶id
        resultMap.put("syncKingdeeId",entity.getKingdeeId());
        resultMap.put("operate", operate);
        String useOrgCode = entity.getUseOrgCode();
        resultMap.put("createOrgCode", useOrgCode);
        resultMap.put("useOrgCode",useOrgCode);
        resultMap.put("deptCode", entity.getKingdeeDeptCode());
        //生成任务
        return saveTask(entity,operate,resultMap);

    }

    /**
     * 生成任务
     * @description
     * @return
     * @date 2024-03-13 15:53
     * @author Lambda
     */
    private String saveTask(KingdeePostEntity entity, String operate, Map<String, Object> resultMap) {
        //添加推送任务
        DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
        dmpSyncTaskDTO.setSourceId(entity.getId());
        dmpSyncTaskDTO.setSourceCode(entity.getId());
        dmpSyncTaskDTO.setSourceType(SourceTypeEnum.SYS_POST.getCode());
        dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
        dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_SYS_POST_TAG.getName());
        dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
        dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
        dmpSyncTaskDTO.setSyncOperate(operate);
        return dmpMqFeign.saveTask(dmpSyncTaskDTO);
    }
}
