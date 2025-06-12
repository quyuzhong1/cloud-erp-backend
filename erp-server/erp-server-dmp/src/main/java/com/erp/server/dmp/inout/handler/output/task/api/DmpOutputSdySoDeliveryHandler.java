package com.erp.server.dmp.inout.handler.output.task.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.Tools;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoDeliveryDetailEntity;
import com.erp.model.dmp.entity.DmpSoDeliveryEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * 旺店通原始订单推送数帝云
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdySoDeliveryHandler extends DmpOutputSdyBaseTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoDeliveryEntity> dmpSoDeliveryEntityMap = new HashMap<>();
        Map<String, List<DmpSoDeliveryDetailEntity>> dmpSoDeliveryDetailEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_delivery".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoDeliveryEntity dmpSoOriginalInfoEntity = (DmpSoDeliveryEntity) v;
                        dmpSoDeliveryEntityMap.put(dmpSoOriginalInfoEntity.getId(), dmpSoOriginalInfoEntity);
                    }
                } else if ("dmp_so_delivery_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoDeliveryDetailEntity dmpSoOriginalDetailEntity = (DmpSoDeliveryDetailEntity) v;
                        String mainId = dmpSoOriginalDetailEntity.getMainId();
                        List<DmpSoDeliveryDetailEntity> list = dmpSoDeliveryDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpSoOriginalDetailEntity);
                        dmpSoDeliveryDetailEntityMap.put(mainId, list);
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
                if ("dmp_so_delivery".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_so_delivery_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoDeliveryDetailEntity DmpSoDetailEntity = (DmpSoDeliveryDetailEntity) v;
                        changeIds.add(DmpSoDetailEntity.getMainId());
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
        	Map<String, ShudiyunB2cOrderDTO> result = this.convert(dmpSoDeliveryEntityMap.get(changId), dmpSoDeliveryDetailEntityMap.get(changId) , cfgOutputId);
        	if(!result.isEmpty()) {
            	for(Map.Entry<String, ShudiyunB2cOrderDTO> r : result.entrySet()) {
            		map.put(r.getKey(), JSON.toJSONString(r.getValue()));
            	}
            }
        }
        return map;
    }
    
    private Map<String, ShudiyunB2cOrderDTO> convert(DmpSoDeliveryEntity dmpSoDeliveryEntity , List<DmpSoDeliveryDetailEntity> dmpSoDeliveryDetailEntityList , String cfgOutputId){
    	Map<String, ShudiyunB2cOrderDTO> result = new HashMap<>();
    	if(dmpSoDeliveryEntity != null && CollUtil.isNotEmpty(dmpSoDeliveryDetailEntityList)) {
    		if(validateDataBlack(dmpSoDeliveryEntity, cfgOutputId)) {
    			return result;
    		}
    		Tools.nullToBlank(dmpSoDeliveryEntity);
    		DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    		String thirdDeliveryId = dmpSoDeliveryEntity.getThirdDeliveryId();
    		String thirdDeliveryCode = dmpSoDeliveryEntity.getThirdDeliveryCode();
    		LocalDateTime payTime = dmpSoDeliveryEntity.getPayTime();
    		String deliveryStatus = dmpSoDeliveryEntity.getDeliveryStatus();
    		String transactionType = dmpSoDeliveryEntity.getTransactionType();
    		String transactionSubType = dmpSoDeliveryEntity.getTransactionSubType();
    		BigDecimal allAmount = dmpSoDeliveryEntity.getAllAmount();
    		BigDecimal totalDiscountAmount = dmpSoDeliveryEntity.getTotalDiscountAmount();
    		BigDecimal totalCancelAmount = dmpSoDeliveryEntity.getTotalCancelAmount();
    		BigDecimal payAmount = dmpSoDeliveryEntity.getPayAmount();
    		BigDecimal shippingAmount = dmpSoDeliveryEntity.getShippingAmount();
    		BigDecimal totalTaxAmount = dmpSoDeliveryEntity.getTotalTaxAmount();
    		Integer totalQty = dmpSoDeliveryEntity.getTotalQty();
    		Integer cancelQty = dmpSoDeliveryEntity.getCancelQty();
    		Integer shippingQty = dmpSoDeliveryEntity.getShippingQty();
    		String salesCompanyCode = dmpSoDeliveryEntity.getSalesCompanyCode();
    		String receivingCompanyCode = dmpSoDeliveryEntity.getReceivingCompanyCode();
    		String organizationCode = dmpSoDeliveryEntity.getOrganizationCode();
    		String organizationName = dmpSoDeliveryEntity.getOrganizationName();
    		String sourcePlatform = dmpSoDeliveryEntity.getSourcePlatform();
    		String platformName = dmpSoDeliveryEntity.getPlatformName();
    		String subplatformNo = dmpSoDeliveryEntity.getSubplatformNo();
    		String subplatformName = dmpSoDeliveryEntity.getSubplatformName();
    		String shopNo = dmpSoDeliveryEntity.getShopNo();
    		String shopName = dmpSoDeliveryEntity.getShopName();
    		String platformCode = dmpSoDeliveryEntity.getPlatformCode();
    		if(StringUtils.isBlank(platformCode)) {
	        	platformCode = thirdDeliveryCode;
	        }
    		
    		boolean isB2B = "B2B仓".equals(dmpSoDeliveryEntity.getDataSource());
    		String payTimeFormat = null;
    		if(payTime != null) {
    			payTimeFormat = localDateTime.format(payTime);
    		}
    		LocalDateTime thirdCreateTime = dmpSoDeliveryEntity.getThirdCreateTime();
    		String thirdCreateTimeFormat = null;
    		if(thirdCreateTime != null) {
    			thirdCreateTimeFormat = localDateTime.format(thirdCreateTime);
    		}
    		
    		LocalDateTime thirdUpdateTime = dmpSoDeliveryEntity.getThirdUpdateTime();
    		String thirdUpdateTimeFormat = null;
    		if(thirdUpdateTime != null) {
    			thirdUpdateTimeFormat = localDateTime.format(thirdUpdateTime);
    		}
    		
    		for(DmpSoDeliveryDetailEntity dmpSoDeliveryDetailEntity : dmpSoDeliveryDetailEntityList) {
    			if(validateDataBlack(dmpSoDeliveryDetailEntity, cfgOutputId)) {
    				continue;
    			}
    			Tools.nullToBlank(dmpSoDeliveryDetailEntity);
    			String detailId = dmpSoDeliveryDetailEntity.getId();
    			ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();
    			
    			shudiyunB2cOrderDTO.setBiz_uni_key(thirdDeliveryId + dmpSoDeliveryDetailEntity.getThirdDeliveryDetailId());
    	        
    	        shudiyunB2cOrderDTO.setBiz_no(thirdDeliveryCode);
    	        shudiyunB2cOrderDTO.setBiz_time(payTimeFormat);
    	        //默认退货入库单
    	        shudiyunB2cOrderDTO.setTransaction_type(transactionType);
    	        shudiyunB2cOrderDTO.setTransaction_sub_type(transactionSubType);
    	        
    	        shudiyunB2cOrderDTO.setBiz_status(deliveryStatus);
    	        shudiyunB2cOrderDTO.setStatus(dmpSoDeliveryDetailEntity.getDataStatus());

    	        shudiyunB2cOrderDTO.setSales_company_code(salesCompanyCode);
    	        shudiyunB2cOrderDTO.setReceiving_company_code(receivingCompanyCode);
                shudiyunB2cOrderDTO.setOrganization_code(organizationCode);
                shudiyunB2cOrderDTO.setOrganization_name(organizationName);
                
                shudiyunB2cOrderDTO.setPlatform_id(sourcePlatform);
	            shudiyunB2cOrderDTO.setPlatform_name(platformName);
	            shudiyunB2cOrderDTO.setShop_no(shopNo);
	            shudiyunB2cOrderDTO.setShop_name(shopName);
                
	            shudiyunB2cOrderDTO.setTotal_goods_transaction_amount(allAmount);
	            shudiyunB2cOrderDTO.setDiscount_deduction_amount(totalDiscountAmount);
	            shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(totalCancelAmount);
	            shudiyunB2cOrderDTO.setBuyer_actual_payment(payAmount);
	            shudiyunB2cOrderDTO.setTotal_freight(shippingAmount);
	            shudiyunB2cOrderDTO.setPost_amount(shippingAmount);
	            shudiyunB2cOrderDTO.setTotal_goods_quantity(totalQty);
	            shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(cancelQty);
	            shudiyunB2cOrderDTO.setSubplatform_no(subplatformNo);
	            shudiyunB2cOrderDTO.setSubplatform_name(subplatformName);
	            
    	        shudiyunB2cOrderDTO.setRoot_node_no(platformCode);
    	        shudiyunB2cOrderDTO.setRoot_node_no_initial(platformCode);
    	        
    	        String skuNo = dmpSoDeliveryDetailEntity.getSkuNo();
				shudiyunB2cOrderDTO.setGoods_no(skuNo);
    	        String skuName = dmpSoDeliveryDetailEntity.getSkuName();
				shudiyunB2cOrderDTO.setGoods_name(skuName);
				shudiyunB2cOrderDTO.setGoods_transaction_quantity(dmpSoDeliveryDetailEntity.getTransactionQty());
				shudiyunB2cOrderDTO.setPrice(dmpSoDeliveryDetailEntity.getTransactionPrice());
				shudiyunB2cOrderDTO.setUnit(dmpSoDeliveryDetailEntity.getUnit());
				shudiyunB2cOrderDTO.setGoods_transaction_amount(dmpSoDeliveryDetailEntity.getTransactionAmount());
				shudiyunB2cOrderDTO.setGoods_status(dmpSoDeliveryDetailEntity.getDetailStatus());
				shudiyunB2cOrderDTO.setGoods_benchmark_selling_price(dmpSoDeliveryDetailEntity.getListPrice());
				shudiyunB2cOrderDTO.setSettlement_currency_code(dmpSoDeliveryDetailEntity.getSettlementCurrencyCode());
    	        
				String spuNo = dmpSoDeliveryDetailEntity.getSpuNo();
    	        String spuName = dmpSoDeliveryDetailEntity.getSpuName();
    	        shudiyunB2cOrderDTO.setSpec_no(spuNo);
	            shudiyunB2cOrderDTO.setSpec_name(spuName);

    	        shudiyunB2cOrderDTO.setIs_gift(dmpSoDeliveryDetailEntity.getIsGift());
    	        shudiyunB2cOrderDTO.setIs_comb(dmpSoDeliveryDetailEntity.getIsComb());
	            shudiyunB2cOrderDTO.setSuite_no(dmpSoDeliveryDetailEntity.getSuiteNo());
	            shudiyunB2cOrderDTO.setSuite_name(dmpSoDeliveryDetailEntity.getSuiteName());


    	        shudiyunB2cOrderDTO.setTransaction_currency(dmpSoDeliveryDetailEntity.getCurrencyName());
	            shudiyunB2cOrderDTO.setTransaction_currency_code(dmpSoDeliveryDetailEntity.getCurrencyCode());

	            if(!isB2B) {
	            	shudiyunB2cOrderDTO.setMsku_code(dmpSoDeliveryDetailEntity.getPlatformSkuNo());
	            	shudiyunB2cOrderDTO.setMsku_name(dmpSoDeliveryDetailEntity.getPlatformSkuName());
	            	shudiyunB2cOrderDTO.setTaxation(totalTaxAmount);
	            	shudiyunB2cOrderDTO.setRoot_node_create_time(payTimeFormat);
	            }else {
	            	shudiyunB2cOrderDTO.setRoot_node_create_time(thirdCreateTimeFormat);
	            }
	            
	            shudiyunB2cOrderDTO.setOrder_quantity_to_be_shipped(shippingQty);
	            shudiyunB2cOrderDTO.setRemark(dmpSoDeliveryDetailEntity.getRemark());
    	        shudiyunB2cOrderDTO.setRoot_node_modify_time(thirdUpdateTimeFormat);
    	        
    	        shudiyunB2cOrderDTO.setSku_code(skuNo);
    	        shudiyunB2cOrderDTO.setSku_name(skuName);

    	        shudiyunB2cOrderDTO.setSource_system("SDC");
    	        
    	        // 国家编码
    	        shudiyunB2cOrderDTO.setCountry_code(dmpSoDeliveryDetailEntity.getCountryCode());
    	        // 国家名称
    	        shudiyunB2cOrderDTO.setCountry(dmpSoDeliveryDetailEntity.getCountryName());
    	        // 区域编码
    	        shudiyunB2cOrderDTO.setRegion_code(dmpSoDeliveryDetailEntity.getRegionCode());
    	        // 区域名称
    	        shudiyunB2cOrderDTO.setRegion_name(dmpSoDeliveryDetailEntity.getRegionName());
    	        // 军区编码
    	        shudiyunB2cOrderDTO.setMilitary_region_code(dmpSoDeliveryDetailEntity.getMilitaryRegionCode());
    	        // 军区名称
    	        shudiyunB2cOrderDTO.setMilitary_region_name(dmpSoDeliveryDetailEntity.getMilitaryRegionName());
    	        // 部门编码
    	        shudiyunB2cOrderDTO.setDepartment_code(dmpSoDeliveryDetailEntity.getDepartmentCode());
    	        // 部门名称
    	        shudiyunB2cOrderDTO.setDepartment_name(dmpSoDeliveryDetailEntity.getDepartmentName());
    			
    			result.put(detailId, shudiyunB2cOrderDTO);
    		}
    	}
    	return result;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("biz_no" , "sku_code");
    }
}
