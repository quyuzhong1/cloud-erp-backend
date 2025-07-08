package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.DmpSoInfoService;
import com.sdk.oms.temu.dto.TemuOrderDTO;
import com.sdk.wms.goodcang.dto.response.GoodCangReturnInstockResp;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputTeMuSoOutStockNextDmpHandler extends DmpInputDoNextDmpHandler{

	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
		TemuOrderDTO.PageItemsDTO sourceOrder = JSON.parseObject(JSON.toJSONString(dmpInputMongoEntity), TemuOrderDTO.PageItemsDTO.class);
		Object detailListObj = dmpInputMongoEntity.get("orderList");
		if (null == detailListObj) {
			return Collections.emptyList();
		}
		// 明细信息
		List<TemuOrderDTO.PageItemsDTO.OrderListDTO> productList = JSON.parseArray(JSON.toJSONString(detailListObj), TemuOrderDTO.PageItemsDTO.OrderListDTO.class);
		if (CollectionUtils.isEmpty(productList)) {
			return Collections.emptyList();
		}
		List<Map<String, Object>> resultList = new LinkedList<>();

		for (TemuOrderDTO.PageItemsDTO.OrderListDTO product : productList) {
			Map<String, Object> map = new HashMap<>();
			map.put("thirdDetailId",product.getOrderSn());
			map.put("platformDetailId",product.getOrderSn());
			map.put("platformSku",product.getProductList().get(0).getExtCode());
			map.put("warehouseId",sourceOrder.getShipmentInfoDTODTO().getCooperativeWarehouseDTO().getWarehouseCode());
			map.put("warehouseName",sourceOrder.getShipmentInfoDTODTO().getCooperativeWarehouseDTO().getWarehouseName());
			map.put("qty",product.getQuantity());
			map.put("specifics",product.getSpec());
			map.put("thirdOrderCode",sourceOrder.getParentOrderMap().getParentOrderSn());
			map.put("platformOrderCode",sourceOrder.getParentOrderMap().getParentOrderSn());
			resultList.add(map);
		}
		return resultList;
	}

}
