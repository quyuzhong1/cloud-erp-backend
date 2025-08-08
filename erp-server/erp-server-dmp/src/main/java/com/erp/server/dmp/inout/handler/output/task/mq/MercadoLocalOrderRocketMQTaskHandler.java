package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.*;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.entity.DmpSoReceiverEntity;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class MercadoLocalOrderRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoInfoEntity> dmpSoInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSoDetailEntity>> dmpSoDetailEntityMap = new HashMap<>();
        Map<String, List<DmpSoReceiverEntity>> dmpSoReceiverEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoInfoEntity dmpSoInfoEntity = (DmpSoInfoEntity) v;
                        dmpSoInfoEntityMap.put(dmpSoInfoEntity.getId(), dmpSoInfoEntity);
                    }
                } else if ("dmp_so_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoDetailEntity dmpSoReturnDetailEntity = (DmpSoDetailEntity) v;
                        String mainId = dmpSoReturnDetailEntity.getMainId();
                        List<DmpSoDetailEntity> list = dmpSoDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpSoReturnDetailEntity);
                        dmpSoDetailEntityMap.put(mainId, list);
                    }
                } else if ("dmp_so_receiver".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoReceiverEntity dmpSoReceiverEntity = (DmpSoReceiverEntity) v;
                        String mainId = dmpSoReceiverEntity.getMainId();
                        List<DmpSoReceiverEntity> list = dmpSoReceiverEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
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
                        DmpSoDetailEntity dmpSoReturnDetailEntity = (DmpSoDetailEntity) v;
                        changeIds.add(dmpSoReturnDetailEntity.getMainId());
                    }
                } else if ("dmp_so_receiver".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoReceiverEntity dmpSoReceiverEntity = (DmpSoReceiverEntity) v;
                        changeIds.add(dmpSoReceiverEntity.getMainId());
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        List<DmpSoInfoEntity> list = new ArrayList<>();
        List<DmpSoDetailEntity> dmpSoDetailEntityList = new ArrayList<>();
        List<DmpSoReceiverEntity> dmpSoReceiverEntityList = new ArrayList<>();
        for (String changeId : changeIds) {
            list.add(dmpSoInfoEntityMap.get(changeId));
            List<DmpSoDetailEntity> detailList = dmpSoDetailEntityMap.get(changeId);
            if(CollUtil.isNotEmpty(detailList)) {
            	dmpSoDetailEntityList.addAll(detailList);
            }
            List<DmpSoReceiverEntity> receiverList = dmpSoReceiverEntityMap.get(changeId);
            if(CollUtil.isNotEmpty(receiverList)) {
            	dmpSoReceiverEntityList.addAll(receiverList);
            }
        }

        Map<String, List<DmpSoInfoEntity>> sysReportMap = list.stream().collect(Collectors.groupingBy(DmpSoInfoEntity::getPlatformCode));
        for (Map.Entry<String, List<DmpSoInfoEntity>> stringListEntry : sysReportMap.entrySet()) {
            List<String> ids = stringListEntry.getValue().stream().map(DmpSoInfoEntity::getId).collect(Collectors.toList());
            List<DmpSoDetailEntity> soDetailEntities = dmpSoDetailEntityList.stream().filter(req -> ids.contains(req.getMainId())).collect(Collectors.toList());
            List<DmpSoReceiverEntity> soReceiverEntities = dmpSoReceiverEntityList.stream().filter(req -> ids.contains(req.getMainId())).collect(Collectors.toList());

            PlatformOrderDTO orderDTO = this.convert(stringListEntry.getValue(), soDetailEntities
                    , soReceiverEntities , cfgOutputId);
            if(orderDTO != null) {
                map.put(stringListEntry.getKey(), JSON.toJSONString(orderDTO));
            }
        }
        return map;
    }

    /**
     * 解析订单数据
     **/
    public PlatformOrderDTO convert(List<DmpSoInfoEntity> dmpSoInfoEntityList , List<DmpSoDetailEntity> dmpSoDetailEntityList , List<DmpSoReceiverEntity> dmpSoReceiverEntityList
            , String cfgOutputId) {
        if (CollUtil.isEmpty(dmpSoInfoEntityList)) {
            return null;
        }
        for (DmpSoInfoEntity dmpSoInfoEntity : dmpSoInfoEntityList) {
            if(this.validateDataBlack(dmpSoInfoEntity, cfgOutputId)) {
                return null;
            }
        }

        if (CollUtil.isEmpty(dmpSoDetailEntityList)) {
            return null;
        }
        //设置对应关系
        PlatformOrderDTO orderDTO = new PlatformOrderDTO();

        //平台订单号
        orderDTO.setPlatformCode(dmpSoInfoEntityList.get(0).getPlatformCode());

        //销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode());
        orderDTO.setNfeInvoiceStatus(dmpSoInfoEntityList.get(0).getNfeInvoiceStatus());
        // 店铺ID
        orderDTO.setShopId(dmpSoInfoEntityList.get(0).getNextLevelId());

        //订单金额
        BigDecimal amount = dmpSoInfoEntityList.stream().map(DmpSoInfoEntity::getPayAmount).filter(Objects::nonNull).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        orderDTO.setAmount(amount);
        //币别
        orderDTO.setCurrency(dmpSoInfoEntityList.get(0).getCurrencyCode());

        //付款时间
        //使用 DateTimeFormatter 解析字符串日期
        if (ObjectUtil.isNotEmpty(dmpSoInfoEntityList.get(0).getPayTime())) {
            // 使用Instant类将Unix时间戳转换为LocalDateTime对象
            orderDTO.setPayTime(dmpSoInfoEntityList.get(0).getPayTime());
        }
        //付款方式
        orderDTO.setDictPayMethod(dmpSoInfoEntityList.get(0).getPayMethod());

        //买家备注
        orderDTO.setBuyerRemark(dmpSoInfoEntityList.get(0).getBuyerRemark());

        orderDTO.setRemark(dmpSoInfoEntityList.get(0).getSellRemark());

        // 是否拦截
        orderDTO.setIsIntercept(false);

        // 拦截备注
        orderDTO.setInterceptRemark("");

        // 来源类型
        orderDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());

        // 来源id
        orderDTO.setSourceId(dmpSoInfoEntityList.get(0).getThirdCode());

        // 来源编码
        orderDTO.setSourceCode(dmpSoInfoEntityList.get(0).getThirdCode());

        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");

        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        orderDTO.setInvalidStatus(Boolean.FALSE);

        //付款状态
        if (dmpSoInfoEntityList.get(0).getPayStatus() != null && dmpSoInfoEntityList.get(0).getPayStatus()) {
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
        }

        //运费
        BigDecimal shippingAmount = dmpSoInfoEntityList.stream().map(DmpSoInfoEntity::getShippingAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        orderDTO.setShippingFee(shippingAmount);

        orderDTO.setLabelJson(dmpSoInfoEntityList.get(0).getExtendData());
        orderDTO.setApproveStatusStr(dmpSoInfoEntityList.get(0).getOrderStatus());
        orderDTO.setBillStatus(dmpSoInfoEntityList.get(0).getDeliveryStatus());
        orderDTO.setInvalidStatus(dmpSoInfoEntityList.get(0).getInvalidStatus());
        // 作废类型（manual手动作废，automatic自动作废）
        orderDTO.setInvalidType(dmpSoInfoEntityList.get(0).getInvalidStatus() ? "automatic" : "");

        // 平台订单原始取消状态(已退款,部分退款)
//        DmpOrderReturnStatusEnum dmpBasicSystemCodeEnum = DmpOrderReturnStatusEnum.getByCode(dmpSoInfoEntity.getReturnStatus());
//        if (DmpOrderReturnStatusEnum.NOT_RETURN.equals(dmpBasicSystemCodeEnum)) {
//            orderDTO.setIsCancel(Boolean.FALSE);
//        } else {
//            orderDTO.setIsCancel(Boolean.TRUE);
//        }
        orderDTO.setIsCancel(dmpSoInfoEntityList.get(0).getInvalidStatus());

        //创建时间
        orderDTO.setPlatformOrderCreateTime(dmpSoInfoEntityList.get(0).getPlatformCreateTime());

        //优惠金额
        BigDecimal totalDiscount = dmpSoInfoEntityList.stream().map(DmpSoInfoEntity::getTotalDiscount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        orderDTO.setTotalDiscount(totalDiscount);
        //扩展字段
        orderDTO.setExtendData(dmpSoInfoEntityList.get(0).getExtendData());
        // 税金
        orderDTO.setTotalTaxFee(dmpSoInfoEntityList.get(0).getTotalTaxFee());
        // 税后支付金额
        orderDTO.setAfterTaxAmount(dmpSoInfoEntityList.get(0).getAfterTaxAmount());

        // 订单明细
        List<PlatformOrderDetailDTO> details = parseDetailDto(dmpSoInfoEntityList, dmpSoDetailEntityList);
        orderDTO.setDetails(details);
        if(CollUtil.isNotEmpty(dmpSoReceiverEntityList)) {
        	//B2C销售订单买家信息表
            orderDTO.setReceiver(parseReceiver(dmpSoReceiverEntityList.get(0)));
        }
        //B2C销售订单物流信息表
        orderDTO.setLogisticsList(parseLogistics(dmpSoInfoEntityList));
        //B2C销售订单财务信息表
        orderDTO.setFinances(parseFinances(dmpSoInfoEntityList, dmpSoDetailEntityList));
        return orderDTO;
    }

    /**
     * 批量转换明细
     */
    public static List<PlatformOrderDetailDTO> parseDetailDto(List<DmpSoInfoEntity> dmpSoInfoEntityList, List<DmpSoDetailEntity> dmpSoDetailEntities) {
        return dmpSoDetailEntities.stream()
                .map(e -> intPlatformOrderDetailDTO(dmpSoInfoEntityList, e))
                .collect(Collectors.toList());
    }


    /**
     * 转换明细
     */
    private static PlatformOrderDetailDTO intPlatformOrderDetailDTO(List<DmpSoInfoEntity> dmpSoInfoEntityList, DmpSoDetailEntity soDetailEntity) {
        PlatformOrderDetailDTO detailDTO = new PlatformOrderDetailDTO();

        // 图片URL
        detailDTO.setImageUrl("");
        // skuId
        detailDTO.setSkuId("");
        // skuNo
        detailDTO.setSkuNo("");

        //平台明细行号
        detailDTO.setPlatformLineNumber("");

        // 平台sku编号
        detailDTO.setPlatformSkuNo(soDetailEntity.getPlatformSku());

        //销售费用
        detailDTO.setSaleFee(soDetailEntity.getSaleFee());

        //平台产品id
        detailDTO.setPlatformSpuNo(soDetailEntity.getPlatformSpuNo());

        // 库存sku编号
        detailDTO.setWarehouseName("");
        // 仓库名称
        // 库存是否扣除
        detailDTO.setWarehouseId("");
        // 数量
        detailDTO.setQty(soDetailEntity.getQty());
        // 金额
        detailDTO.setAmount(soDetailEntity.getAfterAmount());
        // 单价
        detailDTO.setPrice(NumberUtil.toBigDecimal(soDetailEntity.getSellPriceOrigin()));
        // 币别（原币）
        detailDTO.setCurrency(dmpSoInfoEntityList.get(0).getCurrencyCode());
        // 汇率
        detailDTO.setExchangeRate(BigDecimal.ZERO);
        // 建议售价（本位币）
        detailDTO.setAdvicePrice(BigDecimal.ZERO);
        // 含税成本（本位币）
        detailDTO.setTaxCost(BigDecimal.ZERO);
        // 来源明细id
        detailDTO.setSourceDetailId(soDetailEntity.getThirdDetailId());

        // 当前明细标签
        Map<String, Object> lableMap = new HashMap<>();
        JSONObject jsonObject = JSONObject.parseObject(dmpSoInfoEntityList.get(0).getExtendData());
        Set<String> refundedLineItemIds = (Set<String>) jsonObject.get("refundedLineItemIds");
        if (!CollectionUtils.isEmpty(refundedLineItemIds) && refundedLineItemIds.contains(soDetailEntity.getThirdDetailId())) {
            lableMap.put("isRefunded", true);
            detailDTO.setIsDetailRefund(true);
        } else {
            lableMap.put("isRefunded", false);
        }
        detailDTO.setLabelJson(JSONUtil.toJsonStr(lableMap));

        // 库存组织id
        detailDTO.setWarehouseOrgId("");
        // 库存组织名称
        detailDTO.setWarehouseOrgName("");
        // 库位
        detailDTO.setWarehouseLocation("");
        //包裹号
        detailDTO.setPlatformPackageId(soDetailEntity.getPlatformPackageId());

        return detailDTO;
    }


    /**
     * 买家信息字段处理
     * @Author Luo_WG
     * @Date 2023/12/4 9:38
     * @param soReceiverEntity
     * @return java.util.List<com.common.business.dto.PlatformOrderReceiverDTO>
     **/
    private static PlatformOrderReceiverDTO parseReceiver(DmpSoReceiverEntity soReceiverEntity) {
        if (Objects.isNull(soReceiverEntity)) {
            return null;
        }

        return PlatformOrderReceiverDTO.builder()
                .loginId(String.valueOf(soReceiverEntity.getBuyerId()))
                .customerId(soReceiverEntity.getBuyerId())
                .name(soReceiverEntity.getBuyerName())
                .receiverName(soReceiverEntity.getReceiverName())
                .telNumber(soReceiverEntity.getMainPhone())
                .receiverTelNumber(soReceiverEntity.getReceiverTelNumber())
                .email(soReceiverEntity.getEmail())
                .country(soReceiverEntity.getCountry())
                .provinceName(soReceiverEntity.getProvince())
                .cityName(soReceiverEntity.getCity())
                .districtName(soReceiverEntity.getDistrict())
                .postCode(soReceiverEntity.getPostCode())
                .firstAddress(soReceiverEntity.getMainStreet())
                .secondAddress(soReceiverEntity.getSecondStreet())
                .fullAddress(soReceiverEntity.getFullAddress())
                .receiverTaxNo(soReceiverEntity.getReceiverTaxNo())
                .ieNo(soReceiverEntity.getIeNo())
                .taxidType(soReceiverEntity.getTaxidType())
                .build();
    }


    /**
     * 物流信息字段处理
     * @Author Luo_WG
     * @Date 2023/12/4 14:07
     * @param dmpSoInfoEntityList
     * @return java.util.List<com.common.business.dto.PlatformOrderLogisticsDTO>
     **/
    private static List<PlatformOrderLogisticsDTO> parseLogistics(List<DmpSoInfoEntity> dmpSoInfoEntityList) {
        if (CollUtil.isEmpty(dmpSoInfoEntityList)) {
            return Collections.emptyList();
        }

        BigDecimal cost = BigDecimal.ZERO;
        JSONObject jsonObject = JSONObject.parseObject(dmpSoInfoEntityList.get(0).getExtendData());
        if (jsonObject.get("cost") != null) {
            cost = MathUtil.valueOf(jsonObject.get("cost"));
        }
        BigDecimal shippingAmount = dmpSoInfoEntityList.stream().map(DmpSoInfoEntity::getShippingAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

        List<PlatformOrderLogisticsDTO> logisticsDTOS = new ArrayList<>();
        PlatformOrderLogisticsDTO dto = PlatformOrderLogisticsDTO.builder()
//                .code(dmpSoInfoEntity.getLogisticsCode())
                .name(dmpSoInfoEntityList.get(0).getLogisticsName())
                .deliveryTime(dmpSoInfoEntityList.get(0).getDeliveryTime())
                .logisticsChannelId(dmpSoInfoEntityList.get(0).getLogisticsChannelId())
                .logisticsChannelName(dmpSoInfoEntityList.get(0).getLogisticsChannelName())
                .estimatedShippingCost(cost)
                .actualShippingCost(shippingAmount)
                .accessoriesCostCurrency("")
                .actualShippingCurrency("")
                .estimatedShippingCurrency("")
                .logisticType(dmpSoInfoEntityList.get(0).getLogisticType())
                .build();
        logisticsDTOS.add(dto);
        return logisticsDTOS;
    }

    /**
     * 财务信息表
     * @Author Luo_WG
     * @Date 2023/12/4 14:07
     * @param dmpSoInfoEntityList
     * @return java.util.List<com.common.business.dto.PlatformOrderFinanceDTO>
     **/
    private static PlatformOrderFinanceDTO parseFinances(List<DmpSoInfoEntity> dmpSoInfoEntityList, List<DmpSoDetailEntity> dmpSoDetailEntities) {
        BigDecimal taxesAmount = BigDecimal.ZERO;
        BigDecimal shippingAmount = BigDecimal.ZERO;
        for (DmpSoInfoEntity dmpSoInfoEntity : dmpSoInfoEntityList) {
            JSONObject jsonObject = JSONObject.parseObject(dmpSoInfoEntity.getExtendData());
            if (jsonObject.get("taxesAmount") != null) {
                taxesAmount = taxesAmount.add(MathUtil.valueOf(jsonObject.get("taxesAmount")));
            }
            shippingAmount = shippingAmount.add(dmpSoInfoEntity.getShippingAmount());
        }

        return PlatformOrderFinanceDTO.builder()
                .currency(dmpSoInfoEntityList.get(0).getCurrencyCode())
                .shippingCost(shippingAmount)
                .vatRate(taxesAmount)
                .build();
    }
    
    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("platformCode");
    }
}
