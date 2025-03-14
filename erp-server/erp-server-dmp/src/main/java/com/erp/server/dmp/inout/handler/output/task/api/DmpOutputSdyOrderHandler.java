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
import com.common.business.wrapper.FeignQuery;
import com.common.business.wrapper.QueryParam;
import com.common.business.wrapper.QueryTypeEnum;
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
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.inout.dto.request.DmpOutputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.factory.DmpOutputCreateFactory;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.push.consumer.sdy.SdyDeliveryOrderConsumer;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 数帝云线上订单映射推送
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdyOrderHandler extends DmpOutputTaskHandler {
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

    @Override
    protected List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<String, String> map = this.getPushJsonDataMap(dmpRequest, dmpResponse);
        List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = new ArrayList<>();
        if(!map.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            int i = 0;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String dataId = entry.getKey();
                String value = entry.getValue();
                ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = JSON.parseObject(value, ShudiyunB2cOrderDTO.class);
                DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = new DmpOutputTaskRecordEntity();
                String id = identifierGenerator.nextId(dmpOutputTaskRecordEntity).toString();
                dmpOutputTaskRecordEntity.setId(id);
                dmpOutputTaskRecordEntity.setMainId(dmpRequest.getOutputTaskId());
                dmpOutputTaskRecordEntity.setDataId(dataId);
                dmpOutputTaskRecordEntity.setSourceCode(shudiyunB2cOrderDTO.getBiz_no() + "_" + shudiyunB2cOrderDTO.getMsku_code());
                dmpOutputTaskRecordEntity.setRequestData(value);
                dmpOutputTaskRecordEntity.setStatus(DmpOutputTaskRecordStatusEnum.INIT.getCode());
                LocalDateTime insertTime = now.plus(i, ChronoUnit.MILLIS);
                dmpOutputTaskRecordEntity.setCreateTime(insertTime);
                dmpOutputTaskRecordEntity.setUpdateTime(insertTime);
                dmpOutputTaskRecordEntityList.add(dmpOutputTaskRecordEntity);
                i = i + 1;
            }
        }

        return dmpOutputTaskRecordEntityList;
    }

    @Override
    protected void pushData(DmpCfgOutputEntity dmpCfgOutputEntity, DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity) {
        String id = dmpOutputTaskRecordEntity.getId();
        String status = "";
        String requestData = dmpOutputTaskRecordEntity.getRequestData();
        ApiResult handle = sdyDeliveryOrderConsumer.handle(requestData);
        if (200 == handle.getCode()) {
            status = DmpOutputTaskRecordStatusEnum.FINISH.getCode();
        } else {
            status = DmpOutputTaskRecordStatusEnum.COSUMERERROR.getCode();
        }

        dmpOutputUtils.updateStatus(id, status, String.valueOf(handle.getData()) , handle.getMsg());

        //创建旺店通原始订单任务
        List<ShudiyunB2cOrderDTO> shudiyunB2cOrderDTOList = new ArrayList<>();

        if(requestData.trim().startsWith("{")) {
            ShudiyunB2cOrderDTO dto = JSON.parseObject(requestData, ShudiyunB2cOrderDTO.class);
            shudiyunB2cOrderDTOList.add(dto);
        }else {
            shudiyunB2cOrderDTOList = JSON.parseArray(requestData, ShudiyunB2cOrderDTO.class);
        }

        shudiyunB2cOrderDTOList.forEach(shudiyunB2cOrderDTO -> {
            if (PlatformDictEnum.WDT.getCode().equalsIgnoreCase(shudiyunB2cOrderDTO.getPlatform_id())) {
                DmpOutputHotfixCreateRequest request = new DmpOutputHotfixCreateRequest();
                request.setCfgOutputId("1861317267527064372");
                List<QueryParam> queryParams = new ArrayList<>();
                QueryParam queryParam = new QueryParam();
                queryParam.setType(QueryTypeEnum.EQ);
                queryParam.setName("third_code");
                queryParam.setValue(shudiyunB2cOrderDTO.getBiz_no());
                queryParams.add(queryParam);
                request.setQueryParams(queryParams);
                dmpOutputCreateFactory.doHotfixOutputTask(request);
            }
        });
    }


    /**
     * 解析订单数据
     **/
    public Map<String, ShudiyunB2cOrderDTO> convert(DmpSoInfoEntity dmpSoInfoEntity, List<DmpSoDetailEntity> dmpSoDetailEntityList1 , Map<String, Map<String, Object>> cacheMap) {
        Map<String, ShudiyunB2cOrderDTO> result = new HashMap<>();
        if (CollUtil.isEmpty(dmpSoDetailEntityList1)) {
            return result;
        }
        if (dmpSoInfoEntity.getPayStatus() == null || !dmpSoInfoEntity.getPayStatus()) {
            return result;
        }
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
            shudiyunB2cOrderDTO.setStatus("已创建");

            shudiyunB2cOrderDTO.setTransaction_sub_type(OrderSubTypeEnum.ONLINE_ORDER.getName());

            shudiyunB2cOrderDTO.setTotal_goods_transaction_amount(dmpSoInfoEntity.getAllAmount());
            //总优惠金额
            shudiyunB2cOrderDTO.setDiscount_deduction_amount(totalDiscount);

            Integer totalQty = dmpSoDetailEntities.stream().mapToInt(DmpSoDetailEntity::getQty).sum();
            shudiyunB2cOrderDTO.setTotal_goods_quantity(totalQty);


            shudiyunB2cOrderDTO.setBuyer_actual_payment(dmpSoInfoEntity.getPayAmount());
            shudiyunB2cOrderDTO.setTotal_freight(dmpSoInfoEntity.getShippingAmount());
            if (PlatformDictEnum.WDT.getCode().equals(dmpSoInfoEntity.getSourceSystem())) {

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
                                    shudiyunB2cOrderDTO.setSubplatform_no(subPlatformTypeDict.getName());
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
                        shudiyunB2cOrderDTO.setTransaction_currency_code("CNY");
                        shudiyunB2cOrderDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());
                        shudiyunB2cOrderDTO.setPlatform_id(shopInfo.getDictPlatform());
                        shudiyunB2cOrderDTO.setPlatform_name(PlatformDictEnum.getNameByCode(shopInfo.getDictPlatform()));
                        shudiyunB2cOrderDTO.setSku_code(dmpSoDetailEntity.getSkuNo());
                        shudiyunB2cOrderDTO.setSku_name(dmpSoDetailEntity.getSkuName());
                        shudiyunB2cOrderDTO.setSpec_no(dmpSoDetailEntity.getPlatformSpuNo());
                        shudiyunB2cOrderDTO.setSpec_name(dmpSoDetailEntity.getSpecifics());
                    }
                }

                shudiyunB2cOrderDTO.setGoods_status(wdtItemStatus(dmpSoDetailEntity.getPlatformStatus()));

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
                        shudiyunB2cOrderDTO.setTransaction_currency(dmpSoInfoEntity.getCurrencyCode());
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
                shudiyunB2cOrderDTO.setPlatform_id(dmpSoInfoEntity.getSourceSystem());
                shudiyunB2cOrderDTO.setPlatform_name(PlatformDictEnum.getNameByCode(dmpSoInfoEntity.getSourcePlatform()));

                shudiyunB2cOrderDTO.setSku_code("");
                shudiyunB2cOrderDTO.setSku_name("");
                shudiyunB2cOrderDTO.setSpec_no("");
                shudiyunB2cOrderDTO.setSpec_name("");

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
                        || dmpSoInfoEntity.getSourceSystem().equals(PlatformDictEnum.SHOPEE.getCode())
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

            shudiyunB2cOrderDTO.setTaxation(dmpSoInfoEntity.getTotalTaxFee());

            shudiyunB2cOrderDTO.setRoot_node_no(dmpSoInfoEntity.getThirdCode());

            if (dmpSoInfoEntity.getPayTime() != null) {
                shudiyunB2cOrderDTO.setRoot_node_create_time(localDateTime.format(dmpSoInfoEntity.getPayTime()));
            } else {
                shudiyunB2cOrderDTO.setRoot_node_create_time(localDateTime.format(dmpSoInfoEntity.getPlatformCreateTime()));
            }
            shudiyunB2cOrderDTO.setRoot_node_modify_time(localDateTime.format(dmpSoInfoEntity.getPlatformUpdateTime()));
            shudiyunB2cOrderDTO.setGoods_no(dmpSoDetailEntity.getPlatformSku());
            shudiyunB2cOrderDTO.setGoods_name(dmpSoDetailEntity.getSkuName());

            if (dmpSoDetailEntity.getIsGift() && dmpSoDetailEntity.getIsGift() != null) {
                shudiyunB2cOrderDTO.setIs_gift(1);
            } else {
                shudiyunB2cOrderDTO.setIs_gift(0);
            }

            shudiyunB2cOrderDTO.setRemark(dmpSoDetailEntity.getItemRemark());
            shudiyunB2cOrderDTO.setGoods_transaction_quantity(dmpSoDetailEntity.getQty());
            shudiyunB2cOrderDTO.setGoods_transaction_amount(MathUtil.multiply(shudiyunB2cOrderDTO.getPrice(), shudiyunB2cOrderDTO.getGoods_transaction_quantity()));

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

            result.put(dmpSoDetailEntity.getId(), shudiyunB2cOrderDTO);

        }
        return result;
    }



    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoInfoEntity> DmpSoInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSoDetailEntity>> DmpSoDetailEntityMap = new HashMap<>();
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
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        Map<String, Map<String, Object>> cacheMap = new HashMap<>();
        for(String changId : changeIds) {
            Map<String, ShudiyunB2cOrderDTO> result = this.convert(DmpSoInfoEntityMap.get(changId), DmpSoDetailEntityMap.get(changId) , cacheMap);
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


}
