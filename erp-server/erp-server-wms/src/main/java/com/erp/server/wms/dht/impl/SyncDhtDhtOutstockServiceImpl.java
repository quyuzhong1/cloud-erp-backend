package com.erp.server.wms.dht.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.server.wms.dht.SyncDhtOutstockService;
import com.erp.server.wms.service.WmsPushMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SyncDhtDhtOutstockServiceImpl implements SyncDhtOutstockService {


    @Resource
    private WmsPushMsgService wmsPushMsgService;

    @Resource
    private LogisticsBillFeign logisticsBillFeign;

    @Override
    public void syncB2bSoOutstockDht(SoOutstockEntity entity, List<SoOutstockDetailEntity> soOutstockDetailEntityList, String operate) {
        //非b2b的销售出库单不推送
        if (!CharSequenceUtil.equals(entity.getOrderType(), OrderTypeEnum.B2B.getCode())) {
            return;
        }
        //数据对象
        JSONObject jsonObject = JSONUtil.parseObj(entity);
        jsonObject.set("details", JSON.toJSON(soOutstockDetailEntityList));
        //物流跟踪单号
        List<LogisticsBillEntity> logisticsBillList = FeignQuery.create(LogisticsBillEntity.class).eq(LogisticsBillEntity::getOutstockId, entity.getId()).list();
        if (CollUtil.isNotEmpty(logisticsBillList)) {
            List<String> billIdList = logisticsBillList.stream().map(LogisticsBillEntity::getId).distinct().collect(Collectors.toList());
            List<LogisticsBillDetailEntity> logisticsBillDetailList = FeignQuery.create(LogisticsBillDetailEntity.class).in(LogisticsBillDetailEntity::getMainId, billIdList).list();
            if (CollUtil.isNotEmpty(logisticsBillDetailList)) {
                String trackNos = logisticsBillDetailList.stream().distinct().map(LogisticsBillDetailEntity::getTrackNo).collect(Collectors.joining(","));
                jsonObject.set("trackNos", trackNos);
            }
        }
        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.SO_OUTSTOCK.getCode());
        wmsPushMsgEntity.setPushData(com.alibaba.fastjson.JSONObject.toJSONString(jsonObject));
        wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.DHT.getCode());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgService.save(wmsPushMsgEntity);
    }
}
