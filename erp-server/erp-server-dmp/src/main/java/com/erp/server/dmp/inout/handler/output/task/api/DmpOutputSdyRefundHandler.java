package com.erp.server.dmp.inout.handler.output.task.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdDTO.CodeDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.entity.DmpSoRefundDetailEntity;
import com.erp.model.dmp.entity.DmpSoRefundInfoEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdShopEntity;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.enums.DmpRefundInfoStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.push.consumer.sdy.SdyDeliveryOrderConsumer;
import com.erp.server.dmp.service.DmpSoReturnInfoService;
import com.erp.server.dmp.service.ThirdMappingService;
import com.erp.server.dmp.service.ThirdShopService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdyRefundHandler extends DmpOutputSdyBaseTaskHandler {
    @Resource
    private ThirdMappingService thirdMappingService;
    @Resource
    private SdyDeliveryOrderConsumer sdyDeliveryOrderConsumer;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private ThirdShopService thirdShopService;
    @Resource
    private DmpSoReturnInfoService dmpSoReturnInfoService;

    /**
     * 解析订单数据
     **/
    public Map<String, ShudiyunB2cOrderDTO> convert(DmpSoRefundInfoEntity dmpSoRefundEntity, List<DmpSoRefundDetailEntity> dmpSoRefundDetailEntityList , Map<String, Map<String, Object>> cacheMap) {
    	Map<String , ShudiyunB2cOrderDTO> result = new HashMap<>();
    	if (CollUtil.isEmpty(dmpSoRefundDetailEntityList) || dmpSoRefundEntity == null) {
            return result;
        }
        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (DmpSoRefundDetailEntity dmpSoRefundDetailEntity : dmpSoRefundDetailEntityList) {
        	if(dmpSoRefundDetailEntity == null) {
        		continue;
        	}
            ShudiyunB2cOrderDTO sdyDTO = new ShudiyunB2cOrderDTO();
            sdyDTO.setBiz_uni_key(dmpSoRefundEntity.getId() + dmpSoRefundDetailEntity.getId());
            sdyDTO.setBiz_no(dmpSoRefundEntity.getPlatformCode());
            sdyDTO.setBiz_time(localDateTime.format(dmpSoRefundEntity.getRefundTime()));

            //仅退款
            sdyDTO.setTransaction_type("RMA.退货单");
            sdyDTO.setTransaction_sub_type("退款不退货");

            if (CharSequenceUtil.isNotBlank(dmpSoRefundEntity.getStatus()) && CharSequenceUtil.isNotBlank(DmpRefundInfoStatusEnum.getName(dmpSoRefundEntity.getStatus()))) {
                sdyDTO.setBiz_status(DmpRefundInfoStatusEnum.getName(dmpSoRefundEntity.getStatus()));
            } else {
                sdyDTO.setBiz_status("成功");
            }
            sdyDTO.setStatus("已创建");

            int qtyTotal = dmpSoRefundDetailEntityList.stream().mapToInt(DmpSoRefundDetailEntity::getQty).sum();
            sdyDTO.setOnline_appled_return_quanty(qtyTotal);
            sdyDTO.setCustomer_refundable_quantity(qtyTotal);
            sdyDTO.setQuantity_buyer_returned(qtyTotal);
            if (PlatformDictEnum.SHOPIFY.getCode().equalsIgnoreCase(dmpSoRefundEntity.getSourceSystem())) {
                sdyDTO.setOnline_applied_amount(dmpSoRefundEntity.getAmount());
                sdyDTO.setOrder_seller_payed(dmpSoRefundEntity.getAmount());
            } else {
                BigDecimal amountTotal = dmpSoRefundDetailEntityList.stream().filter(req -> req.getAmount() != null).map(DmpSoRefundDetailEntity::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                sdyDTO.setOnline_applied_amount(amountTotal);
                sdyDTO.setOrder_seller_payed(amountTotal);
            }

            if (dmpSoRefundDetailEntity.getAmount() != null && dmpSoRefundDetailEntity.getQty() != null && dmpSoRefundDetailEntity.getQty() != 0) {
                sdyDTO.setPrice(dmpSoRefundDetailEntity.getAmount().divide(MathUtil.valueOf(dmpSoRefundDetailEntity.getQty()), 2, RoundingMode.DOWN));
            } else {
                sdyDTO.setPrice(dmpSoRefundDetailEntity.getAmount());
            }

            sdyDTO.setGoods_transaction_quantity(dmpSoRefundDetailEntity.getQty());
            sdyDTO.setGoods_transaction_amount(dmpSoRefundDetailEntity.getAmount());

            String shopId = "";
            if (PlatformDictEnum.WDT.getCode().equalsIgnoreCase(dmpSoRefundEntity.getSourceSystem())) {
            	Map<String, Object> thirdShopEntityListMap = cacheMap.get("thirdShopEntityList");
                if(thirdShopEntityListMap == null) {
                	thirdShopEntityListMap = new HashMap<>();
                }
                
                String shopNo = dmpSoRefundEntity.getShopId();
                Object thirdShopEntityListObject = thirdShopEntityListMap.get(shopNo);
                List<ThirdShopEntity> thirdShopEntityList = null;
                if(thirdShopEntityListObject == null) {
                	//查询旺店通对应系统店铺
                    thirdShopEntityList = thirdShopService.lambdaQuery()
                            .eq(ThirdShopEntity::getCode, shopNo)
                            .eq(ThirdShopEntity::getSysType, PlatformDictEnum.WDT.getCode())
                            .list();
                }else {
                	thirdShopEntityList = (List<ThirdShopEntity>) thirdShopEntityListObject;
                }
                thirdShopEntityListMap.put(shopNo, thirdShopEntityList);
                cacheMap.put("thirdShopEntityList", thirdShopEntityListMap);
                
                if (CollUtil.isNotEmpty(thirdShopEntityList)) {
                	Map<String, Object> shopListMap = cacheMap.get("shopList");
                    if(shopListMap == null) {
                    	shopListMap = new HashMap<>();
                    }
                    
                    String thirdInfoId = thirdShopEntityList.get(0).getId();
                    Object shopListObject = shopListMap.get(thirdInfoId);
                    List<ThirdMappingEntity> shopList = null;
                    if(shopListObject == null) {
                    	//查询旺店通对应系统店铺
                        shopList = thirdMappingService.lambdaQuery()
                                .eq(ThirdMappingEntity::getType, ThirdSysTypeEnum.SHOP.getCode())
                                .eq(ThirdMappingEntity::getThirdSysType, PlatformDictEnum.WDT.getCode())
                                .eq(ThirdMappingEntity::getThirdInfoId, thirdInfoId)
                                .list();
                    }else {
                    	shopList = (List<ThirdMappingEntity>)shopListObject;
                    }
                    shopListMap.put(thirdInfoId, shopList);
                    cacheMap.put("shopList", shopListMap);
                    if (CollectionUtils.isNotEmpty(shopList)) {
                    	shopId = shopList.get(0).getSysId();
                    }
                }
            } else {
                if (CharSequenceUtil.isNotBlank(dmpSoRefundEntity.getNextLevelId())) {
                    shopId = dmpSoRefundEntity.getNextLevelId();
                } else {
                    shopId = dmpSoRefundEntity.getShopId();
                }
            }
            
            Map<String, Object> shopInfoMap = cacheMap.get("shopInfo");
            if(shopInfoMap == null) {
            	shopInfoMap = new HashMap<>();
            }
            Object shopInfObject = shopInfoMap.get(shopId);
            ShopInfoEntity shopInfo = null;
            if(shopInfObject == null) {
            	shopInfo = FeignQuery.getById(ShopInfoEntity.class, shopId);
            }else {
            	shopInfo = (ShopInfoEntity)shopInfObject; 
            }
            shopInfoMap.put(shopId, shopInfo);
            cacheMap.put("shopInfo", shopInfoMap);
            
            Map<String, Object> customerInfoMap = cacheMap.get("customerInfo");
            if(customerInfoMap == null) {
            	customerInfoMap = new HashMap<>();
            }
            String customerId = shopInfo.getCustomerId();
            Object customerInfobject = customerInfoMap.get(customerId);
            CustomerInfoEntity customerInfo = null;
            if(customerInfobject == null) {
            	customerInfo = FeignQuery.getById(CustomerInfoEntity.class, customerId);
            }else {
            	customerInfo = (CustomerInfoEntity)customerInfobject; 
            }
            customerInfoMap.put(customerId, customerInfo);
            cacheMap.put("customerInfo", customerInfoMap);
            
            if (ObjectUtil.isNotEmpty(customerInfo)) {
                //组织编码
            	String salesOrgId = shopInfo.getSalesOrgId();
                String financialOrganization = customerInfo.getFinancialOrganization();

				Map<String, Object> companyEntityMap = cacheMap.get("companyEntity");
                if(companyEntityMap == null) {
                	companyEntityMap = new HashMap<>();
                }
                BaseIdDTO.CodeDTO salesOrg = null;
                Object salesOrgIdObject = companyEntityMap.get(salesOrgId);
                if(salesOrgIdObject == null) {
                	List<CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(salesOrgId));
                	if(CollUtil.isNotEmpty(accountingCompanyList)) {
                		salesOrg = accountingCompanyList.get(0);
                	}
                }else {
                	salesOrg = (BaseIdDTO.CodeDTO)salesOrgIdObject;
                }
                companyEntityMap.put(salesOrgId, salesOrg);
                
                BaseIdDTO.CodeDTO sysAccountingCompanyEntity = null;
                Object sysAccountingCompanyEntityObject = companyEntityMap.get(financialOrganization);
                if(sysAccountingCompanyEntityObject == null) {
                	List<CodeDTO> sysAccountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(financialOrganization));
                	if(CollUtil.isNotEmpty(sysAccountingCompanyList)) {
                		sysAccountingCompanyEntity = sysAccountingCompanyList.get(0);
                	}
                }else {
                	sysAccountingCompanyEntity = (BaseIdDTO.CodeDTO)sysAccountingCompanyEntityObject;
                }
                companyEntityMap.put(financialOrganization, sysAccountingCompanyEntity);
                cacheMap.put("companyEntity", companyEntityMap);
                
                //销售组织
                sdyDTO.setSales_company_code(salesOrg.getCode());

                if (ObjectUtil.isNotEmpty(sysAccountingCompanyEntity)) {
                    sdyDTO.setReceiving_company_code(sysAccountingCompanyEntity.getCode());
                    sdyDTO.setOrganization_code(sysAccountingCompanyEntity.getCode());
                    sdyDTO.setOrganization_name(sysAccountingCompanyEntity.getName());
                }
                sdyDTO.setShop_no(customerInfo.getCode());
                sdyDTO.setShop_name(customerInfo.getName());

                String subPlatformType = customerInfo.getPlatformType();
                if(StringUtils.isNotBlank(subPlatformType)) {
                	Map<String, Object> subPlatformTypeMap = cacheMap.get("subPlatformType");
                    if(subPlatformTypeMap == null) {
                    	subPlatformTypeMap = new HashMap<>();
                    }
                    Object subPlatformTypebject = subPlatformTypeMap.get(subPlatformType);
                    com.erp.model.oms.entity.DictBasicEntity subPlatformTypeDict = null;
                    if(subPlatformTypebject == null) {
                    	List<com.erp.model.oms.entity.DictBasicEntity> dictList = FeignQuery.create(com.erp.model.oms.entity.DictBasicEntity.class).eq(com.erp.model.oms.entity.DictBasicEntity::getType, "sdySubPlatform").eq(DictBasicEntity::getName, subPlatformType).list();
                    	if(CollUtil.isNotEmpty(dictList)) {
                    		subPlatformTypeDict = dictList.get(0);
                    	}
                    }else {
                    	subPlatformTypeDict = (com.erp.model.oms.entity.DictBasicEntity)subPlatformTypebject; 
                    }
                    subPlatformTypeMap.put(subPlatformType, subPlatformTypeDict);
                    cacheMap.put("subPlatformType", subPlatformTypeMap);
                    
                    if(subPlatformTypeDict != null) {
                    	sdyDTO.setPlatform_id(subPlatformTypeDict.getRemark());
                        sdyDTO.setPlatform_name(subPlatformTypeDict.getRemark());
                        sdyDTO.setSubplatform_no(subPlatformTypeDict.getValue());
                        sdyDTO.setSubplatform_name(subPlatformTypeDict.getValue());
                    }
                }
            }
            
            Map<String, Object> dictCurrencyMap = cacheMap.get("dictCurrency");
            if(dictCurrencyMap == null) {
            	dictCurrencyMap = new HashMap<>();
            }
            String tradeCurrency = shopInfo.getTradeCurrency();
            if (CharSequenceUtil.isNotBlank(dmpSoRefundEntity.getCurrencyCode())) {
            	tradeCurrency = dmpSoRefundEntity.getCurrencyCode();
            }
            Object dictCurrencybject = dictCurrencyMap.get(tradeCurrency);
            DictCurrencyEntity dictCurrency = null;
            if(dictCurrencybject == null) {
            	dictCurrency = FeignQuery.getById(DictCurrencyEntity.class, tradeCurrency);
            }else {
            	dictCurrency = (DictCurrencyEntity)dictCurrencybject; 
            }
            dictCurrencyMap.put(tradeCurrency, dictCurrency);
            cacheMap.put("dictCurrency", dictCurrencyMap);
            
            if (ObjectUtil.isNotEmpty(dictCurrency)) {
                sdyDTO.setTransaction_currency(dictCurrency.getName());
            }
            sdyDTO.setTransaction_currency_code(tradeCurrency);
            sdyDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());

            sdyDTO.setUnit("PCS");
            sdyDTO.setRoot_node_create_time(localDateTime.format(dmpSoRefundEntity.getRefundTime()));
            sdyDTO.setRoot_node_modify_time(localDateTime.format(dmpSoRefundEntity.getPlatformUpdateTime()));
            sdyDTO.setGoods_status("已退货");
            sdyDTO.setMsku_code(dmpSoRefundDetailEntity.getSkuNo());
            if (CharSequenceUtil.isBlank(dmpSoRefundDetailEntity.getSkuName())) {
                sdyDTO.setMsku_name(dmpSoRefundDetailEntity.getSkuNo());
            } else {
                sdyDTO.setMsku_name(dmpSoRefundDetailEntity.getSkuName());
            }
            sdyDTO.setReason(dmpSoRefundEntity.getReason());
            sdyDTO.setSource_system("SDC");
            // 目标
            String targetPlatformCode = dmpSoRefundEntity.getThirdCode();
            if (StringUtils.isNotBlank(dmpSoRefundEntity.getPlatformOrderCode())){
                targetPlatformCode = dmpSoRefundEntity.getPlatformOrderCode();
            }
            sdyDTO.setRoot_node_no(targetPlatformCode);
            sdyDTO.setRoot_node_no_initial(targetPlatformCode);
            result.put(dmpSoRefundDetailEntity.getId(), sdyDTO);
        }

        return result;

    }

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoRefundInfoEntity> dmpSoRefundInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSoRefundDetailEntity>> dmpSoRefundDetailEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_refund_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoRefundInfoEntity dmpSoInfoEntity = (DmpSoRefundInfoEntity) v;
                        dmpSoRefundInfoEntityMap.put(dmpSoInfoEntity.getId(), dmpSoInfoEntity);
                    }
                } else if ("dmp_so_refund_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoRefundDetailEntity dmpSoRefundDetailEntity = (DmpSoRefundDetailEntity) v;
                        String mainId = dmpSoRefundDetailEntity.getMainId();
                        List<DmpSoRefundDetailEntity> list = dmpSoRefundDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpSoRefundDetailEntity);
                        dmpSoRefundDetailEntityMap.put(mainId, list);
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
                if ("dmp_so_refund_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_so_refund_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoRefundDetailEntity dmpSoRefundDetailEntity = (DmpSoRefundDetailEntity) v;
                        changeIds.add(dmpSoRefundDetailEntity.getMainId());
                    }
                }
            }
        }

        List<DmpSoRefundInfoEntity> changeDmpSoRefundInfoList = dmpSoRefundInfoEntityMap.values().stream().filter(d -> changeIds.contains(d.getId())).collect(Collectors.toList());
        List<String> platformOrderCodeList = changeDmpSoRefundInfoList.stream().filter(d -> StringUtils.isNotBlank(d.getPlatformOrderCode())).map(DmpSoRefundInfoEntity::getPlatformOrderCode).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(platformOrderCodeList)) {
        	platformOrderCodeList = dmpSoReturnInfoService.lambdaQuery().eq(DmpSoReturnInfoEntity::getSourceSystem, changeDmpSoRefundInfoList.get(0).getSourceSystem())
        		.in(DmpSoReturnInfoEntity::getPlatformOrderCode, platformOrderCodeList)
        		.select(DmpSoReturnInfoEntity::getPlatformOrderCode)
        		.list().stream().map(DmpSoReturnInfoEntity::getPlatformOrderCode).collect(Collectors.toList());
        }
        
        Map<String, String> map = new HashMap<>();
        Map<String, Map<String, Object>> cacheMap = new HashMap<>();
        for (String changId : changeIds) {
            DmpSoRefundInfoEntity dmpSoRefundEntity = dmpSoRefundInfoEntityMap.get(changId);
            if(dmpSoRefundEntity == null) {
            	continue;
            }
            String platformOrderCode = dmpSoRefundEntity.getPlatformOrderCode();
            if(StringUtils.isNotBlank(platformOrderCode) && platformOrderCodeList.contains(platformOrderCode)) {
            	continue;
            }
			List<DmpSoRefundDetailEntity> dmpSoRefundDetailEntityList = dmpSoRefundDetailEntityMap.get(changId);
			if(CollUtil.isEmpty(dmpSoRefundDetailEntityList)) {
				DmpSoRefundDetailEntity dmpSoRefundDetailEntity = new DmpSoRefundDetailEntity();
				dmpSoRefundDetailEntity.setId(dmpSoRefundEntity.getId());
				dmpSoRefundDetailEntity.setQty(0);
				dmpSoRefundDetailEntityList = Arrays.asList(dmpSoRefundDetailEntity);
			}
			Map<String, ShudiyunB2cOrderDTO> result = this.convert(dmpSoRefundEntity, dmpSoRefundDetailEntityList , cacheMap);
            if(!result.isEmpty()) {
            	for(Map.Entry<String, ShudiyunB2cOrderDTO> r : result.entrySet()) {
            		map.put(r.getKey(), JSON.toJSONString(r.getValue()));
            	}
            }
        }
        return map;
    }
}
