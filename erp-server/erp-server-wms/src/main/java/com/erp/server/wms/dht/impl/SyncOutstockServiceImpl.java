package com.erp.server.wms.dht.impl;

import com.alibaba.fastjson.JSON;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.server.wms.dht.SyncOutstockService;
import com.erp.server.wms.service.WmsPushMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class SyncOutstockServiceImpl implements SyncOutstockService {


    @Resource
    private WmsPushMsgService wmsPushMsgService;

    @Override
    public void createSyncSoOutstockTaskToDht(CustomerInfoEntity entity, String operate) {
        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.CUSTOMER_INFO.getCode());
        wmsPushMsgEntity.setPushData(JSON.toJSONString(entity));
        wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.DHT.getCode());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgService.save(wmsPushMsgEntity);
    }
}
