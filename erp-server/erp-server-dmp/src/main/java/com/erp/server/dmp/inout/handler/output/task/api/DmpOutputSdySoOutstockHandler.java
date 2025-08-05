package com.erp.server.dmp.inout.handler.output.task.api;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.dmp.dto.DictBasicDTO;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.server.dmp.service.CfgTimezoneService;
import com.erp.server.dmp.service.DictBasicService;
import com.erp.server.dmp.service.DmpAmzSoOutstockDetailService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.dto.base.BaseIdDTO.CodeDTO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.Tools;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO.ViewDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.model.sys.entity.DictPartitionEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;


/**
 * 旺店通原始订单推送数帝云
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdySoOutstockHandler extends DmpOutputSdyBaseTaskHandler {
	
	@Autowired
	private SysUserFeign sysUserFeign;
	@Resource
	private DmpAmzSoOutstockDetailService dmpAmzSoOutstockDetailService;
	@Resource
	private CfgTimezoneService cfgTimezoneService;
	@Resource
	private DictBasicService dictBasicService;

	
    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoOutstockEntity> dmpSoOutstockEntityMap = new HashMap<>();
        Map<String, List<DmpSoOutstockDetailEntity>> dmpSoOutstockDetailEntityMap = new HashMap<>();
        List<DmpSoOutstockEntity> changeDmpSoOutstockEntity = new ArrayList<>();
        List<DmpSoOutstockDetailEntity> changeDmpSoOutstockDetailEntity = new ArrayList<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_outstock".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoOutstockEntity dmpSoOriginalInfoEntity = (DmpSoOutstockEntity) v;
                        dmpSoOutstockEntityMap.put(dmpSoOriginalInfoEntity.getId(), dmpSoOriginalInfoEntity);
                        changeDmpSoOutstockEntity.add(dmpSoOriginalInfoEntity);
                    }
                } else if ("dmp_so_outstock_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoOutstockDetailEntity dmpSoOriginalDetailEntity = (DmpSoOutstockDetailEntity) v;
                        String mainId = dmpSoOriginalDetailEntity.getMainId();
                        List<DmpSoOutstockDetailEntity> list = dmpSoOutstockDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpSoOriginalDetailEntity);
                        dmpSoOutstockDetailEntityMap.put(mainId, list);
                        changeDmpSoOutstockDetailEntity.add(dmpSoOriginalDetailEntity);
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
                if ("dmp_so_outstock".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_so_outstock_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoOutstockDetailEntity DmpSoDetailEntity = (DmpSoOutstockDetailEntity) v;
                        changeIds.add(DmpSoDetailEntity.getMainId());
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        Map<String, Map<String, String>> cfgMaps = new HashMap<>();
        if(CollUtil.isNotEmpty(changeDmpSoOutstockEntity)) {
        	List<String> countryIds = changeDmpSoOutstockEntity.stream().map(DmpSoOutstockEntity::getCountry).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        	Map<String, String> countryIdNameMap = FeignQuery.getByIds(DictCountryEntity.class, countryIds).stream().collect(Collectors.toMap(DictCountryEntity::getId, DictCountryEntity::getShortNameCn));
        	cfgMaps.put("country", countryIdNameMap);
        	
        	List<String> provinceIds = changeDmpSoOutstockEntity.stream().map(DmpSoOutstockEntity::getProvince).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        	Map<String, String> provinceIdNameMap = FeignQuery.getByIds(DictGlobalAreaEntity.class, provinceIds).stream().collect(Collectors.toMap(DictGlobalAreaEntity::getId, DictGlobalAreaEntity::getSubregionName));
        	cfgMaps.put("province", provinceIdNameMap);
        	
        	List<String> districtIds = changeDmpSoOutstockEntity.stream().map(DmpSoOutstockEntity::getDistrict).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        	List<DictPartitionEntity> dictPartitionEntityList = FeignQuery.create(DictPartitionEntity.class).in(DictPartitionEntity::getCode, districtIds).list();
			Map<String, String> districtIdNameMap = dictPartitionEntityList.stream().collect(Collectors.toMap(DictPartitionEntity::getCode, DictPartitionEntity::getName , (d1 , d2) -> d1));
			cfgMaps.put("district", districtIdNameMap);
        	
        	List<String> financialCompanyIds = changeDmpSoOutstockEntity.stream().map(DmpSoOutstockEntity::getFinancialCompanyId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        	Map<String, String> companyCodeMap = new HashMap<>();
        	Map<String, String> companyNameMap = new HashMap<>();
        	List<CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(financialCompanyIds);
        	if(CollUtil.isNotEmpty(accountingCompanyList)) {
        		companyCodeMap = accountingCompanyList.stream().collect(Collectors.toMap(CodeDTO::getId, CodeDTO::getCode));
        		companyNameMap = accountingCompanyList.stream().collect(Collectors.toMap(CodeDTO::getId, CodeDTO::getName));
        	}
        	cfgMaps.put("companyCode", companyCodeMap);
        	cfgMaps.put("companyName", companyNameMap);
        }
        if(CollUtil.isNotEmpty(changeDmpSoOutstockDetailEntity)) {
        	List<SysDepartmentEntity> deptEntityList = sysUserFeign.getDeptEntityList();
        	Map<String, String> departmentMap = new HashMap<>();
        	if(CollUtil.isNotEmpty(deptEntityList)) {
        		departmentMap = deptEntityList.stream().collect(Collectors.toMap(SysDepartmentEntity::getCode, SysDepartmentEntity::getName , (s1 , s2) -> s1));
        	}
        	cfgMaps.put("department", departmentMap);
        	
        	List<String> currencyList = new ArrayList<>();
        	currencyList.addAll(changeDmpSoOutstockDetailEntity.stream().map(DmpSoOutstockDetailEntity::getPayCurrency).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
        	currencyList.addAll(changeDmpSoOutstockDetailEntity.stream().map(DmpSoOutstockDetailEntity::getCurrency).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
        	List<ViewDTO> listByCurrency = sysUserFeign.listByCurrency(currencyList);
        	Map<String, String> currencyMap = new HashMap<>();
        	if(CollUtil.isNotEmpty(listByCurrency)) {
        		currencyMap = listByCurrency.stream().collect(Collectors.toMap(ViewDTO::getId, ViewDTO::getName));
        	}
        	cfgMaps.put("currency", currencyMap);
        	
        	List<String> platformTypeIds = changeDmpSoOutstockDetailEntity.stream().map(DmpSoOutstockDetailEntity::getPlatformType).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        	List<DictBasicEntity> dictBasicEntityList = FeignQuery.create(DictBasicEntity.class)
                    .eq(DictBasicEntity::getType, DictBasicTypeEnum.SDY_SUB_PLATFORM.getType())
                    .in(DictBasicEntity::getName, platformTypeIds)
                    .list();
        	Map<String, String> platformMap = new HashMap<>();
        	Map<String, String> subPlatformMap = new HashMap<>();
        	if(CollUtil.isNotEmpty(dictBasicEntityList)) {
        		platformMap = dictBasicEntityList.stream().collect(Collectors.toMap(DictBasicEntity::getName, DictBasicEntity::getRemark , (d1 , d2) -> d1));
        		subPlatformMap = dictBasicEntityList.stream().collect(Collectors.toMap(DictBasicEntity::getName, DictBasicEntity::getValue , (d1 , d2) -> d1));
        	}
        	cfgMaps.put("platform", platformMap);
        	cfgMaps.put("subPlatform", subPlatformMap);

			// 亚马逊出库单明细添加签收时间
			Map<String, String> amzPlatformSignTimeInfo = queryAndConvertArrivalDate(changeDmpSoOutstockDetailEntity);
			cfgMaps.put("amzPlatformSignTimeInfo", amzPlatformSignTimeInfo);

			// 旺店通仓库店铺发货类型映射
			List<DictBasicDTO.ViewDTO> dictbaseList = dictBasicService.getByKey("wdtSdyPlatformDeliveryType");
			if (CollUtil.isNotEmpty(dictbaseList)) {
				Map<String, String> deliveryTypeMap = dictbaseList.stream()
						.collect(Collectors.toMap(e -> CharSequenceUtil.format("{}_{}",e.getName(), e.getValue()), DictBasicDTO.ViewDTO::getValue));
				cfgMaps.put("wdtSdyPlatformDeliveryType", deliveryTypeMap);
			}
			
			cfgMaps.put("warehouse", FeignQuery.list(WarehouseEntity.class).stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getKingdeeWarehouseCode)));
		}
        for (String changId : changeIds) {
        	Map<String, ShudiyunB2cOrderDTO> result = this.convert(dmpSoOutstockEntityMap.get(changId), dmpSoOutstockDetailEntityMap.get(changId) , cfgOutputId , cfgMaps);
        	if(!result.isEmpty()) {
            	for(Map.Entry<String, ShudiyunB2cOrderDTO> r : result.entrySet()) {
            		map.put(r.getKey(), JSON.toJSONString(r.getValue()));
            	}
            }
        }
        return map;
    }

	private Map<String, ShudiyunB2cOrderDTO> convert(DmpSoOutstockEntity dmpSoOutstockEntity , List<DmpSoOutstockDetailEntity> dmpSoOutstockDetailEntityList , String cfgOutputId , Map<String, Map<String, String>> cfgMaps){
    	Map<String, ShudiyunB2cOrderDTO> result = new HashMap<>();
    	if(dmpSoOutstockEntity != null && CollUtil.isNotEmpty(dmpSoOutstockDetailEntityList)) {
    		if(validateDataBlack(dmpSoOutstockEntity, cfgOutputId)) {
    			return result;
    		}
    		Tools.nullToBlank(dmpSoOutstockEntity);
    		DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    		String country = dmpSoOutstockEntity.getCountry();
    		String province = dmpSoOutstockEntity.getProvince();
    		String district = dmpSoOutstockEntity.getDistrict();
    		String thirdCode = dmpSoOutstockEntity.getThirdCode();
    		String thirdBillNo = dmpSoOutstockEntity.getThirdBillNo();
    		LocalDateTime billDate = dmpSoOutstockEntity.getBillDate();
    		String tradeLabel = dmpSoOutstockEntity.getTradeLabel();
    		String platformStatus = dmpSoOutstockEntity.getPlatformStatus();
    		String saleOrgId = dmpSoOutstockEntity.getSaleOrgId();
    		String financialCompanyId = dmpSoOutstockEntity.getFinancialCompanyId();
    		String shopId = dmpSoOutstockEntity.getShopId();
    		String shopName = dmpSoOutstockEntity.getShopName();
    		String platformCode = dmpSoOutstockEntity.getPlatformCode();
    		LocalDateTime deliveryTime = dmpSoOutstockEntity.getDeliveryTime();
    		
    		String billDateFormat = null;
    		if(billDate != null) {
    			billDateFormat = localDateTime.format(billDate);
    		}
    		String deliveryTimeFormat = null;
    		if(deliveryTime != null) {
    			deliveryTimeFormat = localDateTime.format(deliveryTime);
    		}
    		for(DmpSoOutstockDetailEntity dmpSoOutstockDetailEntity : dmpSoOutstockDetailEntityList) {
    			if(validateDataBlack(dmpSoOutstockDetailEntity, cfgOutputId)) {
    				continue;
    			}
    			Tools.nullToBlank(dmpSoOutstockDetailEntity);
    			String detailId = dmpSoOutstockDetailEntity.getId();
    			ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();
    			
    			shudiyunB2cOrderDTO.setCountry_code(country);
    			if(country != null) {
    				String countryName = cfgMaps.get("country").get(country);
    				if(countryName == null) {
    					countryName = "";
    				}
					shudiyunB2cOrderDTO.setCountry(countryName);
    			}
    			shudiyunB2cOrderDTO.setRegion_code(province);
    			if(province != null) {
    				String region_name = cfgMaps.get("province").get(province);
    				if(region_name == null) {
    					region_name = "";
    				}
					shudiyunB2cOrderDTO.setRegion_name(region_name);
    			}
    			shudiyunB2cOrderDTO.setMilitary_region_code(district);
    			if(district != null) {
    				String military_region_name = cfgMaps.get("district").get(district);
    				if(military_region_name == null) {
    					military_region_name = "";
    				}
					shudiyunB2cOrderDTO.setMilitary_region_name(military_region_name);
    			}
    			String saleDeptName = dmpSoOutstockDetailEntity.getSaleDeptName();
				shudiyunB2cOrderDTO.setDepartment_code(saleDeptName);
				if(saleDeptName != null) {
					String department_name = cfgMaps.get("department").get(saleDeptName);
					if(department_name == null) {
						department_name = "";
					}
					shudiyunB2cOrderDTO.setDepartment_name(department_name);
				}
    			
    	        shudiyunB2cOrderDTO.setBiz_uni_key(thirdCode + dmpSoOutstockDetailEntity.getThirdDetailId());
    	        shudiyunB2cOrderDTO.setBiz_no(thirdBillNo);
    	        shudiyunB2cOrderDTO.setBiz_time(billDateFormat);
    	        //默认出库单
    	        shudiyunB2cOrderDTO.setTransaction_type("销售出库单");
    	        shudiyunB2cOrderDTO.setTransaction_sub_type(tradeLabel);
    	        shudiyunB2cOrderDTO.setBiz_status(platformStatus);
    	        shudiyunB2cOrderDTO.setStatus(dmpSoOutstockDetailEntity.getDataStatus());

    	        shudiyunB2cOrderDTO.setSales_company_code(saleOrgId);
    	        if(financialCompanyId != null) {
    	        	String financialCode = cfgMaps.get("companyCode").get(financialCompanyId);
    	        	if(financialCode == null) {
    	        		financialCode = "";
    	        	}
    				shudiyunB2cOrderDTO.setReceiving_company_code(financialCode);
        	        shudiyunB2cOrderDTO.setOrganization_code(financialCode);
        	        String organization_name = cfgMaps.get("companyName").get(financialCompanyId);
        	        if(organization_name == null) {
        	        	organization_name = "";
        	        }
					shudiyunB2cOrderDTO.setOrganization_name(organization_name);
    	        }
    	        String payCurrency = dmpSoOutstockDetailEntity.getPayCurrency();
				shudiyunB2cOrderDTO.setSettlement_currency_code(payCurrency);
				if(payCurrency != null) {
					String settlement_currency = cfgMaps.get("currency").get(payCurrency);
					if(settlement_currency == null) {
						settlement_currency = "";
					}
					shudiyunB2cOrderDTO.setSettlement_currency(settlement_currency);
				}
                String currency = dmpSoOutstockDetailEntity.getCurrency();
				shudiyunB2cOrderDTO.setTransaction_currency_code(currency);
				if(currency != null) {
					String transaction_currency = cfgMaps.get("currency").get(currency);
					if(transaction_currency == null) {
						transaction_currency = "";
					}
					shudiyunB2cOrderDTO.setTransaction_currency(transaction_currency);
				}
                String platformType = dmpSoOutstockDetailEntity.getPlatformType();
				
	            shudiyunB2cOrderDTO.setShop_no(shopId);
	            shudiyunB2cOrderDTO.setShop_name(shopName);
	            if(platformType != null) {
	            	String platform = cfgMaps.get("platform").get(platformType);
	            	shudiyunB2cOrderDTO.setPlatform_id(platform);
		            shudiyunB2cOrderDTO.setPlatform_name(platform);
		            
	            	String subplatform = cfgMaps.get("subPlatform").get(platformType);
	            	shudiyunB2cOrderDTO.setSubplatform_no(subplatform);
					shudiyunB2cOrderDTO.setSubplatform_name(subplatform);
					String platformDetailId = dmpSoOutstockDetailEntity.getPlatformDetailId();
					if (StringUtils.isNotBlank(platformDetailId) && DmpBasicSystemCodeEnum.AMAZON.getCode().equalsIgnoreCase(platformType)) {
						String signTime = cfgMaps.get("amzPlatformSignTimeInfo").get(platformDetailId);
						if (StringUtils.isNotBlank(signTime)) {
							shudiyunB2cOrderDTO.setEstimate_investment_time(signTime);
						}
					}
				}

				// 添加发货类型
				boolean isPlatformDelivery = checkPlatformDelivery(dmpSoOutstockEntity, dmpSoOutstockDetailEntity, cfgMaps.get("wdtSdyPlatformDeliveryType"));
				shudiyunB2cOrderDTO.setFulfillment_type(isPlatformDelivery ? "0":"1");
                
    	        shudiyunB2cOrderDTO.setRoot_node_no(dmpSoOutstockDetailEntity.getThirdOrderCode());

    	        String skuNo = dmpSoOutstockDetailEntity.getSkuNo();
    	        String skuName = dmpSoOutstockDetailEntity.getSkuName();
    	        String spuNo = dmpSoOutstockDetailEntity.getPlatformSku();
    	        String spuName = dmpSoOutstockDetailEntity.getSpecifics();
    	        //产品信息
    	        shudiyunB2cOrderDTO.setGoods_no(skuNo);
    	        shudiyunB2cOrderDTO.setGoods_name(skuName);
    	        if (StringUtils.isBlank(spuNo)) {
    	            shudiyunB2cOrderDTO.setSpec_no(skuNo);
    	            shudiyunB2cOrderDTO.setSpec_name(skuName);
    	        } else {
    	            shudiyunB2cOrderDTO.setSpec_no(spuNo);
    	            shudiyunB2cOrderDTO.setSpec_name(spuName);
    	        }
    	        shudiyunB2cOrderDTO.setSku_code(skuNo);
    	        shudiyunB2cOrderDTO.setSku_name(skuName);
//    	        Boolean isGift = dmpSoOutstockDetailEntity.getIsGift();
//    	        if(isGift != null && isGift) {
//    	        	shudiyunB2cOrderDTO.setIs_gift(1);
//    	        }else {
//    	        	shudiyunB2cOrderDTO.setIs_gift(0);
//    	        }
    	        
    	        Integer isComb = dmpSoOutstockDetailEntity.getIsComb();
    	        if(isComb == null) {
    	        	isComb = 0;
    	        }
				shudiyunB2cOrderDTO.setIs_comb(isComb);
    	        shudiyunB2cOrderDTO.setSuite_no(dmpSoOutstockDetailEntity.getSuiteNo());
                shudiyunB2cOrderDTO.setSuite_name(dmpSoOutstockDetailEntity.getSuiteName());

    	        shudiyunB2cOrderDTO.setRemark(dmpSoOutstockDetailEntity.getRemark());
    	        
    	        Map<String, String> warehouseMap = cfgMaps.get("warehouse");
				shudiyunB2cOrderDTO.setWarehouse_no(warehouseMap.get(dmpSoOutstockDetailEntity.getWarehouseId()));
    	        shudiyunB2cOrderDTO.setWarehouse_name(dmpSoOutstockDetailEntity.getWarehouseName());

    	        // 商品状态
    	        shudiyunB2cOrderDTO.setGoods_status("已发货");
    	        shudiyunB2cOrderDTO.setDelivery_time(deliveryTimeFormat);

    	        shudiyunB2cOrderDTO.setGoods_transaction_quantity(dmpSoOutstockDetailEntity.getQty());
    	        shudiyunB2cOrderDTO.setUnit(dmpSoOutstockDetailEntity.getProductUnit());
    	        shudiyunB2cOrderDTO.setGoods_benchmark_selling_price(dmpSoOutstockDetailEntity.getSellPrice());

    	        shudiyunB2cOrderDTO.setSource_system("SDC");
    	        shudiyunB2cOrderDTO.setRoot_node_no_initial(dmpSoOutstockDetailEntity.getThirdOrderCode());
    	        shudiyunB2cOrderDTO.setParent_node_no(platformCode);
    			
    			result.put(detailId, shudiyunB2cOrderDTO);
    		}
    	}
    	return result;
    }


	/**
	 * 查询亚马逊平台的签收时间，并转换为所需格式
	 */
	private Map<String, String> queryAndConvertArrivalDate(List<DmpSoOutstockDetailEntity> changeDmpSoOutstockDetailEntity) {
		List<String> amzPlatformDetailIdList = changeDmpSoOutstockDetailEntity.stream()
				.filter(e -> DmpBasicSystemCodeEnum.AMAZON.getCode().equalsIgnoreCase(e.getPlatformType()) && StringUtils.isNotBlank(e.getPlatformDetailId()))
				.map(DmpSoOutstockDetailEntity::getPlatformDetailId)
				.collect(Collectors.toList());
		if (CollUtil.isEmpty(amzPlatformDetailIdList)) {
			return new HashMap<>();
		}
		List<DmpAmzSoOutstockDetailEntity> list = dmpAmzSoOutstockDetailService.lambdaQuery()
				.in(DmpAmzSoOutstockDetailEntity::getShipmentItemId, amzPlatformDetailIdList)
				.list();
		if (CollUtil.isEmpty(list)) {
			return new HashMap<>();
		}
		Map<String, String> resultMap = new HashMap<>();
		// 渠道配置
		List<CfgTimezoneEntity> timeList = cfgTimezoneService.listAndCache();

		for (DmpAmzSoOutstockDetailEntity dmpAmzSoOutstockDetailEntity : list) {
			// 解析后的时区(按销售渠道)
			CfgTimezoneEntity timeZoneEntity = timeList.stream()
					.filter(t -> t.getAndParseCondition().contains(dmpAmzSoOutstockDetailEntity.getSalesChannel()))
					.findFirst()
					.orElse(null);
			if (null != timeZoneEntity) {
				// 设置所有本地时区
				if (StringUtils.isNotBlank(dmpAmzSoOutstockDetailEntity.getEstimatedArrivalDate())){
					OffsetDateTime parseDate = OffsetDateTime.parse(dmpAmzSoOutstockDetailEntity.getEstimatedArrivalDate());
					String lastTime = parseDate.atZoneSameInstant(ZoneId.of(timeZoneEntity.getTimeZone())).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
					resultMap.put(dmpAmzSoOutstockDetailEntity.getShipmentItemId(), lastTime);
				}
			}
		}
		return resultMap;
	}

	/**
	 * 检查类型是否为平台发货
	 */
	private boolean checkPlatformDelivery(DmpSoOutstockEntity dmpSoOutstockEntity, DmpSoOutstockDetailEntity dmpSoOutstockDetailEntity, Map<String, String> wdtSdyPlatformDeliveryType) {
		if (SourceTypeEnum.PLATFORM_SO_OUT_STOCK.getCode().equals(dmpSoOutstockEntity.getSourceType())) {
			// 平台出库单
			return true;
		} else if (SourceTypeEnum.QIMEN_SO_OUT_STOCK.getCode().equals(dmpSoOutstockEntity.getSourceType())
			|| SourceTypeEnum.SO_OUTSTOCK.getCode().equals(dmpSoOutstockEntity.getSourceType())
			|| SourceTypeEnum.WDT_OUT_STOCK.getCode().equals(dmpSoOutstockEntity.getSourceType())
		) {
			String key = CharSequenceUtil.format("{}_{}", dmpSoOutstockEntity.getShopId(), dmpSoOutstockDetailEntity.getWarehouseId());
			return wdtSdyPlatformDeliveryType.containsKey(key);
		} else {
			// 其他属于自发货
			return false;
		}
	}
    
    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("biz_no" , "sku_code");
    }

}
