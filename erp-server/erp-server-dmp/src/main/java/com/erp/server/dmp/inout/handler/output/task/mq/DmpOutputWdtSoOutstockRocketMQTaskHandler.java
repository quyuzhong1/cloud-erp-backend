package com.erp.server.dmp.inout.handler.output.task.mq;

import static com.common.core.enums.CountrySiteEnum.CHINA;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.WdtSoOutStockDTO;
import com.common.business.dto.WdtSoOutStockDetailDTO;
import com.common.business.dto.WdtSoOutStockDetailDTO.PositionDetailsList;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.CurrencyEnum;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpSoOutstockDetailEntity;
import com.erp.model.dmp.entity.DmpSoOutstockEntity;
import com.erp.model.dmp.entity.DmpSoOutstockPositionEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;

@Service
@Scope("prototype")
public class DmpOutputWdtSoOutstockRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{

	public static List<String> nullPositionNo = Arrays.asList("直发暂存" , "发货暂存待放回" , "下架暂存" , "销退质检" , "补货暂存" , "其它未上架" , "销退暂存" , "盘亏暂存" , "发货暂存" , "采购未上架" , "空仓位");
	
	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpSoOutstockEntity> dmpSoOutstockEntityMap = new HashMap<>();
		Map<String, List<DmpSoOutstockDetailEntity>> dmpSoOutstockDetailEntityEntityMap = new HashMap<>();
		Map<String, List<DmpSoOutstockPositionEntity>> dmpSoOutstockPositionEntityEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_so_outstock".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoOutstockEntity dmpSoOutstockEntity = (DmpSoOutstockEntity) v;
						dmpSoOutstockEntityMap.put(dmpSoOutstockEntity.getId(), dmpSoOutstockEntity);
					}
				}else if("dmp_so_outstock_detail".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoOutstockDetailEntity dmpSoOutstockDetailEntity = (DmpSoOutstockDetailEntity) v;
						String mainId = dmpSoOutstockDetailEntity.getMainId();
						List<DmpSoOutstockDetailEntity> list = dmpSoOutstockDetailEntityEntityMap.get(mainId);
						if(CollUtil.isEmpty(list)) {
							list = new ArrayList<>();
						}
						list.add(dmpSoOutstockDetailEntity);
						dmpSoOutstockDetailEntityEntityMap.put(mainId, list);
					}
				}else if("dmp_so_outstock_position".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoOutstockPositionEntity dmpSoOutstockPositionEntity = (DmpSoOutstockPositionEntity) v;
						String mainId = dmpSoOutstockPositionEntity.getMainId();
						List<DmpSoOutstockPositionEntity> list = dmpSoOutstockPositionEntityEntityMap.get(mainId);
						if(CollUtil.isEmpty(list)) {
							list = new ArrayList<>();
						}
						list.add(dmpSoOutstockPositionEntity);
						dmpSoOutstockPositionEntityEntityMap.put(mainId, list);
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
				if("dmp_so_outstock".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}else if("dmp_so_outstock_detail".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoOutstockDetailEntity dmpSoOutstockDetailEntity = (DmpSoOutstockDetailEntity) v;
						changeIds.add(dmpSoOutstockDetailEntity.getMainId());
					}
				}else if("dmp_so_outstock_position".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpSoOutstockPositionEntity dmpSoOutstockPositionEntity = (DmpSoOutstockPositionEntity) v;
						changeIds.add(dmpSoOutstockPositionEntity.getMainId());
					}
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			List<DmpSoOutstockDetailEntity> itemList = dmpSoOutstockDetailEntityEntityMap.get(changId);
			List<DmpSoOutstockPositionEntity> positionlist = dmpSoOutstockPositionEntityEntityMap.get(changId);
			Map<String, List<DmpSoOutstockPositionEntity>> recIdMaps = new HashMap<>();
			if(CollUtil.isNotEmpty(positionlist)) {
				recIdMaps = positionlist.stream().collect(Collectors.groupingBy(DmpSoOutstockPositionEntity::getStockoutDetailId));
			}
			WdtSoOutStockDTO wdtSoOutStockDTO = this.initOrderInfoEntity(dmpSoOutstockEntityMap.get(changId) , itemList , cfgOutputId);
			if(wdtSoOutStockDTO != null) {
				List<WdtSoOutStockDetailDTO> initOrderItem = this.initOrderItem(itemList , recIdMaps);
				wdtSoOutStockDTO.setDetailList(initOrderItem);
				map.put(changId, JSON.toJSONString(wdtSoOutStockDTO));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public WdtSoOutStockDTO initOrderInfoEntity(DmpSoOutstockEntity entity , List<DmpSoOutstockDetailEntity> itemList , String cfgOutputId) {
    	if(this.validateDataBlack(entity, cfgOutputId)) {
    		return null;
    	}
    	WdtSoOutStockDTO resultEntity = new WdtSoOutStockDTO();
    	
    	//单据编号
        resultEntity.setCode(entity.getThirdCode());
        //单据状态
        resultEntity.setApproveStatus(ApproveStatusEnum.APPROVE);
        //是否作废
        resultEntity.setInvalidStatus(false);
        //销售订单code
        resultEntity.setSoCode(entity.getPlatformCode());
        //店铺id
        resultEntity.setShopId(entity.getShopId());
        resultEntity.setShopName(entity.getShopName());
        //仓库id
        resultEntity.setWarehouseId(itemList.stream().map(DmpSoOutstockDetailEntity::getWarehouseId).filter(StringUtils::isNotBlank).findAny().orElse(""));
        resultEntity.setWarehouseName(itemList.stream().map(DmpSoOutstockDetailEntity::getWarehouseName).filter(StringUtils::isNotBlank).findAny().orElse(""));
        //出库时间
        LocalDateTime deliveryTime = entity.getDeliveryTime();
		LocalDate localDate = deliveryTime.toLocalDate();
		resultEntity.setPlanDeliveryDate(localDate);
        resultEntity.setPackDate(localDate);
        resultEntity.setActualDeliveryDate(deliveryTime);
        // 出库日期
        resultEntity.setBillDate(localDate);
        //优惠金额
        resultEntity.setTotalDiscountAmount(entity.getTotalDiscountAmount());
        //运输单号
        resultEntity.setTrackNo(entity.getLogisticsCode());
        //来源信息
        resultEntity.setSourceId(entity.getSourceId());
        resultEntity.setSourceType(SourceTypeEnum.SO_OUTSTOCK.getCode());
        resultEntity.setSourceCode(entity.getThirdBillNo());
        resultEntity.setOrderType(OrderTypeEnum.B2C.getCode());
        //审核时间
        resultEntity.setApproveTime(deliveryTime);
        resultEntity.setCreated(entity.getPlatformCreateTime());
        resultEntity.setCreateUserName("wangdiantong");
        resultEntity.setCountry(CHINA.getSite());
        //第三方单据编号
        resultEntity.setThirdCode(itemList.stream().map(DmpSoOutstockDetailEntity::getThirdOrderCode).distinct().collect(Collectors.joining(",")));
    	
        resultEntity.setLogisticsCompanyCode(entity.getLogisticsCompanyCode());
        resultEntity.setLogisticsCompanyName(entity.getLogisticsCompanyName());
        
        return resultEntity;
    }

    /**
     * 解析订单商品数据
     **/
    public List<WdtSoOutStockDetailDTO> initOrderItem(List<DmpSoOutstockDetailEntity> itemList , Map<String, List<DmpSoOutstockPositionEntity>> recIdMaps) {
        List<WdtSoOutStockDetailDTO> entityItemList = new ArrayList<>();
        for (DmpSoOutstockDetailEntity item : itemList) {
        	WdtSoOutStockDetailDTO itemEntity = new WdtSoOutStockDetailDTO();
        	
        	itemEntity.setSkuNo(item.getSkuNo());
            //实发
            Integer qty = item.getQty();
			itemEntity.setActualQty(qty);
            itemEntity.setPlanQty(qty);
            //单价
            itemEntity.setPrice(item.getSellPrice());
            //税率
            itemEntity.setTaxRate(item.getTaxRate());
            //成交价
            itemEntity.setAmount(item.getAmount());
            itemEntity.setCurrency(CurrencyEnum.RMB.getCurrencyCode());
            itemEntity.setCurrencySymbol(CurrencyEnum.RMB.getCurrencySymbol());
            itemEntity.setAllAmountLocalCurrency(item.getSellPrice().multiply(new BigDecimal(qty)));
            itemEntity.setExchangeRate(new BigDecimal(1));
            itemEntity.setSoDetailId(item.getSrcOrderDetailId());
            itemEntity.setRemark(item.getItemRemark());
            itemEntity.setSourceDetailId(item.getSrcOrderDetailId());
            itemEntity.setInvalidStatus(false);
        	
            List<DmpSoOutstockPositionEntity> positionList = recIdMaps.get(item.getThirdDetailId());
            if(CollUtil.isNotEmpty(positionList)) {
            	positionList.forEach(p -> {
            		String positionNo = p.getPositionNo();
            		if(StringUtils.isNotBlank(positionNo)) {
            			if(nullPositionNo.contains(positionNo)) {
            				p.setPositionNo("");
            			}else if(positionNo.equals("TC-JHZC1")) {
            				p.setPositionNo("TC-JHZC");
            			}else if(positionNo.equals("B2B-JHZC1")) {
            				p.setPositionNo("TC-JHZC");
            			}
            		}
            		
            	});
            	if(CollUtil.isNotEmpty(positionList)) {
            		itemEntity.setPositionDetailsList(BeanUtil.copyToList(positionList, PositionDetailsList.class));
            	}
            }
            
            entityItemList.add(itemEntity);
        }
        return entityItemList;
    }
    
    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("code");
    }
}
