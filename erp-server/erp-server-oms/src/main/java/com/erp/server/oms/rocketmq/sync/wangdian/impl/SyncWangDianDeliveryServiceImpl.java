package com.erp.server.oms.rocketmq.sync.wangdian.impl;


import com.alibaba.fastjson.JSON;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.entity.OmsPushMsgEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.server.oms.rocketmq.sync.wangdian.SyncWangDianDeliveryService;
import com.erp.server.oms.service.OmsPushMsgService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class SyncWangDianDeliveryServiceImpl implements SyncWangDianDeliveryService {

    @Resource
    private OmsPushMsgService omsPushMsgService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void syncDataToWangDian(SoInfoEntity soInfoEntity) {
        OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.WDT.getCode());
        omsPushMsgEntity.setSourceType(SourceTypeEnum.WDT_SO_B2B_DELIVERY.getCode());
        omsPushMsgEntity.setSourceId(soInfoEntity.getId());
        omsPushMsgEntity.setSourceCode(soInfoEntity.getCode());
        omsPushMsgEntity.setSyncOperate(SyncOperateEnum.OPERATE_DELIVERY.getCode());
        omsPushMsgEntity.setPushData(JSON.toJSONString(soInfoEntity));
        omsPushMsgService.save(omsPushMsgEntity);
    }

}
