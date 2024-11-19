package com.erp.server.dmp.inout.handler.output.task.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.server.dmp.enums.DmpRefundInfoStatusEnum;
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
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdyRefundHandler extends DmpOutputTaskHandler {
    @Resource
    private ThirdMappingService thirdMappingService;
    @Resource
    private SdyDeliveryOrderConsumer sdyDeliveryOrderConsumer;

    @Override
    protected List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
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

        Map<String, String> map = new HashMap<>();
        for(String changId : changeIds) {
            List<ShudiyunB2cOrderDTO> sdyDtoList = this.convert(dmpSoRefundInfoEntityMap.get(changId), dmpSoRefundDetailEntityMap.get(changId));
            if(CollUtil.isEmpty(sdyDtoList)) {
                map.put(changId, JSON.toJSONString(sdyDtoList));
            }
        }
        List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = new ArrayList<>();
        if(!map.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            int i = 0;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String dataId = entry.getKey();
                String pushData = entry.getValue();
                List<ShudiyunB2cOrderDTO> shudiyunB2cOrderDTOList = JSON.parseArray(entry.getValue(), ShudiyunB2cOrderDTO.class);
                DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = new DmpOutputTaskRecordEntity();
                String id = identifierGenerator.nextId(dmpOutputTaskRecordEntity).toString();
                dmpOutputTaskRecordEntity.setId(id);
                dmpOutputTaskRecordEntity.setMainId(dmpRequest.getOutputTaskId());
                dmpOutputTaskRecordEntity.setDataId(dataId);
                dmpOutputTaskRecordEntity.setSourceCode(shudiyunB2cOrderDTOList.get(0).getBiz_no());
                dmpOutputTaskRecordEntity.setRequestData(pushData);
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
        String requestData = dmpOutputTaskRecordEntity.getRequestData();
        sdyDeliveryOrderConsumer.handle(requestData);
    }

    /**
     * 解析订单数据
     **/
    public List<ShudiyunB2cOrderDTO> convert(DmpSoRefundInfoEntity dmpSoRefundEntity, List<DmpSoRefundDetailEntity> dmpSoRefundDetailEntityList) {
        List<ShudiyunB2cOrderDTO> sdyListDTO = new ArrayList<>();

        for (DmpSoRefundDetailEntity dmpSoRefundDetailEntity : dmpSoRefundDetailEntityList) {
            ShudiyunB2cOrderDTO sdyDTO = new ShudiyunB2cOrderDTO();
            sdyDTO.setTransaction_unique_key(dmpSoRefundEntity.getId() + dmpSoRefundDetailEntity.getId());
            sdyDTO.setBiz_no(dmpSoRefundEntity.getThirdCode());
            sdyDTO.setBiz_time(dmpSoRefundEntity.getRefundTime());

            //仅退款
            sdyDTO.setTransaction_type("CC");
            sdyDTO.setTransaction_type("110.10.02");

            if (CharSequenceUtil.isNotBlank(dmpSoRefundEntity.getStatus()) && CharSequenceUtil.isNotBlank(DmpRefundInfoStatusEnum.getName(dmpSoRefundEntity.getStatus()))) {
                sdyDTO.setBiz_status(DmpRefundInfoStatusEnum.getName(dmpSoRefundEntity.getStatus()));
            } else {
                sdyDTO.setBiz_status("成功");
            }
            int qtyTotal = dmpSoRefundDetailEntityList.stream().mapToInt(DmpSoRefundDetailEntity::getQty).sum();
            sdyDTO.setOnline_appled_return_quanty(qtyTotal);
            sdyDTO.setCustomer_refundable_quantity(qtyTotal);
            sdyDTO.setQuantity_buyer_returned(qtyTotal);

            BigDecimal amountTotal = dmpSoRefundDetailEntityList.stream().map(DmpSoRefundDetailEntity::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            sdyDTO.setOnline_applied_amount(amountTotal);
            sdyDTO.setOrder_seller_payed(amountTotal);

            if (PlatformDictEnum.WDT.getCode().equals(dmpSoRefundEntity.getSourceSystem())) {
                //查询旺店通对应系统店铺
                List<ThirdMappingEntity> shop = thirdMappingService.lambdaQuery()
                        .eq(ThirdMappingEntity::getType, ThirdSysTypeEnum.SHOP.getCode())
                        .eq(ThirdMappingEntity::getThirdSysType, PlatformDictEnum.WDT.getCode())
                        .eq(ThirdMappingEntity::getThirdId, dmpSoRefundEntity.getShopId())
                        .list();
                if (CollectionUtils.isEmpty(shop)) {
                    throw new ServiceException(ApiError.ERROR_SDY_NOT_FOUND_SHOP_MAPPING, dmpSoRefundEntity.getShopId());
                }
                ShopInfoEntity shopInfo = FeignQuery.getById(ShopInfoEntity.class, shop.get(0).getSysId());
                CustomerInfoEntity customerInfo = FeignQuery.getById(CustomerInfoEntity.class, shopInfo.getCustomerId());

                sdyDTO.setSales_company_code(shopInfo.getSalesOrgId());
                sdyDTO.setSales_company_code(customerInfo.getFinancialOrganization());
                sdyDTO.setReceiving_company_code(customerInfo.getFinancialOrganization());
                sdyDTO.setShop_no(shopInfo.getId());
                sdyDTO.setShop_name(shopInfo.getName());
                DictCurrencyEntity dictCurrencyEntity = FeignQuery.getById(DictCurrencyEntity.class, shopInfo.getTradeCurrency());
                if (ObjectUtil.isNotEmpty(dictCurrencyEntity)) {
                    sdyDTO.setTransaction_currency(dictCurrencyEntity.getName());
                }
                sdyDTO.setTransaction_currency_code(shopInfo.getTradeCurrency());
                sdyDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());
            } else {
                String shopId = "";
                if (CharSequenceUtil.isNotBlank(dmpSoRefundEntity.getNextLevelId())) {
                    shopId = dmpSoRefundEntity.getNextLevelId();
                } else {
                    shopId = dmpSoRefundEntity.getShopId();
                }
                ShopInfoEntity shopInfo = FeignQuery.getById(ShopInfoEntity.class, shopId);
                if (ObjectUtil.isEmpty(shopInfo)) {
                    throw new ServiceException(ApiError.ERROR_SDY_NOT_FOUND_SHOP, shopId);
                }
                CustomerInfoEntity customerInfo = FeignQuery.getById(CustomerInfoEntity.class, shopInfo.getCustomerId());
                if (ObjectUtil.isEmpty(customerInfo)) {
                    throw new ServiceException(ApiError.ERROR_SDY_NOT_FOUND_SHOP, shopInfo.getCustomerId());
                }
                sdyDTO.setSales_company_code(shopInfo.getSalesOrgId());
                sdyDTO.setSales_company_code(customerInfo.getFinancialOrganization());
                sdyDTO.setReceiving_company_code(customerInfo.getFinancialOrganization());
                sdyDTO.setShop_no(shopInfo.getId());
                sdyDTO.setShop_name(shopInfo.getName());
                DictCurrencyEntity dictCurrencyEntity = FeignQuery.getById(DictCurrencyEntity.class, shopInfo.getTradeCurrency());
                if (ObjectUtil.isNotEmpty(dictCurrencyEntity)) {
                    sdyDTO.setTransaction_currency(dictCurrencyEntity.getName());
                }
                sdyDTO.setTransaction_currency_code(shopInfo.getTradeCurrency());
                sdyDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());
            }


            sdyDTO.setPlatform_id(dmpSoRefundEntity.getSourceSystem());
            sdyDTO.setPlatform_name(PlatformDictEnum.getNameByCode(dmpSoRefundEntity.getSourceSystem()));
            sdyDTO.setRoot_node_no(dmpSoRefundEntity.getThirdCode());
            sdyDTO.setRoot_node_create_time(dmpSoRefundEntity.getRefundTime());
            sdyDTO.setRoot_node_modify_time(dmpSoRefundEntity.getPlatformUpdateTime());
            sdyDTO.setGoods_status("10.30");
            sdyDTO.setMsku_code(dmpSoRefundDetailEntity.getSkuNo());
            sdyDTO.setReason(dmpSoRefundEntity.getReason());
            sdyDTO.setSource_system("SDC");
            sdyDTO.setRoot_node_no_initial(dmpSoRefundEntity.getThirdCode());
            sdyListDTO.add(sdyDTO);
        }

        return sdyListDTO;

    }
}
