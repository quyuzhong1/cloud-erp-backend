package com.erp.server.oms.dht.impl;

import com.alibaba.fastjson.JSON;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.OmsPushMsgEntity;
import com.erp.server.oms.dht.SyncDhtService;
import com.erp.server.oms.service.OmsPushMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class SyncDhtServiceImpl implements SyncDhtService {


    @Resource
    private OmsPushMsgService omsPushMsgService;

    @Override
    public void createSyncCustomerTaskToDht(CustomerInfoEntity entity, String operate) {
        OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        omsPushMsgEntity.setSourceId(entity.getId());
        omsPushMsgEntity.setSourceCode(entity.getCode());
        omsPushMsgEntity.setSourceType(SourceTypeEnum.CUSTOMER_INFO.getCode());
        omsPushMsgEntity.setPushData(JSON.toJSONString(entity));
        omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.DHT.getCode());
        omsPushMsgEntity.setSyncOperate(operate);
        omsPushMsgService.save(omsPushMsgEntity);
    }

    @Override
    public void createSyncCustomerAddressTaskToDht(CustomerAddressEntity entity, String operate) {
        OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        omsPushMsgEntity.setSourceId(entity.getId());
        omsPushMsgEntity.setSourceCode(entity.getCode());
        omsPushMsgEntity.setSourceType(SourceTypeEnum.CUSTOMER_ADDRESS.getCode());
        omsPushMsgEntity.setPushData(JSON.toJSONString(entity));
        omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.DHT.getCode());
        omsPushMsgEntity.setSyncOperate(operate);
        omsPushMsgService.save(omsPushMsgEntity);
    }

}
