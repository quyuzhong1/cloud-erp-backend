package com.erp.server.dmp.inout.handler.output.task.mq;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.business.dto.PlatformOrderFinanceDTO;
import com.common.business.dto.PlatformOrderLogisticsDTO;
import com.common.business.dto.PlatformOrderReceiverDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.entity.DmpSoReceiverEntity;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.sdk.oms.shopee.enums.OrderStatusEnum;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputShopeeOrderRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpSoInfoEntity> dmpSoInfoEntityMap = new HashMap<>();
		Map<String, List<DmpSoDetailEntity>> dmpSoDetailEntityMap = new HashMap<>();
		Map<String, List<DmpSoReceiverEntity>> dmpSoReceiverEntityMap = new HashMap<>();
		
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_so_info".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoInfoEntity dmpSoInfoEntity = (DmpSoInfoEntity) v;
						dmpSoInfoEntityMap.put(dmpSoInfoEntity.getId(), dmpSoInfoEntity);
					}
				}else if("dmp_so_detail".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoDetailEntity dmpSoReturnDetailEntity = (DmpSoDetailEntity) v;
						String mainId = dmpSoReturnDetailEntity.getMainId();
						List<DmpSoDetailEntity> list = dmpSoDetailEntityMap.get(mainId);
						if(CollUtil.isEmpty(list)) {
							list = new ArrayList<>();
						}
						list.add(dmpSoReturnDetailEntity);
						dmpSoDetailEntityMap.put(mainId, list);
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
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_so_info".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}else if("dmp_so_detail".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoDetailEntity dmpSoReturnDetailEntity = (DmpSoDetailEntity) v;
						changeIds.add(dmpSoReturnDetailEntity.getMainId());
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
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			PlatformOrderDTO orderDTO = this.convert(dmpSoInfoEntityMap.get(changId), dmpSoDetailEntityMap.get(changId) 
					, dmpSoReceiverEntityMap.get(changId) , cfgOutputId);
			if(orderDTO != null) {
				map.put(changId, JSON.toJSONString(orderDTO));
			}
		}
		return map;
	}

	private PlatformOrderDTO convert(DmpSoInfoEntity dmpSoInfoEntity , List<DmpSoDetailEntity> dmpSoDetailEntityList , List<DmpSoReceiverEntity> dmpSoReceiverEntityList, String cfgOutputId) {
		if(this.validateDataBlack(dmpSoInfoEntity, cfgOutputId)) {
    		return null;
    	}
		
		PlatformOrderDTO orderDTO = new PlatformOrderDTO();
		LocalDateTime platformCreateTime = dmpSoInfoEntity.getPlatformCreateTime();
		orderDTO.setBillDate(platformCreateTime.toLocalDate());
        // 平台订单创建时间
        orderDTO.setPlatformOrderCreateTime(platformCreateTime);
        
        // 平台订单号
        String thirdCode = dmpSoInfoEntity.getThirdCode();
		orderDTO.setPlatformCode(thirdCode);
        // 销售平台
        orderDTO.setDictPlatform(PlatformDictEnum.SHOPEE.getCode());
        // 店铺ID
        orderDTO.setShopId(dmpSoInfoEntity.getShopId());

        // 作废类型（manual手动作废，automatic自动作废）
        orderDTO.setInvalidType("");
        // 作废原因
        orderDTO.setInvalidRemark("");
        // 作废状态（false未作废，true已作废）
        orderDTO.setInvalidStatus(false);
        
        orderDTO.setPayStatus(dmpSoInfoEntity.getPayStatus() ? SoB2cPayStatusEnum.ENUM_PAID.getCode() : SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());
        orderDTO.setBillStatus(dmpSoInfoEntity.getDeliveryStatus());
        orderDTO.setApproveStatusStr(dmpSoInfoEntity.getOrderStatus());
        orderDTO.setInvalidStatus(dmpSoInfoEntity.getInvalidStatus());
        String platformOriginalStatus = dmpSoInfoEntity.getPlatformOriginalStatus();
		orderDTO.setPlatformOrderStatus(platformOriginalStatus);
        if(OrderStatusEnum.IN_CANCEL.getCode().equals(platformOriginalStatus)) {
        	orderDTO.setInvalidRemark("订单取消中");
        }else if(OrderStatusEnum.CANCELLED.getCode().equals(platformOriginalStatus)) {
        	orderDTO.setInvalidRemark("订单已取消");
        }
        orderDTO.setPayTime(dmpSoInfoEntity.getPayTime());
        
        // 订单金额
        orderDTO.setAmount(dmpSoInfoEntity.getPayAmount());
        // 币别（原币）
        orderDTO.setCurrency(dmpSoInfoEntity.getCurrencyCode());
        // 汇率
        orderDTO.setExchangeRate(BigDecimal.ONE);
        // 运费收入
        orderDTO.setShippingFee(dmpSoInfoEntity.getShippingAmount());
        orderDTO.setTotalDiscount(dmpSoInfoEntity.getTotalDiscount());
        // 付款金额
        orderDTO.setPayAmount(dmpSoInfoEntity.getPayAmount());
        // 付款方式
        orderDTO.setDictPayMethod(dmpSoInfoEntity.getPayMethod());
        // 买家备注
        orderDTO.setBuyerRemark(dmpSoInfoEntity.getSellRemark());
        // 订单备注
        orderDTO.setRemark(dmpSoInfoEntity.getRemark());
        
        DmpSoReceiverEntity dmpSoReceiverEntity = null;
        if(CollUtil.isNotEmpty(dmpSoReceiverEntityList)) {
        	dmpSoReceiverEntity = dmpSoReceiverEntityList.get(0);
            // 销售组织id
            orderDTO.setOrgId(dmpSoReceiverEntity.getBuyerId());
            // 销售组织名称
            orderDTO.setOrgName(dmpSoReceiverEntity.getBuyerName());
        }
        // 是否拦截
        orderDTO.setIsIntercept(false);
        // 拦截备注
        orderDTO.setInterceptRemark(dmpSoInfoEntity.getBuyerRemark());
        // 来源类型
        orderDTO.setSourceType(SourceTypeEnum.SO_B2C.getCode());
        // 来源id
        orderDTO.setSourceId(thirdCode);
        // 来源编码
        orderDTO.setSourceCode(thirdCode);
//        // 标签json
//        orderDTO.setLabelJson("{}");
        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");
        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");
        orderDTO.setDetails(parseDetailDto(dmpSoDetailEntityList));
		// 订单状态
		orderDTO.setIsCancel(dmpSoInfoEntity.getIsCancel());

		// 税金
		orderDTO.setTotalTaxFee(dmpSoInfoEntity.getTotalTaxFee());
		// 税后支付金额
		orderDTO.setAfterTaxAmount(dmpSoInfoEntity.getAfterTaxAmount());

		if(dmpSoReceiverEntity != null) {
        	PlatformOrderReceiverDTO receiver = PlatformOrderReceiverDTO.builder()
	            .loginId(dmpSoReceiverEntity.getBuyerId())
	            .customerId(dmpSoReceiverEntity.getBuyerId())
	            .name(dmpSoReceiverEntity.getBuyerName())
	            .receiverName(dmpSoReceiverEntity.getReceiverName())
	            .telNumber(dmpSoReceiverEntity.getReceiverTelNumber())
	            .receiverTelNumber(dmpSoReceiverEntity.getReceiverTelNumber())
	            .email("")
	            .country(dmpSoReceiverEntity.getCountry())
	            .provinceName(dmpSoReceiverEntity.getProvince())
	            .cityName(dmpSoReceiverEntity.getCity())
	            .districtName(dmpSoReceiverEntity.getDistrict())
	            .postCode(dmpSoReceiverEntity.getPostCode())
	            .firstAddress(dmpSoReceiverEntity.getFullAddress())
	            .fullAddress(dmpSoReceiverEntity.getFullAddress())
	            .build();
        	orderDTO.setReceiver(receiver);
        }
        //B2C销售订单买家信息表
        //B2C销售订单物流信息表
        List<PlatformOrderLogisticsDTO> platformOrderLogisticsDTOS = parseLogisticsList(dmpSoInfoEntity);
        orderDTO.setLogisticsList(platformOrderLogisticsDTOS);
        String extendData = dmpSoInfoEntity.getExtendData();
        // 标签json
        if (StringUtils.isNotBlank(extendData)){
            orderDTO.setLabelJson(extendData);
        }else {
            orderDTO.setLabelJson("{}");
        }
        
        PlatformOrderFinanceDTO dto = PlatformOrderFinanceDTO.builder()
                .currency(dmpSoInfoEntity.getCurrencyCode())
                .shippingCost(dmpSoInfoEntity.getShippingAmount())
                .logisticsCost(dmpSoInfoEntity.getActualShippingFee())
                .build();
        //B2C销售订单财务信息表
        orderDTO.setFinances(dto);
        orderDTO.setPlatform(PlatformDictEnum.SHOPEE.getCode());
        orderDTO.setUniqueId(dmpSoInfoEntity.getId());
		return orderDTO;
	}
	
	@Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("platformCode");
    }
	
	private List<PlatformOrderDetailDTO> parseDetailDto(List<DmpSoDetailEntity> dmpSoDetailEntityList) {
        List<PlatformOrderDetailDTO> list = new ArrayList<>();
        for(DmpSoDetailEntity item : dmpSoDetailEntityList) {
        	PlatformOrderDetailDTO detailDTO = new PlatformOrderDetailDTO();
            // 图片URL
        	detailDTO.setImageUrl(item.getSkuUrl());
            // skuId
            detailDTO.setSkuId("");
            // skuNo
            detailDTO.setSkuNo("");

            // 平台sku编号
            detailDTO.setPlatformSkuNo(item.getPlatformSku());
            //平台skuId
            detailDTO.setPlatformSkuId(item.getPlatformSpuNo());

            // 平台产品id
            detailDTO.setPlatformSpuNo(item.getPlatformSpuNo());

            // 库存sku编号
            detailDTO.setWarehouseName("");
            // 仓库名称
            // 库存是否扣除
            detailDTO.setWarehouseId("");
            // 数量
            detailDTO.setQty(item.getQty());
            // 单价
            detailDTO.setPrice(item.getSellPriceOrigin());
            // 金额
            detailDTO.setAmount(item.getAfterAmount());

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
            
            list.add(detailDTO);
        }
		
        return list;
    }
	
	private List<PlatformOrderLogisticsDTO> parseLogisticsList(DmpSoInfoEntity dmpSoInfoEntity) {
        List<PlatformOrderLogisticsDTO> logisticsDTOS = new ArrayList<>();
        String extendData = dmpSoInfoEntity.getExtendData();
        if(StringUtils.isNotBlank(extendData)) {
        	JSONObject parseObject = JSON.parseObject(extendData);
        	String package_number = parseObject.getString("package_number");
			String logisticType = parseObject.getString("logisticType");

        	if(StringUtils.isNotBlank(package_number)) {
        		String[] split = package_number.split(",");
        		for(String s : split) {
        			PlatformOrderLogisticsDTO dto = PlatformOrderLogisticsDTO.builder()
//                          .code(p.getPackageNumber())
                          .name(LogisticsPlatformEnum.SHOPEE.getName())
                          .deliveryTime(dmpSoInfoEntity.getDeliveryTime())
//                          .logisticsChannelName(p.getShippingCarrier())
                          .estimatedShippingCost(dmpSoInfoEntity.getEstimatedShippingFee())
                          .actualShippingCost(dmpSoInfoEntity.getActualShippingFee())
                          .accessoriesCostCurrency(dmpSoInfoEntity.getCurrencyCode())
                          .actualShippingCurrency(dmpSoInfoEntity.getCurrencyCode())
                          .estimatedShippingCurrency(dmpSoInfoEntity.getCurrencyCode())
							.logisticType(logisticType)
                          .build();
                  logisticsDTOS.add(dto);
        		}
        	}
        }
        return logisticsDTOS;
    }

}
