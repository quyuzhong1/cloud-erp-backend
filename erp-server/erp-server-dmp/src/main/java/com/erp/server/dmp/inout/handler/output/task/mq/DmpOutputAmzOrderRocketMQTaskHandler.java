package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.*;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.utils.RedisUtil;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.*;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonOrderDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.*;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class DmpOutputAmzOrderRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

    @Resource
    private RedisUtil redisUtil;

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
                        DmpSoDetailEntity dmpSoDetailEntity = (DmpSoDetailEntity) v;
                        String mainId = dmpSoDetailEntity.getMainId();
                        List<DmpSoDetailEntity> list = dmpSoDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpSoDetailEntity);
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
        for (String changId : changeIds) {
            PlatformOrderDTO orderDTO = this.convert(dmpSoInfoEntityMap.get(changId),
                    dmpSoDetailEntityMap.get(changId),
                    dmpSoReceiverEntityMap.get(changId),
                    cfgOutputId);
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
                                    String cfgOutputId) {
        if (this.validateDataBlack(dmpSoInfoEntity, cfgOutputId)) {
            return null;
        }
        if (CollUtil.isEmpty(dmpSoDetailEntityList)) {
            return null;
        }
        PlatformOrderDTO orderDTO = new PlatformOrderDTO();
        orderDTO.setPlatform(dmpSoInfoEntity.getSourcePlatform());
//    	LocalDateTime platformCreateTime = dmpSoInfoEntity.getPlatformCreateTime();
//		orderDTO.setBillDate(platformCreateTime.toLocalDate());
//		orderDTO.setPlatformOrderCreateTime(platformCreateTime);
//		String platformCode = dmpSoInfoEntity.getPlatformCode();
//		orderDTO.setPlatformCode(platformCode);
//		orderDTO.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
//		orderDTO.setShopId(dmpSoInfoEntity.getShopId());
//
//		orderDTO.setInvalidStatus(dmpSoInfoEntity.getInvalidStatus());
//		orderDTO.setIsCancel(dmpSoInfoEntity.getIsCancel());
//		// 作废类型（manual手动作废，automatic自动作废）
//        orderDTO.setInvalidType("");
//        // 作废原因
//        orderDTO.setInvalidRemark("");
//
//        BigDecimal payAmount = dmpSoInfoEntity.getPayAmount();
//		orderDTO.setAmount(payAmount);
//        String currencyCode = dmpSoInfoEntity.getCurrencyCode();
//		orderDTO.setCurrency(currencyCode);
//        orderDTO.setExchangeRate(dmpSoInfoEntity.getExchangeRate());
//        orderDTO.setPlatformOrderStatus(dmpSoInfoEntity.getPlatformOriginalStatus());
//
//        BigDecimal shippingAmount = dmpSoInfoEntity.getShippingAmount();
//		orderDTO.setShippingFee(shippingAmount);
//
//
//        orderDTO.setPayTime(dmpSoInfoEntity.getPayTime());
//        orderDTO.setPayAmount(payAmount);
//
//        orderDTO.setDictPayMethod(dmpSoInfoEntity.getPayMethod());
//
//        orderDTO.setBuyerRemark(dmpSoInfoEntity.getBuyerRemark());
//
//        // 订单备注
//        orderDTO.setRemark("");
//        // 销售组织id
//        orderDTO.setOrgId("");
//        // 销售组织名称
//        orderDTO.setOrgName("");
//        // 是否拦截
//        orderDTO.setIsIntercept(false);
//        // 拦截备注
//        orderDTO.setInterceptRemark("");
//        // 来源类型
//        orderDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());
//
//        // 来源id
//        orderDTO.setSourceId(platformCode);
//        // 来源编码
//        orderDTO.setSourceCode(platformCode);
//
//        // 标签json
//        orderDTO.setLabelJson(dmpSoInfoEntity.getExtendData());
//
//        // 订单状态
//        // （soB2cBillStatus字典类型）
//        orderDTO.setBillStatus(dmpSoInfoEntity.getOrderStatus());
//
//        // 审核状态状态
//        // （ApproveStatus字典类型）
//        orderDTO.setApproveStatusStr(dmpSoInfoEntity.getApproveStatus());
//
//        // 付款状态（待付款、已付款）
//        // （soB2cPayStatus字典类型）
//        orderDTO.setPayStatus(dmpSoInfoEntity.getPayStatus() ? SoB2cPayStatusEnum.ENUM_PAID.getCode() : SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
//
//        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
//        orderDTO.setAbnormalType("");
//        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
//        orderDTO.setSyncKingdeeStatus("0");
//
//        List<PlatformOrderLogisticsDTO> orderLogisticList = new ArrayList<>();
//        if(CollUtil.isNotEmpty(dmpLogisticInfoEntityList)) {
//        	for(DmpLogisticInfoEntity dmpLogisticInfoEntity : dmpLogisticInfoEntityList) {
//        		PlatformOrderLogisticsDTO logisticsDTO = new PlatformOrderLogisticsDTO();
//            	logisticsDTO.setCode(dmpLogisticInfoEntity.getLogisticsNo());
//                String logisticsServiceName = dmpLogisticInfoEntity.getLogisticsServiceName();
//                if(StringUtils.isBlank(logisticsServiceName)) {
//                	logisticsServiceName = "";
//                }
//                logisticsDTO.setName(logisticsServiceName);
//                //发货时间
//                logisticsDTO.setDeliveryTime(dmpLogisticInfoEntity.getDeliveryTime());
//                logisticsDTO.setActualShippingCost(shippingAmount);
//                logisticsDTO.setActualShippingCurrency(dmpLogisticInfoEntity.getCurrencyCode());
//            	orderLogisticList.add(logisticsDTO);
//        	}
//        }
//        orderDTO.setLogisticsList(orderLogisticList);
//
//        // 订单明细
//        List<PlatformOrderDetailDTO> details = new ArrayList<>();
//        if(CollUtil.isNotEmpty(dmpSoDetailEntityList)) {
//        	for(DmpSoDetailEntity dmpSoDetailEntity : dmpSoDetailEntityList) {
//        		PlatformOrderDetailDTO detailDTO = new PlatformOrderDetailDTO();
//                // 图片URL
//                detailDTO.setImageUrl(dmpSoDetailEntity.getSkuUrl());
//                // skuId
//                detailDTO.setSkuId("");
//                // skuNo
//                detailDTO.setSkuNo("");
//
//                // 平台sku编号
//                detailDTO.setPlatformSkuNo(dmpSoDetailEntity.getPlatformSku());
//
//                // 平台sku编号
//                detailDTO.setPlatformSpuNo(dmpSoDetailEntity.getPlatformSpuNo());
//                // 库存sku编号
//                detailDTO.setWarehouseName(dmpSoDetailEntity.getWarehouseName());
//
//                detailDTO.setWarehouseId("");
//
//                // 数量
//                Integer qty = dmpSoDetailEntity.getQty();
//				detailDTO.setQty(qty);
//
//                BigDecimal sellPriceOrigin = dmpSoDetailEntity.getSellPriceOrigin();
//                if(sellPriceOrigin != null && qty != null && qty.compareTo(0) != 0) {
//                	// 单价
//                	detailDTO.setPrice(sellPriceOrigin.divide(new BigDecimal(qty) , 2, RoundingMode.HALF_UP));
//                }
//
//				detailDTO.setAmount(sellPriceOrigin);
//                detailDTO.setCurrency(dmpSoDetailEntity.getCurrencyCode());
//
//                // 汇率
//                detailDTO.setExchangeRate(BigDecimal.ONE);
//                // 建议售价（本位币）
//                detailDTO.setAdvicePrice(BigDecimal.ZERO);
//                // 含税成本（本位币）
//                detailDTO.setTaxCost(BigDecimal.ZERO);
//
//                // 来源明细id
//                detailDTO.setSourceDetailId(dmpSoDetailEntity.getPlatformDetailId());
//
//                detailDTO.setLabelJson(dmpSoDetailEntity.getExtendData());
//
//                // 库存组织id
//                detailDTO.setWarehouseOrgId("");
//                // 库存组织名称
//                detailDTO.setWarehouseOrgName("");
//                // 库位
//                detailDTO.setWarehouseLocation("");
//
//                details.add(detailDTO);
//        	}
//        }
//        orderDTO.setDetails(details);
//
//        if(CollUtil.isNotEmpty(dmpSoOutstockDetailEntityList)) {
//        	List<PlatformDeliveryDetailDTO> platformDeliveryDetailDTOList = new ArrayList<>();
//        	for(DmpSoOutstockDetailEntity v : dmpSoOutstockDetailEntityList) {
//        		PlatformDeliveryDetailDTO deliveryDetailDTO = new PlatformDeliveryDetailDTO();
//                deliveryDetailDTO.setPlatformSkuNo(v.getPlatformSku());
//                deliveryDetailDTO.setQty(v.getQty());
//                deliveryDetailDTO.setPlatformWarehouseName(v.getWarehouseName());
//                deliveryDetailDTO.setPlatformSpuNo(v.getThirdDetailId());
//                deliveryDetailDTO.setPlatformSkuId(v.getSkuId());
//                deliveryDetailDTO.setScItemId(v.getPlatformDetailId());
//                platformDeliveryDetailDTOList.add(deliveryDetailDTO);
//        	}
//        	orderDTO.setDeliveryDetailDTOList(platformDeliveryDetailDTOList);
//        }
//
//        PlatformOrderReceiverDTO receiverDTO = new PlatformOrderReceiverDTO();
//        if(dmpSoReceiverEntity != null) {
//        	receiverDTO.setCountry(dmpSoReceiverEntity.getCountry());
//            String firstAddress = dmpSoReceiverEntity.getMainStreet();
//            String fullAddress = dmpSoReceiverEntity.getFullAddress();
//            if(StringUtils.isBlank(fullAddress)) {
//            	fullAddress = "";
//            }
//            String secondStreet = dmpSoReceiverEntity.getSecondStreet();
//            if(StringUtils.isBlank(secondStreet)) {
//            	secondStreet = "";
//            }
//			if(StringUtils.isBlank(firstAddress)) {
//            	firstAddress = fullAddress + " " + secondStreet;
//            }
//        	receiverDTO.setFirstAddress(firstAddress);
//            receiverDTO.setSecondAddress(secondStreet);
//            receiverDTO.setFullAddress(fullAddress);
//            receiverDTO.setCityName(dmpSoReceiverEntity.getCity());
//            receiverDTO.setProvinceName(dmpSoReceiverEntity.getProvince());
//            receiverDTO.setReceiverName(dmpSoReceiverEntity.getReceiverName());
//            String mainPhone = dmpSoReceiverEntity.getMainPhone();
//            if(StringUtils.isNotBlank(mainPhone)) {
//            	receiverDTO.setReceiverTelNumber(mainPhone);
//            }else {
//            	receiverDTO.setReceiverTelNumber(dmpSoReceiverEntity.getReceiverTelNumber());
//            }
//            receiverDTO.setPostCode(dmpSoReceiverEntity.getPostCode());
//            receiverDTO.setReceiverTaxNo(dmpSoReceiverEntity.getReceiverTaxNo());
//            // 买家电话
//            receiverDTO.setTelNumber(mainPhone);
//
//            String buyerId = dmpSoReceiverEntity.getBuyerId();
//			receiverDTO.setLoginId(buyerId);
//            receiverDTO.setCustomerId(buyerId);
//            receiverDTO.setName(dmpSoReceiverEntity.getBuyerName());
//        }else {
//        	receiverDTO.setCountry("");
//            receiverDTO.setName("");
//            receiverDTO.setFirstAddress("");
//            receiverDTO.setSecondAddress("");
//            receiverDTO.setFullAddress("");
//            receiverDTO.setCityName("");
//            receiverDTO.setCountryName("");
//            receiverDTO.setDistrictName("");
//            receiverDTO.setPostCode("");
//            receiverDTO.setCustomerId("");
//            receiverDTO.setLoginId("");
//        }
//        receiverDTO.setEmail("");
//        receiverDTO.setCountryName("");
//        receiverDTO.setDistrictName("");
//
//        orderDTO.setReceiver(receiverDTO);
//
//        // 订单财务信息
//        PlatformOrderFinanceDTO financeDTO = new PlatformOrderFinanceDTO();
//        financeDTO.setShippingCost(shippingAmount);
//        financeDTO.setLogisticsCost(shippingAmount);
//        orderDTO.setFinances(financeDTO);
//        return orderDTO;
        // 来源类型/多渠道订单/B2C销售订单
        String sourceType = "soB2c";
        orderDTO.setSourceType(sourceType);
        // 来源id
        orderDTO.setSourceId(dmpSoInfoEntity.getPlatformCode());
        // 来源编码
        orderDTO.setSourceCode("");
        // 标签json
        orderDTO.setLabelJson(dmpSoInfoEntity.getExtendData());

        // 订单日期
        orderDTO.setBillDate(dmpSoInfoEntity.getPlatformCreateTime().toLocalDate());
        // 平台订单创建时间
        orderDTO.setPlatformOrderCreateTime(dmpSoInfoEntity.getPlatformCreateTime());

        // 平台订单号
        orderDTO.setPlatformCode(dmpSoInfoEntity.getPlatformCode());
        // 销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
        // 店铺ID
        orderDTO.setShopId(dmpSoInfoEntity.getShopId());

        // 平台订单原始状态
        orderDTO.setPlatformOrderStatus(dmpSoInfoEntity.getPlatformOriginalStatus());
        // 平台订单原始取消状态
        orderDTO.setIsCancel(dmpSoInfoEntity.getIsCancel());
        // 作废状态（false未作废，true已作废）
        orderDTO.setInvalidStatus(dmpSoInfoEntity.getIsCancel());
        // 作废类型（manual手动作废，automatic自动作废）
        orderDTO.setInvalidType(dmpSoInfoEntity.getInvalidStatus() ? "automatic" : "");
        // 作废原因
        orderDTO.setInvalidRemark("");
        // 订单状态
        // （soB2cBillStatus字典类型）
        orderDTO.setBillStatus(dmpSoInfoEntity.getOrderStatus());
        // 付款状态（待付款、已付款）
        // （soB2cPayStatus字典类型）
        orderDTO.setPayStatus(dmpSoInfoEntity.getPayStatus() ? SoB2cPayStatusEnum.ENUM_PAID.getCode() : SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
        // 审核状态
        orderDTO.setApproveStatusStr(dmpSoInfoEntity.getApproveStatus());

        // 订单金额
        orderDTO.setAmount(dmpSoInfoEntity.getAllAmount());
        // 币别（原币）
        orderDTO.setCurrency(dmpSoInfoEntity.getCurrencyCode());
        // 汇率
        orderDTO.setExchangeRate(BigDecimal.ZERO);
        //  运费
        BigDecimal shippingFee = BigDecimal.ZERO;
        orderDTO.setShippingFee(shippingFee);
        // 付款时间
        // 未付款无付款时间
        orderDTO.setPayTime("payment".equalsIgnoreCase(orderDTO.getPayStatus()) ? null : dmpSoInfoEntity.getPlatformCreateTime());
        // 付款金额
        orderDTO.setPayAmount(dmpSoInfoEntity.getPayAmount());
        // 付款方式
        orderDTO.setDictPayMethod(dmpSoInfoEntity.getPayMethod());
        // 买家备注
        orderDTO.setBuyerRemark("");
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

        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");
        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        // 数据下载状态:
        // 0=详情数据需要更新(不发送MQ)
        // 1=详情数据已更新(发送MQ)
        orderDTO.setDownloadStatus(1);
        // 记录详情
        if (!CollectionUtils.isEmpty(dmpSoDetailEntityList)) {
            List<PlatformOrderDetailDTO> detailDTO = dmpSoDetailEntityList.stream()
                    .map(this::intPlatformOrderDetailDTO)
                    .collect(Collectors.toList());
            orderDTO.setDetails(detailDTO);
            orderDTO.setDownloadStatus(1);
        }


        // 订单财务信息
        if (!CollectionUtils.isEmpty(dmpSoDetailEntityList)) {
            String currency = dmpSoDetailEntityList.stream().map(e -> e.getCurrencyCode()).distinct().findFirst().orElse("");
            PlatformOrderFinanceDTO financeDTO = new PlatformOrderFinanceDTO();
            // 亚马逊物流费用不显示
            financeDTO.setShippingCost(BigDecimal.ZERO);
            financeDTO.setCurrency(currency);
            orderDTO.setFinances(financeDTO);
        }

        // 订单物流信息(无)

        // 订单买家信息
        PlatformOrderReceiverDTO receiverDTO = new PlatformOrderReceiverDTO();
        if (!CollectionUtils.isEmpty(dmpSoReceiverEntityList)) {
            DmpSoReceiverEntity dmpSoReceiverEntity = dmpSoReceiverEntityList.stream().findFirst().orElse(null);
            // 税号
            receiverDTO.setReceiverTaxNo(dmpSoReceiverEntity.getReceiverTaxNo());
            // 买家名称
            receiverDTO.setName(dmpSoReceiverEntity.getBuyerName());
            receiverDTO.setEmail(dmpSoReceiverEntity.getEmail());

            // 买家名称为空使用发货单名称覆盖
            if (StringUtils.isBlank(receiverDTO.getName())) {
                receiverDTO.setName(dmpSoReceiverEntity.getReceiverName());
            }
            receiverDTO.setFirstAddress(dmpSoReceiverEntity.getMainStreet());
            receiverDTO.setSecondAddress(dmpSoReceiverEntity.getSecondStreet());

            receiverDTO.setReceiverTelNumber(dmpSoReceiverEntity.getReceiverTelNumber());
            receiverDTO.setTelNumber(dmpSoReceiverEntity.getReceiverTelNumber());
            receiverDTO.setCityName(dmpSoReceiverEntity.getCity());
            receiverDTO.setCountry(dmpSoReceiverEntity.getCountry());
            receiverDTO.setReceiverName(dmpSoReceiverEntity.getReceiverName());
            // 区域
            receiverDTO.setDistrictName(dmpSoReceiverEntity.getDistrict());
            // 省/州
            receiverDTO.setProvinceName(dmpSoReceiverEntity.getProvince());

            receiverDTO.setFullAddress(dmpSoReceiverEntity.getFullAddress());
            receiverDTO.setPostCode(dmpSoReceiverEntity.getPostCode());
        }
        orderDTO.setReceiver(receiverDTO);
        return orderDTO;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("platformCode", "shopId");
    }


    /**
     * 转换明细
     */
    public PlatformOrderDetailDTO intPlatformOrderDetailDTO(DmpSoDetailEntity item) {
        PlatformOrderDetailDTO detailDTO = new PlatformOrderDetailDTO();
        // 图片URL
        detailDTO.setImageUrl("");
        // skuId
        detailDTO.setSkuId("");
        // skuNo
        detailDTO.setSkuNo("");

        // 平台sku编号
        detailDTO.setPlatformSkuNo(item.getPlatformSku());

        // 平台产品id
        detailDTO.setPlatformSpuNo(item.getPlatformSpuNo());
        // 库存sku编号
        detailDTO.setWarehouseSkuNo("");
        detailDTO.setWarehouseName("");
        // 仓库名称
        // 库存是否扣除
        detailDTO.setWarehouseId("");
        // 数量
        detailDTO.setQty(item.getQty());
        // item总价
        // 金额
        detailDTO.setAmount(item.getAfterAmount());

        // 单价
        detailDTO.setPrice(item.getSellPrice());

        // 币别（原币）
        detailDTO.setCurrency(item.getCurrencyCode());
        // 汇率
        detailDTO.setExchangeRate(BigDecimal.ONE);
        // 建议售价（本位币）
        detailDTO.setAdvicePrice(BigDecimal.ZERO);
        // 含税成本（本位币）
        detailDTO.setTaxCost(BigDecimal.ZERO);
        // 来源明细id
        detailDTO.setSourceDetailId(item.getPlatformDetailId());
        // 标签json
        detailDTO.setLabelJson("");
        // 库存组织id
        detailDTO.setWarehouseOrgId("");
        // 库存组织名称
        detailDTO.setWarehouseOrgName("");
        // 库位
        detailDTO.setWarehouseLocation("");
        return detailDTO;
    }


}
