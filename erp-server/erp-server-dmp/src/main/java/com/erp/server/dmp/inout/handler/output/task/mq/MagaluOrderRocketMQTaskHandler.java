package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.business.dto.PlatformOrderFinanceDTO;
import com.common.business.dto.PlatformOrderLogisticsDTO;
import com.common.business.dto.PlatformOrderReceiverDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.entity.DmpSoReceiverEntity;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class MagaluOrderRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    private static final String MAGALU_PLATFORM = "Magalu";
    private static final String SOURCE_TYPE_SO_B2C = "soB2c";

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoInfoEntity> dmpSoInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSoDetailEntity>> dmpSoDetailEntityMap = new HashMap<>();
        Map<String, List<DmpSoReceiverEntity>> dmpSoReceiverEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = entry.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            String storageName = entry.getKey().getStorageName();
            if ("dmp_so_info".equals(storageName)) {
                for (BaseEntity item : value) {
                    DmpSoInfoEntity entity = (DmpSoInfoEntity) item;
                    dmpSoInfoEntityMap.put(entity.getId(), entity);
                }
            } else if ("dmp_so_detail".equals(storageName)) {
                for (BaseEntity item : value) {
                    DmpSoDetailEntity entity = (DmpSoDetailEntity) item;
                    dmpSoDetailEntityMap.computeIfAbsent(entity.getMainId(), k -> new ArrayList<>()).add(entity);
                }
            } else if ("dmp_so_receiver".equals(storageName)) {
                for (BaseEntity item : value) {
                    DmpSoReceiverEntity entity = (DmpSoReceiverEntity) item;
                    dmpSoReceiverEntityMap.computeIfAbsent(entity.getMainId(), k -> new ArrayList<>()).add(entity);
                }
            }
        }

        Set<String> changeIds = new HashSet<>();
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : changeMaps.entrySet()) {
            List<BaseEntity> value = entry.getValue();
            if (CollUtil.isEmpty(value)) {
                continue;
            }
            String storageName = entry.getKey().getStorageName();
            if ("dmp_so_info".equals(storageName)) {
                value.forEach(e -> changeIds.add(e.getId()));
            } else if ("dmp_so_detail".equals(storageName)) {
                value.forEach(e -> changeIds.add(((DmpSoDetailEntity) e).getMainId()));
            } else if ("dmp_so_receiver".equals(storageName)) {
                value.forEach(e -> changeIds.add(((DmpSoReceiverEntity) e).getMainId()));
            }
        }

        Map<String, String> resultMap = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changeId : changeIds) {
            PlatformOrderDTO orderDTO = convert(
                    dmpSoInfoEntityMap.get(changeId),
                    dmpSoDetailEntityMap.get(changeId),
                    dmpSoReceiverEntityMap.get(changeId),
                    cfgOutputId);
            if (orderDTO != null) {
                resultMap.put(changeId, JSON.toJSONString(orderDTO));
            }
        }
        return resultMap;
    }

    private PlatformOrderDTO convert(DmpSoInfoEntity main, List<DmpSoDetailEntity> detailList, List<DmpSoReceiverEntity> receiverList, String cfgOutputId) {
        if (main == null || validateDataBlack(main, cfgOutputId) || CollUtil.isEmpty(detailList)) {
            return null;
        }
        PlatformOrderDTO orderDTO = new PlatformOrderDTO();
        orderDTO.setBillDate(main.getPlatformCreateTime() == null ? null : main.getPlatformCreateTime().toLocalDate());
        orderDTO.setPlatformOrderCreateTime(main.getPlatformCreateTime());
        orderDTO.setPlatformCode(main.getPlatformCode());
        orderDTO.setSellerOrderCode(main.getThirdCode());
        orderDTO.setThirdSystem(MAGALU_PLATFORM);
        orderDTO.setThirdCode(main.getThirdCode());
        orderDTO.setDictPlatform(MAGALU_PLATFORM);
        orderDTO.setShopId(main.getShopId());
        orderDTO.setInvalidStatus(main.getInvalidStatus());
        orderDTO.setInvalidType(Boolean.TRUE.equals(main.getInvalidStatus()) ? "automatic" : "");
        orderDTO.setBillStatus(main.getDeliveryStatus());
        orderDTO.setApproveStatusStr(main.getOrderStatus());
        orderDTO.setPlatformOrderStatus(main.getPlatformOriginalStatus());
        orderDTO.setPayStatus(Boolean.TRUE.equals(main.getPayStatus()) ? SoB2cPayStatusEnum.ENUM_PAID.getCode() : SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
        orderDTO.setAmount(main.getPayAmount());
        orderDTO.setCurrency(main.getCurrencyCode());
        orderDTO.setExchangeRate(BigDecimal.ONE);
        orderDTO.setShippingFee(main.getShippingAmount());
        orderDTO.setPayTime(main.getPayTime());
        orderDTO.setPayAmount(main.getPayAmount());
        orderDTO.setDictPayMethod(main.getPayMethod());
        orderDTO.setBuyerRemark(main.getBuyerRemark());
        orderDTO.setRemark(main.getRemark());
        orderDTO.setIsIntercept(false);
        orderDTO.setInterceptRemark("");
        orderDTO.setSourceType(SOURCE_TYPE_SO_B2C);
        orderDTO.setSourceId(main.getThirdCode());
        orderDTO.setSourceCode(main.getThirdCode());
        orderDTO.setAbnormalType("");
        orderDTO.setSyncKingdeeStatus("0");
        orderDTO.setLabelJson(main.getExtendData());
        orderDTO.setExtendData(main.getExtendData());
        orderDTO.setTotalDiscount(main.getTotalDiscount());
        orderDTO.setTotalTaxFee(main.getTotalTaxFee());
        orderDTO.setAfterTaxAmount(main.getAfterTaxAmount());
        orderDTO.setIsCancel(main.getIsCancel());
        orderDTO.setDetails(parseDetails(detailList));
        if (CollUtil.isNotEmpty(receiverList)) {
            orderDTO.setReceiver(parseReceiver(receiverList.get(0)));
        }
        orderDTO.setLogisticsList(parseLogistics(main));
        orderDTO.setFinances(PlatformOrderFinanceDTO.builder()
                .currency(main.getCurrencyCode())
                .shippingCost(main.getShippingAmount())
                .platformCost(main.getPlatformCost())
                .build());
        orderDTO.setPlatform(MAGALU_PLATFORM);
        orderDTO.setUniqueId(main.getId());
        return orderDTO;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("platformCode");
    }

    private List<PlatformOrderDetailDTO> parseDetails(List<DmpSoDetailEntity> detailList) {
        return detailList.stream().map(entity -> {
            PlatformOrderDetailDTO dto = new PlatformOrderDetailDTO();
            dto.setImageUrl(entity.getSkuUrl());
            dto.setSkuId(entity.getSkuId());
            dto.setSkuNo(entity.getSkuNo());
            dto.setPlatformSkuNo(entity.getPlatformSku());
            dto.setPlatformSkuId(entity.getPlatformSkuId());
            dto.setPlatformSpuNo(entity.getPlatformSpuNo());
            dto.setQty(entity.getQty());
            dto.setWarehouseId(entity.getWarehouseId());
            dto.setWarehouseName(entity.getWarehouseName());
            dto.setPrice(entity.getSellPrice());
            dto.setAmount(entity.getAfterAmount());
            dto.setCurrency(entity.getCurrencyCode());
            dto.setExchangeRate(entity.getExchangeRate());
            dto.setSourceDetailId(entity.getThirdDetailId());
            dto.setThirdDetailId(entity.getThirdDetailId());
            dto.setLabelJson(entity.getExtendData());
            dto.setExtendData(entity.getExtendData());
            dto.setPlatformLineNumber(entity.getPlatformDetailId());
            dto.setPlatformPackageId(entity.getPlatformPackageId());
            dto.setPlatformSubSoCode(entity.getPlatformPackageId());
            dto.setSaleFee(entity.getSaleFee());
            return dto;
        }).collect(Collectors.toList());
    }

    private PlatformOrderReceiverDTO parseReceiver(DmpSoReceiverEntity entity) {
        return PlatformOrderReceiverDTO.builder()
                .loginId(entity.getBuyerId())
                .customerId(entity.getBuyerId())
                .name(entity.getBuyerName())
                .email(entity.getEmail())
                .telNumber(entity.getReceiverTelNumber())
                .receiverName(entity.getReceiverName())
                .receiverTelNumber(entity.getReceiverTelNumber())
                .postCode(entity.getPostCode())
                .firstAddress(entity.getMainStreet())
                .secondAddress(entity.getSecondStreet())
                .fullAddress(entity.getFullAddress())
                .country(entity.getCountry())
                .provinceName(entity.getProvince())
                .cityName(entity.getCity())
                .districtName(entity.getDistrict())
                .receiverTaxNo(entity.getReceiverTaxNo())
                .taxidType(entity.getTaxidType())
                .build();
    }

    private List<PlatformOrderLogisticsDTO> parseLogistics(DmpSoInfoEntity main) {
        PlatformOrderLogisticsDTO logisticsDTO = new PlatformOrderLogisticsDTO();
        logisticsDTO.setCode(main.getLogisticsCode());
        logisticsDTO.setName(main.getLogisticsName());
        logisticsDTO.setDeliveryTime(main.getDeliveryTime());
        logisticsDTO.setActualShippingCost(main.getActualShippingFee());
        logisticsDTO.setEstimatedShippingCost(main.getEstimatedShippingFee());
        logisticsDTO.setLogisticType(main.getLogisticType());
        logisticsDTO.setBuyerSelectedLogistics(main.getBuyerSelectedLogistics());
        logisticsDTO.setPlanPackageNo(main.getPlanPackageNo());
        logisticsDTO.setPlanSupplierId(main.getPlanSupplierId());
        return Arrays.asList(logisticsDTO);
    }
}
