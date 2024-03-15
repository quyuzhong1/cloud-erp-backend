package com.erp.server.sys.rocketmq.sync.kingdee.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.sys.entity.KingdeeBusinessOperatorEntity;
import com.erp.model.sys.entity.KingdeeOperatorRefPostEntity;
import com.erp.model.sys.entity.KingdeeUserRefPostEntity;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeOperatorService;
import com.erp.server.sys.service.KingdeeOperatorRefPostService;
import com.erp.server.sys.service.KingdeeUserRefPostService;
import com.erp.server.sys.service.SysAccountingCompanyService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.EAN;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Lambda
 * @Classname SyncKingdeeOperatorServiceImpl
 * @Description TODO
 * @Date 2024-03-15 14:39
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeeOperatorServiceImpl implements SyncKingdeeOperatorService {


    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private KingdeeUserRefPostService kingdeeUserRefPostService;

    @Resource
    private SysAccountingCompanyService sysAccountingCompanyService;

    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public void syncDataToKingdee(KingdeeOperatorRefPostEntity entity, String operate) {
        if (ObjectUtils.isEmpty(entity)) {
            return;
        }
        Map<String, Object> resultMap = new HashMap<>();
        //业务id
        resultMap.put("id", entity.getId());
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getKingdeeId());
        resultMap.put("operate", operate);
        //业务类型
        resultMap.put("typeCode", entity.getTypeCode());

        List<Map<String, Object>> list = new ArrayList<>(1);
        Map<String, Object> itemMap = new HashMap<>();

        SysAccountingCompanyEntity org = sysAccountingCompanyService.getById(entity.getUseOrgId());
        if (Objects.nonNull(org)) {
            //业务组织
            itemMap.put("useOrgCode", org.getCode());
        }

        KingdeeUserRefPostEntity userPost = kingdeeUserRefPostService.getById(entity.getUserPostId());
        if(Objects.nonNull(userPost)){
            itemMap.put("userPostCode", userPost.getCode());
        }
        list.add(itemMap);
        resultMap.put("list", list);
        //生成任务
        sendMqAndSaveTask(entity, operate, resultMap);
    }

    private void sendMqAndSaveTask(KingdeeOperatorRefPostEntity entity, String operate, Map<String, Object> resultMap) {
        //添加推送任务
        DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
        dmpSyncTaskDTO.setSourceId(entity.getId());
        dmpSyncTaskDTO.setSourceCode(entity.getId());
        dmpSyncTaskDTO.setSourceType(SourceTypeEnum.KINGDEE_OPERATOR.getCode());
        dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
        dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_OPERATOR_TAG.getName());
        dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
        dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
        dmpSyncTaskDTO.setSyncOperate(operate);
        dmpMqFeign.sendMqAndSaveTask(dmpSyncTaskDTO);
    }
}
