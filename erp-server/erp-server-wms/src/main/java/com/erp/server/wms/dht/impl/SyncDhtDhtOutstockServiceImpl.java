package com.erp.server.wms.dht.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.server.wms.dht.SyncDhtOutstockService;
import com.erp.server.wms.service.WmsPushMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class SyncDhtDhtOutstockServiceImpl implements SyncDhtOutstockService {


    @Resource
    private WmsPushMsgService wmsPushMsgService;

    @Override
    public void syncB2bSoOutstockDht(SoOutstockEntity entity, List<SoOutstockDetailEntity> soOutstockDetailEntityList, String operate) {
        //非b2b的销售出库单不推送
        if (!CharSequenceUtil.equals(entity.getOrderType(), OrderTypeEnum.B2B.getCode())) {
            return;
        }
        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.SO_OUTSTOCK.getCode());
        wmsPushMsgEntity.setPushData(JSONObject.toJSONString(entity.getId()));
        wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.DHT.getCode());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgService.save(wmsPushMsgEntity);
    }
}
