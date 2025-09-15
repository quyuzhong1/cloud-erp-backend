package com.erp.server.oms.dht.impl;

import com.alibaba.fastjson.JSON;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.entity.*;
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



    @Override
    public void createSyncCustomerCreditApplyTaskToDht(CustomerCreditApplyEntity entity, String operate) {
        OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        omsPushMsgEntity.setSourceId(entity.getId());
        omsPushMsgEntity.setSourceCode(entity.getCode());
        omsPushMsgEntity.setSourceType(SourceTypeEnum.CUSTOMER_CREDIT_APPLY.getCode());
        omsPushMsgEntity.setPushData(JSON.toJSONString(entity));
        omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.DHT.getCode());
        omsPushMsgEntity.setSyncOperate(operate);
        omsPushMsgService.save(omsPushMsgEntity);
    }

    @Override
    public void createSyncReceiptTaskToDht(SoReceiptEntity soReceiptEntity, String operate) {
        OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        omsPushMsgEntity.setSourceId(soReceiptEntity.getId());
        omsPushMsgEntity.setSourceCode(soReceiptEntity.getCode());
        omsPushMsgEntity.setSourceType(SourceTypeEnum.SO_RECEIPT.getCode());
        omsPushMsgEntity.setPushData(JSON.toJSONString(soReceiptEntity));
        omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.DHT.getCode());
        omsPushMsgEntity.setSyncOperate(operate);
        omsPushMsgService.save(omsPushMsgEntity);
    }

    @Override
    public void createSyncSoInfoTaskToDht(SoInfoEntity soInfoEntity, String operate) {
        OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        omsPushMsgEntity.setSourceId(soInfoEntity.getId());
        omsPushMsgEntity.setSourceCode(soInfoEntity.getCode());
        omsPushMsgEntity.setSourceType(SourceTypeEnum.SO_INFO.getCode());
        omsPushMsgEntity.setPushData(JSON.toJSONString(soInfoEntity));
        omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.DHT.getCode());
        omsPushMsgEntity.setSyncOperate(operate);
        omsPushMsgService.save(omsPushMsgEntity);
    }
}
