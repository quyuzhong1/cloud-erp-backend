package com.erp.server.dmp.inout.handler.output.task.mq;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cItemStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.PlatformDeliveryDetailDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderDetailDTO;
import com.common.business.dto.PlatformOrderFinanceDTO;
import com.common.business.dto.PlatformOrderLogisticsDTO;
import com.common.business.dto.PlatformOrderReceiverDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.utils.StringUtil;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpLogisticInfoEntity;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.entity.DmpSoOutstockDetailEntity;
import com.erp.model.dmp.entity.DmpSoOutstockEntity;
import com.erp.model.dmp.entity.DmpSoReceiverEntity;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputAliExpressOrderRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpSoInfoEntity> dmpSoInfoEntityMap = new HashMap<>();
		Map<String, List<DmpSoDetailEntity>> dmpSoDetailEntityMap = new HashMap<>();
		Map<String, List<DmpSoReceiverEntity>> dmpSoReceiverEntityMap = new HashMap<>();
		Map<String, List<DmpSoOutstockEntity>> dmpSoOutstockEntityMap = new HashMap<>();
		Map<String, String> dmpSoOutstockIdMap = new HashMap<>();
		Map<String, List<DmpSoOutstockDetailEntity>> dmpSoOutstockDetailEntityMap = new HashMap<>();
		Map<String, List<DmpLogisticInfoEntity>> dmpLogisticInfoEntityMap = new HashMap<>();
		
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
				}else if("dmp_so_outStock".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoOutstockEntity dmpSoOutstockEntity = (DmpSoOutstockEntity) v;
						String sourceId = dmpSoOutstockEntity.getSourceId();
						List<DmpSoOutstockEntity> list = dmpSoOutstockEntityMap.get(sourceId);
						if(CollUtil.isEmpty(list)) {
							list = new ArrayList<>();
						}
						list.add(dmpSoOutstockEntity);
						dmpSoOutstockEntityMap.put(sourceId, list);
						dmpSoOutstockIdMap.put(dmpSoOutstockEntity.getId(), dmpSoOutstockEntity.getSourceId());
					}
				}else if("dmp_so_outStock_detail".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoOutstockDetailEntity dmpSoOutstockDetailEntity = (DmpSoOutstockDetailEntity) v;
						String mainId = dmpSoOutstockDetailEntity.getMainId();
						List<DmpSoOutstockDetailEntity> list = dmpSoOutstockDetailEntityMap.get(mainId);
						if(CollUtil.isEmpty(list)) {
							list = new ArrayList<>();
						}
						list.add(dmpSoOutstockDetailEntity);
						dmpSoOutstockDetailEntityMap.put(mainId, list);
					}
				}else if("dmp_logistic_info".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpLogisticInfoEntity dmpLogisticInfoEntity = (DmpLogisticInfoEntity) v;
						String mainId = dmpLogisticInfoEntity.getMainId();
						List<DmpLogisticInfoEntity> list = dmpLogisticInfoEntityMap.get(mainId);
						if(CollUtil.isEmpty(list)) {
							list = new ArrayList<>();
						}
						list.add(dmpLogisticInfoEntity);
						dmpLogisticInfoEntityMap.put(mainId, list);
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
				}else if("dmp_so_outStock".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoOutstockEntity dmpSoOutstockEntity = (DmpSoOutstockEntity) v;
						changeIds.add(dmpSoOutstockEntity.getSourceId());
					}
				}else if("dmp_so_outStock_detail".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoOutstockDetailEntity dmpSoOutstockDetailEntity = (DmpSoOutstockDetailEntity) v;
						String mainId = dmpSoOutstockDetailEntity.getMainId();
						changeIds.add(dmpSoOutstockIdMap.get(mainId));
					}
				}else if("dmp_logistic_info".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpLogisticInfoEntity dmpLogisticInfoEntity = (DmpLogisticInfoEntity) v;
						changeIds.add(dmpLogisticInfoEntity.getMainId());
					}
				}
			}
		}
		
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			List<DmpSoOutstockEntity> dmpSoOutstockEntityList = dmpSoOutstockEntityMap.get(changId);
			List<DmpSoOutstockDetailEntity> dmpSoOutstockDetailEntityList = new ArrayList<>();
			if(CollUtil.isNotEmpty(dmpSoOutstockEntityList)) {
				for(DmpSoOutstockEntity dmpSoOutstockEntity : dmpSoOutstockEntityList) {
					List<DmpSoOutstockDetailEntity> list = dmpSoOutstockDetailEntityMap.get(dmpSoOutstockEntity.getId());
					if(CollUtil.isNotEmpty(list)) {
						dmpSoOutstockDetailEntityList.addAll(list);
					}
				}
			}
			PlatformOrderDTO orderDTO = this.convert(dmpSoInfoEntityMap.get(changId), dmpSoDetailEntityMap.get(changId) 
					, dmpSoReceiverEntityMap.get(changId) , dmpSoOutstockEntityList , dmpSoOutstockDetailEntityList , dmpLogisticInfoEntityMap.get(changId) , cfgOutputId);
			if(orderDTO != null) {
				map.put(changId, JSON.toJSONString(orderDTO));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public PlatformOrderDTO convert(DmpSoInfoEntity dmpSoInfoEntity , List<DmpSoDetailEntity> dmpSoDetailEntityList , List<DmpSoReceiverEntity> dmpSoReceiverEntityList
    		, List<DmpSoOutstockEntity> dmpSoOutstockEntityList , List<DmpSoOutstockDetailEntity> dmpSoOutstockDetailEntityList , List<DmpLogisticInfoEntity> dmpLogisticInfoEntityList , String cfgOutputId) {
    	if(this.validateDataBlack(dmpSoInfoEntity, cfgOutputId)) {
    		return null;
    	}
    	if(CollUtil.isEmpty(dmpSoDetailEntityList)) {
    		return null;
    	}
    	PlatformOrderDTO orderDTO = new PlatformOrderDTO();
    	orderDTO.setPlatform(dmpSoInfoEntity.getSourcePlatform());
    	LocalDateTime platformCreateTime = dmpSoInfoEntity.getPlatformCreateTime();
		orderDTO.setBillDate(platformCreateTime.toLocalDate());
		orderDTO.setPlatformOrderCreateTime(platformCreateTime);
		String thirdCode = dmpSoInfoEntity.getThirdCode();
		orderDTO.setPlatformCode(thirdCode);
		orderDTO.setDictPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
		orderDTO.setShopId(dmpSoInfoEntity.getShopId());
		
		orderDTO.setInvalidStatus(dmpSoInfoEntity.getInvalidStatus());
		orderDTO.setIsCancel(dmpSoInfoEntity.getIsCancel());
		// 作废类型（manual手动作废，automatic自动作废）
        orderDTO.setInvalidType("");
        // 作废原因
        orderDTO.setInvalidRemark("");
        
        BigDecimal payAmount = dmpSoInfoEntity.getPayAmount();
		orderDTO.setAmount(payAmount);
        String currencyCode = dmpSoInfoEntity.getCurrencyCode();
		orderDTO.setCurrency(currencyCode);
        orderDTO.setExchangeRate(dmpSoInfoEntity.getExchangeRate());
        orderDTO.setPlatformOrderStatus(dmpSoInfoEntity.getPlatformOriginalStatus());
        
        BigDecimal shippingAmount = dmpSoInfoEntity.getShippingAmount();
		orderDTO.setShippingFee(shippingAmount);
        
        JSONObject oaidJson = new JSONObject();
        DmpSoReceiverEntity dmpSoReceiverEntity = null;
        if(CollUtil.isNotEmpty(dmpSoReceiverEntityList)) {
        	dmpSoReceiverEntity = dmpSoReceiverEntityList.get(0);
        	oaidJson.put("oaid",dmpSoReceiverEntity.getOaId());
        }
        orderDTO.setExtendData(oaidJson.toString());
    	
        orderDTO.setPayTime(dmpSoInfoEntity.getPayTime());
        orderDTO.setPayAmount(payAmount);
        
        orderDTO.setDictPayMethod(dmpSoInfoEntity.getPayMethod());
        //TODO
        orderDTO.setBuyerRemark(dmpSoInfoEntity.getBuyerRemark());
        
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
        orderDTO.setSourceId(thirdCode);
        // 来源编码
        orderDTO.setSourceCode(thirdCode);
        
        // 标签json
        orderDTO.setLabelJson(dmpSoInfoEntity.getExtendData());

        // 订单状态
        // （soB2cBillStatus字典类型）
        orderDTO.setBillStatus(dmpSoInfoEntity.getOrderStatus());

        // 审核状态状态
        // （ApproveStatus字典类型）
        orderDTO.setApproveStatusStr(dmpSoInfoEntity.getApproveStatus());

        // 付款状态（待付款、已付款）
        // （soB2cPayStatus字典类型）
        orderDTO.setPayStatus(dmpSoInfoEntity.getPayStatus() ? SoB2cPayStatusEnum.ENUM_PAID.getCode() : SoB2cPayStatusEnum.ENUM_PAYMENT.getCode());

        // 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        orderDTO.setAbnormalType("");
        // 同步金蝶状态（默认0无需同步,1待同步,2同步中,3同步成功,4同步失败）
        orderDTO.setSyncKingdeeStatus("0");

        //总优惠金额
		orderDTO.setTotalDiscount(dmpSoInfoEntity.getTotalDiscount());

		//取消商品总价
		orderDTO.setTotalCancelGoodsAmount(dmpSoInfoEntity.getTotalCancelGoodsAmount());

		//取消商品币别
		orderDTO.setCancelGoodsCurrency(dmpSoInfoEntity.getCancelGoodsCurrency());
        
        List<PlatformOrderLogisticsDTO> orderLogisticList = new ArrayList<>();
        if(CollUtil.isNotEmpty(dmpLogisticInfoEntityList)) {
        	for(DmpLogisticInfoEntity dmpLogisticInfoEntity : dmpLogisticInfoEntityList) {
        		String logisticsNo = dmpLogisticInfoEntity.getLogisticsNo();
        		if(dmpLogisticInfoEntityList.size() > 1 && StringUtils.isBlank(logisticsNo)) {
        			continue;
        		}
        		PlatformOrderLogisticsDTO logisticsDTO = new PlatformOrderLogisticsDTO();
				logisticsDTO.setCode(logisticsNo);
                String logisticsServiceName = dmpLogisticInfoEntity.getLogisticsServiceName();
                if(StringUtils.isBlank(logisticsServiceName)) {
                	logisticsServiceName = "";
                }
                logisticsDTO.setName(logisticsServiceName);
                //发货时间
                logisticsDTO.setDeliveryTime(dmpLogisticInfoEntity.getDeliveryTime());
                logisticsDTO.setActualShippingCost(shippingAmount);
                logisticsDTO.setActualShippingCurrency(dmpLogisticInfoEntity.getCurrencyCode());
            	orderLogisticList.add(logisticsDTO);
        	}
        }
        orderDTO.setLogisticsList(orderLogisticList);
        
        // 订单明细
        List<PlatformOrderDetailDTO> details = new ArrayList<>();
        if(CollUtil.isNotEmpty(dmpSoDetailEntityList)) {
        	for(DmpSoDetailEntity dmpSoDetailEntity : dmpSoDetailEntityList) {
        		PlatformOrderDetailDTO detailDTO = new PlatformOrderDetailDTO();
                // 图片URL
                detailDTO.setImageUrl(dmpSoDetailEntity.getSkuUrl());
                // skuId
                detailDTO.setSkuId("");
                // skuNo
                detailDTO.setSkuNo("");
                
                // 平台sku编号
                detailDTO.setPlatformSkuNo(dmpSoDetailEntity.getPlatformSku());

                // 平台sku编号
                detailDTO.setPlatformSpuNo(dmpSoDetailEntity.getPlatformSpuNo());
                // 库存sku编号
                detailDTO.setWarehouseName(dmpSoDetailEntity.getWarehouseName());
                
                detailDTO.setWarehouseId("");
                
                // 数量
                Integer qty = dmpSoDetailEntity.getQty();
				detailDTO.setQty(qty);
                
                BigDecimal sellPriceOrigin = dmpSoDetailEntity.getSellPriceOrigin();
                if(sellPriceOrigin != null && qty != null && qty.compareTo(0) != 0) {
                	// 单价
                	detailDTO.setPrice(sellPriceOrigin.divide(new BigDecimal(qty) , 2, RoundingMode.HALF_UP));
                }
                
				detailDTO.setAmount(sellPriceOrigin);
                detailDTO.setCurrency(dmpSoDetailEntity.getCurrencyCode());
                
                // 汇率
                detailDTO.setExchangeRate(BigDecimal.ONE);
                // 建议售价（本位币）
                detailDTO.setAdvicePrice(BigDecimal.ZERO);
                // 含税成本（本位币）
                detailDTO.setTaxCost(BigDecimal.ZERO);
                
                // 来源明细id
                detailDTO.setSourceDetailId(dmpSoDetailEntity.getPlatformDetailId());
                
                detailDTO.setLabelJson(dmpSoDetailEntity.getExtendData());
                
                // 库存组织id
                detailDTO.setWarehouseOrgId("");
                // 库存组织名称
                detailDTO.setWarehouseOrgName("");
                // 库位
                detailDTO.setWarehouseLocation("");

				// 商品状态
                detailDTO.setItemStatus(dmpSoDetailEntity.getItemStatus());
                details.add(detailDTO);
        	}
        }
        orderDTO.setDetails(details);
        
        if(CollUtil.isNotEmpty(dmpSoOutstockDetailEntityList)) {
        	List<PlatformDeliveryDetailDTO> platformDeliveryDetailDTOList = new ArrayList<>();
        	for(DmpSoOutstockDetailEntity v : dmpSoOutstockDetailEntityList) {
        		PlatformDeliveryDetailDTO deliveryDetailDTO = new PlatformDeliveryDetailDTO();
                deliveryDetailDTO.setPlatformSkuNo(v.getPlatformSku());
                deliveryDetailDTO.setQty(v.getQty());
                deliveryDetailDTO.setPlatformWarehouseName(v.getWarehouseName());
                deliveryDetailDTO.setPlatformSpuNo(v.getThirdDetailId());
                deliveryDetailDTO.setPlatformSkuId(v.getSkuId());
                deliveryDetailDTO.setScItemId(v.getPlatformDetailId());
                platformDeliveryDetailDTOList.add(deliveryDetailDTO);
        	}
        	orderDTO.setDeliveryDetailDTOList(platformDeliveryDetailDTOList);
        }
        
        PlatformOrderReceiverDTO receiverDTO = new PlatformOrderReceiverDTO();
        if(dmpSoReceiverEntity != null) {
        	receiverDTO.setCountry(dmpSoReceiverEntity.getCountry());
            String firstAddress = dmpSoReceiverEntity.getMainStreet();
            String fullAddress = dmpSoReceiverEntity.getFullAddress();
            if(StringUtils.isBlank(fullAddress)) {
            	fullAddress = "";
            }
            String secondStreet = dmpSoReceiverEntity.getSecondStreet();
            if(StringUtils.isBlank(secondStreet)) {
            	secondStreet = "";
            }
			if(StringUtils.isBlank(firstAddress)) {
            	firstAddress = fullAddress + " " + secondStreet;
            }
        	receiverDTO.setFirstAddress(firstAddress);
            receiverDTO.setSecondAddress(secondStreet);
            receiverDTO.setFullAddress(fullAddress);
            receiverDTO.setCityName(dmpSoReceiverEntity.getCity());
            receiverDTO.setProvinceName(dmpSoReceiverEntity.getProvince());
            receiverDTO.setReceiverName(dmpSoReceiverEntity.getReceiverName());
            String mainPhone = dmpSoReceiverEntity.getMainPhone();
            if(StringUtils.isNotBlank(mainPhone)) {
            	receiverDTO.setReceiverTelNumber(mainPhone);
            }else {
            	receiverDTO.setReceiverTelNumber(dmpSoReceiverEntity.getReceiverTelNumber());
            }
            receiverDTO.setPostCode(dmpSoReceiverEntity.getPostCode());
            receiverDTO.setReceiverTaxNo(dmpSoReceiverEntity.getReceiverTaxNo());
            // 买家电话
            receiverDTO.setTelNumber(mainPhone);
            
            String buyerId = dmpSoReceiverEntity.getBuyerId();
			receiverDTO.setLoginId(buyerId);
            receiverDTO.setCustomerId(buyerId);
            receiverDTO.setName(dmpSoReceiverEntity.getBuyerName());
        }else {
        	receiverDTO.setCountry("");
            receiverDTO.setName("");
            receiverDTO.setFirstAddress("");
            receiverDTO.setSecondAddress("");
            receiverDTO.setFullAddress("");
            receiverDTO.setCityName("");
            receiverDTO.setCountryName("");
            receiverDTO.setDistrictName("");
            receiverDTO.setPostCode("");
            receiverDTO.setCustomerId("");
            receiverDTO.setLoginId("");
        }
        receiverDTO.setEmail("");
        receiverDTO.setCountryName("");
        receiverDTO.setDistrictName("");
        
        orderDTO.setReceiver(receiverDTO);
        
        // 订单财务信息
        PlatformOrderFinanceDTO financeDTO = new PlatformOrderFinanceDTO();
        financeDTO.setShippingCost(shippingAmount);
        financeDTO.setLogisticsCost(shippingAmount);
        orderDTO.setFinances(financeDTO);
        return orderDTO;
    }

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("platformCode");
    }
    
    @Override
    public void getRetryPushSourceData(List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList,
    		DmpOutputTaskRequest dmpOutputTaskRequest) {
    	DmpCfgInputConvertEntity dmpCfgInputConvertEntity = dmpCfgInputConvertEntityList.get(0);
		List<String> mainIds = dmpOutputTaskRequest.getConvertInputDmpBaseEntityListMaps().get(dmpCfgInputConvertEntity).stream().map(BaseEntity::getId).collect(Collectors.toList());
    	List<String> soOutStockIds = null;
		for(int i = 1; i < dmpCfgInputConvertEntityList.size(); i++) {
			DmpCfgInputConvertEntity childDmpCfgInputConvertEntity = dmpCfgInputConvertEntityList.get(i);
			String storageName = childDmpCfgInputConvertEntity.getStorageName();
			ServiceImpl serviceImpl = ApplicationContextUtils.getBean(StrUtils.underlineToCamel(storageName, true) + "ServiceImpl" , ServiceImpl.class);
			QueryWrapper<?> wrapper = new QueryWrapper<>();
			if("dmp_so_outStock".equals(storageName)) {
				wrapper.in("source_id", mainIds);
			}else if("dmp_so_outStock_detail".equals(storageName)){
				if(CollUtil.isEmpty(soOutStockIds)) {
					continue;
				}
				wrapper.in("main_id", soOutStockIds);
			}else {
				wrapper.in("main_id", mainIds);
			}
			List<BaseEntity> childEntityList = serviceImpl.list(wrapper);
			if("dmp_so_outStock".equals(storageName)) {
				soOutStockIds = childEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
			}
			dmpOutputTaskRequest.getConvertInputDmpBaseEntityListMaps().put(childDmpCfgInputConvertEntity, childEntityList);
			dmpOutputTaskRequest.getChangeConvertInputDmpBaseEntityListMaps().put(childDmpCfgInputConvertEntity, childEntityList);
		}
    }
}
