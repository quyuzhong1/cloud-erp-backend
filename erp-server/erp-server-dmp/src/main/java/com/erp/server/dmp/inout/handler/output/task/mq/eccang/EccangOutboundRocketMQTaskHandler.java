package com.erp.server.dmp.inout.handler.output.task.mq.eccang;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.PlatformOutboundDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpThirdOutboundEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import com.sdk.wms.antu.enums.AntuEnums;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@Scope("prototype")
public class EccangOutboundRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpThirdOutboundEntity> dmpThirdOutboundEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_third_outbound".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpThirdOutboundEntity dmpThirdOutboundEntity = (DmpThirdOutboundEntity) v;
						dmpThirdOutboundEntityMap.put(dmpThirdOutboundEntity.getId(), dmpThirdOutboundEntity);
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
				if("dmp_third_outbound".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
		for(String changId : changeIds) {
			DmpThirdOutboundEntity dmpThirdOutboundEntity = dmpThirdOutboundEntityMap.get(changId);
			PlatformOutboundDTO platformOutboundDTO = this.convert(dmpThirdOutboundEntity, cfgOutputId);
			if(platformOutboundDTO != null) {
				map.put(dmpThirdOutboundEntity.getId(), JSON.toJSONString(platformOutboundDTO));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public PlatformOutboundDTO convert(DmpThirdOutboundEntity dmpThirdOutboundEntity , String cfgOutputId) {
    	if(this.validateDataBlack(dmpThirdOutboundEntity, cfgOutputId)) {
    		return null;
    	}
    	String orderStatus = dmpThirdOutboundEntity.getOrderStatus();
		String erpOrderStatus = AntuEnums.OrderStatusEnum.getErpOrderStatus(orderStatus);
		if(StringUtils.isBlank(erpOrderStatus)) {
			return null;
		}
    	PlatformOutboundDTO platformOutboundDTO = BeanUtil.copyProperties(dmpThirdOutboundEntity, PlatformOutboundDTO.class);
    	String sourcePlatform = dmpThirdOutboundEntity.getSourcePlatform();
		platformOutboundDTO.setPlatform(sourcePlatform);
    	platformOutboundDTO.setProvider(sourcePlatform);
    	platformOutboundDTO.setOutBoundTime(dmpThirdOutboundEntity.getDateShipping());
		platformOutboundDTO.setOrderStatus(erpOrderStatus);
    	platformOutboundDTO.setThirdOrderStatus(AntuEnums.OrderStatusEnum.getName(orderStatus));
    	platformOutboundDTO.setTrackNo(dmpThirdOutboundEntity.getTrackingNo());
    	platformOutboundDTO.setItems(this.convertItems(dmpThirdOutboundEntity.getDetailListJson(), dmpThirdOutboundEntity.getOrderCode()));
    	
        return platformOutboundDTO;
    }

    private List<PlatformOutboundDTO.Item> convertItems(String detailListJson, String orderCode) {
		if(StringUtils.isBlank(detailListJson)) {
			return Collections.emptyList();
		}
		try {
			JSONArray jsonArray = JSON.parseArray(detailListJson);
			if(CollUtil.isEmpty(jsonArray)) {
				return Collections.emptyList();
			}
			List<PlatformOutboundDTO.Item> items = new ArrayList<>();
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject row = jsonArray.getJSONObject(i);
				if (row == null) {
					continue;
				}
				String productSku = row.getString("productSku");
				if (StringUtils.isBlank(productSku)) {
					productSku = row.getString("product_sku");
				}
				if (StringUtils.isBlank(productSku)) {
					productSku = row.getString("platformSkuNo");
				}
				if (StringUtils.isBlank(productSku)) {
					productSku = row.getString("platform_sku_no");
				}
				if (StringUtils.isBlank(productSku)) {
					productSku = row.getString("skuNo");
				}
				Integer qty = row.getInteger("quantity");
				if (qty == null) {
					qty = row.getInteger("actualQty");
				}
				if (qty == null) {
					qty = row.getInteger("actual_qty");
				}
				if (qty == null) {
					qty = row.getInteger("qty");
				}
				if (qty == null) {
					qty = parseIntegerValue(row.getString("quantity"));
				}
				if (StringUtils.isBlank(productSku)) {
					continue;
				}
				if (qty == null) {
					log.warn("三方仓出库明细数量缺失, orderCode={}, productSku={}", orderCode, productSku);
					continue;
				}
				items.add(new PlatformOutboundDTO.Item(productSku, qty));
			}
			return items;
		} catch (Exception e) {
			log.error("解析三方仓出库明细 JSON 格式异常, orderCode={}, jsonLen={}, detailListJson={}",
					orderCode, detailListJson.length(), abbreviateDetailListJson(detailListJson), e);
			return Collections.emptyList();
		}
    }

    private Integer parseIntegerValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
    }

    private String abbreviateDetailListJson(String detailListJson) {
		if (detailListJson == null) {
			return null;
		}
		int maxLen = 500;
		if (detailListJson.length() <= maxLen) {
			return detailListJson;
		}
		return detailListJson.substring(0, maxLen) + "...(len=" + detailListJson.length() + ")";
    }

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Arrays.asList("orderCode");
    }
}
