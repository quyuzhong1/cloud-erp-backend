package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.SoB2cNfeStatusEnum;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.oms.aliexpress.dto.response.AmountInfo;
import com.erp.oms.aliexpress.dto.response.OrderItemDetail;
import com.erp.sdk.oms.amz.spapi.model.orders.Order;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.DmpSoInfoService;
import com.sdk.oms.temu.dto.TemuOrderDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class TeMuOutStockDmpHandler extends DmpInputDbConvertDmpHandler{

	@Autowired
	private DmpSoInfoService dmpSoInfoService;
	
	@Autowired
	private DmpSoDetailService dmpSoDetailService;
	
	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
			Map<String, Object> mongoDataMap = mongoDataMaps.get(0);
			TemuOrderDTO.PageItemsDTO sourceOrder = JSON.parseObject(JSON.toJSONString(mongoDataMap), TemuOrderDTO.PageItemsDTO.class);
			for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
				TemuOrderDTO.PageItemsDTO.ParentOrderMapDTO parentOrderMapDTO = sourceOrder.getParentOrderMap();
				dmpDataMap.put("thirdCode",parentOrderMapDTO.getParentOrderSn());
				dmpDataMap.put("platformCode",parentOrderMapDTO.getParentOrderSn());
				Long shippingTimeInt = parentOrderMapDTO.getParentShippingTime();
				Instant instant = Instant.ofEpochSecond(shippingTimeInt);
				// 获取系统默认时区
				ZoneId zoneId = ZoneId.systemDefault();
				// 将 Instant 对象转换为 LocalDateTime 对象
				LocalDateTime outTime = LocalDateTime.ofInstant(instant, zoneId);
				dmpDataMap.put("deliveryTime",outTime);
				dmpDataMap.put("logisticsCode",sourceOrder.getTrackNo());
				dmpDataMap.put("shopId",sourceOrder.getShopId());
				dmpDataMap.put("shopName",sourceOrder.getShopName());
			}
		}
	}

}
