package com.erp.server.dmp.inout.handler.output.task.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
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
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.dmp.enums.WdtSourcePlatformEnum;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.push.consumer.sdy.SdyDeliveryOrderConsumer;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.DmpSoInfoService;
import com.erp.server.dmp.service.DmpSoOutstockDetailService;
import com.erp.server.dmp.service.ThirdMappingService;
import com.erp.server.dmp.service.ThirdShopService;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 旺店通原始订单推送数帝云
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdyWdtOriginalOrderHandler extends DmpOutputSdyBaseTaskHandler {
    @Resource
    private ThirdMappingService thirdMappingService;
    @Resource
    private SdyDeliveryOrderConsumer sdyDeliveryOrderConsumer;
    @Resource
    private DmpSoDetailService dmpSoDetailService;
    @Resource
    private DmpSoInfoService dmpSoInfoService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private ThirdShopService thirdShopService;
    @Resource
    private DmpSoOutstockDetailService dmpSoOutstockDetailService;

    /**
     * 解析订单数据
     **/
    public Map<String, ShudiyunB2cOrderDTO> convert(DmpSoOriginalInfoEntity dmpSoInfoEntity, List<DmpSoOriginalDetailEntity> dmpSoDetailEntityList , Map<String, Map<String, Object>> cacheMap) {
    	Map<String , ShudiyunB2cOrderDTO> result = new HashMap<>();
    	if (CollUtil.isEmpty(dmpSoDetailEntityList)) {
    		return result;
        }

    	if (!"1".equals(dmpSoInfoEntity.getPayStatus()) && !"2".equals(dmpSoInfoEntity.getPayStatus())) {
            return result;
        }
        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Map<String, Object> wdtSoDetailMap = cacheMap.get("wdtSoDetail");
        //总售价 = 明细的单价 * 数量 汇总
        BigDecimal allAmount = dmpSoDetailEntityList.stream().map(req -> req.getPrice().multiply(MathUtil.valueOf(req.getNum()))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        //订单详情
        for (int i = 0; i < dmpSoDetailEntityList.size(); i++) {
            DmpSoOriginalDetailEntity dmpSoDetailEntity = dmpSoDetailEntityList.get(i);

            ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();
            shudiyunB2cOrderDTO.setBiz_uni_key(dmpSoInfoEntity.getId() + dmpSoDetailEntity.getId());
            shudiyunB2cOrderDTO.setBiz_no(dmpSoInfoEntity.getPlatformCode());
            if (dmpSoInfoEntity.getPayTime() != null) {
                shudiyunB2cOrderDTO.setRoot_node_create_time(localDateTime.format(dmpSoInfoEntity.getPayTime()));
            } else {
            	return result;
            }

            //线上原始订单
            shudiyunB2cOrderDTO.setTransaction_type("线上订单");
            shudiyunB2cOrderDTO.setStatus("已创建");
            shudiyunB2cOrderDTO.setTransaction_sub_type(OrderSubTypeEnum.ONLINE_ORDER.getName());
            shudiyunB2cOrderDTO.setBiz_status(wdtStatusHandler(dmpSoInfoEntity.getTradeStatus()));
            if (dmpSoInfoEntity.getPayTime() != null) {
                shudiyunB2cOrderDTO.setBiz_time(localDateTime.format(dmpSoInfoEntity.getPayTime()));
            } else {
                shudiyunB2cOrderDTO.setBiz_time(localDateTime.format(dmpSoInfoEntity.getPlatformCreateTime()));
            }

            shudiyunB2cOrderDTO.setTotal_goods_transaction_amount(allAmount);
            //总优惠金额
            shudiyunB2cOrderDTO.setDiscount_deduction_amount(dmpSoInfoEntity.getDiscount());

            BigDecimal totalQty = dmpSoDetailEntityList.stream().map(DmpSoOriginalDetailEntity::getNum).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            shudiyunB2cOrderDTO.setTotal_goods_quantity(totalQty.intValue());

            //取消金额、数量
            shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(dmpSoInfoEntity.getRefundAmount());

            boolean haveSoOutstock = false;
            Object wdtSoDetail = wdtSoDetailMap.get(dmpSoDetailEntity.getPlatformDetailId());
            // 取消商品数量（合计）
            if (wdtSoDetail != null) {
            	Pair<Boolean, BigDecimal> p = (Pair<Boolean, BigDecimal>)wdtSoDetail;
            	Boolean key = p.getKey();
            	if(key != null) {
            		haveSoOutstock = key.booleanValue();
            	}
            	BigDecimal value = p.getValue();
            	if(value == null) {
            		value = BigDecimal.ZERO;
            	}
                shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(value.intValue());
            } else {
                shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(0);
            }
            shudiyunB2cOrderDTO.setOrder_quantity_to_be_shipped(totalQty.intValue() - shudiyunB2cOrderDTO.getTotal_canceled_goods_quantity());

            shudiyunB2cOrderDTO.setBuyer_actual_payment(dmpSoInfoEntity.getPaid());

            shudiyunB2cOrderDTO.setTotal_freight(BigDecimal.ZERO);

            Map<String, Object> thirdShopEntityListMap = cacheMap.get("thirdShopEntityList");
            if(thirdShopEntityListMap == null) {
            	thirdShopEntityListMap = new HashMap<>();
            }
            
            String shopNo = dmpSoInfoEntity.getShopNo();
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
                	Map<String, Object> shopInfoMap = cacheMap.get("shopInfo");
                    if(shopInfoMap == null) {
                    	shopInfoMap = new HashMap<>();
                    }
                    String sysId = shopList.get(0).getSysId();
                    Object shopInfObject = shopInfoMap.get(sysId);
                    ShopInfoEntity shopInfo = null;
                    if(shopInfObject == null) {
                    	shopInfo = FeignQuery.getById(ShopInfoEntity.class, sysId);
                    }else {
                    	shopInfo = (ShopInfoEntity)shopInfObject; 
                    }
                    shopInfoMap.put(sysId, shopInfo);
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
                        shudiyunB2cOrderDTO.setSales_company_code(salesOrg.getCode());

                        if (ObjectUtil.isNotEmpty(sysAccountingCompanyEntity)) {
                            shudiyunB2cOrderDTO.setReceiving_company_code(sysAccountingCompanyEntity.getCode());
                            shudiyunB2cOrderDTO.setOrganization_code(sysAccountingCompanyEntity.getCode());
                            shudiyunB2cOrderDTO.setOrganization_name(sysAccountingCompanyEntity.getName());
                        }
                        shudiyunB2cOrderDTO.setShop_no(customerInfo.getCode());
                        shudiyunB2cOrderDTO.setShop_name(customerInfo.getName());

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
                            	shudiyunB2cOrderDTO.setPlatform_id(subPlatformTypeDict.getRemark());
                            	shudiyunB2cOrderDTO.setPlatform_name(subPlatformTypeDict.getRemark());
                                shudiyunB2cOrderDTO.setSubplatform_no(subPlatformTypeDict.getValue());
                                shudiyunB2cOrderDTO.setSubplatform_name(subPlatformTypeDict.getValue());
                            }
                        }
                    }
                    
                    Map<String, Object> dictCurrencyMap = cacheMap.get("dictCurrency");
                    if(dictCurrencyMap == null) {
                    	dictCurrencyMap = new HashMap<>();
                    }
                    String tradeCurrency = shopInfo.getTradeCurrency();
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
                        shudiyunB2cOrderDTO.setTransaction_currency(dictCurrency.getName());
                    }
                    shudiyunB2cOrderDTO.setTransaction_currency_code(shopInfo.getTradeCurrency());
                    shudiyunB2cOrderDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());
                }
            }

            shudiyunB2cOrderDTO.setRoot_node_no(dmpSoInfoEntity.getPlatformCode());
            shudiyunB2cOrderDTO.setRoot_node_modify_time(localDateTime.format(dmpSoInfoEntity.getPlatformUpdateTime()));
            if (dmpSoDetailEntity.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                shudiyunB2cOrderDTO.setIs_gift(1);
            } else {
                shudiyunB2cOrderDTO.setIs_gift(0);
            }

            shudiyunB2cOrderDTO.setRemark("");

            if(haveSoOutstock) {
            	shudiyunB2cOrderDTO.setGoods_status("已发货");
            }else {
            	shudiyunB2cOrderDTO.setGoods_status(wdtItemStatus(dmpSoDetailEntity.getStatus()));
            }

            shudiyunB2cOrderDTO.setGoods_transaction_quantity(dmpSoDetailEntity.getNum().intValue());
            shudiyunB2cOrderDTO.setUnit("PCS");

            shudiyunB2cOrderDTO.setPrice(dmpSoDetailEntity.getPrice());
            shudiyunB2cOrderDTO.setGoods_transaction_amount(MathUtil.multiplyWithTwo(dmpSoDetailEntity.getPrice(), dmpSoDetailEntity.getNum()));

            shudiyunB2cOrderDTO.setPost_amount(BigDecimal.ZERO);
            if (CharSequenceUtil.isBlank(dmpSoDetailEntity.getGoodsNo())) {
//                continue;
            }
            shudiyunB2cOrderDTO.setMsku_code(dmpSoDetailEntity.getGoodsNo());
            shudiyunB2cOrderDTO.setMsku_name(dmpSoDetailEntity.getGoodsName());
            shudiyunB2cOrderDTO.setSku_code("");
            shudiyunB2cOrderDTO.setSku_name("");

            shudiyunB2cOrderDTO.setSource_system("SDC");
            shudiyunB2cOrderDTO.setRoot_node_no_initial(dmpSoInfoEntity.getPlatformCode());

            result.put(dmpSoDetailEntity.getId(), shudiyunB2cOrderDTO);

        }
        return result;

    }

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoOriginalInfoEntity> dmpSoOriginalInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSoOriginalDetailEntity>> dmpSoOriginalDetailEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_original_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoOriginalInfoEntity dmpSoOriginalInfoEntity = (DmpSoOriginalInfoEntity) v;
                        dmpSoOriginalInfoEntityMap.put(dmpSoOriginalInfoEntity.getId(), dmpSoOriginalInfoEntity);
                    }
                } else if ("dmp_so_original_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoOriginalDetailEntity dmpSoOriginalDetailEntity = (DmpSoOriginalDetailEntity) v;
                        String mainId = dmpSoOriginalDetailEntity.getMainId();
                        List<DmpSoOriginalDetailEntity> list = dmpSoOriginalDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpSoOriginalDetailEntity);
                        dmpSoOriginalDetailEntityMap.put(mainId, list);
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
                if ("dmp_so_original_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_so_original_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoOriginalDetailEntity DmpSoDetailEntity = (DmpSoOriginalDetailEntity) v;
                        changeIds.add(DmpSoDetailEntity.getMainId());
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        Map<String, Map<String, Object>> cacheMap = new HashMap<>();
        Map<String, Object> wdtSoDetail = new HashMap<>();
        
        List<String> platformCodeList = dmpSoOriginalInfoEntityMap.values().stream().filter(d -> StringUtils.isNotBlank(d.getPlatformCode())).map(DmpSoOriginalInfoEntity::getPlatformCode).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(platformCodeList)) {
        	List<DmpSoInfoEntity> dmpSoInfoEntityList = dmpSoInfoService.lambdaQuery().eq(DmpSoInfoEntity::getSourceSystem, PlatformDictEnum.WDT.getCode())
            		.in(DmpSoInfoEntity::getPlatformCode, platformCodeList).list();
            if(CollUtil.isNotEmpty(dmpSoInfoEntityList)) {
            	List<DmpSoDetailEntity> dmpSoDetailEntityList = dmpSoDetailService.lambdaQuery()
                		.in(DmpSoDetailEntity::getMainId, dmpSoInfoEntityList.stream().map(DmpSoInfoEntity::getId).collect(Collectors.toList())).list();
                Set<String> srcOrderDetailIdSet = dmpSoOutstockDetailService.lambdaQuery().in(DmpSoOutstockDetailEntity::getThirdOrderCode, dmpSoInfoEntityList.stream().filter(d -> StringUtils.isNotBlank(d.getThirdCode())).map(DmpSoInfoEntity::getThirdCode).collect(Collectors.toList()))
            			.list().stream().filter(d -> StringUtils.isNotBlank(d.getSrcOrderDetailId())).map(DmpSoOutstockDetailEntity::getSrcOrderDetailId).collect(Collectors.toSet());
                for(DmpSoDetailEntity d : dmpSoDetailEntityList) {
                	wdtSoDetail.put(d.getPlatformDetailId(), Pair.of(Boolean.valueOf(srcOrderDetailIdSet.contains(d.getThirdDetailId())), d.getRefundNum()));
                }
            }
            cacheMap.put("wdtSoDetail", wdtSoDetail);
        }
        for (String changId : changeIds) {
        	Map<String, ShudiyunB2cOrderDTO> result = this.convert(dmpSoOriginalInfoEntityMap.get(changId), dmpSoOriginalDetailEntityMap.get(changId) , cacheMap);
        	if(!result.isEmpty()) {
            	for(Map.Entry<String, ShudiyunB2cOrderDTO> r : result.entrySet()) {
            		map.put(r.getKey(), JSON.toJSONString(r.getValue()));
            	}
            }
        }
        return map;
    }

    private String wdtItemStatus(String status) {
        if ("40".equals(status)) {
            return "已发货";
        } else if ("50".equals(status)) {
            return "已发货";
        } else if ("60".equals(status)) {
            return "已发货";
        } else if ("70".equals(status)) {
            return "已发货";
        } else if ("80".equals(status)) {
            return "已取消";
        } else if ("90".equals(status)) {
            return "已取消";
        } else {
            return "未发货";
        }
    }


    private String wdtStatusHandler(String status) {
        if ("10".equals(status)) {
            return "未确认";
        } else if ("20".equals(status)) {
            return "待尾款";
        } else if ("30".equals(status)) {
            return "待发货";
        } else if ("40".equals(status)) {
            return "部分发货";
        } else if ("50".equals(status)) {
            return "已发货";
        } else if ("60".equals(status)) {
            return "已签收";
        } else if ("70".equals(status)) {
            return "已完成";
        } else if ("80".equals(status)) {
            return "已退款";
        } else if ("90".equals(status)) {
            return "已关闭";
        } else {
            return "已完成";
        }
    }
}
