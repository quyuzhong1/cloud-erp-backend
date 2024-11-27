package com.erp.server.dmp.inout.handler.output.task.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.enums.DmpReturnInfoStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import com.erp.server.dmp.push.consumer.sdy.SdyDeliveryOrderConsumer;
import com.erp.server.dmp.service.ThirdMappingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdyReturnHandler extends DmpOutputTaskHandler {
    @Resource
    private ThirdMappingService thirdMappingService;
    @Resource
    private SdyDeliveryOrderConsumer sdyDeliveryOrderConsumer;
    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    protected List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
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

        Map<String, String> map = new HashMap<>();
        for(String changId : changeIds) {
            List<ShudiyunB2cOrderDTO> sdyDtoList = this.convert(dmpSoReturnInfoEntityMap.get(changId), dmpSoReturnDetailEntityMap.get(changId));
            if(CollUtil.isNotEmpty(sdyDtoList)) {
                map.put(changId, JSON.toJSONString(sdyDtoList));
            }
        }
        List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = new ArrayList<>();
        if(!map.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            int i = 0;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String dataId = entry.getKey();
                List<ShudiyunB2cOrderDTO> shudiyunB2cOrderDTOList = JSON.parseArray(entry.getValue(), ShudiyunB2cOrderDTO.class);
                for (ShudiyunB2cOrderDTO shudiyunB2cOrderDTO : shudiyunB2cOrderDTOList) {
                    DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = new DmpOutputTaskRecordEntity();
                    String id = identifierGenerator.nextId(dmpOutputTaskRecordEntity).toString();
                    dmpOutputTaskRecordEntity.setId(id);
                    dmpOutputTaskRecordEntity.setMainId(dmpRequest.getOutputTaskId());
                    dmpOutputTaskRecordEntity.setDataId(shudiyunB2cOrderDTO.getBiz_uni_key().substring(dataId.length()));
                    dmpOutputTaskRecordEntity.setSourceCode(shudiyunB2cOrderDTOList.get(0).getBiz_no());
                    dmpOutputTaskRecordEntity.setRequestData(JSON.toJSONString(shudiyunB2cOrderDTO));
                    dmpOutputTaskRecordEntity.setStatus(DmpOutputTaskRecordStatusEnum.INIT.getCode());
                    LocalDateTime insertTime = now.plus(i, ChronoUnit.MILLIS);
                    dmpOutputTaskRecordEntity.setCreateTime(insertTime);
                    dmpOutputTaskRecordEntity.setUpdateTime(insertTime);
                    dmpOutputTaskRecordEntityList.add(dmpOutputTaskRecordEntity);
                    i = i + 1;
                }
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
            status = DmpOutputTaskRecordStatusEnum.ERROR.getCode();
        }

        dmpOutputUtils.updateStatus(id, status, String.valueOf(handle.getData()) , handle.getMsg());
    }

    /**
     * 解析订单数据
     **/
    public List<ShudiyunB2cOrderDTO> convert(DmpSoReturnInfoEntity dmpSoReturnEntity, List<DmpSoReturnDetailEntity> dmpSoReturnDetailEntityList) {
        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<ShudiyunB2cOrderDTO> sdyListDTO = new ArrayList<>();

        for (DmpSoReturnDetailEntity dmpSoReturnDetailEntity : dmpSoReturnDetailEntityList) {
            ShudiyunB2cOrderDTO sdyDTO = new ShudiyunB2cOrderDTO();
            sdyDTO.setBiz_uni_key(dmpSoReturnEntity.getId() + dmpSoReturnDetailEntity.getId());
            sdyDTO.setBiz_no(dmpSoReturnEntity.getThirdCode());

            if (dmpSoReturnEntity.getReturnTime() != null) {
                sdyDTO.setBiz_time(localDateTime.format(dmpSoReturnEntity.getReturnTime()));
            } else {
                return Collections.emptyList();
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

            if (CharSequenceUtil.isNotBlank(dmpSoReturnEntity.getStatus()) && CharSequenceUtil.isNotBlank(DmpReturnInfoStatusEnum.getName(Integer.valueOf(dmpSoReturnEntity.getStatus())))) {
                sdyDTO.setBiz_status(DmpReturnInfoStatusEnum.getName(Integer.valueOf(dmpSoReturnEntity.getStatus())));
            } else {
                sdyDTO.setBiz_status("已完成");
            }
            sdyDTO.setStatus("已创建");
            int qtyTotal = dmpSoReturnDetailEntityList.stream().mapToInt(DmpSoReturnDetailEntity::getQty).sum();
            sdyDTO.setOnline_appled_return_quanty(qtyTotal);
            sdyDTO.setCustomer_refundable_quantity(qtyTotal);
            sdyDTO.setQuantity_buyer_returned(qtyTotal);

            BigDecimal amountTotal = dmpSoReturnDetailEntityList.stream().map(DmpSoReturnDetailEntity::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            sdyDTO.setOnline_applied_amount(amountTotal);
            sdyDTO.setOrder_seller_payed(amountTotal);

            if (PlatformDictEnum.WDT.getCode().equalsIgnoreCase(dmpSoReturnEntity.getSourceSystem())) {
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
                    return Collections.emptyList();
                }

                sdyDTO.setBiz_no(dmpSoReturnEntity.getPlatformCode());
                //查询旺店通对应系统店铺
                List<ThirdMappingEntity> shop = thirdMappingService.lambdaQuery()
                        .eq(ThirdMappingEntity::getType, ThirdSysTypeEnum.SHOP.getCode())
                        .eq(ThirdMappingEntity::getThirdSysType, PlatformDictEnum.WDT.getCode())
                        .eq(ThirdMappingEntity::getThirdCode, dmpSoReturnEntity.getShopId())
                        .list();
                if (CollectionUtils.isNotEmpty(shop)) {
                    ShopInfoEntity shopInfo = FeignQuery.getById(ShopInfoEntity.class, shop.get(0).getSysId());
                    sdyDTO.setShop_no(shopInfo.getId());
                    sdyDTO.setShop_name(shopInfo.getName());

                    CustomerInfoEntity customerInfo = FeignQuery.getById(CustomerInfoEntity.class, shopInfo.getCustomerId());

                    if (ObjectUtil.isNotEmpty(customerInfo)) {
                        //组织编码
                        List<BaseIdDTO.CodeDTO> companyEntities = sysUserFeign.getAccountingCompanyList(Arrays.asList(customerInfo.getFinancialOrganization(), shopInfo.getSalesOrgId()));
                        //销售组织
                        BaseIdDTO.CodeDTO salesOrg = companyEntities.stream().filter(req -> req.getId().equals(shopInfo.getSalesOrgId())).findFirst().orElse(null);
                        sdyDTO.setSales_company_code(salesOrg.getCode());

                        BaseIdDTO.CodeDTO sysAccountingCompanyEntity = companyEntities.stream().filter(req -> req.getId().equals(customerInfo.getFinancialOrganization())).findFirst().orElse(null);
                        if (ObjectUtil.isNotEmpty(sysAccountingCompanyEntity)) {
                            sdyDTO.setReceiving_company_code(sysAccountingCompanyEntity.getCode());
                            sdyDTO.setOrganization_code(sysAccountingCompanyEntity.getCode());
                            sdyDTO.setOrganization_name(sysAccountingCompanyEntity.getName());
                        }
                    }

                    DictCurrencyEntity dictCurrencyEntity = FeignQuery.getById(DictCurrencyEntity.class, shopInfo.getTradeCurrency());
                    if (ObjectUtil.isNotEmpty(dictCurrencyEntity)) {
                        sdyDTO.setTransaction_currency(dictCurrencyEntity.getName());
                    }
                    sdyDTO.setTransaction_currency_code(shopInfo.getTradeCurrency());
                    sdyDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());
                }

            } else {
                String shopId = "";
                if (CharSequenceUtil.isNotBlank(dmpSoReturnEntity.getNextLevelId())) {
                    shopId = dmpSoReturnEntity.getNextLevelId();
                } else {
                    shopId = dmpSoReturnEntity.getShopId();
                }
                ShopInfoEntity shopInfo = FeignQuery.getById(ShopInfoEntity.class, shopId);
                if (ObjectUtil.isEmpty(shopInfo)) {
                    throw new ServiceException(ApiError.ERROR_SDY_NOT_FOUND_SHOP, shopId);
                }
                //组织信息
                List<BaseIdDTO.CodeDTO> companyEntities = new ArrayList<>();
                CustomerInfoEntity customerInfo = FeignQuery.getById(CustomerInfoEntity.class, shopInfo.getCustomerId());
                if (ObjectUtil.isEmpty(customerInfo)) {
                    throw new ServiceException(ApiError.ERROR_SDY_NOT_FOUND_CUSTOMER, shopInfo.getId());
                }

                //收款组织编码
                companyEntities = sysUserFeign.getAccountingCompanyList(Arrays.asList(customerInfo.getFinancialOrganization(), shopInfo.getSalesOrgId()));
                BaseIdDTO.CodeDTO sysAccountingCompanyEntity = companyEntities.stream().filter(req -> req.getId().equals(customerInfo.getFinancialOrganization())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(sysAccountingCompanyEntity)) {
                    sdyDTO.setReceiving_company_code(sysAccountingCompanyEntity.getCode());
                    sdyDTO.setOrganization_code(sysAccountingCompanyEntity.getCode());
                    sdyDTO.setOrganization_name(sysAccountingCompanyEntity.getName());
                }

                String salesOrgCode = companyEntities.stream().filter(req -> req.getId().equals(shopInfo.getSalesOrgId())).map(req -> req.getCode()).findFirst().orElse("");
                sdyDTO.setSales_company_code(salesOrgCode);
                sdyDTO.setShop_no(shopInfo.getId());
                sdyDTO.setShop_name(shopInfo.getName());
                DictCurrencyEntity dictCurrencyEntity = FeignQuery.getById(DictCurrencyEntity.class, shopInfo.getTradeCurrency());
                if (ObjectUtil.isNotEmpty(dictCurrencyEntity)) {
                    sdyDTO.setTransaction_currency(dictCurrencyEntity.getName());
                }
                sdyDTO.setTransaction_currency_code(shopInfo.getTradeCurrency());
                sdyDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());
            }
            sdyDTO.setUnit("PCS");
            sdyDTO.setPlatform_id(dmpSoReturnEntity.getSourceSystem());
            sdyDTO.setPlatform_name(PlatformDictEnum.getNameByCode(dmpSoReturnEntity.getSourceSystem()));
            sdyDTO.setRoot_node_no(dmpSoReturnEntity.getPlatformCode());

            if (dmpSoReturnEntity.getReturnTime() != null) {
                sdyDTO.setRoot_node_create_time(localDateTime.format(dmpSoReturnEntity.getReturnTime()));
            }
            sdyDTO.setRoot_node_modify_time(localDateTime.format(dmpSoReturnEntity.getPlatformUpdateTime()));

            sdyDTO.setGoods_status("已退货");
            sdyDTO.setMsku_code(dmpSoReturnDetailEntity.getSkuNo());
            if (CharSequenceUtil.isBlank(dmpSoReturnDetailEntity.getSkuName())) {
                sdyDTO.setMsku_name(dmpSoReturnDetailEntity.getSkuNo());
            } else {
                sdyDTO.setMsku_name(dmpSoReturnDetailEntity.getSkuName());
            }
            sdyDTO.setReason(dmpSoReturnDetailEntity.getReason());
            sdyDTO.setSource_system("SDC");
            sdyDTO.setRoot_node_no_initial(dmpSoReturnEntity.getPlatformCode());
            sdyListDTO.add(sdyDTO);
        }

        return sdyListDTO;

    }
}
