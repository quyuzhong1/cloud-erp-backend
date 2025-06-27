package com.erp.server.dmp.inout.handler.output.task.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdDTO.CodeDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.enums.DmpReturnInfoStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.push.consumer.sdy.SdyDeliveryOrderConsumer;
import com.erp.server.dmp.service.DmpSoRefundInfoService;
import com.erp.server.dmp.service.ThirdMappingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdyReturnHandler extends DmpOutputSdyBaseTaskHandler {
    @Resource
    private ThirdMappingService thirdMappingService;
    @Resource
    private SdyDeliveryOrderConsumer sdyDeliveryOrderConsumer;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private DmpSoRefundInfoService dmpSoRefundInfoService;

    /**
     * 解析订单数据
     **/
    public Map<String, ShudiyunB2cOrderDTO> convert(DmpSoReturnInfoEntity dmpSoReturnEntity, List<DmpSoReturnDetailEntity> dmpSoReturnDetailEntityList , Map<String, Map<String, Object>> cacheMap , List<String> platformOrderCodeList) {
    	Map<String , ShudiyunB2cOrderDTO> result = new HashMap<>();
    	if(dmpSoReturnEntity == null) {
    		return result;
    	}
    	if (CollUtil.isEmpty(dmpSoReturnDetailEntityList)) {
            return result;
        }
        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (DmpSoReturnDetailEntity dmpSoReturnDetailEntity : dmpSoReturnDetailEntityList) {
            ShudiyunB2cOrderDTO sdyDTO = new ShudiyunB2cOrderDTO();
            sdyDTO.setBiz_uni_key(dmpSoReturnEntity.getId() + dmpSoReturnDetailEntity.getId());

            sdyDTO.setBiz_no(dmpSoReturnEntity.getPlatformCode());

            if (PlatformDictEnum.ALI_EXPRESS.getCode().equals(dmpSoReturnEntity.getSourceSystem())) {
                if (CharSequenceUtil.isNotBlank(dmpSoReturnDetailEntity.getPlatformDetailId())) {
                    sdyDTO.setBiz_no(dmpSoReturnDetailEntity.getPlatformDetailId());
                }
            }

            LocalDateTime returnTime = dmpSoReturnEntity.getReturnTime();
            if("WDT".equals(dmpSoReturnEntity.getSourceSystem()) && returnTime == null) {
            	returnTime = dmpSoReturnEntity.getPlatformCreateTime();
            }
			if (returnTime != null) {
                sdyDTO.setBiz_time(localDateTime.format(returnTime));
            } else {
                return result;
            }


            if ("refund".equals(dmpSoReturnDetailEntity.getSolutionType())) {
                //仅退款
                sdyDTO.setTransaction_type("RMA.退货单");
                sdyDTO.setTransaction_sub_type("退款不退货");
            } else if ("replacement".equals(dmpSoReturnDetailEntity.getSolutionType())) {
                //RMA.退换货
                sdyDTO.setTransaction_type("RMA.换货单");
                //换货退货
                sdyDTO.setTransaction_sub_type("换货退货");
            } else {
                //RMA.退货单
                sdyDTO.setTransaction_type("RMA.退货单");
                //退货退款
                sdyDTO.setTransaction_sub_type("退货退款");
            }
            
            if (PlatformDictEnum.SHOPIFY.getCode().equals(dmpSoReturnEntity.getSourceSystem())) {
            	String platformOrderCode = dmpSoReturnEntity.getPlatformOrderCode();
            	if(StringUtils.isNotBlank(platformOrderCode) && platformOrderCodeList.contains(platformOrderCode)) {
            		//RMA.退货单
                    sdyDTO.setTransaction_type("RMA.退货单");
                    //退货退款
                    sdyDTO.setTransaction_sub_type("退货退款");
            	}
            }

            if (CharSequenceUtil.isNotBlank(dmpSoReturnEntity.getStatus()) && CharSequenceUtil.isNotBlank(DmpReturnInfoStatusEnum.getName(Integer.valueOf(dmpSoReturnEntity.getStatus())))) {
                sdyDTO.setBiz_status(DmpReturnInfoStatusEnum.getName(Integer.valueOf(dmpSoReturnEntity.getStatus())));
            } else {
                sdyDTO.setBiz_status("已完成");
            }
            sdyDTO.setStatus("已创建");

            sdyDTO.setPrice(dmpSoReturnDetailEntity.getSellPrice());
            sdyDTO.setGoods_transaction_quantity(dmpSoReturnDetailEntity.getQty());
            sdyDTO.setGoods_transaction_amount(dmpSoReturnDetailEntity.getAmount());
            BigDecimal totalAmount = dmpSoReturnDetailEntity.getTotalAmount();
            if(totalAmount != null) {
            	sdyDTO.setTotal_goods_transaction_amount(totalAmount);
            }

            int qtyTotal = dmpSoReturnDetailEntityList.stream().filter(d -> d.getQty() != null).mapToInt(DmpSoReturnDetailEntity::getQty).sum();
            sdyDTO.setOnline_appled_return_quanty(qtyTotal);
            sdyDTO.setCustomer_refundable_quantity(qtyTotal);
            sdyDTO.setQuantity_buyer_returned(qtyTotal);
            if (PlatformDictEnum.SHOPIFY.getCode().equalsIgnoreCase(dmpSoReturnEntity.getSourceSystem())) {
                sdyDTO.setOnline_applied_amount(dmpSoReturnEntity.getAllAmount());
                sdyDTO.setOrder_seller_payed(dmpSoReturnEntity.getAllAmount());
            } else {
                BigDecimal amountTotal = dmpSoReturnDetailEntityList.stream().map(DmpSoReturnDetailEntity::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                sdyDTO.setOnline_applied_amount(amountTotal);
                sdyDTO.setOrder_seller_payed(amountTotal);
            }

            String shopId = "";
            String parentNodeNo = "";
            if (PlatformDictEnum.WDT.getCode().equalsIgnoreCase(dmpSoReturnEntity.getSourceSystem())) {
                parentNodeNo = dmpSoReturnEntity.getPlatformCode();
                //RMA.退货单
            	if ("2".equals(dmpSoReturnDetailEntity.getReturnOriginalType())) {
                    sdyDTO.setTransaction_type("RMA.退货单");
                    sdyDTO.setTransaction_sub_type("退款退货");
                } else if ("3".equals(dmpSoReturnDetailEntity.getReturnOriginalType())) {
                    sdyDTO.setTransaction_type("RMA.换货单");
                    sdyDTO.setTransaction_sub_type("换货退货");
                } else if ("4".equals(dmpSoReturnDetailEntity.getReturnOriginalType())) {
                    sdyDTO.setTransaction_type("RMA.退货单");
                    sdyDTO.setTransaction_sub_type("退款不退货");
                } else if ("6".equals(dmpSoReturnDetailEntity.getReturnOriginalType())) {
                    sdyDTO.setTransaction_type("RMA.退货单");
                    sdyDTO.setTransaction_sub_type("小额退款");
                } else {
                    return result;
                }
            	
            	if(StringUtils.isNotBlank(dmpSoReturnEntity.getPlatformCode())) {
            		sdyDTO.setBiz_no(dmpSoReturnEntity.getPlatformCode());
            	}else {
            		sdyDTO.setBiz_no(dmpSoReturnEntity.getPlatformOrderCode());
            	}
                
                Map<String, Object> shopListMap = cacheMap.get("shopList");
                if(shopListMap == null) {
                	shopListMap = new HashMap<>();
                }
                
                String thirdInfoId = dmpSoReturnEntity.getShopId();
                Object shopListObject = shopListMap.get(thirdInfoId);
                List<ThirdMappingEntity> shopList = null;
                if(shopListObject == null) {
                	//查询旺店通对应系统店铺
                	shopList = thirdMappingService.lambdaQuery()
                            .eq(ThirdMappingEntity::getType, ThirdSysTypeEnum.SHOP.getCode())
                            .eq(ThirdMappingEntity::getThirdSysType, PlatformDictEnum.WDT.getCode())
                            .eq(ThirdMappingEntity::getThirdId, thirdInfoId)
                            .list();
                }else {
                	shopList = (List<ThirdMappingEntity>)shopListObject;
                }
                shopListMap.put(thirdInfoId, shopList);
                cacheMap.put("shopList", shopListMap);
                
                if (CollectionUtils.isNotEmpty(shopList)) {
                	shopId = shopList.get(0).getSysId();
                }

            } else {
                if (CharSequenceUtil.isNotBlank(dmpSoReturnEntity.getNextLevelId())) {
                    shopId = dmpSoReturnEntity.getNextLevelId();
                } else {
                    shopId = dmpSoReturnEntity.getShopId();
                }
            }
            //退换单号
            sdyDTO.setParent_node_no(parentNodeNo);
            
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
            if (CharSequenceUtil.isNotBlank(dmpSoReturnEntity.getCurrencyCode())) {
            	tradeCurrency = dmpSoReturnEntity.getCurrencyCode();
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
            if (StringUtils.isNotBlank(dmpSoReturnEntity.getPlatformOrderCode())){
                sdyDTO.setRoot_node_no(dmpSoReturnEntity.getPlatformOrderCode());
            } else {
                sdyDTO.setRoot_node_no(dmpSoReturnEntity.getPlatformCode());
            }

            if (returnTime != null) {
                sdyDTO.setRoot_node_create_time(localDateTime.format(returnTime));
            }
            sdyDTO.setRoot_node_modify_time(localDateTime.format(dmpSoReturnEntity.getPlatformUpdateTime()));

            if("10".equals(dmpSoReturnEntity.getStatus())) {
            	sdyDTO.setGoods_status("已取消");
        	}else {
        		sdyDTO.setGoods_status("已退货");
        	}
            sdyDTO.setMsku_code(dmpSoReturnDetailEntity.getSkuNo());
            if (CharSequenceUtil.isBlank(dmpSoReturnDetailEntity.getSkuName())) {
                sdyDTO.setMsku_name(dmpSoReturnDetailEntity.getSkuNo());
            } else {
                sdyDTO.setMsku_name(dmpSoReturnDetailEntity.getSkuName());
            }
            sdyDTO.setReason(dmpSoReturnDetailEntity.getReason());
            sdyDTO.setSource_system("SDC");
            if (StringUtils.isNotBlank(dmpSoReturnEntity.getPlatformOrderCode())){
                sdyDTO.setRoot_node_no_initial(dmpSoReturnEntity.getPlatformOrderCode());
            } else {
                sdyDTO.setRoot_node_no_initial(dmpSoReturnEntity.getPlatformCode());
            }
            result.put(dmpSoReturnDetailEntity.getId(), sdyDTO);
        }

        return result;

    }

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoReturnInfoEntity> dmpSoReturnInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSoReturnDetailEntity>> dmpSoReturnDetailEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_return_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoReturnInfoEntity dmpSoInfoEntity = (DmpSoReturnInfoEntity) v;
                        dmpSoReturnInfoEntityMap.put(dmpSoInfoEntity.getId(), dmpSoInfoEntity);
                    }
                } else if ("dmp_so_return_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoReturnDetailEntity dmpSoReturnDetailEntity = (DmpSoReturnDetailEntity) v;
                        String mainId = dmpSoReturnDetailEntity.getMainId();
                        List<DmpSoReturnDetailEntity> list = dmpSoReturnDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpSoReturnDetailEntity);
                        dmpSoReturnDetailEntityMap.put(mainId, list);
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
                if ("dmp_so_return_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_so_return_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoReturnDetailEntity dmpSoReturnDetailEntity = (DmpSoReturnDetailEntity) v;
                        changeIds.add(dmpSoReturnDetailEntity.getMainId());
                    }
                }
            }
        }

        List<String> platformOrderCodeList = new ArrayList<>();
        List<DmpSoReturnInfoEntity> changeDmpSoReturnInfoList = dmpSoReturnInfoEntityMap.values().stream().filter(d -> changeIds.contains(d.getId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(changeDmpSoReturnInfoList)) {
        	String sourceSystem = changeDmpSoReturnInfoList.get(0).getSourceSystem();
        	if(PlatformDictEnum.SHOPIFY.getCode().equalsIgnoreCase(sourceSystem)) {
        		platformOrderCodeList = changeDmpSoReturnInfoList.stream().filter(d -> StringUtils.isNotBlank(d.getPlatformOrderCode())).map(DmpSoReturnInfoEntity::getPlatformOrderCode).collect(Collectors.toList());
                if(CollUtil.isNotEmpty(platformOrderCodeList)) {
    				platformOrderCodeList = dmpSoRefundInfoService.lambdaQuery().eq(DmpSoRefundInfoEntity::getSourceSystem, sourceSystem)
                		.in(DmpSoRefundInfoEntity::getPlatformOrderCode, platformOrderCodeList)
                		.select(DmpSoRefundInfoEntity::getPlatformOrderCode)
                		.list().stream().map(DmpSoRefundInfoEntity::getPlatformOrderCode).collect(Collectors.toList());
                }
        	}
        }
        Map<String, String> map = new HashMap<>();
        Map<String, Map<String, Object>> cacheMap = new HashMap<>();
        for(String changId : changeIds) {
        	Map<String, ShudiyunB2cOrderDTO> result = this.convert(dmpSoReturnInfoEntityMap.get(changId), dmpSoReturnDetailEntityMap.get(changId) , cacheMap , platformOrderCodeList);
        	if(!result.isEmpty()) {
            	for(Map.Entry<String, ShudiyunB2cOrderDTO> r : result.entrySet()) {
            		map.put(r.getKey(), JSON.toJSONString(r.getValue()));
            	}
            }
        }

        return map;
    }
}
