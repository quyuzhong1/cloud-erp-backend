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
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpOrderReturnStatusEnum;
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
public class ShopifyOrderRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoInfoEntity> dmpSoInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSoDetailEntity>> dmpSoDetailEntityMap = new HashMap<>();
        Map<String, List<DmpSoReceiverEntity>> dmpSoReceiverEntityMap = new HashMap<>();

        // 退货信息
        Map<String, String> dmpSoReturnIdMap = new HashMap<>();
        // Map<平台订单ID, 退货单列表>
        Map<String, List<DmpSoReturnInfoEntity>> dmpSoReturnInfoEntityMap = new HashMap<>();
        // Map<平台订单ID, 退款单列表>
        Map<String, List<DmpSoReturnDetailEntity>> dmpSoReturnDetailEntityMap = new HashMap<>();

        // 退款信息
        Map<String, String> dmpSoRefundIdMap = new HashMap<>();
        // Map<平台订单ID, 退款单列表>
        Map<String, List<DmpSoRefundInfoEntity>> dmpSoRefundInfoEntityMap = new HashMap<>();
        // Map<退款单中台mainId, 退款单列表>
        Map<String, List<DmpSoRefundDetailEntity>> dmpSoRefundDetailEntityMap = new HashMap<>();

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
                } else if ("dmp_so_return_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoReturnInfoEntity dmpEntity = (DmpSoReturnInfoEntity) v;
                        String returnOrderId = dmpEntity.getThirdCode();
                        List<DmpSoReturnInfoEntity> list = dmpSoReturnInfoEntityMap.get(returnOrderId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpEntity);
                        dmpSoReturnInfoEntityMap.put(returnOrderId, list);
                        dmpSoReturnIdMap.put(dmpEntity.getId(), returnOrderId);
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
                        String refundOrderId = dmpEntity.getThirdCode();
                        List<DmpSoRefundInfoEntity> list = dmpSoRefundInfoEntityMap.get(refundOrderId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpEntity);
                        dmpSoRefundInfoEntityMap.put(refundOrderId, list);
                        dmpSoRefundIdMap.put(dmpEntity.getId(), refundOrderId);
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
        Set<String> changeIds = new HashSet<>();
        // 退货单变更
        Set<String> returnChangeIds = new HashSet<>();
        // 退款单变更
        Set<String> refundChangeIds = new HashSet<>();

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
                    // 仅判断明细，主表不用判断
                    for (BaseEntity v : value) {
                        DmpSoReturnDetailEntity dmpEntity = (DmpSoReturnDetailEntity) v;
                        String mainId = dmpEntity.getMainId();
                        changeIds.add(dmpSoReturnIdMap.get(mainId));
                        returnChangeIds.add(mainId);
                    }
                } else if ("dmp_so_refund_detail".equals(storageName)) {
                    // 仅判断明细，主表不用判断
                    for (BaseEntity v : value) {
                        DmpSoRefundDetailEntity dmpEntity = (DmpSoRefundDetailEntity) v;
                        String mainId = dmpEntity.getMainId();
                        changeIds.add(dmpSoRefundIdMap.get(mainId));
                        refundChangeIds.add(mainId);
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for (String changId : changeIds) {
            // 退货单变更
            Map<DmpSoReturnInfoEntity, List<DmpSoReturnDetailEntity>> changeReturnMap = new HashMap<>();
            List<DmpSoReturnInfoEntity> dmpSoReturnInfoEntity = dmpSoReturnInfoEntityMap.get(changId);
            if (CollUtil.isNotEmpty(dmpSoReturnInfoEntity)) {
                List<DmpSoReturnInfoEntity> returnInfoList = dmpSoReturnInfoEntityMap.get(changId);
                // 变更的退货单
                List<DmpSoReturnInfoEntity> curReturnInfoList = returnInfoList.stream()
                        .filter(e -> returnChangeIds.contains(e.getId()))
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
            List<DmpSoRefundDetailEntity> dmpSoRefundDetailEntityList = dmpSoRefundDetailEntityMap.get(changId);
            if (CollUtil.isNotEmpty(dmpSoRefundDetailEntityList)) {
                List<DmpSoRefundInfoEntity> returnInfoList = dmpSoRefundInfoEntityMap.get(changId);
                // 变更的退货单
                List<DmpSoRefundInfoEntity> curReturnInfoList = returnInfoList.stream()
                        .filter(e -> refundChangeIds.contains(e.getId()))
                        .collect(Collectors.toList());
                if (CollUtil.isNotEmpty(curReturnInfoList)) {
                    for (DmpSoRefundInfoEntity soRefundInfoEntity : curReturnInfoList) {
                        List<DmpSoRefundDetailEntity> refundEntityList = dmpSoRefundDetailEntityMap.get(soRefundInfoEntity.getId());
                        if (CollUtil.isEmpty(refundEntityList)) {
                            ServiceException.runError("Shopify退款单数据异常，找不到明细：mainId={}", soRefundInfoEntity.getId());
                        }
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
        orderDTO.setSourceCode("");

        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");

        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        orderDTO.setInvalidStatus(Boolean.FALSE);

        //付款状态
        if (dmpSoInfoEntity.getPayTime() != null) {
            orderDTO.setPayStatus(SoB2cPayStatusEnum.ENUM_PAID.getCode());
        }

        orderDTO.setLabelJson(dmpSoInfoEntity.getExtendData());
        orderDTO.setApproveStatusStr(dmpSoInfoEntity.getOrderStatus());
        orderDTO.setBillStatus(dmpSoInfoEntity.getDeliveryStatus());
        orderDTO.setInvalidStatus(dmpSoInfoEntity.getInvalidStatus());
        // 作废类型（manual手动作废，automatic自动作废）
        orderDTO.setInvalidType(dmpSoInfoEntity.getInvalidStatus() ? "automatic" : "");

        // 平台订单原始取消状态(已退款,部分退款)
        DmpBasicSystemCodeEnum dmpBasicSystemCodeEnum = DmpOrderReturnStatusEnum.getByCode(dmpSoInfoEntity.getReturnStatus());
        if (dmpBasicSystemCodeEnum != null && !DmpOrderReturnStatusEnum.NOT_RETURN.equals(dmpBasicSystemCodeEnum)) {
            orderDTO.setIsCancel(Boolean.TRUE);
        } else {
            orderDTO.setIsCancel(Boolean.FALSE);
        }

        JSONObject jsonObject = JSONObject.parseObject(dmpSoInfoEntity.getExtendData());
        if (jsonObject.get("sellerOrderCode") != null) {
            orderDTO.setSellerOrderCode(jsonObject.get("sellerOrderCode") + "");
        }


        //创建时间
        orderDTO.setPlatformOrderCreateTime(dmpSoInfoEntity.getPlatformCreateTime());

        // 订单明细
        List<PlatformOrderDetailDTO> details = parseDetailDto(dmpSoInfoEntity, dmpSoDetailEntityList);
        orderDTO.setDetails(details);
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
            dto.setUniqueId(dmpEntity.getThirdCode());
            dto.setPlatformReturnNo(dmpEntity.getThirdCode());
            dto.setPlatformOrderNo(dmpEntity.getPlatformCode());
            dto.setReason(dmpEntity.getRemark());
            dto.setDictPlatform(dmpEntity.getSourceSystem());
            dto.setPlatform(dmpEntity.getSourceSystem());
            dto.setDmpSyncTaskId(cfgOutputId);
            // 明细
            List<PlatformRefundOrderDTO.Detail> detailList = parseReturnDetailList(dmpDetailList);
            dto.setDetailList(detailList);

            resultList.add(dto);
        }
        return resultList;
    }

    /**
     * 批量转换退货单明细DTO
     */
    private List<PlatformRefundOrderDTO.Detail> parseReturnDetailList(List<DmpSoReturnDetailEntity> dmpDetailList) {
        List<PlatformRefundOrderDTO.Detail> resultList = new LinkedList<>();
        for (DmpSoReturnDetailEntity dmpDetailEntity : dmpDetailList) {
            PlatformRefundOrderDTO.Detail detail = new PlatformRefundOrderDTO.Detail();
            detail.setPlatformSkuNo(dmpDetailEntity.getSkuNo());
            detail.setRefundAmount(dmpDetailEntity.getAmount());
            detail.setRefundQty(dmpDetailEntity.getQty());
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
            dto.setUniqueId(dmpEntity.getThirdCode());
            dto.setPlatformRefundNo(dmpEntity.getThirdCode());
            dto.setPlatformOrderNo(dmpEntity.getPlatformCode());
            dto.setRemark(dmpEntity.getRemark());
            dto.setDictPlatform(dmpEntity.getSourceSystem());
            dto.setPlatform(dmpEntity.getSourceSystem());
            dto.setDmpSyncTaskId(cfgOutputId);
            // 明细
            List<PlatformRefundOrderDTO.Detail> detailList = parseRefundDetailList(dmpDetailList);
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
            detail.setRefundAmount(dmpDetailEntity.getAmount());
            detail.setRefundQty(dmpDetailEntity.getQty());
            resultList.add(detail);
        }
        return resultList;
    }
}
