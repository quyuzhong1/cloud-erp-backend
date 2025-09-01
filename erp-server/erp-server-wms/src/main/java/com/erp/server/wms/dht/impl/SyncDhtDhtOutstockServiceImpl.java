package com.erp.server.wms.dht.impl;

import com.alibaba.fastjson.JSON;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.server.wms.dht.SyncDhtOutstockService;
import com.erp.server.wms.service.WmsPushMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class SyncDhtDhtOutstockServiceImpl implements SyncDhtOutstockService {


    @Resource
    private WmsPushMsgService wmsPushMsgService;

    @Override
    public void syncB2bSoOutstockDht(SoOutstockEntity entity, String operate) {
        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.SO_OUTSTOCK.getCode());
        wmsPushMsgEntity.setPushData(JSON.toJSONString(entity));
        wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.DHT.getCode());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgService.save(wmsPushMsgEntity);
    }
}
