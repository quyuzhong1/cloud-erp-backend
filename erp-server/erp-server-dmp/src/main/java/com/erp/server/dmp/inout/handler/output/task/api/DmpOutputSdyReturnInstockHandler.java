package com.erp.server.dmp.inout.handler.output.task.api;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.service.DmpSoReturnInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.Tools;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpReturnInstockDetailEntity;
import com.erp.model.dmp.entity.DmpReturnInstockEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.wms.entity.WarehouseEntity;
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
public class DmpOutputSdyReturnInstockHandler extends DmpOutputSdyBaseTaskHandler {
	@Resource
	private DmpSoReturnInfoService dmpSoReturnInfoService;
	@Resource
	private SysUserFeign sysUserFeign;

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpReturnInstockEntity> dmpReturnInstockEntityMap = new HashMap<>();
        Map<String, List<DmpReturnInstockDetailEntity>> dmpReturnInstockDetailEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_return_instock".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpReturnInstockEntity dmpSoOriginalInfoEntity = (DmpReturnInstockEntity) v;
                        dmpReturnInstockEntityMap.put(dmpSoOriginalInfoEntity.getId(), dmpSoOriginalInfoEntity);
                    }
                } else if ("dmp_return_instock_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpReturnInstockDetailEntity dmpSoOriginalDetailEntity = (DmpReturnInstockDetailEntity) v;
                        String mainId = dmpSoOriginalDetailEntity.getMainId();
                        List<DmpReturnInstockDetailEntity> list = dmpReturnInstockDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpSoOriginalDetailEntity);
                        dmpReturnInstockDetailEntityMap.put(mainId, list);
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
                if ("dmp_return_instock".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_return_instock_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpReturnInstockDetailEntity DmpSoDetailEntity = (DmpReturnInstockDetailEntity) v;
                        changeIds.add(DmpSoDetailEntity.getMainId());
                    }
                }
            }
        }

		List<String> thirdCodeList = dmpReturnInstockEntityMap.values().stream().map(DmpReturnInstockEntity::getThirdCode)
				.filter(StringUtils::isNotBlank)
				.distinct()
				.collect(Collectors.toList());
		Map<String, List<DmpSoReturnInfoEntity>> soReturnInfoMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(thirdCodeList)){
			soReturnInfoMap = dmpSoReturnInfoService.lambdaQuery()
					.in(DmpSoReturnInfoEntity::getThirdCode, thirdCodeList)
					.list()
					.stream().collect(Collectors.groupingBy(DmpSoReturnInfoEntity::getThirdCode));
		}

        List<DictBasicEntity> dictBasicEntityList = FeignQuery.create(DictBasicEntity.class)
                .eq(DictBasicEntity::getType, DictBasicTypeEnum.SDY_SUB_PLATFORM.getType())
                .list();
        Map<String, DictBasicEntity> dictMaps = dictBasicEntityList.stream().collect(Collectors.toMap(DictBasicEntity::getName, d -> d , (d1 , d2) -> d1));
        
        Map<String, String> warehouseMap = FeignQuery.list(WarehouseEntity.class).stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getKingdeeWarehouseCode));

		// 部门信息
		List<KingdeeDepartmentEntity> kingdeeDeptList = FeignQuery.create(KingdeeDepartmentEntity.class).list();

		List<SysDepartmentEntity> deptList = sysUserFeign.getDeptEntityList();


        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
        	Map<String, ShudiyunB2cOrderDTO> result = this.convert(dmpReturnInstockEntityMap.get(changId), dmpReturnInstockDetailEntityMap.get(changId) , cfgOutputId , dictMaps, soReturnInfoMap , warehouseMap, deptList, kingdeeDeptList);
        	if(!result.isEmpty()) {
            	for(Map.Entry<String, ShudiyunB2cOrderDTO> r : result.entrySet()) {
            		map.put(r.getKey(), JSON.toJSONString(r.getValue()));
            	}
            }
        }
        return map;
    }
    
    private Map<String, ShudiyunB2cOrderDTO> convert(DmpReturnInstockEntity dmpReturnInstockEntity , List<DmpReturnInstockDetailEntity> dmpReturnInstockDetailEntityList , String cfgOutputId , Map<String, DictBasicEntity> dictMaps, Map<String, List<DmpSoReturnInfoEntity>> soReturnInfoMap , Map<String, String> warehouseMap, List<SysDepartmentEntity> deptList, List<KingdeeDepartmentEntity> kingdeeDeptList){
    	Map<String, ShudiyunB2cOrderDTO> result = new HashMap<>();
    	if(dmpReturnInstockEntity != null && CollUtil.isNotEmpty(dmpReturnInstockDetailEntityList)) {
    		if(validateDataBlack(dmpReturnInstockEntity, cfgOutputId)) {
    			return result;
    		}
    		Tools.nullToBlank(dmpReturnInstockEntity);
    		DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    		String thirdReturnInstockId = dmpReturnInstockEntity.getThirdReturnInstockId();
    		String thirdReturnInstockCode = dmpReturnInstockEntity.getThirdReturnInstockCode();
    		String returnInstockStatus = dmpReturnInstockEntity.getReturnInstockStatus();
    		LocalDateTime returnInstockTime = dmpReturnInstockEntity.getReturnInstockTime();
    		String logisticCompanyCode = dmpReturnInstockEntity.getLogisticCompanyCode();
    		String logisticCompanyName = dmpReturnInstockEntity.getLogisticCompanyName();
    		String returnLogisticCode = dmpReturnInstockEntity.getReturnLogisticCode();
    		String salesCompanyCode = dmpReturnInstockEntity.getSalesCompanyCode();
    		String receivingCompanyCode = dmpReturnInstockEntity.getReceivingCompanyCode();
    		String organizationCode = dmpReturnInstockEntity.getOrganizationCode();
    		String organizationName = dmpReturnInstockEntity.getOrganizationName();
    		String sourcePlatform = dmpReturnInstockEntity.getSourcePlatform();
    		String platformName = dmpReturnInstockEntity.getPlatformName();
    		String shopNo = dmpReturnInstockEntity.getShopNo();
    		String shopName = dmpReturnInstockEntity.getShopName();
    		String platformReturnInstockCode = dmpReturnInstockEntity.getPlatformReturnInstockCode();
    		String platformOrderCode = dmpReturnInstockEntity.getPlatformOrderCode();
    		
    		String returnInstockTimeFormat = null;
    		if(returnInstockTime != null) {
    			returnInstockTimeFormat = localDateTime.format(returnInstockTime);
    		}
    		for(DmpReturnInstockDetailEntity dmpReturnInstockDetailEntity : dmpReturnInstockDetailEntityList) {
    			if(validateDataBlack(dmpReturnInstockDetailEntity, cfgOutputId)) {
    				continue;
    			}
    			Tools.nullToBlank(dmpReturnInstockDetailEntity);
    			String detailId = dmpReturnInstockDetailEntity.getId();
    			ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();
    			
    			shudiyunB2cOrderDTO.setBiz_uni_key(thirdReturnInstockId + dmpReturnInstockDetailEntity.getThirdReturnInstockDetailId());
    	        
    	        shudiyunB2cOrderDTO.setBiz_no(thirdReturnInstockCode);
    	        shudiyunB2cOrderDTO.setBiz_time(returnInstockTimeFormat);
    	        //默认退货入库单
    	        shudiyunB2cOrderDTO.setTransaction_type("退货入库单");
    	        shudiyunB2cOrderDTO.setTransaction_sub_type("退货入库");
    	        shudiyunB2cOrderDTO.setBiz_status(returnInstockStatus);
    	        String dataStatus = dmpReturnInstockDetailEntity.getDataStatus();
    	        if(StringUtils.isNotBlank(returnInstockStatus) && "待提交".equals(returnInstockStatus)) {
    	        	dataStatus = "已删除";
    	        }
				shudiyunB2cOrderDTO.setStatus(dataStatus);

    	        shudiyunB2cOrderDTO.setSales_company_code(salesCompanyCode);
    	        shudiyunB2cOrderDTO.setReceiving_company_code(receivingCompanyCode);
                shudiyunB2cOrderDTO.setOrganization_code(organizationCode);
                shudiyunB2cOrderDTO.setOrganization_name(organizationName);
                
                DictBasicEntity dictBasicEntity = dictMaps.get(sourcePlatform);
                if(dictBasicEntity != null) {
                	shudiyunB2cOrderDTO.setPlatform_id(dictBasicEntity.getRemark());
    	            shudiyunB2cOrderDTO.setPlatform_name(dictBasicEntity.getRemark());
    	            shudiyunB2cOrderDTO.setSubplatform_no(dictBasicEntity.getValue());
    	            shudiyunB2cOrderDTO.setSubplatform_name(dictBasicEntity.getValue());
                }
	            shudiyunB2cOrderDTO.setShop_no(shopNo);
	            shudiyunB2cOrderDTO.setShop_name(shopName);
                
	            shudiyunB2cOrderDTO.setSettlement_currency_code(dmpReturnInstockDetailEntity.getSettlementCurrencyCode());

    	        shudiyunB2cOrderDTO.setRoot_node_no(platformOrderCode);
    	        
    	        String skuNo = dmpReturnInstockDetailEntity.getSkuNo();
				shudiyunB2cOrderDTO.setGoods_no(skuNo);
    	        String skuName = dmpReturnInstockDetailEntity.getSkuName();
				shudiyunB2cOrderDTO.setGoods_name(skuName);
    	        
				String spuNo = dmpReturnInstockDetailEntity.getSpuNo();
    	        String spuName = dmpReturnInstockDetailEntity.getSpuName();
    	        shudiyunB2cOrderDTO.setSpec_no(spuNo);
	            shudiyunB2cOrderDTO.setSpec_name(spuName);

//				shudiyunB2cOrderDTO.setIs_gift(dmpReturnInstockDetailEntity.getIsGift());
    	        Integer isComb = dmpReturnInstockDetailEntity.getIsComb();
    	        if(isComb == null) {
    	        	isComb = 0;
    	        }
				shudiyunB2cOrderDTO.setIs_comb(isComb);
	            shudiyunB2cOrderDTO.setSuite_no(dmpReturnInstockDetailEntity.getSuiteNo());
	            shudiyunB2cOrderDTO.setSuite_name(dmpReturnInstockDetailEntity.getSuiteName());


	            shudiyunB2cOrderDTO.setLogistic_company(logisticCompanyName);
	            shudiyunB2cOrderDTO.setLogistic_company_code(logisticCompanyCode);
	            shudiyunB2cOrderDTO.setDomestic_return_waybill_number(returnLogisticCode);
	            
    	        shudiyunB2cOrderDTO.setInternational_return_waybill_number("空");
    	        shudiyunB2cOrderDTO.setReturn_status(returnInstockStatus);
    	        shudiyunB2cOrderDTO.setReturn_receipt_number(thirdReturnInstockCode);
    	        Integer returnInstockQty = dmpReturnInstockDetailEntity.getReturnInstockQty();
				shudiyunB2cOrderDTO.setReturned_quantity(returnInstockQty);

    	        shudiyunB2cOrderDTO.setRemark(dmpReturnInstockDetailEntity.getRemark());
    	        shudiyunB2cOrderDTO.setWarehouse_no(warehouseMap.get(dmpReturnInstockDetailEntity.getWarehouseNo()));
    	        shudiyunB2cOrderDTO.setWarehouse_name(dmpReturnInstockDetailEntity.getWarehouseName());
    	        shudiyunB2cOrderDTO.setReturn_receipt_time(returnInstockTimeFormat);
    	        shudiyunB2cOrderDTO.setReturn_receipt_amount(dmpReturnInstockDetailEntity.getReturnInstockAmount());

    	        // 商品状态
    	        shudiyunB2cOrderDTO.setGoods_status(dmpReturnInstockDetailEntity.getDetailStatus());

    	        LocalDateTime deliveryTime = dmpReturnInstockDetailEntity.getDeliveryTime();
    	        if (deliveryTime != null) {
    	            shudiyunB2cOrderDTO.setDelivery_time(localDateTime.format(deliveryTime));
    	        }
    	        shudiyunB2cOrderDTO.setGoods_transaction_quantity(returnInstockQty);
    	        shudiyunB2cOrderDTO.setUnit(dmpReturnInstockDetailEntity.getUnit());
    	        shudiyunB2cOrderDTO.setGoods_benchmark_selling_price(dmpReturnInstockDetailEntity.getListPrice());

    	        shudiyunB2cOrderDTO.setTransaction_currency(dmpReturnInstockDetailEntity.getCurrencyName());
	            shudiyunB2cOrderDTO.setTransaction_currency_code(dmpReturnInstockDetailEntity.getCurrencyCode());

    	        shudiyunB2cOrderDTO.setMsku_code(dmpReturnInstockDetailEntity.getPlatformSkuNo());
    	        shudiyunB2cOrderDTO.setMsku_name(dmpReturnInstockDetailEntity.getPlatformSkuName());
    	        shudiyunB2cOrderDTO.setSku_code(skuNo);
    	        shudiyunB2cOrderDTO.setSku_name(skuName);

    	        shudiyunB2cOrderDTO.setSource_system("SDC");
    	        shudiyunB2cOrderDTO.setRoot_node_no_initial(platformOrderCode);
				List<DmpSoReturnInfoEntity> dmpSoReturnInfoEntities = soReturnInfoMap.get(dmpReturnInstockEntity.getThirdCode());
				if (CollectionUtils.isNotEmpty(dmpSoReturnInfoEntities)){
					// 旺店通退货入库取refund_no
					shudiyunB2cOrderDTO.setParent_node_no(dmpSoReturnInfoEntities.get(0).getPlatformCode());
				} else {
					shudiyunB2cOrderDTO.setParent_node_no(dmpReturnInstockEntity.getPlatformReturnInstockCode());
				}

    	        
    	        // 国家编码
    	        shudiyunB2cOrderDTO.setCountry_code(dmpReturnInstockDetailEntity.getCountryCode());
    	        // 国家名称
    	        shudiyunB2cOrderDTO.setCountry(dmpReturnInstockDetailEntity.getCountryName());
    	        // 区域编码
    	        shudiyunB2cOrderDTO.setRegion_code(dmpReturnInstockDetailEntity.getRegionCode());
    	        // 区域名称
    	        shudiyunB2cOrderDTO.setRegion_name(dmpReturnInstockDetailEntity.getRegionName());
    	        // 军区编码
    	        shudiyunB2cOrderDTO.setMilitary_region_code(dmpReturnInstockDetailEntity.getMilitaryRegionCode());
    	        // 军区名称
    	        shudiyunB2cOrderDTO.setMilitary_region_name(dmpReturnInstockDetailEntity.getMilitaryRegionName());

				String kingdeeDeptCode = "";
				String kingdeeDeptName = "";
				SysDepartmentEntity sysDepartmentEntity = deptList.stream()
						.filter(e -> e.getCode().equals(dmpReturnInstockDetailEntity.getDepartmentCode()))
						.findFirst()
						.orElse(null);
				if (null != sysDepartmentEntity){
					KingdeeDepartmentEntity kingdeeDepartmentEntity = kingdeeDeptList.stream()
							.filter(e -> e.getErpDeptId().equals(sysDepartmentEntity.getId()) && e.getUseOrgCode().equals(dmpReturnInstockEntity.getOrganizationCode()))
							.findFirst()
							.orElseThrow(() -> new ServiceException("金蝶部门编码不存在：部门Id=" + dmpReturnInstockDetailEntity.getDepartmentCode()));
					kingdeeDeptCode = kingdeeDepartmentEntity.getKingdeeDeptCode();
					kingdeeDeptName = kingdeeDepartmentEntity.getKingdeeDeptName();
				}

				// 部门编码
    	        shudiyunB2cOrderDTO.setDepartment_code(kingdeeDeptCode);
    	        // 部门名称
    	        shudiyunB2cOrderDTO.setDepartment_name(kingdeeDeptName);
    			
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
