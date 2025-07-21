package com.erp.server.dmp.inout.handler.output.task.api;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.Tools;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.service.DmpSoOutstockDetailService;
import com.erp.server.dmp.service.DmpSoOutstockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 旺店通原始订单推送数帝云
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdyLogisticsHandler extends DmpOutputSdyBaseTaskHandler {

    @Resource
    private DmpSoOutstockService dmpSoOutstockService;
    @Resource
    private DmpSoOutstockDetailService dmpSoOutstockDetailService;


    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoLogisticsEntity> dmpSoLogisticsEntityMap = new HashMap<>();
        Map<String, List<DmpSoLogisticsDetailEntity>> dmpSoLogisticsDetailEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_logistics".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoLogisticsEntity dmpSoOriginalInfoEntity = (DmpSoLogisticsEntity) v;
                        dmpSoLogisticsEntityMap.put(dmpSoOriginalInfoEntity.getId(), dmpSoOriginalInfoEntity);
                    }
                } else if ("dmp_so_logistics_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoLogisticsDetailEntity dmpSoOriginalDetailEntity = (DmpSoLogisticsDetailEntity) v;
                        String mainId = dmpSoOriginalDetailEntity.getMainId();
                        List<DmpSoLogisticsDetailEntity> list = dmpSoLogisticsDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpSoOriginalDetailEntity);
                        dmpSoLogisticsDetailEntityMap.put(mainId, list);
                    }
                }
            }
        }
        
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_logistics".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_so_logistics_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoLogisticsDetailEntity DmpSoDetailEntity = (DmpSoLogisticsDetailEntity) v;
                        changeIds.add(DmpSoDetailEntity.getMainId());
                    }
                }
            }
        }

        // 字典信息
        List<DictBasicEntity> dictList = FeignQuery.create(DictBasicEntity.class)
                .eq(DictBasicEntity::getType, DictBasicTypeEnum.SDY_SUB_PLATFORM.getType())
                .list();

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
        	Map<String, ShudiyunB2cOrderDTO> result = this.convert(dmpSoLogisticsEntityMap.get(changId), dmpSoLogisticsDetailEntityMap.get(changId), dictList, cfgOutputId);
        	if(!result.isEmpty()) {
            	for(Map.Entry<String, ShudiyunB2cOrderDTO> r : result.entrySet()) {
            		map.put(r.getKey(), JSON.toJSONString(r.getValue()));
            	}
            }
        }
        return map;
    }
    
    private Map<String, ShudiyunB2cOrderDTO> convert(DmpSoLogisticsEntity dmpSoLogisticsEntity , List<DmpSoLogisticsDetailEntity> dmpSoLogisticsDetailEntityList , List<DictBasicEntity> dictList, String cfgOutputId){
    	Map<String, ShudiyunB2cOrderDTO> result = new HashMap<>();
    	if(dmpSoLogisticsEntity != null && CollUtil.isNotEmpty(dmpSoLogisticsDetailEntityList)) {
    		if(validateDataBlack(dmpSoLogisticsEntity, cfgOutputId)) {
    			return result;
    		}
    		Tools.nullToBlank(dmpSoLogisticsEntity);
    		DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    		String bizNo = dmpSoLogisticsEntity.getThirdLogisticsCode();
    		LocalDateTime deliveryTime = dmpSoLogisticsEntity.getDeliveryTime();
    		String deliverTimeFormat = null;
    		if(deliveryTime != null) {
    			deliverTimeFormat = localDateTime.format(deliveryTime);
    		}
    		String signTimeFormat = "";
    		LocalDateTime signTime = dmpSoLogisticsEntity.getSignTime();
    		if (signTime != null) {
    			signTimeFormat = localDateTime.format(signTime);
    		}
    		for(DmpSoLogisticsDetailEntity dmpSoLogisticsDetailEntity : dmpSoLogisticsDetailEntityList) {
    			if(validateDataBlack(dmpSoLogisticsDetailEntity, cfgOutputId)) {
    				continue;
    			}
    			Tools.nullToBlank(dmpSoLogisticsDetailEntity);
    			String detailId = dmpSoLogisticsDetailEntity.getId();
    			ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();
    			
    			shudiyunB2cOrderDTO.setBiz_uni_key(dmpSoLogisticsDetailEntity.getThirdLogisticsId() + dmpSoLogisticsDetailEntity.getThirdLogisticsDetailId());
    	        shudiyunB2cOrderDTO.setBiz_no(bizNo);
    	        
    	        shudiyunB2cOrderDTO.setBiz_time(deliverTimeFormat);
	            shudiyunB2cOrderDTO.setDelivery_time(deliverTimeFormat);
    	        //默认运单
    	        shudiyunB2cOrderDTO.setTransaction_type("运单");
    	        shudiyunB2cOrderDTO.setTransaction_sub_type("普通运单");

    	        shudiyunB2cOrderDTO.setBiz_status(dmpSoLogisticsDetailEntity.getTrackStatus());
    	        
    	        shudiyunB2cOrderDTO.setStatus(dmpSoLogisticsDetailEntity.getDataStatus());

    	        shudiyunB2cOrderDTO.setLogistics_delivery_time(signTimeFormat);

    	        shudiyunB2cOrderDTO.setDelivery_number(dmpSoLogisticsEntity.getOutstockCode());

    	        shudiyunB2cOrderDTO.setLogistic_company_code(dmpSoLogisticsEntity.getLogisticCompanyCode());
    	        shudiyunB2cOrderDTO.setLogistic_company(dmpSoLogisticsEntity.getLogisticCompanyName());

    	        shudiyunB2cOrderDTO.setWaybill_number(bizNo);
    	        shudiyunB2cOrderDTO.setForeign_waybill_number(bizNo);
    	        shudiyunB2cOrderDTO.setSource_system("SDC");
    	        shudiyunB2cOrderDTO.setRoot_node_no_initial(dmpSoLogisticsEntity.getOrderPlatformCode());

                shudiyunB2cOrderDTO.setShop_no(dmpSoLogisticsEntity.getShopNo());
                shudiyunB2cOrderDTO.setShop_name(dmpSoLogisticsEntity.getShopName());
                shudiyunB2cOrderDTO.setRoot_node_no(dmpSoLogisticsEntity.getOrderPlatformCode());

                String platformType = dmpSoLogisticsEntity.getPlatformType();
                if (StringUtils.isNotBlank(platformType)) {
                    DictBasicEntity dictBasicEntity = dictList.stream().filter(req -> req.getName().equals(platformType)).findFirst().orElse(null);
                    if (null != dictBasicEntity) {
                        shudiyunB2cOrderDTO.setPlatform_id(dictBasicEntity.getRemark());
                        shudiyunB2cOrderDTO.setPlatform_name(dictBasicEntity.getRemark());
                        shudiyunB2cOrderDTO.setSubplatform_no(dictBasicEntity.getValue());
                        shudiyunB2cOrderDTO.setSubplatform_name(dictBasicEntity.getValue());
                    }
                }

                shudiyunB2cOrderDTO.setParent_node_no(dmpSoLogisticsEntity.getOutstockCode());

    			result.put(detailId, shudiyunB2cOrderDTO);
    		}
    	}
    	return result;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("biz_no");
    }
}
