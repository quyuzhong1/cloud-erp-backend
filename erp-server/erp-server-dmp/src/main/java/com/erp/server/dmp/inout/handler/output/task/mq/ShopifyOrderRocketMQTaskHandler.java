package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.*;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpOrderReturnStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cItemStatusEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class ShopifyOrderRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoInfoEntity> dmpSoInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSoDetailEntity>> dmpSoDetailEntityMap = new HashMap<>();
        Map<String, List<DmpSoReceiverEntity>> dmpSoReceiverEntityMap = new HashMap<>();

        // Map<平台订单ID, DMP订单ID>
        Map<String, String> platformOrderIdDmpSoIdMap = new HashMap<>();

        // 退货信息 Map<DMP退货单ID, DMP订单ID>
        Map<String, String> dmpReturnIdAndDmpSoIdMap = new HashMap<>();
        // Map<DMP订单ID, 退货单列表>
        Map<String, List<DmpSoReturnInfoEntity>> dmpSoReturnInfoEntityMap = new HashMap<>();
        // Map<DMP退货订单ID, 退货单列表>
        Map<String, List<DmpSoReturnDetailEntity>> dmpSoReturnDetailEntityMap = new HashMap<>();

        // 退款信息  Map<DMP退款单ID, DMP订单ID>
        Map<String, String> dmpRefundIdAndDmpSoIdMap = new HashMap<>();
        // Map<DMP订单ID, 退款单列表>
        Map<String, List<DmpSoRefundInfoEntity>> dmpSoRefundInfoEntityMap = new HashMap<>();
        //  Map<退款单中台ID, 退款单列表>>
        Map<String, List<DmpSoRefundDetailEntity>> dmpSoRefundDetailEntityMap = new HashMap<>();

        // convert按order排序固定解析顺序
        List<Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>>> sortedEntryMap = convertInputDmpBaseEntityListMaps.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(entry -> entry.getKey().getOrder())) // 按 order 字段排序
                .collect(Collectors.toList());

        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : sortedEntryMap) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoInfoEntity dmpSoInfoEntity = (DmpSoInfoEntity) v;
                        dmpSoInfoEntityMap.put(dmpSoInfoEntity.getId(), dmpSoInfoEntity);
                        // 记录 Map<平台订单ID, DMP订单ID>
                        platformOrderIdDmpSoIdMap.put(dmpSoInfoEntity.getPlatformCode(), dmpSoInfoEntity.getId());
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
                } else if ("dmp_so_return_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoReturnInfoEntity dmpEntity = (DmpSoReturnInfoEntity) v;
                        String platformOrderId = dmpEntity.getPlatformCode();
                        List<DmpSoReturnInfoEntity> list = dmpSoReturnInfoEntityMap.get(platformOrderId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpEntity);
                        String dmpOrderId = platformOrderIdDmpSoIdMap.get(platformOrderId);
                        dmpSoReturnInfoEntityMap.put(dmpOrderId, list);
                        dmpReturnIdAndDmpSoIdMap.put(dmpEntity.getId(), dmpOrderId);
                    }
                } else if ("dmp_so_return_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoReturnDetailEntity dmpEntity = (DmpSoReturnDetailEntity) v;
                        String mainId = dmpEntity.getMainId();
                        List<DmpSoReturnDetailEntity> list = dmpSoReturnDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpEntity);
                        dmpSoReturnDetailEntityMap.put(mainId, list);
                    }
                } else if ("dmp_so_refund_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoRefundInfoEntity dmpEntity = (DmpSoRefundInfoEntity) v;
                        String platformOrderId = dmpEntity.getPlatformCode();
                        List<DmpSoRefundInfoEntity> list = dmpSoRefundInfoEntityMap.get(platformOrderId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpEntity);
                        String dmpOrderId = platformOrderIdDmpSoIdMap.get(platformOrderId);
                        dmpSoRefundInfoEntityMap.put(dmpOrderId, list);
                        dmpRefundIdAndDmpSoIdMap.put(dmpEntity.getId(), dmpOrderId);
                    }
                } else if ("dmp_so_refund_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoRefundDetailEntity dmpEntity = (DmpSoRefundDetailEntity) v;
                        String mainId = dmpEntity.getMainId();
                        List<DmpSoRefundDetailEntity> list = dmpSoRefundDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpEntity);
                        dmpSoRefundDetailEntityMap.put(mainId, list);
                    }
                }
            }
        }

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        // DMP订单ID
        Set<String> changeIds = new HashSet<>();
        // 退货单变更<DMP订单ID, DMP退货单IDS>
        Map<String, Set<String>> returnChangeIds = new HashMap<>();
        // 退款单变更<DMP订单ID, DMP退款单IDS>
        Map<String, Set<String>>refundChangeIds = new HashMap<>();

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
                } else if ("dmp_so_return_detail".equals(storageName)) {
                    // 仅判断明细，主表不用判断(退货单的主表是由明细合并)
                    for (BaseEntity v : value) {
                        DmpSoReturnDetailEntity dmpEntity = (DmpSoReturnDetailEntity) v;
                        String mainId = dmpEntity.getMainId();
                        String dmpSoId = dmpReturnIdAndDmpSoIdMap.get(mainId);
                        changeIds.add(dmpSoId);
                        Set<String> dmpReturnIds = returnChangeIds.get(dmpSoId);
                        if (CollUtil.isEmpty(dmpReturnIds)){
                            dmpReturnIds = new LinkedHashSet<>();
                        }
                        dmpReturnIds.add(dmpEntity.getMainId());
                        returnChangeIds.put(dmpSoId, dmpReturnIds);
                    }
                } else if ("dmp_so_refund_info".equals(storageName)) {
                    // 仅判断明细，主表不用判断
                    for (BaseEntity v : value) {
                        DmpSoRefundInfoEntity dmpEntity = (DmpSoRefundInfoEntity) v;
                        String dmpSoId = dmpRefundIdAndDmpSoIdMap.get(dmpEntity.getId());
                        changeIds.add(dmpSoId);
                        Set<String> dmpRefundIds = refundChangeIds.get(dmpSoId);
                        if (CollUtil.isEmpty(dmpRefundIds)){
                            dmpRefundIds = new LinkedHashSet<>();
                        }
                        dmpRefundIds.add(dmpEntity.getId());
                        refundChangeIds.put(dmpSoId, dmpRefundIds);
                    }
                } else if ("dmp_so_refund_detail".equals(storageName)) {
                    // 仅判断明细，主表不用判断
                    for (BaseEntity v : value) {
                        DmpSoRefundDetailEntity dmpEntity = (DmpSoRefundDetailEntity) v;
                        String mainId = dmpEntity.getMainId();

                        String dmpSoId = dmpRefundIdAndDmpSoIdMap.get(mainId);
                        changeIds.add(dmpSoId);
                        Set<String> dmpRefundIds = refundChangeIds.get(dmpSoId);
                        if (CollUtil.isEmpty(dmpRefundIds)){
                            dmpRefundIds = new LinkedHashSet<>();
                        }
                        dmpRefundIds.add(dmpEntity.getMainId());
                        refundChangeIds.put(dmpSoId, dmpRefundIds);
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
            // 退货单变更
            Map<DmpSoReturnInfoEntity, List<DmpSoReturnDetailEntity>> changeReturnMap = new HashMap<>();
            Set<String> curChangeReturnIds = returnChangeIds.get(changId);
            if (CollUtil.isNotEmpty(curChangeReturnIds)) {
                // 变更的退货单
                List<DmpSoReturnInfoEntity> curReturnInfoList = dmpSoReturnInfoEntityMap.values()
                        .stream()
                        .flatMap(List::stream)
                        .filter(e -> curChangeReturnIds.contains(e.getId()))
                        .collect(Collectors.toList());
                if (CollUtil.isNotEmpty(curReturnInfoList)) {
                    for (DmpSoReturnInfoEntity soReturnInfoEntity : curReturnInfoList) {
                        List<DmpSoReturnDetailEntity> returnEntityList = dmpSoReturnDetailEntityMap.get(soReturnInfoEntity.getId());
                        if (CollUtil.isEmpty(returnEntityList)) {
                            ServiceException.runError("Shopify退货单数据异常，找不到明细：mainId={}", soReturnInfoEntity.getId());
                        }
                        changeReturnMap.put(soReturnInfoEntity, returnEntityList);
                    }
                }
            }
            // 退款单变更
            Map<DmpSoRefundInfoEntity, List<DmpSoRefundDetailEntity>> changeRefundMap = new HashMap<>();
            Set<String> curChangeRefundIds = refundChangeIds.get(changId);
            if (CollUtil.isNotEmpty(curChangeRefundIds)) {
                // 变更的退货单
                List<DmpSoRefundInfoEntity> curReturnInfoList = dmpSoRefundInfoEntityMap.values()
                        .stream()
                        .flatMap(List::stream)
                        .filter(e -> curChangeRefundIds.contains(e.getId()))
                        .collect(Collectors.toList());
                if (CollUtil.isNotEmpty(curReturnInfoList)) {
                    for (DmpSoRefundInfoEntity soRefundInfoEntity : curReturnInfoList) {
                        List<DmpSoRefundDetailEntity> refundEntityList = dmpSoRefundDetailEntityMap.get(soRefundInfoEntity.getId());
//                        if (CollUtil.isEmpty(refundEntityList)) {
//                            ServiceException.runError("Shopify退款单数据异常，找不到明细：mainId={}", soRefundInfoEntity.getId());
//                        }
                        changeRefundMap.put(soRefundInfoEntity, refundEntityList);
                    }
                }
            }

            PlatformOrderDTO orderDTO = this.convert(dmpSoInfoEntityMap.get(changId),
                    dmpSoDetailEntityMap.get(changId),
                    dmpSoReceiverEntityMap.get(changId),
                    cfgOutputId,
                    changeReturnMap,
                    changeRefundMap
            );
            if (orderDTO != null) {
                map.put(changId, JSON.toJSONString(orderDTO));
            }
        }
        return map;
    }

    /**
     * 解析订单数据
     **/
    public PlatformOrderDTO convert(DmpSoInfoEntity dmpSoInfoEntity,
                                    List<DmpSoDetailEntity> dmpSoDetailEntityList,
                                    List<DmpSoReceiverEntity> dmpSoReceiverEntityList,
                                    String cfgOutputId,
                                    Map<DmpSoReturnInfoEntity, List<DmpSoReturnDetailEntity>> changeReturnMap,
                                    Map<DmpSoRefundInfoEntity, List<DmpSoRefundDetailEntity>> changeRefundMap
    ) {
        if (this.validateDataBlack(dmpSoInfoEntity, cfgOutputId)) {
            return null;
        }
        if (CollUtil.isEmpty(dmpSoDetailEntityList)) {
            return null;
        }
        //设置对应关系
        PlatformOrderDTO orderDTO = new PlatformOrderDTO();

        //平台订单号
        orderDTO.setPlatformCode(dmpSoInfoEntity.getPlatformCode());

        //销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.SHOPIFY.getCode());

        // 店铺ID
        orderDTO.setShopId(dmpSoInfoEntity.getNextLevelId());

        //订单金额
        BigDecimal amount = NumberUtil.toBigDecimal(dmpSoInfoEntity.getPayAmount());
        orderDTO.setAmount(amount);
        //币别
        orderDTO.setCurrency(dmpSoInfoEntity.getCurrencyCode());

        //付款时间
        //使用 DateTimeFormatter 解析字符串日期
        if (ObjectUtil.isNotEmpty(dmpSoInfoEntity.getPayTime())) {
            // 使用Instant类将Unix时间戳转换为LocalDateTime对象
            orderDTO.setPayTime(dmpSoInfoEntity.getPayTime());

            //付款方式
            orderDTO.setDictPayMethod(dmpSoInfoEntity.getPayMethod());
        }

        //买家备注
        orderDTO.setBuyerRemark(dmpSoInfoEntity.getBuyerRemark());

        // 是否拦截
        orderDTO.setIsIntercept(false);

        // 拦截备注
        orderDTO.setInterceptRemark("");

        // 来源类型
        orderDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());

        // 来源id
        orderDTO.setSourceId(dmpSoInfoEntity.getThirdCode());

        // 来源编码
        orderDTO.setSourceCode(dmpSoInfoEntity.getThirdCode());

        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");

        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        orderDTO.setInvalidStatus(Boolean.FALSE);

        //付款状态
        if (dmpSoInfoEntity.getPayTime() != null) {
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
        } else {
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
        }

        orderDTO.setApproveStatusStr(dmpSoInfoEntity.getOrderStatus());
        orderDTO.setBillStatus(dmpSoInfoEntity.getDeliveryStatus());
        orderDTO.setInvalidStatus(dmpSoInfoEntity.getInvalidStatus());
        // 作废类型（manual手动作废，automatic自动作废）
        orderDTO.setInvalidType(dmpSoInfoEntity.getInvalidStatus() ? "automatic" : "");

        // 订单明细
        List<PlatformOrderDetailDTO> details = parseDetailDto(dmpSoInfoEntity, dmpSoDetailEntityList);
        orderDTO.setDetails(details);

        // 卖家单号
        JSONObject jsonObject = JSONObject.parseObject(dmpSoInfoEntity.getExtendData());
        if (jsonObject.get("sellerOrderCode") != null) {
            orderDTO.setSellerOrderCode(jsonObject.get("sellerOrderCode") + "");
        }

        // 平台订单原始取消状态(已退款,部分退款)
        DmpOrderReturnStatusEnum dmpBasicSystemCodeEnum = DmpOrderReturnStatusEnum.getByCode(dmpSoInfoEntity.getReturnStatus());
        // 整单退款 或 明细存在退货 才推送取消状态
        if (DmpOrderReturnStatusEnum.ORDER_RETURN.equals(dmpBasicSystemCodeEnum)
            || details.stream().anyMatch(PlatformOrderDetailDTO::getIsDetailRefund)
        ) {
            orderDTO.setIsCancel(Boolean.TRUE);
        } else {
            orderDTO.setIsCancel(Boolean.FALSE);
        }
        // 主单退款标签(包含退款/部分退款)
        if (dmpBasicSystemCodeEnum != null && !DmpOrderReturnStatusEnum.NOT_RETURN.equals(dmpBasicSystemCodeEnum)) {
            jsonObject.put("isRefunded", true);
        }

        orderDTO.setLabelJson(JSON.toJSONString(jsonObject));
        //创建时间
        orderDTO.setPlatformOrderCreateTime(dmpSoInfoEntity.getPlatformCreateTime());

        //总优惠
        orderDTO.setTotalDiscount(dmpSoInfoEntity.getTotalDiscount());

        //B2C销售订单买家信息表
        orderDTO.setReceiver(parseReceiver(dmpSoInfoEntity, dmpSoReceiverEntityList.get(0)));
        //B2C销售订单物流信息表
        orderDTO.setLogisticsList(parseLogistics(dmpSoInfoEntity));
        //B2C销售订单财务信息表
        orderDTO.setFinances(parseFinances(dmpSoInfoEntity, dmpSoDetailEntityList));

        // 退货订单
        orderDTO.setReturnDTOList(parseReturnList(changeReturnMap, cfgOutputId));

        // 退款订单
        orderDTO.setRefundDTOList(parseRefundList(changeRefundMap, cfgOutputId));

        return orderDTO;
    }


    /**
     * 批量转换明细
     */
    public static List<PlatformOrderDetailDTO> parseDetailDto(DmpSoInfoEntity dmpSoInfoEntity, List<DmpSoDetailEntity> dmpSoDetailEntities) {
        return dmpSoDetailEntities.stream()
                .map(e -> intPlatformOrderDetailDTO(dmpSoInfoEntity, e))
                .collect(Collectors.toList());
    }


    /**
     * 转换明细
     */
    private static PlatformOrderDetailDTO intPlatformOrderDetailDTO(DmpSoInfoEntity dmpSoInfoEntity, DmpSoDetailEntity soDetailEntity) {
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
        detailDTO.setPrice(NumberUtil.toBigDecimal(soDetailEntity.getSellPrice()));
        // 币别（原币）
        detailDTO.setCurrency(dmpSoInfoEntity.getCurrencyCode());
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
        JSONObject jsonObject = JSONObject.parseObject(dmpSoInfoEntity.getExtendData());
        if (jsonObject.get("refundedLineItemIds") != null) {
            List<String> refundedLineItemIds = (List<String>) jsonObject.get("refundedLineItemIds");
            if (!CollectionUtils.isEmpty(refundedLineItemIds) && refundedLineItemIds.contains(soDetailEntity.getThirdDetailId())) {
                lableMap.put("isRefunded", true);
                detailDTO.setIsDetailRefund(true);
            } else {
                lableMap.put("isRefunded", false);
            }
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
     *
     * @param soReceiverEntity
     * @return java.util.List<com.common.business.dto.PlatformOrderReceiverDTO>
     * @Author Luo_WG
     * @Date 2023/12/4 9:38
     **/
    private static PlatformOrderReceiverDTO parseReceiver(DmpSoInfoEntity dmpSoInfoEntity, DmpSoReceiverEntity soReceiverEntity) {
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
                .build();
    }


    /**
     * 物流信息字段处理
     *
     * @param dmpSoInfoEntity
     * @return java.util.List<com.common.business.dto.PlatformOrderLogisticsDTO>
     * @Author Luo_WG
     * @Date 2023/12/4 14:07
     **/
    private static List<PlatformOrderLogisticsDTO> parseLogistics(DmpSoInfoEntity dmpSoInfoEntity) {
        if (ObjectUtil.isEmpty(dmpSoInfoEntity)) {
            return Collections.emptyList();
        }

        String name = "";
        BigDecimal cost = BigDecimal.ZERO;


        List<PlatformOrderLogisticsDTO> logisticsDTOS = new ArrayList<>();
        PlatformOrderLogisticsDTO dto = PlatformOrderLogisticsDTO.builder()
                .code(dmpSoInfoEntity.getLogisticsCode())
                .name(name)
                .deliveryTime(dmpSoInfoEntity.getDeliveryTime())
                .estimatedShippingCost(cost)
                .actualShippingCost(BigDecimal.ZERO)
                .accessoriesCostCurrency("")
                .actualShippingCurrency("")
                .estimatedShippingCurrency("")
                .logisticType(dmpSoInfoEntity.getLogisticType())
                .build();
        logisticsDTOS.add(dto);
        return logisticsDTOS;
    }

    /**
     * 财务信息表
     *
     * @param dmpSoInfoEntity
     * @return java.util.List<com.common.business.dto.PlatformOrderFinanceDTO>
     * @Author Luo_WG
     * @Date 2023/12/4 14:07
     **/
    private static PlatformOrderFinanceDTO parseFinances(DmpSoInfoEntity dmpSoInfoEntity, List<DmpSoDetailEntity> dmpSoDetailEntities) {
        return PlatformOrderFinanceDTO.builder()
                .currency(dmpSoInfoEntity.getCurrencyCode())
                .shippingCost(dmpSoInfoEntity.getShippingAmount())
                .build();
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("platformCode");
    }

    /**
     * 批量转换退货单DTO
     */
    public List<PlatformReturnOrderDTO> parseReturnList(Map<DmpSoReturnInfoEntity, List<DmpSoReturnDetailEntity>> changeReturnMap, String cfgOutputId) {
        if (CollectionUtils.isEmpty(changeReturnMap)) {
            return Collections.emptyList();
        }
        List<PlatformReturnOrderDTO> resultList = new LinkedList<>();
        for (Map.Entry<DmpSoReturnInfoEntity, List<DmpSoReturnDetailEntity>> entry : changeReturnMap.entrySet()) {
            DmpSoReturnInfoEntity dmpEntity = entry.getKey();
            List<DmpSoReturnDetailEntity> dmpDetailList = entry.getValue();

            PlatformReturnOrderDTO dto = new PlatformReturnOrderDTO();
            BeanUtils.copyProperties(dmpEntity, dto);
            dto.setUniqueId(dmpEntity.getThirdCode());
            dto.setPlatformReturnNo(dmpEntity.getThirdCode());
            dto.setPlatformOrderNo(dmpEntity.getPlatformCode());
            dto.setReason(dmpEntity.getRemark());
            dto.setDictPlatform(dmpEntity.getSourceSystem());
            dto.setPlatform(dmpEntity.getSourceSystem());
            dto.setDmpSyncTaskId(cfgOutputId);
            // 明细
            List<PlatformReturnOrderDTO.Detail> detailList = parseReturnDetailList(dmpDetailList);
            dto.setDetailList(detailList);

            resultList.add(dto);
        }
        return resultList;
    }

    /**
     * 批量转换退货单明细DTO
     */
    private List<PlatformReturnOrderDTO.Detail> parseReturnDetailList(List<DmpSoReturnDetailEntity> dmpDetailList) {
        List<PlatformReturnOrderDTO.Detail> resultList = new LinkedList<>();
        for (DmpSoReturnDetailEntity dmpDetailEntity : dmpDetailList) {
            PlatformReturnOrderDTO.Detail detail = new PlatformReturnOrderDTO.Detail();
            detail.setPlatformSkuNo(dmpDetailEntity.getSkuNo());
            detail.setReturnQty(dmpDetailEntity.getQty());
            resultList.add(detail);
        }
        return resultList;
    }


    /**
     * 批量转换退款单DTO
     */
    public List<PlatformRefundOrderDTO> parseRefundList(Map<DmpSoRefundInfoEntity, List<DmpSoRefundDetailEntity>> changeRefundMap, String cfgOutputId) {
        if (CollectionUtils.isEmpty(changeRefundMap)) {
            return Collections.emptyList();
        }
        List<PlatformRefundOrderDTO> resultList = new LinkedList<>();
        for (Map.Entry<DmpSoRefundInfoEntity, List<DmpSoRefundDetailEntity>> entry : changeRefundMap.entrySet()) {
            DmpSoRefundInfoEntity dmpEntity = entry.getKey();
            List<DmpSoRefundDetailEntity> dmpDetailList = entry.getValue();

            PlatformRefundOrderDTO dto = new PlatformRefundOrderDTO();
            BeanUtils.copyProperties(dmpEntity, dto);
            dto.setUniqueId(dmpEntity.getThirdCode());
            dto.setPlatformRefundNo(dmpEntity.getThirdCode());
            dto.setPlatformOrderNo(dmpEntity.getPlatformCode());
            dto.setRemark(dmpEntity.getRemark());
            dto.setDictPlatform(dmpEntity.getSourceSystem());
            dto.setPlatform(dmpEntity.getSourceSystem());
            dto.setRefundAmount(dmpEntity.getAmount());
            dto.setCurrency(dmpEntity.getCurrencyCode());
            dto.setDmpSyncTaskId(cfgOutputId);
            List<PlatformRefundOrderDTO.Detail> detailList = new LinkedList<>();
            // 退款单可能存在没有明细
            if (!CollectionUtils.isEmpty(dmpDetailList)){
                // 明细
                detailList = parseRefundDetailList(dmpDetailList);
            }
            dto.setDetailList(detailList);

            resultList.add(dto);
        }
        return resultList;
    }

    /**
     * 批量转换退款单明细DTO
     */
    private List<PlatformRefundOrderDTO.Detail> parseRefundDetailList(List<DmpSoRefundDetailEntity> dmpDetailList) {
        List<PlatformRefundOrderDTO.Detail> resultList = new LinkedList<>();
        for (DmpSoRefundDetailEntity dmpDetailEntity : dmpDetailList) {
            PlatformRefundOrderDTO.Detail detail = new PlatformRefundOrderDTO.Detail();
            detail.setPlatformSkuNo(dmpDetailEntity.getSkuNo());
            detail.setRefundQty(dmpDetailEntity.getQty());
            resultList.add(detail);
        }
        return resultList;
    }

    @Override
    public void getRetryPushSourceData(List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList,
                                       DmpOutputTaskRequest dmpOutputTaskRequest) {
        DmpCfgInputConvertEntity dmpCfgInputConvertEntity = dmpCfgInputConvertEntityList.get(0);
        List<String> mainIds = dmpOutputTaskRequest.getConvertInputDmpBaseEntityListMaps().get(dmpCfgInputConvertEntity).stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<String> soReturnIds = null;
        List<String> soRefundIds = null;
        for(int i = 1; i < dmpCfgInputConvertEntityList.size(); i++) {
            DmpCfgInputConvertEntity childDmpCfgInputConvertEntity = dmpCfgInputConvertEntityList.get(i);
            String storageName = childDmpCfgInputConvertEntity.getStorageName();
            ServiceImpl serviceImpl = ApplicationContextUtils.getBean(StrUtils.underlineToCamel(storageName, true) + "ServiceImpl" , ServiceImpl.class);
            QueryWrapper<?> wrapper = new QueryWrapper<>();
            if("dmp_so_return_info".equals(storageName)) {
                wrapper.in("source_id", mainIds);
            } else if("dmp_so_return_detail".equals(storageName)){
                if(CollUtil.isEmpty(soReturnIds)) {
                    continue;
                }
                wrapper.in("main_id", soReturnIds);
            } else if("dmp_so_refund_info".equals(storageName)){
                wrapper.in("source_id", mainIds);
            } else if("dmp_so_refund_detail".equals(storageName)){
                if(CollUtil.isEmpty(soRefundIds)) {
                    continue;
                }
                wrapper.in("main_id", soRefundIds);
            } else {
                wrapper.in("main_id", mainIds);
            }
            List<BaseEntity> childEntityList = serviceImpl.list(wrapper);
            if("dmp_so_return_info".equals(storageName)) {
                soReturnIds = childEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            }
            if("dmp_so_refund_info".equals(storageName)) {
                soRefundIds = childEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            }
            dmpOutputTaskRequest.getConvertInputDmpBaseEntityListMaps().put(childDmpCfgInputConvertEntity, childEntityList);
            dmpOutputTaskRequest.getChangeConvertInputDmpBaseEntityListMaps().put(childDmpCfgInputConvertEntity, childEntityList);
        }
    }

}
