package com.erp.server.tms.sync.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.server.tms.service.*;
import com.erp.server.tms.sync.SyncLogisticsBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class SyncLogisticsBillServiceImpl implements SyncLogisticsBillService {

    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;

    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Resource
    private TmsPushMsgService tmsPushMsgService;

    @Override
    public Map<String, Object> syncDataToSdyFieldHandler(LogisticsBillEntity entity, LogisticsBillDetailEntity logisticsBillDetailEntity, String operate) {

        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<LogisticsBillDetailEntity> detailEntityList = logisticsBillDetailService.listByMainIds(Arrays.asList(entity.getId()));
        LogisticsChannelEntity channelEntity = logisticsChannelService.getById(entity.getChannelId());

        String supplierName = "";
        if (Objects.nonNull(channelEntity)) {
            LogisticsSupplierEntity supplierEntity = logisticsSupplierService.getById(channelEntity.getMainId());
            supplierName = supplierEntity.getSupplierName();
        }

        ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();

        shudiyunB2cOrderDTO.setBiz_uni_key(entity.getId() + logisticsBillDetailEntity.getId());
        shudiyunB2cOrderDTO.setBiz_no(CharSequenceUtil.isBlank(entity.getTransportNo()) ? logisticsBillDetailEntity.getTrackNo() : entity.getTransportNo());
        if (entity.getDeliveryTime() != null) {
            shudiyunB2cOrderDTO.setBiz_time(localDateTime.format(entity.getDeliveryTime()));
        }
        //默认运单
        shudiyunB2cOrderDTO.setTransaction_type("运单");
        shudiyunB2cOrderDTO.setTransaction_sub_type("普通运单");
        shudiyunB2cOrderDTO.setBiz_status(LogisticTrackStatusEnum.getName(logisticsBillDetailEntity.getTrackStatus()));
        if (entity.getVersion() == null) {
            entity.setVersion(0);
        }
        if (logisticsBillDetailEntity.getVersion() == null) {
            entity.setVersion(0);
        }
        shudiyunB2cOrderDTO.setStatus(shudiyunB2cOrderDTO.sdyStatusHandle(operate, entity.getVersion(), logisticsBillDetailEntity.getVersion()));

        if (entity.getDeliveryTime() != null) {
            shudiyunB2cOrderDTO.setDelivery_time(localDateTime.format(entity.getDeliveryTime()));
        }
        if (logisticsBillDetailEntity.getSignTime() != null) {
            shudiyunB2cOrderDTO.setLogistics_delivery_time(localDateTime.format(logisticsBillDetailEntity.getSignTime()));
        } else {
            shudiyunB2cOrderDTO.setLogistics_delivery_time(localDateTime.format(LocalDateTime.now()));
        }
        shudiyunB2cOrderDTO.setDelivery_number(entity.getOutstockCode());

        if (CharSequenceUtil.isBlank(supplierName)) {
            shudiyunB2cOrderDTO.setLogistic_company("无");
        } else {
            shudiyunB2cOrderDTO.setLogistic_company(supplierName);
        }

        if (channelEntity != null && CharSequenceUtil.isNotBlank(channelEntity.getMainId())) {
            shudiyunB2cOrderDTO.setLogistic_company_code(channelEntity.getMainId());
        } else {
            shudiyunB2cOrderDTO.setLogistic_company_code("无");
        }

        shudiyunB2cOrderDTO.setWaybill_number(CharSequenceUtil.isBlank(entity.getTransportNo()) ? logisticsBillDetailEntity.getTrackNo() : entity.getTransportNo());
        shudiyunB2cOrderDTO.setForeign_waybill_number(logisticsBillDetailEntity.getTrackNo());
        shudiyunB2cOrderDTO.setSource_system("SDC");
        shudiyunB2cOrderDTO.setRoot_node_no_initial(logisticsBillDetailEntity.getTrackNo());

        return BeanUtil.beanToMap(shudiyunB2cOrderDTO);
    }

    @Override
    public void syncDataToSdy(LogisticsBillEntity entity, LogisticsBillDetailEntity detailEntity, String operate) {
        TmsPushMsgEntity tmsPushMsgEntity = new TmsPushMsgEntity();
        tmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
        tmsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_LOGISTICS_BILL.getCode());
        tmsPushMsgEntity.setSourceId(detailEntity.getId());
        tmsPushMsgEntity.setSourceCode(CharSequenceUtil.isBlank(entity.getTransportNo()) ? detailEntity.getTrackNo() : entity.getTransportNo());
        tmsPushMsgEntity.setSyncOperate(operate);
        tmsPushMsgEntity.setPushData(JSON.toJSONString(this.syncDataToSdyFieldHandler(entity, detailEntity, operate)));
        tmsPushMsgService.save(tmsPushMsgEntity);
    }
}
