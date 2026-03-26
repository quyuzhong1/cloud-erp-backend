package com.erp.server.wms.dht.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.entity.OmsPushMsgEntity;
import com.erp.model.oms.entity.SoInfoEntity;
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
        /**
         * 1、销售订单平台为订货通的销售出库单推送订货通
         * 2、本地消息任务表销售订单存在推送订货通的记录的销售出库单推送订货通
         */
        SoInfoEntity soInfoEntity = FeignQuery.getById(SoInfoEntity.class, entity.getSoId());
        if (ObjUtil.isEmpty(soInfoEntity)) {
            throw new ServiceException(ApiError.SO_NOT_FOUND);
        }
        if (!CharSequenceUtil.equals(soInfoEntity.getDictPlatform(), PlatformDictEnum.DHT.getCode())) {
            //查询oms本地消息任务表是否存在销售订单推送订货通记录
            List<OmsPushMsgEntity> list = FeignQuery.create(OmsPushMsgEntity.class).eq(OmsPushMsgEntity::getSourceId, entity.getSoId()).
                    eq(OmsPushMsgEntity::getSourceType, SourceTypeEnum.SO_INFO.getCode()).
                    eq(OmsPushMsgEntity::getTargetPlatform, DmpBasicSystemCodeEnum.DHT.getCode()).
                    list();
            if (CollUtil.isEmpty(list)) {
                //既不是订货通平台的订单，且本地消息任务表也没有推送订货通的记录，则不推送
                return;
            }
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
