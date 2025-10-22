package com.erp.server.dmp.inout.handler.output.task.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import org.apache.commons.collections4.CollectionUtils;
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
import com.erp.server.dmp.service.DmpSoDeliveryService;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * 旺店通原始订单推送数帝云
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdySoDeliveryHandler extends DmpOutputSdyBaseTaskHandler {
	
	@Resource
	private DmpSoDeliveryService dmpSoDeliveryService;
	@Resource
	private SysUserFeign sysUserFeign;

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
        
        boolean selfAddFlag = isSelfAdd();
        String selftAdd = "selfAdd";
		List<String> selfAddPlatformCodes = dmpSoDeliveryEntityMap.values().stream()
        		.filter(d -> selftAdd.equals(d.getSourceType()) && StringUtils.isNotBlank(d.getPlatformCode()) && changeIds.contains(d.getId()))
        		.map(DmpSoDeliveryEntity::getPlatformCode)
        		.collect(Collectors.toList());
        if(CollUtil.isNotEmpty(selfAddPlatformCodes)) {
        	selfAddPlatformCodes = dmpSoDeliveryService.lambdaQuery().in(DmpSoDeliveryEntity::getPlatformCode, selfAddPlatformCodes)
        		.ne(DmpSoDeliveryEntity::getSourceType, selftAdd)
        		.select(DmpSoDeliveryEntity::getPlatformCode)
        		.list().stream().map(DmpSoDeliveryEntity::getPlatformCode).collect(Collectors.toList());
        }
        
        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		Map<String, Map<String, Object>> cacheMap = new HashMap<>();
        for (String changId : changeIds) {
        	DmpSoDeliveryEntity dmpSoDeliveryEntity = dmpSoDeliveryEntityMap.get(changId);
        	if(dmpSoDeliveryEntity == null) {
        		continue;
        	}
        	String sourceType = dmpSoDeliveryEntity.getSourceType();
        	if(selfAddFlag) {
        		String thirdDeliveryCode = dmpSoDeliveryEntity.getThirdDeliveryCode();
        		if(!selftAdd.equals(sourceType) || thirdDeliveryCode.contains("_")) {
        			continue;
        		}
        		String platformCode = dmpSoDeliveryEntity.getPlatformCode();
    			if(selftAdd.equals(sourceType) && StringUtils.isNotBlank(platformCode) && selfAddPlatformCodes.contains(platformCode)) {
    				continue;
            	}
        	}
			List<DmpSoDeliveryDetailEntity> dmpSoDeliveryDetailEntityList = dmpSoDeliveryDetailEntityMap.get(changId);
			Map<String, ShudiyunB2cOrderDTO> result = this.convert(dmpSoDeliveryEntity, dmpSoDeliveryDetailEntityList , cfgOutputId, cacheMap);
        	if(!result.isEmpty()) {
            	for(Map.Entry<String, ShudiyunB2cOrderDTO> r : result.entrySet()) {
            		map.put(r.getKey(), JSON.toJSONString(r.getValue()));
            	}
            }
        }
        return map;
    }
    
    protected boolean isSelfAdd(){
    	return false;
    }
    
    private Map<String, ShudiyunB2cOrderDTO> convert(DmpSoDeliveryEntity dmpSoDeliveryEntity , List<DmpSoDeliveryDetailEntity> dmpSoDeliveryDetailEntityList , String cfgOutputId, Map<String, Map<String, Object>> cacheMap){
    	Map<String, ShudiyunB2cOrderDTO> result = new HashMap<>();
    	if(dmpSoDeliveryEntity != null && CollUtil.isNotEmpty(dmpSoDeliveryDetailEntityList)) {
    		if(validateDataBlack(dmpSoDeliveryEntity, cfgOutputId)) {
    			return result;
    		}
    		boolean isB2B = "B2B仓".equals(dmpSoDeliveryEntity.getDataSource());
    		if(isB2B) {
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
    		
    		boolean selfAdd = isSelfAdd();
    		for(DmpSoDeliveryDetailEntity dmpSoDeliveryDetailEntity : dmpSoDeliveryDetailEntityList) {
    			if(validateDataBlack(dmpSoDeliveryDetailEntity, cfgOutputId)) {
    				continue;
    			}
    			Tools.nullToBlank(dmpSoDeliveryDetailEntity);
    			String detailId = dmpSoDeliveryDetailEntity.getId();
    			ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();
    			
				if(selfAdd) {
    				shudiyunB2cOrderDTO.setBiz_uni_key(dmpSoDeliveryDetailEntity.getSoId() + dmpSoDeliveryDetailEntity.getSoDetailId() + "_1");
    			}else {
    				shudiyunB2cOrderDTO.setBiz_uni_key(thirdDeliveryId + dmpSoDeliveryDetailEntity.getThirdDeliveryDetailId());
    			}
    	        
    	        shudiyunB2cOrderDTO.setBiz_no(thirdDeliveryCode);
    	        shudiyunB2cOrderDTO.setBiz_time(payTimeFormat);
    	        //默认退货入库单
    	        if(selfAdd) {
    	        	shudiyunB2cOrderDTO.setTransaction_type("线下订单");
    	        }else {
    	        	shudiyunB2cOrderDTO.setTransaction_type(transactionType);
    	        }
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

//    	        shudiyunB2cOrderDTO.setIs_gift(dmpSoDeliveryDetailEntity.getIsGift());
    	        shudiyunB2cOrderDTO.setIs_comb(dmpSoDeliveryDetailEntity.getIsComb());
	            shudiyunB2cOrderDTO.setSuite_no(dmpSoDeliveryDetailEntity.getSuiteNo());
	            shudiyunB2cOrderDTO.setSuite_name(dmpSoDeliveryDetailEntity.getSuiteName());


    	        shudiyunB2cOrderDTO.setTransaction_currency(dmpSoDeliveryDetailEntity.getCurrencyName());
	            shudiyunB2cOrderDTO.setTransaction_currency_code(dmpSoDeliveryDetailEntity.getCurrencyCode());

	            if(!isB2B) {
	            	String platformSkuNo = dmpSoDeliveryDetailEntity.getPlatformSkuNo();
	            	String platformSkuName = dmpSoDeliveryDetailEntity.getPlatformSkuName();
	            	if(selfAdd) {
	            		if(StringUtils.isBlank(platformSkuNo)) {
	            			platformSkuNo = skuNo;
	            		}
	            		if(StringUtils.isBlank(platformSkuName)) {
	            			platformSkuName = skuName;
	            		}
	            	}
	            	shudiyunB2cOrderDTO.setMsku_code(platformSkuNo);
	            	shudiyunB2cOrderDTO.setMsku_name(platformSkuName);
					// 配置指定平台税金为0
					Map<String, Object> dmpDictBasticMap = queryAndCacheDmpDictBasicEntity(cacheMap);
					List<com.erp.model.dmp.entity.DictBasicEntity> tax0PlatformTypeList = (List<com.erp.model.dmp.entity.DictBasicEntity>) dmpDictBasticMap.get("tax0PlatformType");
					if (CollUtil.isNotEmpty(tax0PlatformTypeList)) {
						boolean hasSetTaxO = tax0PlatformTypeList.stream().anyMatch(e -> e.getValue().equalsIgnoreCase(sourcePlatform));
						if (hasSetTaxO){
							totalTaxAmount = BigDecimal.ZERO;
						}
					}
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

				String departmentCode = dmpSoDeliveryDetailEntity.getDepartmentCode();
				String kingdeeDepartmentName = "";
				String kingdeeDepartmentCode = "";
				SysDepartmentEntity sysDepartmentEntity = queryAndCacheDepartment(cacheMap, departmentCode);
				if (null != sysDepartmentEntity) {
					CustomerInfoEntity customerInfo = queryAndCacheDictCustomerEntity(cacheMap, dmpSoDeliveryEntity.getShopNo());
					if (null != customerInfo) {
						KingdeeDepartmentEntity kingdeeDepartmentEntity = queryAndCacheKingdeeDepartment(cacheMap, sysDepartmentEntity.getId(), customerInfo.getUseOrgId());
						kingdeeDepartmentCode = kingdeeDepartmentEntity.getKingdeeDeptCode();
						kingdeeDepartmentName = kingdeeDepartmentEntity.getKingdeeDeptName();
					}
                }
    	        // 部门编码
    	        shudiyunB2cOrderDTO.setDepartment_code(kingdeeDepartmentCode);
    	        // 部门名称
    	        shudiyunB2cOrderDTO.setDepartment_name(kingdeeDepartmentName);
    			
    	        shudiyunB2cOrderDTO.setGoods_discount_deduction_amount(dmpSoDeliveryDetailEntity.getDiscountAmount());
    	        shudiyunB2cOrderDTO.setFreight(dmpSoDeliveryDetailEntity.getFreightAmount());
    	        shudiyunB2cOrderDTO.setGoods_taxation(dmpSoDeliveryDetailEntity.getTaxAmount());
    	        
    	        if(StringUtils.isBlank(shudiyunB2cOrderDTO.getDepartment_code()) && StringUtils.isNotBlank(shudiyunB2cOrderDTO.getShop_no())) {
                	List<CustomerInfoEntity> deptCustomerInfoList = FeignQuery.create(CustomerInfoEntity.class).eq(CustomerInfoEntity::getCode, 
                			shudiyunB2cOrderDTO.getShop_no()).list();
                	if(CollUtil.isNotEmpty(deptCustomerInfoList)) {
                		CustomerInfoEntity deptCustomerInfoEntity = deptCustomerInfoList.get(0);
                		List<KingdeeDepartmentEntity> deptKingdeeDepartmentEntityList = FeignQuery.create(KingdeeDepartmentEntity.class).eq(KingdeeDepartmentEntity::getErpDeptId, deptCustomerInfoEntity.getSalesDeptId())
                			.eq(KingdeeDepartmentEntity::getUseOrgId, deptCustomerInfoEntity.getUseOrgId()).list();
                		if(CollUtil.isNotEmpty(deptKingdeeDepartmentEntityList)) {
                			shudiyunB2cOrderDTO.setDepartment_code(deptKingdeeDepartmentEntityList.get(0).getKingdeeDeptCode());
                			shudiyunB2cOrderDTO.setDepartment_name(deptKingdeeDepartmentEntityList.get(0).getKingdeeDeptName());
                		}
                	}
                }
    	        shudiyunB2cOrderDTO.setDefaultValue();
    			result.put(detailId, shudiyunB2cOrderDTO);
    		}
    	}
    	return result;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("biz_no" , "sku_code");
    }

	private Map<String, Object> queryAndCacheDmpDictBasicEntity(Map<String, Map<String, Object>> cacheMap) {
		Map<String, Object> dmpDictBasic = cacheMap.getOrDefault("dmpDictBasic", new HashMap<>());

		List<com.erp.model.dmp.entity.DictBasicEntity>  tax0PlatformDictBasicList = (List<com.erp.model.dmp.entity.DictBasicEntity>)dmpDictBasic.getOrDefault("tax0PlatformType", new ArrayList<>());
		if (CollUtil.isNotEmpty(tax0PlatformDictBasicList)) {
			return dmpDictBasic;
		}
		List<com.erp.model.dmp.entity.DictBasicEntity> dictBasicEntityList = FeignQuery.create(com.erp.model.dmp.entity.DictBasicEntity.class)
				.in(com.erp.model.dmp.entity.DictBasicEntity::getType, Collections.singletonList("tax0PlatformType"))
				.list();
		if (CollectionUtils.isNotEmpty(dictBasicEntityList)) {
			Map<String, List<com.erp.model.dmp.entity.DictBasicEntity>> dpSourceMap = dictBasicEntityList
					.stream()
					.collect(Collectors.groupingBy(com.erp.model.dmp.entity.DictBasicEntity::getType));
			tax0PlatformDictBasicList = dpSourceMap.getOrDefault("tax0PlatformType", new ArrayList<>());
			dmpDictBasic.put("tax0PlatformType", tax0PlatformDictBasicList);
			cacheMap.put("dmpDictBasic", dmpDictBasic);
		}
		return dmpDictBasic;
	}

	private KingdeeDepartmentEntity queryAndCacheKingdeeDepartment(Map<String, Map<String, Object>> cacheMap , String erpDeptId , String useOrgId) {
		if(StringUtils.isNotBlank(erpDeptId) && StringUtils.isNotBlank(useOrgId)) {
			Map<String, Object> kingdeeDepartment = cacheMap.get("kingdeeDepartment");
			if(kingdeeDepartment == null) {
				List<KingdeeDepartmentEntity> list = FeignQuery.create(KingdeeDepartmentEntity.class).list();
				kingdeeDepartment = list.stream().collect(Collectors.toMap(k -> k.getErpDeptId() + "_" + k.getUseOrgId(), k -> k , (k1 , k2) -> k1));
				cacheMap.put("kingdeeDepartment", kingdeeDepartment);
			}
			Object object = kingdeeDepartment.get(erpDeptId + "_" + useOrgId);
			if(object != null) {
				return (KingdeeDepartmentEntity)object;
			}
		}
		return new KingdeeDepartmentEntity();
	}

	private SysDepartmentEntity queryAndCacheDepartment(Map<String, Map<String, Object>> cacheMap, String deptCode) {
		if (StringUtils.isBlank(deptCode)){
			return null;
		}
		List<SysDepartmentEntity> deptList = new LinkedList<>();
		Map<String, Object> deptListObj = cacheMap.get("deptList");
		if (null == deptListObj) {
			// 部门信息
			deptList = sysUserFeign.getDeptEntityList();
			deptListObj = deptList.stream().collect(Collectors.toMap(SysDepartmentEntity::getCode, k -> k, (k1, k2) -> k1));
            cacheMap.put("deptList", deptListObj);
        } else {
			Object entityObj = deptListObj.get(deptCode);
			return (SysDepartmentEntity) entityObj;
		}
		return deptList.stream().filter(e -> e.getCode().equals(deptCode)
					)
					.findFirst()
					.orElse(null);
	}

	private static CustomerInfoEntity queryAndCacheDictCustomerEntity(Map<String, Map<String, Object>> cacheMap, String customerCode) {
		Map<String, Object> customerInfoMap = cacheMap.get("customerInfo");
		if (null == customerInfoMap) {
			customerInfoMap = new HashMap<>();
			List<CustomerInfoEntity> customerEntityList = FeignQuery.create(CustomerInfoEntity.class).list();
			if (CollUtil.isNotEmpty(customerEntityList)) {
				Map<String, CustomerInfoEntity> allCustomer = customerEntityList.stream()
						.collect(Collectors.toMap(CustomerInfoEntity::getCode, k -> k, (k1, k2) -> k1));
				customerInfoMap.putAll(allCustomer);
			}
			cacheMap.put("customerInfo", customerInfoMap);
		}
        return (CustomerInfoEntity) customerInfoMap.get(customerCode);
	}

}
