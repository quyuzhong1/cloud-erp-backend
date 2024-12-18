package com.erp.oms.aliexpress.dto;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.*;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.utils.StringUtil;
import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.wms.dto.AliexpressDeliveryDetailDTO;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.response.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.DownloadStream;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-11-29 10:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PlatformAliExpressOrderDTO extends CleanBaseDTO {

    /**
     * 速卖通订单信息
     */
    private AliExpressOrder aliExpressOrder;


    private AliExpressShopInfoDTO aliExpressShopInfoDTO;

    private List<ErpFulfillmentForwardDtoBean> aliExpressDeliveryDTOList;

    private List<AliExpressDeliveryDetail> aliExpressDeliveryDetailList;

    /**
     * 数据下载状态
     * -1 异常数据无法更新
     * 0 详情数据需要更新
     * 1 详情数据已更新
     */
    @Panno(findType = PannoEnum.EQ,field = "downloadStatus")
    private Integer downloadStatus;

    /**
     * 地址详情下载状态
     * 0 详情数据需要更新
     * 1 详情数据已更新
     */
    @Panno(findType = PannoEnum.EQ,field = "downloadAddressStatus")
    private Integer downloadAddressStatus;


    /**
     * 发货单下载状态
     * -1 无需下载(无发货信息或非平台仓订单)
     * 0 待下载
     * 1 已下载
     */
    @Panno(findType = PannoEnum.EQ,field = "downloadDeliveryStatus")
    private Integer downloadDeliveryStatus;

    /**
     * 发货单明细下载状态
     * -1 无需下载(无发货信息或非平台仓订单)
     * 0 待下载
     * 1 已下载
     */
    @Panno(findType = PannoEnum.EQ,field = "downloadDeliveryDetailStatus")
    private Integer downloadDeliveryDetailStatus;


    @Panno(findType = PannoEnum.EQ,field = "shopId")
    private String shopId;

    @Panno(findType = PannoEnum.EQ,field = "platformWarehouseOrder")
    private Boolean platformWarehouseOrder;


    public PlatformAliExpressOrderDTO(JobTaskDTO dto, AliExpressOrder aliExpressOrder, AliExpressShopInfoDTO aliExpressShopInfoDTO) {
        this.aliExpressOrder = aliExpressOrder;
        this.aliExpressShopInfoDTO = aliExpressShopInfoDTO;
        this.setIsClean(0);
        this.shopId=aliExpressShopInfoDTO.getId();
        this.setPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
        this.setUniqueId(combineUnique(aliExpressOrder.getOrderId(), this.shopId));
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setDownloadAddressStatus(0);
        this.setDownloadStatus(0);
        // 判断是否下载发货单
        if (aliExpressOrder.canDownloadDelivery()){
            this.setDownloadDeliveryStatus(0);
            this.setDownloadDeliveryDetailStatus(0);
        } else {
            this.setDownloadDeliveryStatus(-1);
            this.setDownloadDeliveryDetailStatus(-1);
        }
        this.setLastPushTime(dto.getNextTime().toString());
    }


    public static String combineUnique(String orderId, String shopId){
        return StrUtil.format("{}_{}", orderId, shopId);
    }

    /**
     * 将订单的数据转化成想要的数据
     *
     * @param dto 原始订单数据
     * @return
     * @author yl
     * @date 2023-11-29 15:47
     */
    public static PlatformOrderDTO convertDTO(PlatformAliExpressOrderDTO dto) {
        // 原订单信息
        AliExpressOrder sourceOrder = dto.getAliExpressOrder();

        PlatformOrderDTO orderDTO = new PlatformOrderDTO();
        // 平台类型
        orderDTO.setPlatform(dto.getPlatform());
        // 唯一ID
        orderDTO.setUniqueId(dto.getUniqueId());
        // 同步任务ID
        orderDTO.setDmpSyncTaskId(dto.getDmpSyncTaskId());
        String fl = "";
        // 订单日期
        orderDTO.setBillDate(LocalDateUtil.parseStrToLocalDate(sourceOrder.getGmtCreate()));
        // 订单创建时间
        orderDTO.setPlatformOrderCreateTime(LocalDateUtil.parseStrToLocalTime(sourceOrder.getGmtCreate()));
        // 平台订单号
        orderDTO.setPlatformCode(sourceOrder.getOrderId());
        // 销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
        // 店铺ID
        orderDTO.setShopId(dto.getShopId());
        // 平台取消
        boolean isCancel = sourceOrder.convertCancel();
        // 平台冻结
        boolean isFrozen = sourceOrder.convertFrozen();

        // 作废状态（false未作废，true已作废）
        // 平台取消 并且 非冻结 作废
        orderDTO.setInvalidStatus(isCancel && !isFrozen);
        // 平台取消状态
        orderDTO.setIsCancel(isCancel);

        // 作废类型（manual手动作废，automatic自动作废）
        orderDTO.setInvalidType("");
        // 作废原因
        orderDTO.setInvalidRemark("");
        String amountStr = sourceOrder.getPayAmount().getAmount();
        String amountCurrency = sourceOrder.getPayAmount().getCurrencyCode();
        // 订单金额
        orderDTO.setAmount(new BigDecimal(amountStr));
        // 币别（原币）
        orderDTO.setCurrency(amountCurrency);
        // 汇率
        orderDTO.setExchangeRate(BigDecimal.ONE);
        orderDTO.setPlatformOrderStatus(sourceOrder.getOrderStatus());

        AliExpressOrderDetail detail = sourceOrder.getDetail();

        BigDecimal shippingFee = BigDecimal.ZERO;
        String shippingCountry="";
        boolean detailNotNull = null != detail;
        if (detailNotNull) {
            AmountInfo shippingAmount = detail.getLogisticsAmount();
            if (Objects.nonNull(shippingAmount)) {
                String shippingFeeStr = shippingAmount.getAmount();
                shippingCountry=shippingAmount.getCurrencyCode();
                if (StringUtils.isNotBlank(shippingFeeStr)) {
                    shippingFee = new BigDecimal(shippingFeeStr);
                }
            }
            String oaid = detail.getOaid();
            if(StringUtils.isNotBlank(oaid)){
                JSONObject oaidJson = new JSONObject();
                oaidJson.put("oaid",oaid);
                orderDTO.setExtendData(oaidJson.toString());
            }else{
                orderDTO.setExtendData("{}");
            }
        }
        //物流成本
        BigDecimal logisticsCost = BigDecimal.ZERO;
        if (detailNotNull) {
            AmountInfo logisticsAmount = detail.getLogisticsAmount();
            if (Objects.nonNull(logisticsAmount)) {
                //物流成本
                String logisticsCostStr = logisticsAmount.getAmount();
                if (StringUtils.isNotBlank(logisticsCostStr)) {
                    logisticsCost = new BigDecimal(logisticsCostStr);
                }
            }

        }
        orderDTO.setShippingFee(shippingFee);
        // 付款时间
        orderDTO.setPayTime(LocalDateUtil.parseStrToLocalTime(sourceOrder.getGmtPayTime()));
        // 付款金额
        orderDTO.setPayAmount(new BigDecimal(amountStr));
        // 付款方式
        orderDTO.setDictPayMethod(sourceOrder.getPaymentType());
        // 买家备注
        orderDTO.setBuyerRemark(Objects.nonNull(detail) ? detail.getMemo() : "");
        // 订单备注
        orderDTO.setRemark("");
        // 销售组织id
        orderDTO.setOrgId("");
        // 销售组织名称
        orderDTO.setOrgName("");
        // 是否拦截
        orderDTO.setIsIntercept(false);
        // 拦截备注
        orderDTO.setInterceptRemark("");
        // 来源类型
        orderDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());
        // 来源id
        orderDTO.setSourceId(sourceOrder.getOrderId());
        // 来源编码
        orderDTO.setSourceCode(sourceOrder.getOrderId());

        // 标签json
        Map<String, Object> labelMap = new HashMap<>();
        //订单明细
        List<OrderItemDetail> orderItemDetailList = detailNotNull ? sourceOrder.getDetail().getChildOrderList() : Collections.emptyList();
        Boolean isAliexpressPlatformWarehouseOrder = Boolean.FALSE;
        if (CollectionUtils.isNotEmpty(orderItemDetailList)) {
            long count = orderItemDetailList.stream().
                    filter(o -> AliexpressConstants.CAINIAO_INTERNATIONAL_WAREHOUSE.equals(o.getLogisticsWarehouseType())).count();
            isAliexpressPlatformWarehouseOrder = count > 0;
        }
        labelMap.put("logisticsWarehouseType", orderItemDetailList.stream().map(OrderItemDetail::getLogisticsWarehouseType).collect(Collectors.joining(",")));
        labelMap.put("isPlatformWarehouseOrder", isAliexpressPlatformWarehouseOrder);
        String orderStatus = sourceOrder.getOrderStatus();
        if ("RISK_CONTROL".equals(orderStatus)
                || "IN_CANCEL".equals(orderStatus)
                || "IN_FROZEN".equals(orderStatus)
        ) {
            labelMap.put("aliexpressStatus", orderStatus);
        }

        // 标签json
        orderDTO.setLabelJson(JSONUtil.toJsonStr(labelMap));

        // 订单状态
        // （soB2cBillStatus字典类型）
        orderDTO.setBillStatus(sourceOrder.convertBillStatus(isAliexpressPlatformWarehouseOrder));

        // 平台订单原始状态
        orderDTO.setPlatformOrderStatus(sourceOrder.getOrderStatus());

        // 审核状态状态
        // （ApproveStatus字典类型）
        orderDTO.setApproveStatusStr(sourceOrder.convertApproveStatus(isAliexpressPlatformWarehouseOrder));

        // 付款状态（待付款、已付款）
        // （soB2cPayStatus字典类型）
        orderDTO.setPayStatus(sourceOrder.convertPayStatus());

        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");
        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        List<PlatformOrderLogisticsDTO> orderLogisticList = new ArrayList(5);
        String warehouseName = "";
        if (detailNotNull) {
            String logisticsServiceName = CollectionUtils.isNotEmpty(detail.getChildOrderList()) ? detail.getChildOrderList().get(0).getLogisticsServiceName() : "";
            if (CollectionUtils.isNotEmpty(detail.getLogisticInfoList())) {
                List<LogisitcsDTO> logisticInfoList = detail.getLogisticInfoList().stream().
                        filter(d -> StringUtils.isNotBlank(d.getLogisticsNo())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(logisticInfoList)) {
                    for (LogisitcsDTO item : logisticInfoList) {
                        warehouseName = item.getWarehouseName();
                        PlatformOrderLogisticsDTO logisticsDTO = new PlatformOrderLogisticsDTO();
                        // 自发货订单
                        // 部分发货拉取过来是已审核、部分发货、有跟踪单号不能操作反审核也不能操作拆单
                        if (!isAliexpressPlatformWarehouseOrder && SoB2cBillStatusEnum.ENUM_PARTIAL_SHIPPED.getCode().equalsIgnoreCase(orderDTO.getBillStatus())){
                            logisticsDTO.setCode("");
                        } else {
                            logisticsDTO.setCode(item.getLogisticsNo());
                        }
                        logisticsDTO.setName(logisticsServiceName);
                        String sendTime = item.getGmtSend();

                        LocalDateTime deliveryTime = LocalDateUtil.strToLocalDateTime(sendTime);
                        //发货时间
                        logisticsDTO.setDeliveryTime(deliveryTime);
                        logisticsDTO.setActualShippingCost(shippingFee);
                        logisticsDTO.setActualShippingCurrency(shippingCountry);
                        orderLogisticList.add(logisticsDTO);
                    }
                }
            }
        }
        orderDTO.setLogisticsList(orderLogisticList);

        // 订单明细
        List<PlatformOrderDetailDTO> details = parseDetailList(detailNotNull ? detail.getChildOrderList() : Collections.emptyList(), warehouseName);
        orderDTO.setDetails(details);

        PlatformOrderReceiverDTO receiverDTO = new PlatformOrderReceiverDTO();
        if (detailNotNull) {
            //收货信息
            ReceiptInfo receiptInfo = detail.getReceiptAddress();
            if (Objects.nonNull(receiptInfo)){
                receiverDTO.setCountry(receiptInfo.getCountry());
                receiverDTO.setFirstAddress(StringUtils.isBlank(receiptInfo.getAddress())?StringUtil.getOrDefault(receiptInfo.getDetailAddress())+" "+StringUtil.getOrDefault(receiptInfo.getAddress2()):receiptInfo.getAddress());
                receiverDTO.setSecondAddress(receiptInfo.getAddress2());
                receiverDTO.setFullAddress(receiptInfo.getDetailAddress());
                receiverDTO.setCityName(receiptInfo.getCity());
                receiverDTO.setProvinceName(receiptInfo.getProvince());
                receiverDTO.setReceiverName(receiptInfo.getContactPerson());
                // 手机号为空取电话
                String telNumber = StringUtils.isBlank(receiptInfo.getMobileNo())? receiptInfo.getPhoneNumber(): receiptInfo.getMobileNo();
                receiverDTO.setReceiverTelNumber(telNumber);
                receiverDTO.setPostCode(receiptInfo.getZip());
                receiverDTO.setReceiverTaxNo(receiptInfo.getCpfNo());
                // 买家电话
                receiverDTO.setTelNumber(receiptInfo.getMobileNo());
            }
            BuyerInfo buyerInfo = detail.getBuyerInfo();
            receiverDTO.setLoginId(buyerInfo.getLoginId());

            receiverDTO.setName(sourceOrder.getBuyerSignerFullname());
            receiverDTO.setEmail("");

            receiverDTO.setCountryName("");
            receiverDTO.setDistrictName("");

            if (null == receiverDTO.getTelNumber()){
                receiverDTO.setTelNumber("");
            }

        } else {
            receiverDTO.setCountry("");
            receiverDTO.setName(sourceOrder.getBuyerSignerFullname());
            receiverDTO.setEmail("");
            receiverDTO.setFirstAddress("");
            receiverDTO.setSecondAddress("");
            receiverDTO.setFullAddress("");
            receiverDTO.setCityName("");
            receiverDTO.setCountryName("");
            receiverDTO.setDistrictName("");
            receiverDTO.setPostCode("");
        }
        receiverDTO.setCustomerId(sourceOrder.getBuyerLoginId());
        receiverDTO.setLoginId(sourceOrder.getBuyerLoginId());


        orderDTO.setReceiver(receiverDTO);
        // 订单财务信息
        PlatformOrderFinanceDTO financeDTO = new PlatformOrderFinanceDTO();
        financeDTO.setShippingCost(shippingFee);
        financeDTO.setLogisticsCost(logisticsCost);
        orderDTO.setFinances(financeDTO);
        return orderDTO;
    }

    /**
     * 批量转发货明细
     *
     * @return
     */
    private static List<PlatformDeliveryDetailDTO> parseDeliveryDetailList(List<AliExpressDeliveryDetail> deliveryDetailList) {
        if (CollectionUtils.isEmpty(deliveryDetailList)) {
            return Collections.emptyList();
        }
        List<PlatformDeliveryDetailDTO> result = new ArrayList<>();
        deliveryDetailList.forEach(v->{
            PlatformDeliveryDetailDTO deliveryDetailDTO = new PlatformDeliveryDetailDTO();
            deliveryDetailDTO.setPlatformSkuNo(v.getPlatformSku());
            deliveryDetailDTO.setQty(Integer.valueOf(v.getDeliveryQty()));
            deliveryDetailDTO.setPlatformWarehouseName(v.getWarehouseName());
            deliveryDetailDTO.setPlatformSpuNo(v.getItemId());
            deliveryDetailDTO.setPlatformSkuId(v.getPlatformSkuId());
            deliveryDetailDTO.setScItemId(v.getScItemId());
            result.add(deliveryDetailDTO);
        });
        return result;
    }

    /**
     * 批量转明细
     *
     * @param detailList
     * @param warehouseName
     * @return
     */
    private static List<PlatformOrderDetailDTO> parseDetailList(List<OrderItemDetail> detailList, String warehouseName) {
        if (CollectionUtils.isEmpty(detailList)) {
            return Collections.emptyList();
        }
        return detailList.stream()
                .map(e -> intPlatformOrderDetailDTO(e, warehouseName))
                .collect(Collectors.toList());
    }

    /**
     * 转换明细
     */
    private static PlatformOrderDetailDTO intPlatformOrderDetailDTO(OrderItemDetail item, String warehouseName) {
        PlatformOrderDetailDTO detailDTO = new PlatformOrderDetailDTO();
        // 图片URL
        detailDTO.setImageUrl(item.getProductImgUrl());
        // skuId
        detailDTO.setSkuId("");
        // skuNo
        detailDTO.setSkuNo("");


        // 平台sku编号
        detailDTO.setPlatformSkuNo(item.getSkuCode());

        // 平台sku编号
        detailDTO.setPlatformSpuNo(item.getProductId().toString());
        // 库存sku编号
        detailDTO.setWarehouseName(warehouseName);
        // 仓库名称
        detailDTO.setWarehouseId("");
        Integer qty = item.getProductCount();
        // 数量
        detailDTO.setQty(qty);

        String priceStr = item.getProductPrice().getAmount();

        BigDecimal price = new BigDecimal(priceStr);

        // 单价
        detailDTO.setPrice(price);
        // 金额
        BigDecimal amount = MathUtil.multiply(price, qty);
        // 金额
        String currency = item.getProductPrice().getCurrencyCode();

        detailDTO.setAmount(amount);
        detailDTO.setCurrency(currency);
        // 汇率
        detailDTO.setExchangeRate(BigDecimal.ONE);
        // 建议售价（本位币）
        detailDTO.setAdvicePrice(BigDecimal.ZERO);
        // 含税成本（本位币）
        detailDTO.setTaxCost(BigDecimal.ZERO);
        // 来源明细id
        detailDTO.setSourceDetailId(item.getChildOrderId());
        // 标签json
        Map<String, Object> lableMap = new HashMap<>();
        lableMap.put("alreadyTaxed", item.getAlreadyTaxed());
        lableMap.put("logisticsWarehouseType", item.getLogisticsWarehouseType());
        lableMap.put("tagList", item.getTags());

        // 标签json
        detailDTO.setLabelJson(JSONUtil.toJsonStr(lableMap));
        // 库存组织id
        detailDTO.setWarehouseOrgId("");
        // 库存组织名称
        detailDTO.setWarehouseOrgName("");
        // 库位
        detailDTO.setWarehouseLocation("");
        return detailDTO;
    }


    /**
     * 是否是平台仓订单
     */
    public Boolean isPlatformWarehouseOrder() {
        AliExpressOrderDetail detail = this.getAliExpressOrder().getDetail();
        if (null == detail){
            return false;
        }
        if (CollectionUtils.isEmpty(detail.getChildOrderList())){
            return false;
        }
        return detail.getChildOrderList()
                .stream()
                .anyMatch(o -> AliexpressConstants.CAINIAO_INTERNATIONAL_WAREHOUSE.equals(o.getLogisticsWarehouseType()));
    }

    /**
     * 转换下载状态
     */
    public Integer convertDownloadDeliveryStatus() {
        boolean canDownloadDeliveryStatus = this.isPlatformWarehouseOrder() && this.existLogisticInfo();
        return canDownloadDeliveryStatus ? 0 : -1;
    }

    /**
     * 是否存在物流信息
     */
    private boolean existLogisticInfo() {
        if (null == this.getAliExpressOrder().getDetail()){
            return false;
        }
        List<LogisitcsDTO> logisticInfoList = this.getAliExpressOrder().getDetail().getLogisticInfoList();
        if (CollectionUtils.isEmpty(logisticInfoList)) {
            return false;
        }
        return logisticInfoList.stream().anyMatch(e-> StringUtils.isNotBlank(e.getGmtSend()));
    }

}
