package com.erp.server.tms.sync.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.dmp.dto.DmpSoLogisticsDTO;
import com.erp.model.dmp.dto.DmpSoLogisticsDetailDTO;
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
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.erp.server.tms.service.TmsPushMsgService;
import com.erp.server.tms.sync.SyncLogisticsBillService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
                                                         Map<String, Map<String, String>> logisticInfoMaps) {

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
        Map<String, String> infoMap = logisticInfoMaps.get(entity.getId());
        if(!CollUtil.isEmpty(infoMap)) {
            logisticCompanyCode = infoMap.getOrDefault("logisticCompanyCode","");
            logisticCompany = infoMap.getOrDefault("logisticCompany","");

            shudiyunB2cOrderDTO.setShop_no(infoMap.getOrDefault("shopNo","无"));
            shudiyunB2cOrderDTO.setShop_name(infoMap.getOrDefault("shopName","无"));
            shudiyunB2cOrderDTO.setRoot_node_no(infoMap.getOrDefault("orderPlatformCode","无"));
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
                                                         Map<String, Map<String, String>> logisticInfoMaps) {

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

        String logisticCompanyCode = "";
        String logisticCompany = "";
        Map<String, String> infoMap = logisticInfoMaps.get(entity.getId());
        if(!CollUtil.isEmpty(infoMap)) {
        	logisticCompanyCode = infoMap.getOrDefault("logisticCompanyCode","");
        	logisticCompany = infoMap.getOrDefault("logisticCompany","");

            viewDto.setShopNo(infoMap.getOrDefault("shopNo","无"));
            viewDto.setShopName(infoMap.getOrDefault("shopName","无"));
            viewDto.setPlatformType(infoMap.getOrDefault("platformType","无"));
            viewDto.setOrderPlatformCode(infoMap.getOrDefault("orderPlatformCode","无"));
        }
        
        if (CharSequenceUtil.isBlank(logisticCompanyCode)) {
        	viewDto.setLogisticCompanyCode("无");
        } else {
        	viewDto.setLogisticCompanyCode(logisticCompanyCode);
        }
        
        if (CharSequenceUtil.isBlank(logisticCompany)) {
        	viewDto.setLogisticCompanyName("无");
        } else {
        	viewDto.setLogisticCompanyName(logisticCompany);
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
                              Map<String, Map<String, String>> logisticInfoMaps , boolean isHistory , boolean isNewQuerySync) {

        for (LogisticsBillDetailEntity billDetailEntity : detailEntityList) {
            String sourceCode = CharSequenceUtil.isBlank(entity.getTransportNo()) ? billDetailEntity.getTrackNo() : entity.getTransportNo();
            if (CharSequenceUtil.isBlank(sourceCode) && !isNewQuerySync) {
                continue;
            }
            TmsPushMsgEntity tmsPushMsgEntity = new TmsPushMsgEntity();
            tmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
            tmsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_LOGISTICS_BILL.getCode());
            tmsPushMsgEntity.setSourceId(billDetailEntity.getId());
            tmsPushMsgEntity.setSourceCode(sourceCode);
            tmsPushMsgEntity.setSyncOperate(operate);
            Map<String, Object> map = new HashMap<>();
            if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate) && !isHistory) {
            	map.put("isQuerySync", Boolean.TRUE);
                map.put("detailId", billDetailEntity.getId());
                map.put("operate", operate);
            }else {
            	if(isNewQuerySync) {
            		map = this.syncNewDataToSdyFieldHandler(entity, billDetailEntity, operate, logisticInfoMaps);
            	}else {
            		map = this.syncDataToSdyFieldHandler(entity, billDetailEntity, operate, logisticInfoMaps);
            	}
            }
            tmsPushMsgEntity.setPushData(JSON.toJSONString(map));
            tmsPushMsgService.save(tmsPushMsgEntity);
        }
    }


	@Override
	public Map<String, Map<String, String>> getLogisticInfo(List<LogisticsBillEntity> entitys) {
		Map<String, Map<String, String>> result = new HashMap<>();
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
                            Map<String, String> curInfoMap = result.get(channelMap.getKey());
                            if (CollUtil.isEmpty(curInfoMap)) {
                                curInfoMap = new HashMap<>();
                            }
                            curInfoMap.put("logisticCompanyCode", supplierCode);
                            curInfoMap.put("logisticCompany", logisticsSupplierEntity.getSupplierName());
							result.put(channelMap.getKey(), curInfoMap);
						}
					}
				}
			}
            List<String> soOutstockIds = entitys.stream()
                    .map(LogisticsBillEntity::getOutstockId)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            if(CollUtil.isEmpty(soOutstockIds)) {
                return result;
            }
			Map<String, SoOutstockEntity> idSoOutMaps = FeignQuery.getByIds(SoOutstockEntity.class, soOutstockIds).stream().collect(Collectors.toMap(SoOutstockEntity::getId, s -> s));
            if (CollUtil.isEmpty(idSoOutMaps)) {
                return result;
            }
            Map<String, SoB2cEntity> idSoB2cMaps = new HashMap<>();

            List<String> soIdList = idSoOutMaps.values().stream().map(SoOutstockEntity::getSoId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(soIdList)) {
                idSoB2cMaps = FeignQuery.getByIds(SoB2cEntity.class, soIdList)
                        .stream()
                        .collect(Collectors.toMap(SoB2cEntity::getId, s -> s));
            }
            Map<String, CustomerInfoEntity> idCustomerInfoMaps = new HashMap<>();
            List<String> customerIdList = idSoOutMaps.values().stream().map(SoOutstockEntity::getCustomerId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(customerIdList)) {
                idCustomerInfoMaps = FeignQuery.getByIds(CustomerInfoEntity.class, customerIdList)
                        .stream()
                        .collect(Collectors.toMap(CustomerInfoEntity::getId, s -> s));
            }
            for (LogisticsBillEntity entity : entitys) {
                SoOutstockEntity soOutstockEntity = idSoOutMaps.get(entity.getOutstockId());
                if (null == soOutstockEntity) {
                    continue;
                }
                Map<String, String> curInfoMap = result.get(entity.getId());
                if (CollUtil.isEmpty(curInfoMap)) {
                    curInfoMap = new HashMap<>();
                }
                if (StringUtils.isBlank(entity.getChannelId())) {
                    curInfoMap.put("logisticCompanyCode", soOutstockEntity.getLogisticsChannelCode());
                    curInfoMap.put("logisticCompany", soOutstockEntity.getLogisticsChannelName());
                }
                String orderPlatformCode = soOutstockEntity.getSoCode();
                if (OrderTypeEnum.B2C.getCode().equals(entity.getOrderType())) {
                    SoB2cEntity soB2cEntity = idSoB2cMaps.get(soOutstockEntity.getSoId());
                    if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                        if (CharSequenceUtil.isNotBlank(soB2cEntity.getPlatformCode())) {
                            orderPlatformCode = soB2cEntity.getPlatformCode();
                        }
                    }
                }
                curInfoMap.put("orderPlatformCode", orderPlatformCode);
                CustomerInfoEntity customerInfo = idCustomerInfoMaps.get(soOutstockEntity.getCustomerId());
                if (null != customerInfo) {
                    curInfoMap.put("shopNo", customerInfo.getCode());
                    curInfoMap.put("shopName", customerInfo.getName());
                    curInfoMap.put("platformType", customerInfo.getPlatformType());
                }

                result.put(entity.getId(), curInfoMap);
			}
		}
		return result;
	}
}
