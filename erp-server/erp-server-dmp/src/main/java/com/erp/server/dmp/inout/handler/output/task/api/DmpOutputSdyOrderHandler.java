package com.erp.server.dmp.inout.handler.output.task.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdDTO.CodeDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.wrapper.FeignQuery;
import com.common.business.wrapper.QueryParam;
import com.common.business.wrapper.QueryTypeEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.sys.entity.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.inout.dto.request.DmpOutputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.factory.DmpOutputCreateFactory;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.push.consumer.sdy.SdyDeliveryOrderConsumer;
import com.erp.server.dmp.service.DictBasicService;
import com.erp.server.dmp.service.DmpSoOutstockDetailService;
import com.erp.server.dmp.service.DmpSoOutstockService;
import com.erp.server.dmp.service.ThirdMappingService;
import com.erp.server.dmp.service.ThirdShopService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 数帝云线上订单映射推送
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdyOrderHandler extends DmpOutputSdyBaseTaskHandler {
    @Resource
    private ThirdShopService thirdShopService;
    @Resource
    private ThirdMappingService thirdMappingService;
    @Resource
    private SdyDeliveryOrderConsumer sdyDeliveryOrderConsumer;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private DmpOutputCreateFactory dmpOutputCreateFactory;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private DmpSoOutstockDetailService dmpSoOutstockDetailService;

    @Override
    protected void afterPushData(DmpCfgOutputEntity dmpCfgOutputEntity,
    		DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity) {
    	if(isRetryPush) {
    		return;
    	}
    	//创建旺店通原始订单任务
        List<ShudiyunB2cOrderDTO> shudiyunB2cOrderDTOList = new ArrayList<>();
        String requestData = dmpOutputTaskRecordEntity.getRequestData();
        if(requestData.trim().startsWith("{")) {
            ShudiyunB2cOrderDTO dto = JSON.parseObject(requestData, ShudiyunB2cOrderDTO.class);
            shudiyunB2cOrderDTOList.add(dto);
        }else {
            shudiyunB2cOrderDTOList = JSON.parseArray(requestData, ShudiyunB2cOrderDTO.class);
        }
        
        List<String> platformCodeList = shudiyunB2cOrderDTOList.stream().filter(s -> "配货单".equals(s.getTransaction_type()) 
        		&& StringUtils.isNotBlank(s.getBiz_no()) && s.getBiz_no().startsWith("JY") 
        		&& StringUtils.isNotBlank(s.getRoot_node_no_initial()))
        	.map(ShudiyunB2cOrderDTO::getRoot_node_no_initial).collect(Collectors.toList());

        if(CollUtil.isNotEmpty(platformCodeList)) {
        	DmpOutputHotfixCreateRequest request = new DmpOutputHotfixCreateRequest();
            request.setCfgOutputId("1861317267527064372");
            List<QueryParam> queryParams = new ArrayList<>();
            queryParams.add(new QueryParam(QueryTypeEnum.IN, "platform_code", platformCodeList));
            request.setQueryParams(queryParams);
            dmpOutputCreateFactory.doHotfixOutputTask(request);
        }
    }

    /**
     * 解析订单数据
     **/
    public Map<String, ShudiyunB2cOrderDTO> convert(DmpSoInfoEntity dmpSoInfoEntity, List<DmpSoDetailEntity> dmpSoDetailEntityList1 , List<DmpSoReceiverEntity> dmpSoReceiverEntityList , Map<String, Map<String, Object>> cacheMap) {
        Map<String, ShudiyunB2cOrderDTO> result = new HashMap<>();
        if (CollUtil.isEmpty(dmpSoDetailEntityList1)) {
            return result;
        }
        if (dmpSoInfoEntity.getPayStatus() == null || !dmpSoInfoEntity.getPayStatus()) {
            return result;
        }
        
        boolean selfAdd = isSelfAdd();

        Map<String, Object> dmpDictBasticMap = queryAndCacheDmpDictBasicEntity(cacheMap);

        List<DmpSoDetailEntity> dmpSoDetailEntities = dmpSoDetailEntityList1;
        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        //优惠额
        BigDecimal totalDiscount = dmpSoInfoEntity.getTotalDiscount();


        BigDecimal shareTotalDiscount = BigDecimal.ZERO;

        //如果是亚马逊的优惠额在明细里
        if (PlatformDictEnum.AMAZON.getCode().equals(dmpSoInfoEntity.getSourcePlatform())) {
            totalDiscount = dmpSoDetailEntities.stream().filter(req -> req.getDiscount() != null).map(req -> req.getDiscount()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        }


        //订单详情
        for (int i = 0; i < dmpSoDetailEntities.size(); i++) {
            DmpSoDetailEntity dmpSoDetailEntity = dmpSoDetailEntities.get(i);

            ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();
            shudiyunB2cOrderDTO.setBiz_uni_key(dmpSoInfoEntity.getId() + dmpSoDetailEntity.getId());

            if (dmpSoInfoEntity.getPayTime() != null) {
                shudiyunB2cOrderDTO.setBiz_time(localDateTime.format(dmpSoInfoEntity.getPayTime()));
            } else {
                if(dmpSoInfoEntity.getPlatformCreateTime() != null) {
                    shudiyunB2cOrderDTO.setBiz_time(localDateTime.format(dmpSoInfoEntity.getPlatformCreateTime()));
                }
            }

            shudiyunB2cOrderDTO.setTransaction_sub_type(OrderSubTypeEnum.ONLINE_ORDER.getName());

            //如果是旺店通中台表的订单属于配货单，其他的都是线上原始订单
            if (PlatformDictEnum.WDT.getCode().equalsIgnoreCase(dmpSoInfoEntity.getSourceSystem())) {
            	if(StringUtils.isBlank(dmpSoDetailEntity.getPlatformSku())) {
            		continue;
            	}
                shudiyunB2cOrderDTO.setBiz_no(dmpSoInfoEntity.getThirdCode());
                //配货单
                shudiyunB2cOrderDTO.setTransaction_type("配货单");
                shudiyunB2cOrderDTO.setBiz_status(wdtStatusHandler(dmpSoInfoEntity.getOrderStatus()));
                shudiyunB2cOrderDTO.setPrice(dmpSoDetailEntity.getSellPriceOrigin());
                List<com.erp.model.dmp.entity.DictBasicEntity> wdtSdyTransTypeList = (List<com.erp.model.dmp.entity.DictBasicEntity>) dmpDictBasticMap.get("wdtSdyTransType");
                if (CollectionUtils.isNotEmpty(wdtSdyTransTypeList)) {
                    com.erp.model.dmp.entity.DictBasicEntity dictBasicEntity = wdtSdyTransTypeList.stream().filter(e -> e.getValue().equals(dmpSoInfoEntity.getOrderType())).findFirst().orElse(null);
                    if (null != dictBasicEntity){
                        shudiyunB2cOrderDTO.setTransaction_type(dictBasicEntity.getName());
                    }
                }
                List<com.erp.model.dmp.entity.DictBasicEntity> wdtSdyTransSubTypeList = (List<com.erp.model.dmp.entity.DictBasicEntity>) dmpDictBasticMap.get("wdtSdyTransSubType");
                if (CollectionUtils.isNotEmpty(wdtSdyTransSubTypeList)) {
                    com.erp.model.dmp.entity.DictBasicEntity dictBasicEntity = wdtSdyTransSubTypeList.stream().filter(e -> e.getValue().equals(dmpSoInfoEntity.getOrderType())).findFirst().orElse(null);
                    if (null != dictBasicEntity){
                        shudiyunB2cOrderDTO.setTransaction_sub_type(dictBasicEntity.getName());
                    }
                }
            } else {
                shudiyunB2cOrderDTO.setBiz_no(dmpSoInfoEntity.getPlatformCode());
                //线上订单
                shudiyunB2cOrderDTO.setTransaction_type("线上订单");
                if (CharSequenceUtil.isBlank(ApproveStatusEnum.getName(dmpSoInfoEntity.getOrderStatus()))) {
                    shudiyunB2cOrderDTO.setBiz_status("已审核");
                } else {
                    shudiyunB2cOrderDTO.setBiz_status(ApproveStatusEnum.getName(dmpSoInfoEntity.getOrderStatus()));
                }
                shudiyunB2cOrderDTO.setPrice(dmpSoDetailEntity.getSellPriceOrigin());
            }
            if(dmpSoInfoEntity.getInvalidStatus() != null && dmpSoInfoEntity.getInvalidStatus()) {
            	shudiyunB2cOrderDTO.setStatus("已删除");
            }else {
            	shudiyunB2cOrderDTO.setStatus("已创建");
            }

            shudiyunB2cOrderDTO.setTotal_goods_transaction_amount(dmpSoInfoEntity.getAllAmount());
            //总优惠金额
            shudiyunB2cOrderDTO.setDiscount_deduction_amount(totalDiscount);

            Integer totalQty = dmpSoDetailEntities.stream().mapToInt(DmpSoDetailEntity::getQty).sum();
            shudiyunB2cOrderDTO.setTotal_goods_quantity(totalQty);


            if (dmpSoInfoEntity.getSourceSystem().equalsIgnoreCase(PlatformDictEnum.ALI_EXPRESS.getCode())){
                shudiyunB2cOrderDTO.setBuyer_actual_payment(dmpSoInfoEntity.getAfterTaxAmount());
            } else {
                shudiyunB2cOrderDTO.setBuyer_actual_payment(dmpSoInfoEntity.getPayAmount());
            }
            shudiyunB2cOrderDTO.setTotal_freight(dmpSoInfoEntity.getShippingAmount());
            if (PlatformDictEnum.WDT.getCode().equals(dmpSoInfoEntity.getSourceSystem())) {
            	shudiyunB2cOrderDTO.setGoods_discount_deduction_amount(dmpSoDetailEntity.getDiscountAmount());
            	shudiyunB2cOrderDTO.setFreight(dmpSoDetailEntity.getShippingCost());
            	shudiyunB2cOrderDTO.setGoods_no(dmpSoDetailEntity.getSkuNo());
                Map<String, Object> thirdShopEntityListMap = cacheMap.get("thirdShopEntityList");
                if(thirdShopEntityListMap == null) {
                	thirdShopEntityListMap = new HashMap<>();
                }
                
                String shopNo = dmpSoInfoEntity.getShopId();
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

                                // 旺店通添加分区
                                if (PlatformDictEnum.WDT.getCode().equals(dmpSoInfoEntity.getSourceSystem())) {
                                    // 国家编码
                                    String countryCode = "CN";

                                    DictCountryEntity countryEntity = queryAndCacheDictCountryEntity(cacheMap, countryCode);

                                    // 国家名称
                                    String countryName = null == countryEntity ? "" : countryEntity.getShortNameCn();
                                    // 区域编码
                                    String regionCode = null == countryEntity ? "" : countryEntity.getRegionCode();

                                    DictGlobalAreaEntity dictGlobalAreaEntity = queryAndCacheDictGlobalAreaEntity(cacheMap, regionCode);
                                    // 区域名称
                                    String regionName = null == dictGlobalAreaEntity ? "" : dictGlobalAreaEntity.getRegionName();

                                    CfgCountryPartitionEntity cfgCountryPartitionEntity = queryAndCacheCfgCountryPartition(cacheMap, countryCode);
                                    // 军区编码
                                    String militaryRegionCode = null == cfgCountryPartitionEntity ? "" : cfgCountryPartitionEntity.getPartitionCode();
                                    // 军区名称
                                    String militaryRegionName = null == cfgCountryPartitionEntity ? "" : cfgCountryPartitionEntity.getPartitionName();
                                    // 部门编码
                                    String departmentCode = "";
                                    // 部门名称
                                    String departmentName= "";

                                    if (null != cfgCountryPartitionEntity){
                                        SysDepartmentEntity departmentDTO = queryAndCacheOmsDictBasic(cacheMap, militaryRegionCode, subPlatformType);
                                        if (null != departmentDTO){
                                        	KingdeeDepartmentEntity kingdeeDepartment = queryAndCacheKingdeeDepartment(cacheMap, departmentDTO.getId(), customerInfo.getUseOrgId());
                                            // 部门编码
                                            departmentCode = kingdeeDepartment.getKingdeeDeptCode();
                                            // 部门名称
                                            departmentName = kingdeeDepartment.getKingdeeDeptName();
                                        }
                                    }

                                    // 国家编码
                                    shudiyunB2cOrderDTO.setCountry_code(countryCode);
                                    // 国家名称
                                    shudiyunB2cOrderDTO.setCountry(countryName);
                                    // 区域编码
                                    shudiyunB2cOrderDTO.setRegion_code(regionCode);
                                    // 区域名称
                                    shudiyunB2cOrderDTO.setRegion_name(regionName);
                                    // 军区编码
                                    shudiyunB2cOrderDTO.setMilitary_region_code(militaryRegionCode);
                                    // 军区名称
                                    shudiyunB2cOrderDTO.setMilitary_region_name(militaryRegionName);
                                    // 部门编码
                                    shudiyunB2cOrderDTO.setDepartment_code(departmentCode);
                                    // 部门名称
                                    shudiyunB2cOrderDTO.setDepartment_name(departmentName);
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
                        shudiyunB2cOrderDTO.setTransaction_currency_code("CNY");
                        shudiyunB2cOrderDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());
                        shudiyunB2cOrderDTO.setSku_code(dmpSoDetailEntity.getSkuNo());
                        shudiyunB2cOrderDTO.setSku_name(dmpSoDetailEntity.getSkuName());
                        shudiyunB2cOrderDTO.setSpec_no(dmpSoDetailEntity.getPlatformSpuNo());
                        shudiyunB2cOrderDTO.setSpec_name(dmpSoDetailEntity.getSpecifics());
                    }
                }
                
                Map<String, Object> wdtSoOutstockMap = cacheMap.get("wdtSoOutstock");
                if(wdtSoOutstockMap.containsKey(dmpSoDetailEntity.getThirdDetailId())) {
                	shudiyunB2cOrderDTO.setGoods_status("已发货");
                }else {
                	shudiyunB2cOrderDTO.setGoods_status(wdtItemStatus(dmpSoDetailEntity.getPlatformStatus()));
                }

                //取消金额、数量
                if (dmpSoDetailEntity.getRefundNum().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal canceledAmount = dmpSoDetailEntities.stream().map(req -> req.getAfterAmount().divide(MathUtil.valueOf(req.getQty()), 4, RoundingMode.DOWN).multiply(dmpSoDetailEntity.getRefundNum())).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(canceledAmount);
                    // 取消商品数量（合计）
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(dmpSoDetailEntity.getRefundNum().intValue());
                } else {
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(BigDecimal.ZERO);
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(0);
                }


            } else {
            	shudiyunB2cOrderDTO.setGoods_no(dmpSoDetailEntity.getPlatformSku());
                String shopId = "";
                if (CharSequenceUtil.isNotBlank(dmpSoInfoEntity.getShopId())) {
                    shopId = dmpSoInfoEntity.getShopId();
                } else {
                    shopId = dmpSoInfoEntity.getNextLevelId();
                }

                Map<String, Object> shopInfoMap = cacheMap.get("shopInfo");
                if(shopInfoMap == null) {
                	shopInfoMap = new HashMap<>();
                }
                String sysId = shopId;
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
                    
                }


                if (CharSequenceUtil.isNotBlank(dmpSoInfoEntity.getCurrencyCode())) {
                    shudiyunB2cOrderDTO.setTransaction_currency_code(dmpSoInfoEntity.getCurrencyCode());
                    Map<String, Object> dictCurrencyMap = cacheMap.get("dictCurrency");
                    if(dictCurrencyMap == null) {
                    	dictCurrencyMap = new HashMap<>();
                    }
                    String tradeCurrency = dmpSoInfoEntity.getCurrencyCode();
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
                    } else {
                        ServiceException.runError("未找到sys币别信息【dict_currency】：{}", tradeCurrency);
                    }
                } else {
                    shudiyunB2cOrderDTO.setTransaction_currency_code(shopInfo.getTradeCurrency());
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
                    } else {
                        shudiyunB2cOrderDTO.setTransaction_currency(shopInfo.getTradeCurrency());
                    }
                }
                shudiyunB2cOrderDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());
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

                shudiyunB2cOrderDTO.setSku_code("");
                shudiyunB2cOrderDTO.setSku_name("");
                shudiyunB2cOrderDTO.setSpec_no("");
                shudiyunB2cOrderDTO.setSpec_name("");
                
                String orderCountryCode = "";
                if(CollUtil.isNotEmpty(dmpSoReceiverEntityList)) {
                	DmpSoReceiverEntity dmpSoReceiverEntity = dmpSoReceiverEntityList.get(0);
                	orderCountryCode = dmpSoReceiverEntity.getCountry();
                }
                // 国家编码
                String countryCode = shopInfo.getDictCountryCode();
                if(StringUtils.isBlank(countryCode) || "ALL".equals(countryCode)) {
                	countryCode = customerInfo.getCountryId();
                	if(StringUtils.isBlank(countryCode) || "ALL".equals(countryCode)) {
                		countryCode = orderCountryCode;
                	}
                }
                
                orderCountryCode = DmpHandlerUtils.convertCountry(orderCountryCode);
                countryCode = DmpHandlerUtils.convertCountry(countryCode);

                DictCountryEntity countryEntity = queryAndCacheDictCountryEntity(cacheMap, orderCountryCode);
                if(countryEntity == null) {
                	countryEntity = queryAndCacheDictCountryEntity(cacheMap, countryCode);
                }

                // 国家名称
                String countryName = null == countryEntity ? "" : countryEntity.getShortNameCn();
                // 区域编码
                String regionCode = null == countryEntity ? "" : countryEntity.getRegionCode();

                DictGlobalAreaEntity dictGlobalAreaEntity = queryAndCacheDictGlobalAreaEntity(cacheMap, regionCode);
                // 区域名称
                String regionName = null == dictGlobalAreaEntity ? "" : dictGlobalAreaEntity.getRegionName();

                CfgCountryPartitionEntity cfgCountryPartitionEntity = queryAndCacheCfgCountryPartition(cacheMap, countryCode);
                // 军区编码
                String militaryRegionCode = null == cfgCountryPartitionEntity ? "" : cfgCountryPartitionEntity.getPartitionCode();
                // 军区名称
                String militaryRegionName = null == cfgCountryPartitionEntity ? "" : cfgCountryPartitionEntity.getPartitionName();
                // 部门编码
                String departmentCode = "";
                // 部门名称
                String departmentName= "";

                if (null != cfgCountryPartitionEntity){
                    SysDepartmentEntity departmentDTO = queryAndCacheOmsDictBasic(cacheMap, militaryRegionCode, subPlatformType);
                    if (null != departmentDTO){
                    	KingdeeDepartmentEntity kingdeeDepartment = queryAndCacheKingdeeDepartment(cacheMap, departmentDTO.getId(), customerInfo.getUseOrgId());
                        // 部门编码
                        departmentCode = kingdeeDepartment.getKingdeeDeptCode();
                        // 部门名称
                        departmentName = kingdeeDepartment.getKingdeeDeptName();
                    }
                }

                // 国家编码
                shudiyunB2cOrderDTO.setCountry_code(orderCountryCode);
                // 国家名称
                shudiyunB2cOrderDTO.setCountry(countryName);
                // 区域编码
                shudiyunB2cOrderDTO.setRegion_code(regionCode);
                // 区域名称
                shudiyunB2cOrderDTO.setRegion_name(regionName);
                // 军区编码
                shudiyunB2cOrderDTO.setMilitary_region_code(militaryRegionCode);
                // 军区名称
                shudiyunB2cOrderDTO.setMilitary_region_name(militaryRegionName);
                // 部门编码
                shudiyunB2cOrderDTO.setDepartment_code(departmentCode);
                // 部门名称
                shudiyunB2cOrderDTO.setDepartment_name(departmentName);

                shudiyunB2cOrderDTO.setGoods_status("未发货");
                // 商品状态
                if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dmpSoInfoEntity.getDeliveryStatus()) || SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(dmpSoInfoEntity.getOrderStatus())) {
                    shudiyunB2cOrderDTO.setGoods_status("已发货");
                }

                if (dmpSoInfoEntity.getIsCancel() && dmpSoInfoEntity.getIsCancel() != null) {
                    shudiyunB2cOrderDTO.setGoods_status("已取消");
                }

                //取消金额、数量
                if (dmpSoInfoEntity.getSourceSystem().equals(PlatformDictEnum.ALI_EXPRESS.getCode())
                ) {
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(dmpSoInfoEntity.getTotalCancelGoodsAmount());
                    if (dmpSoInfoEntity.getIsCancel()) {
                        shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(totalQty);
                    }
                } else {
                    if (dmpSoInfoEntity.getIsCancel()) {
                        BigDecimal amount = dmpSoDetailEntities.stream().map(req -> req.getSellPriceOrigin().multiply(MathUtil.valueOf(req.getQty()))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                        shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(amount);
                        // 取消商品数量（合计）
                        shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(totalQty);
                    }
                }
            }

            BigDecimal totalTaxFee = dmpSoInfoEntity.getTotalTaxFee();
            // 配置指定平台税金为0
            List<com.erp.model.dmp.entity.DictBasicEntity> tax0PlatformTypeList = (List<com.erp.model.dmp.entity.DictBasicEntity>) dmpDictBasticMap.get("tax0PlatformType");
            if (CollUtil.isNotEmpty(tax0PlatformTypeList)) {
                boolean hasSetTaxO = tax0PlatformTypeList.stream().anyMatch(e -> e.getValue().equalsIgnoreCase(dmpSoInfoEntity.getSourcePlatform()));
                if (hasSetTaxO){
                    totalTaxFee = BigDecimal.ZERO;
                }
            }
            shudiyunB2cOrderDTO.setTaxation(totalTaxFee);
            shudiyunB2cOrderDTO.setRoot_node_no(dmpSoInfoEntity.getThirdCode());

            if (dmpSoInfoEntity.getPayTime() != null) {
                shudiyunB2cOrderDTO.setRoot_node_create_time(localDateTime.format(dmpSoInfoEntity.getPayTime()));
            } else {
                shudiyunB2cOrderDTO.setRoot_node_create_time(localDateTime.format(dmpSoInfoEntity.getPlatformCreateTime()));
            }
            shudiyunB2cOrderDTO.setRoot_node_modify_time(localDateTime.format(dmpSoInfoEntity.getPlatformUpdateTime()));
            shudiyunB2cOrderDTO.setGoods_name(dmpSoDetailEntity.getSkuName());

            if (!PlatformDictEnum.WDT.getCode().equalsIgnoreCase(dmpSoInfoEntity.getSourceSystem())) {
                BigDecimal sellPriceOrigin = dmpSoDetailEntity.getSellPriceOrigin();
                if (sellPriceOrigin != null && BigDecimal.ZERO.compareTo(sellPriceOrigin) == 0) {
                    shudiyunB2cOrderDTO.setIs_gift(1);
                } else {
                    shudiyunB2cOrderDTO.setIs_gift(0);
                }
            }

            shudiyunB2cOrderDTO.setRemark(dmpSoDetailEntity.getItemRemark());
            shudiyunB2cOrderDTO.setGoods_transaction_quantity(dmpSoDetailEntity.getQty());
            shudiyunB2cOrderDTO.setGoods_transaction_amount(MathUtil.multiplyWithTwo(shudiyunB2cOrderDTO.getPrice(), shudiyunB2cOrderDTO.getGoods_transaction_quantity()));

            shudiyunB2cOrderDTO.setUnit("PCS");
            shudiyunB2cOrderDTO.setPost_amount(dmpSoInfoEntity.getShippingAmount());
            shudiyunB2cOrderDTO.setMsku_code(dmpSoDetailEntity.getPlatformSku());
            if (CharSequenceUtil.isBlank(dmpSoDetailEntity.getSkuName())) {
                shudiyunB2cOrderDTO.setMsku_name(dmpSoDetailEntity.getSkuNo());
            } else {
                shudiyunB2cOrderDTO.setMsku_name(dmpSoDetailEntity.getSkuName());
            }

            shudiyunB2cOrderDTO.setSource_system("SDC");
            if (CharSequenceUtil.isNotBlank(dmpSoInfoEntity.getPlatformCode())) {
                shudiyunB2cOrderDTO.setRoot_node_no_initial(dmpSoInfoEntity.getPlatformCode());
            } else {
                shudiyunB2cOrderDTO.setRoot_node_no_initial(dmpSoInfoEntity.getThirdCode());
            }
            shudiyunB2cOrderDTO.setOrder_quantity_to_be_shipped(totalQty - shudiyunB2cOrderDTO.getTotal_canceled_goods_quantity());

            if (!PlatformDictEnum.WDT.getCode().equalsIgnoreCase(dmpSoInfoEntity.getSourceSystem())) {
            	BigDecimal goods_transaction_amount = shudiyunB2cOrderDTO.getGoods_transaction_amount();
    			BigDecimal total_goods_transaction_amount = shudiyunB2cOrderDTO.getTotal_goods_transaction_amount();
    			if(BigDecimal.ZERO.compareTo(goods_transaction_amount) != 0 && BigDecimal.ZERO.compareTo(total_goods_transaction_amount) != 0) {
    				if(shudiyunB2cOrderDTO.getDiscount_deduction_amount() != null) {
    					shudiyunB2cOrderDTO.setGoods_discount_deduction_amount(goods_transaction_amount.multiply(shudiyunB2cOrderDTO.getDiscount_deduction_amount()).divide(total_goods_transaction_amount , 4 , RoundingMode.HALF_UP));
    				}
    				if(shudiyunB2cOrderDTO.getTotal_freight() != null) {
    					shudiyunB2cOrderDTO.setFreight(goods_transaction_amount.multiply(shudiyunB2cOrderDTO.getTotal_freight()).divide(total_goods_transaction_amount , 4 , RoundingMode.HALF_UP));
    				}
    				if(shudiyunB2cOrderDTO.getTaxation() != null) {
    					shudiyunB2cOrderDTO.setGoods_taxation(goods_transaction_amount.multiply(shudiyunB2cOrderDTO.getTaxation()).divide(total_goods_transaction_amount , 4 , RoundingMode.HALF_UP));
    				}
    			}
            }
            shudiyunB2cOrderDTO.setDefaultValue();
            if(selfAdd) {
            	if("线下订单".equals(shudiyunB2cOrderDTO.getTransaction_type())) {
            		shudiyunB2cOrderDTO.setBiz_uni_key(shudiyunB2cOrderDTO.getBiz_uni_key() + "_1");
            		result.put(dmpSoDetailEntity.getId(), shudiyunB2cOrderDTO);
            	}
            }else {
            	if("线下订单".equals(shudiyunB2cOrderDTO.getTransaction_type())) {
            		shudiyunB2cOrderDTO.setTransaction_type("配货单");
            	}
            	result.put(dmpSoDetailEntity.getId(), shudiyunB2cOrderDTO);
            }
        }
        return result;
    }


    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoInfoEntity> DmpSoInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSoDetailEntity>> DmpSoDetailEntityMap = new HashMap<>();
        Map<String, List<DmpSoReceiverEntity>> dmpSoReceiverEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoInfoEntity dmpSoInfoEntity = (DmpSoInfoEntity) v;
                        DmpSoInfoEntityMap.put(dmpSoInfoEntity.getId(), dmpSoInfoEntity);
                    }
                } else if ("dmp_so_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoDetailEntity DmpSoDetailEntity = (DmpSoDetailEntity) v;
                        String mainId = DmpSoDetailEntity.getMainId();
                        List<DmpSoDetailEntity> list = DmpSoDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(DmpSoDetailEntity);
                        DmpSoDetailEntityMap.put(mainId, list);
                    }
                }else if("dmp_so_receiver".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoReceiverEntity dmpSoReceiverEntity = (DmpSoReceiverEntity) v;
						String mainId = dmpSoReceiverEntity.getMainId();
						List<DmpSoReceiverEntity> list = dmpSoReceiverEntityMap.get(mainId);
						if(CollUtil.isEmpty(list)) {
							list = new ArrayList<>();
						}
						list.add(dmpSoReceiverEntity);
						dmpSoReceiverEntityMap.put(mainId, list);
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
                if ("dmp_so_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_so_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoDetailEntity DmpSoDetailEntity = (DmpSoDetailEntity) v;
                        changeIds.add(DmpSoDetailEntity.getMainId());
                    }
                }else if("dmp_so_receiver".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoReceiverEntity dmpSoReceiverEntity = (DmpSoReceiverEntity) v;
						changeIds.add(dmpSoReceiverEntity.getMainId());
					}
				}
            }
        }

        Map<String, String> map = new HashMap<>();
        Map<String, Map<String, Object>> cacheMap = new HashMap<>();
        List<String> wdtThirdCodeList = DmpSoInfoEntityMap.values().stream()
        		.filter(d -> PlatformDictEnum.WDT.getCode().equalsIgnoreCase(d.getSourceSystem()))
        		.map(DmpSoInfoEntity::getThirdCode)
        		.collect(Collectors.toList());
        Map<String, Object> wdtSoOutstockMap = new HashMap<>();
        if(CollUtil.isNotEmpty(wdtThirdCodeList)) {
        	List<DmpSoOutstockDetailEntity> outStockDetailList = dmpSoOutstockDetailService.lambdaQuery().in(DmpSoOutstockDetailEntity::getThirdOrderCode, wdtThirdCodeList).list();
        	if(CollUtil.isNotEmpty(outStockDetailList)) {
        		Set<String> finishIds = ApplicationContextUtils.getBean(DmpSoOutstockService.class).lambdaQuery().in(DmpSoOutstockEntity::getId, outStockDetailList.stream().map(DmpSoOutstockDetailEntity::getMainId).collect(Collectors.toList()))
                		.eq(DmpSoOutstockEntity::getStatus, "110").select(DmpSoOutstockEntity::getId).list().stream().map(DmpSoOutstockEntity::getId).collect(Collectors.toSet());
                wdtSoOutstockMap = outStockDetailList.stream().filter(o -> finishIds.contains(o.getMainId()))
                		.collect(Collectors.toMap(DmpSoOutstockDetailEntity::getSrcOrderDetailId, DmpSoOutstockDetailEntity::getSrcOrderDetailId , (d1 , d2) -> d1));
        	}
        }
        cacheMap.put("wdtSoOutstock", wdtSoOutstockMap);
        for(String changId : changeIds) {
            Map<String, ShudiyunB2cOrderDTO> result = this.convert(DmpSoInfoEntityMap.get(changId), DmpSoDetailEntityMap.get(changId) , dmpSoReceiverEntityMap.get(changId) , cacheMap);
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
        if ("4".equals(status)) {
            return "线下退款";
        } else if ("5".equals(status)) {
            return "已取消";
        } else if ("6".equals(status)) {
            return "待转预订单(待审核)";
        } else if ("7".equals(status)) {
            return "待转已完成";
        } else if ("10".equals(status)) {
            return "未付款";
        } else if ("12".equals(status)) {
            return "待尾款";
        } else if ("15".equals(status)) {
            return "等未付";
        } else if ("16".equals(status)) {
            return "延时审核";
        } else if ("19".equals(status)) {
            return "预订单前处理";
        } else if ("20".equals(status)) {
            return "审核前处理";
        } else if ("21".equals(status)) {
            return "自流转待发货";
        } else if ("23".equals(status)) {
            return "异常订单";
        } else if ("24".equals(status)) {
            return "换货预订单";
        } else if ("25".equals(status)) {
            return "待处理预订单";
        } else if ("27".equals(status)) {
            return "待分配预订单";
        } else if ("30".equals(status)) {
            return "待客审";
        } else if ("35".equals(status)) {
            return "待财审";
        } else if ("40".equals(status)) {
            return "审核中";
        } else if ("55".equals(status)) {
            return "已审核";
        } else if ("95".equals(status)) {
            return "已发货";
        } else if ("96".equals(status)) {
            return "成本确认（待录入计划成本，订单结算时有货品无计划成本）";
        } else if ("101".equals(status)) {
            return "已过账";
        } else {
            return "已完成";
        }
    }


    private static DictCountryEntity queryAndCacheDictCountryEntity(Map<String, Map<String, Object>> cacheMap, String countryCode) {
        Map<String, Object> countryInfoMap = cacheMap.get("countryInfo");
        if(null == countryInfoMap) {
            countryInfoMap = new HashMap<>();
        }
        DictCountryEntity countryEntity = null;
        Object countryObj = countryInfoMap.get(countryCode);
        if(null == countryObj) {
            List<DictCountryEntity> countryEntityList = FeignQuery.create(DictCountryEntity.class).eq(DictCountryEntity::getId, countryCode).list();
            if(CollUtil.isNotEmpty(countryEntityList)) {
                countryEntity = countryEntityList.get(0);
            }
        }else {
            countryEntity = (DictCountryEntity) countryObj;
        }
        countryInfoMap.put(countryCode, countryEntity);
        cacheMap.put("countryInfo", countryInfoMap);
        return countryEntity;
    }


    private static DictGlobalAreaEntity queryAndCacheDictGlobalAreaEntity(Map<String, Map<String, Object>> cacheMap, String regionCode) {
        Map<String, Object> dictGlobalAreaMap = cacheMap.get("dictGlobalArea");
        if(null == dictGlobalAreaMap) {
            dictGlobalAreaMap = new HashMap<>();
        }
        DictGlobalAreaEntity entity = null;
        Object countryObj = dictGlobalAreaMap.get(regionCode);
        if(null == countryObj) {
            List<DictGlobalAreaEntity> countryEntityList = FeignQuery.create(DictGlobalAreaEntity.class).eq(DictGlobalAreaEntity::getId, regionCode).list();
            if(CollUtil.isNotEmpty(countryEntityList)) {
                entity = countryEntityList.get(0);
            }
        }else {
            entity = (DictGlobalAreaEntity) countryObj;
        }
        dictGlobalAreaMap.put(regionCode, entity);
        cacheMap.put("dictGlobalArea", dictGlobalAreaMap);
        return entity;
    }

    private static CfgCountryPartitionEntity queryAndCacheCfgCountryPartition(Map<String, Map<String, Object>> cacheMap, String countryCode) {
    	if(StringUtils.isBlank(countryCode)) {
    		return null;
    	}
        Map<String, Object> cfgCountryPartitionMap = cacheMap.get("cfgCountryPartition");
        if(null == cfgCountryPartitionMap) {
            List<CfgCountryPartitionEntity> countryEntityList = FeignQuery.create(CfgCountryPartitionEntity.class).list();
            cfgCountryPartitionMap = countryEntityList.stream().collect(Collectors.toMap(CfgCountryPartitionEntity::getCountry, c -> c , (c1 , c2) -> c1));
            cacheMap.put("cfgCountryPartition", cfgCountryPartitionMap);
        }
        Object object = cfgCountryPartitionMap.get(countryCode);
        if(object != null) {
        	return (CfgCountryPartitionEntity)object;
        }
		return null;
    }


    private SysDepartmentEntity queryAndCacheOmsDictBasic(Map<String, Map<String, Object>> cacheMap, String partitionCode, String dictPlatform) {
        if (StringUtils.isBlank(partitionCode) || StringUtils.isBlank(dictPlatform)){
            return null;
        }
        Map<String, Object> dictBasicMap = cacheMap.getOrDefault("omsDictBasic", new HashMap<>());
        List<DictBasicEntity> sdyPartitionDeptList = new ArrayList<>();
        List<DictBasicEntity> sdyPlatformDeptList = new ArrayList<>();
        List<SysDepartmentEntity> deptList = new LinkedList<>();

        Object level1ListObj = dictBasicMap.get(DictBasicTypeEnum.SDY_PARTITION_LEVEL1_DEPT.getType());
        Object level2ListObj = dictBasicMap.get(DictBasicTypeEnum.SDY_PLATFORM_LEVEL2_DEPT.getType());
        Object deptListObj = dictBasicMap.get("deptList");
        if (null == level2ListObj || null == level1ListObj || null == deptListObj) {
            List<DictBasicEntity> dictBasicEntityList = FeignQuery.create(DictBasicEntity.class)
                    .in(DictBasicEntity::getType, Arrays.asList(
                            DictBasicTypeEnum.SDY_PARTITION_LEVEL1_DEPT.getType(),
                            DictBasicTypeEnum.SDY_PLATFORM_LEVEL2_DEPT.getType()
                    ))
                    .list();
            if (CollectionUtils.isNotEmpty(dictBasicEntityList)) {
                Map<String, List<DictBasicEntity>> groupMap = dictBasicEntityList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));
                sdyPartitionDeptList = groupMap.get(DictBasicTypeEnum.SDY_PARTITION_LEVEL1_DEPT.getType());
                sdyPlatformDeptList = groupMap.get(DictBasicTypeEnum.SDY_PLATFORM_LEVEL2_DEPT.getType());
                dictBasicMap.putAll(groupMap);
            }
            // 部门信息
            deptList = sysUserFeign.getDeptEntityList();
            if (CollectionUtils.isNotEmpty(deptList)){
                dictBasicMap.put("deptList", deptList);
            }
            cacheMap.put("omsDictBasic", dictBasicMap);
        } else {
            sdyPartitionDeptList = (List<DictBasicEntity>) level1ListObj;
            sdyPlatformDeptList = (List<DictBasicEntity>) level2ListObj;
            deptList = (List<SysDepartmentEntity>) deptListObj;
        }

        // 军区一级部门映射
        DictBasicEntity sdyPartitionDeptEntity = sdyPartitionDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(partitionCode)).findFirst().orElse(null);
        // 销售平台二级部门映射
        List<DictBasicEntity> sdyPlatformDeptEntityList = sdyPlatformDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(dictPlatform)).collect(Collectors.toList());
        if (null != sdyPartitionDeptEntity && !CollectionUtils.isEmpty(sdyPlatformDeptEntityList)) {
            List<String> deptLevel2Ids = sdyPlatformDeptEntityList.stream().map(DictBasicEntity::getValue).distinct().collect(Collectors.toList());
            return deptList.stream().filter(e -> e.getPath().contains(sdyPartitionDeptEntity.getValue())
                                    && deptLevel2Ids.contains(e.getId())
                    )
                    .findFirst()
                    .orElse(null);
        }
        return null;
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

    private Map<String, Object> queryAndCacheDmpDictBasicEntity(Map<String, Map<String, Object>> cacheMap) {
        Map<String, Object> dmpDictBasic = cacheMap.getOrDefault("dmpDictBasic", new HashMap<>());

        List<com.erp.model.dmp.entity.DictBasicEntity> wdtSdyTransTypeList= (List<com.erp.model.dmp.entity.DictBasicEntity>) dmpDictBasic.getOrDefault("wdtSdyTransType", new ArrayList<>());
        List<com.erp.model.dmp.entity.DictBasicEntity>  wdtSdyTransSubTypeList = (List<com.erp.model.dmp.entity.DictBasicEntity>) dmpDictBasic.getOrDefault("wdtSdyTransSubType", new ArrayList<>());
        List<com.erp.model.dmp.entity.DictBasicEntity>  tax0PlatformDictBasicList = (List<com.erp.model.dmp.entity.DictBasicEntity>)dmpDictBasic.getOrDefault("tax0PlatformType", new ArrayList<>());
        if (CollUtil.isNotEmpty(tax0PlatformDictBasicList) && CollUtil.isNotEmpty(wdtSdyTransTypeList) && CollUtil.isNotEmpty(wdtSdyTransSubTypeList)) {
            return dmpDictBasic;
        }
        List<com.erp.model.dmp.entity.DictBasicEntity> dictBasicEntityList = FeignQuery.create(com.erp.model.dmp.entity.DictBasicEntity.class)
                .in(com.erp.model.dmp.entity.DictBasicEntity::getType, Arrays.asList("wdtSdyTransType", "wdtSdyTransSubType", "tax0PlatformType"))
                .list();
        if (CollectionUtils.isNotEmpty(dictBasicEntityList)) {
            Map<String, List<com.erp.model.dmp.entity.DictBasicEntity>> dpSourceMap = dictBasicEntityList
                    .stream()
                    .collect(Collectors.groupingBy(com.erp.model.dmp.entity.DictBasicEntity::getType));
            wdtSdyTransTypeList = dpSourceMap.getOrDefault("wdtSdyTransType", new ArrayList<>());
            wdtSdyTransSubTypeList = dpSourceMap.getOrDefault("wdtSdyTransSubType", new ArrayList<>());
            tax0PlatformDictBasicList = dpSourceMap.getOrDefault("tax0PlatformType", new ArrayList<>());
            dmpDictBasic.put("wdtSdyTransType", wdtSdyTransTypeList);
            dmpDictBasic.put("wdtSdyTransSubType", wdtSdyTransSubTypeList);
            dmpDictBasic.put("tax0PlatformType", tax0PlatformDictBasicList);
            cacheMap.put("dmpDictBasic", dmpDictBasic);
        }
        return dmpDictBasic;
    }

    protected boolean isSelfAdd() {
		return false;
	}
}
