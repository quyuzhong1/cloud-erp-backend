package com.erp.server.tms.sync.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.common.business.enums.SyncOperateEnum;
import com.erp.model.dmp.dto.DmpSoLogisticsDTO;
import com.erp.model.dmp.dto.DmpSoLogisticsDetailDTO;
import com.common.business.enums.SyncOperateEnum;
import com.erp.model.dmp.dto.DmpSoLogisticsDTO;
import com.erp.model.dmp.dto.DmpSoLogisticsDetailDTO;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.entity.TmsPushMsgEntity;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.erp.server.tms.service.TmsPushMsgService;
import com.erp.server.tms.sync.SyncLogisticsBillService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SyncLogisticsBillServiceImpl implements SyncLogisticsBillService {

    @Resource
    private TmsPushMsgService tmsPushMsgService;
    
    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Override
    public Map<String, Object> syncDataToSdyFieldHandler(LogisticsBillEntity entity,
                                                         LogisticsBillDetailEntity logisticsBillDetailEntity,
                                                         String operate,
                                                         Map<String, Pair<String, String>> logisticInfoMaps) {

        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();

        shudiyunB2cOrderDTO.setBiz_uni_key(entity.getId() + logisticsBillDetailEntity.getId());
        String bizNo = CharSequenceUtil.isBlank(entity.getTransportNo()) ? logisticsBillDetailEntity.getTrackNo() : entity.getTransportNo();
        shudiyunB2cOrderDTO.setBiz_no(bizNo);
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

        String logisticCompanyCode = "";
        String logisticCompany = "";
        Pair<String, String> pair = logisticInfoMaps.get(entity.getId());
        if(pair != null) {
        	logisticCompanyCode = pair.getKey();
        	logisticCompany = pair.getValue();
        }
        
        if (CharSequenceUtil.isBlank(logisticCompanyCode)) {
            shudiyunB2cOrderDTO.setLogistic_company_code("无");
        } else {
        	shudiyunB2cOrderDTO.setLogistic_company_code(logisticCompanyCode);
        }
        
        if (CharSequenceUtil.isBlank(logisticCompany)) {
            shudiyunB2cOrderDTO.setLogistic_company("无");
        } else {
            shudiyunB2cOrderDTO.setLogistic_company(logisticCompany);
        }

        shudiyunB2cOrderDTO.setWaybill_number(bizNo);
        shudiyunB2cOrderDTO.setForeign_waybill_number(bizNo);
        shudiyunB2cOrderDTO.setSource_system("SDC");
        shudiyunB2cOrderDTO.setRoot_node_no_initial(bizNo);

        return BeanUtil.beanToMap(shudiyunB2cOrderDTO);
    }

    @Override
    public Map<String, Object> syncNewDataToSdyFieldHandler(LogisticsBillEntity entity,
                                                         LogisticsBillDetailEntity logisticsBillDetailEntity,
                                                         String operate,
                                                         List<LogisticsChannelEntity> logisticsChannelEntities,
                                                         List<LogisticsSupplierEntity> logisticsSupplierEntities) {

        LogisticsChannelEntity channelEntity = logisticsChannelEntities.stream().filter(req -> req.getId().equals(entity.getChannelId())).findFirst().orElse(null);
        String supplierName = "";
        if (Objects.nonNull(channelEntity)) {
            LogisticsSupplierEntity supplierEntity = logisticsSupplierEntities.stream().filter(req -> req.getId().equals(channelEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(supplierEntity)) {
                supplierName = supplierEntity.getSupplierName();
            }
        }

        DmpSoLogisticsDTO.ViewDTO viewDto = new DmpSoLogisticsDTO.ViewDTO();

        String thirdLogisticsId = entity.getId();
		viewDto.setThirdLogisticsId(thirdLogisticsId);
        String bizNo = CharSequenceUtil.isBlank(entity.getTransportNo()) ? logisticsBillDetailEntity.getTrackNo() : entity.getTransportNo();
        viewDto.setThirdLogisticsCode(bizNo);
        viewDto.setThirdCreateTime(entity.getCreateTime());
        viewDto.setThirdUpdateTime(entity.getUpdateTime());
        viewDto.setDeliveryTime(entity.getDeliveryTime());
        viewDto.setOutstockCode(entity.getOutstockCode());
        viewDto.setSignTime(logisticsBillDetailEntity.getSignTime());

        if (CharSequenceUtil.isBlank(supplierName)) {
        	viewDto.setLogisticCompanyName("无");
        } else {
        	viewDto.setLogisticCompanyName(supplierName);
        }

        if (channelEntity != null && CharSequenceUtil.isNotBlank(channelEntity.getMainId())) {
        	viewDto.setLogisticCompanyCode(channelEntity.getMainId());
        } else {
        	viewDto.setLogisticCompanyCode("无");
        }
        
        DmpSoLogisticsDetailDTO.ViewDTO detail = new DmpSoLogisticsDetailDTO.ViewDTO();
        detail.setThirdLogisticsId(thirdLogisticsId);
        detail.setThirdLogisticsDetailId(logisticsBillDetailEntity.getId());
        detail.setThirdDetailCreateTime(logisticsBillDetailEntity.getCreateTime());
        detail.setThirdDetailUpdateTime(logisticsBillDetailEntity.getUpdateTime());
        detail.setTrackStatus(LogisticTrackStatusEnum.getName(logisticsBillDetailEntity.getTrackStatus()));
        detail.setDataStatus(new ShudiyunB2cOrderDTO().sdyStatusHandle(operate, entity.getVersion(), logisticsBillDetailEntity.getVersion()));

        viewDto.setDetailList(Arrays.asList(detail));
        return BeanUtil.beanToMap(viewDto);
    }

    @Override
    public void syncDataToSdy(LogisticsBillEntity entity,
                              List<LogisticsBillDetailEntity> detailEntityList,
                              String operate,
                              Map<String, Pair<String, String>> logisticInfoMaps) {

        for (LogisticsBillDetailEntity billDetailEntity : detailEntityList) {
            String sourceCode = CharSequenceUtil.isBlank(entity.getTransportNo()) ? billDetailEntity.getTrackNo() : entity.getTransportNo();
            if (CharSequenceUtil.isBlank(sourceCode)) {
//                continue;
            }
            TmsPushMsgEntity tmsPushMsgEntity = new TmsPushMsgEntity();
            tmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
            tmsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_LOGISTICS_BILL.getCode());
            tmsPushMsgEntity.setSourceId(billDetailEntity.getId());
            tmsPushMsgEntity.setSourceCode(sourceCode);
            tmsPushMsgEntity.setSyncOperate(operate);
            tmsPushMsgEntity.setPushData(JSON.toJSONString(this.syncDataToSdyFieldHandler(entity, billDetailEntity, operate, logisticInfoMaps)));
            tmsPushMsgService.save(tmsPushMsgEntity);
        }
    }


    @Override
    public void syncDataToSdy(LogisticsBillEntity entity,
                              List<LogisticsBillDetailEntity> detailEntityList,
                              String operate) {

    	List<LogisticsChannelEntity> logisticsChannelEntities = new ArrayList<>();
    	List<LogisticsSupplierEntity> logisticsSupplierEntities = new ArrayList<>();
    	
    	if(SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		String channelId = entity.getChannelId();
            if (StringUtils.isNotBlank(channelId)) {
                logisticsChannelEntities = logisticsChannelService.listByIds(Arrays.asList(channelId));
            }

            List<String> supplierIds = logisticsChannelEntities.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());
            if (CollUtil.isNotEmpty(supplierIds)) {
                logisticsSupplierEntities = logisticsSupplierService.listByIds(supplierIds);
            }
    	}
        
        for (LogisticsBillDetailEntity billDetailEntity : detailEntityList) {
            String sourceCode = CharSequenceUtil.isBlank(entity.getTransportNo()) ? billDetailEntity.getTrackNo() : entity.getTransportNo();
            if (CharSequenceUtil.isBlank(sourceCode)) {
//                continue;
            }
            TmsPushMsgEntity tmsPushMsgEntity = new TmsPushMsgEntity();
            tmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
            tmsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_LOGISTICS_BILL.getCode());
            tmsPushMsgEntity.setSourceId(billDetailEntity.getId());
            tmsPushMsgEntity.setSourceCode(sourceCode);
            tmsPushMsgEntity.setSyncOperate(operate);
            Map<String, Object> map = new HashMap<>();
            if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            	map.put("isQuerySync", Boolean.TRUE);
                map.put("detailId", billDetailEntity.getId());
                map.put("operate", operate);
            }else {
            	map = this.syncNewDataToSdyFieldHandler(entity, billDetailEntity, operate, logisticsChannelEntities, logisticsSupplierEntities);
            }
            tmsPushMsgEntity.setPushData(JSON.toJSONString(map));
            tmsPushMsgService.save(tmsPushMsgEntity);
        }
    }


	@Override
	public Map<String, Pair<String, String>> getLogisticInfo(List<LogisticsBillEntity> entitys) {
		Map<String, Pair<String, String>> result = new HashMap<>();
		if(CollUtil.isNotEmpty(entitys)) {
			Map<String, String> channelMaps = entitys.stream().filter(e -> StringUtils.isNotBlank(e.getChannelId())).collect(Collectors.toMap(LogisticsBillEntity::getId, LogisticsBillEntity::getChannelId));
			if(CollUtil.isNotEmpty(channelMaps)) {
				Map<String, String> channelMainMaps = logisticsChannelService.listByIds(channelMaps.values()).stream().collect(Collectors.toMap(LogisticsChannelEntity::getId, LogisticsChannelEntity::getMainId));
				if(CollUtil.isNotEmpty(channelMainMaps)) {
					List<LogisticsSupplierEntity> logisticsSupplierEntityList = logisticsSupplierService.listByIds(channelMainMaps.values());
					List<SupplierEntity> supplierList = new ArrayList<>();
					if(CollUtil.isNotEmpty(logisticsSupplierEntityList)) {
						supplierList = FeignQuery.getByIds(SupplierEntity.class, 
								logisticsSupplierEntityList.stream().filter(l -> StringUtils.isNotBlank(l.getSupplierId())).map(LogisticsSupplierEntity::getSupplierId).collect(Collectors.toList()));
					}
					Map<String, LogisticsSupplierEntity> idSupplierMaps = logisticsSupplierEntityList.stream().collect(Collectors.toMap(LogisticsSupplierEntity::getId, l -> l));
					Map<String, String> supplierIdCodeMaps = supplierList.stream().collect(Collectors.toMap(SupplierEntity::getId, SupplierEntity::getCode));
					for(Map.Entry<String, String> channelMap : channelMaps.entrySet()) {
						String mainId = channelMainMaps.get(channelMap.getValue());
						if(StringUtils.isNotBlank(mainId)) {
							LogisticsSupplierEntity logisticsSupplierEntity = idSupplierMaps.get(mainId);
							String supplierCode = supplierIdCodeMaps.get(logisticsSupplierEntity.getSupplierId());
							result.put(channelMap.getKey(), Pair.of(supplierCode, logisticsSupplierEntity.getSupplierName()));
						}
					}
				}
			}
			Map<String, String> notChannelMaps = entitys.stream().filter(e -> StringUtils.isBlank(e.getChannelId()) || StringUtils.isNotBlank(e.getOutstockId())).collect(Collectors.toMap(LogisticsBillEntity::getId, LogisticsBillEntity::getOutstockId));
			if(CollUtil.isNotEmpty(notChannelMaps)) {
				Map<String, SoOutstockEntity> idSoOutMaps = FeignQuery.getByIds(SoOutstockEntity.class, notChannelMaps.values()).stream().collect(Collectors.toMap(SoOutstockEntity::getId, s -> s));
				for(Map.Entry<String, String> notChannelMap : notChannelMaps.entrySet()) {
					SoOutstockEntity soOutstockEntity = idSoOutMaps.get(notChannelMap.getValue());
					if(soOutstockEntity != null) {
						result.put(notChannelMap.getKey(), Pair.of(soOutstockEntity.getLogisticsChannelCode(), soOutstockEntity.getLogisticsChannelName()));
					}
				}
			}
		}
		return result;
	}
}
