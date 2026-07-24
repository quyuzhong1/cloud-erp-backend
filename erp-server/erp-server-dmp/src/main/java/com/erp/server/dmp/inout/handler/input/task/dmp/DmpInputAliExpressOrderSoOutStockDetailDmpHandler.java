package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.core.utils.CurrencyUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpSoOutstockEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpSoOutstockService;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressOrderSoOutStockDetailDmpHandler extends DmpInputAliExpressOrderDoChildDmpHandler{

	private static final String SO_OUTSTOCK_CODE = "soOutstock";
	
	@Resource
	private DmpSoOutstockService dmpSoOutstockService;
	
	@Override
	protected List<Map<String, Object>> afterDoDmpInputMongoChildEntityList(
			List<Map<String, Object>> dmpInputMongoChildList) {
		if(CollUtil.isNotEmpty(dmpInputMongoChildList)) {
			List<String> orderIdList = dmpInputMongoChildList.stream().map(d -> d.get("fulfillment_order_no").toString()).collect(Collectors.toList());
			List<ParamData> paramDataList = new ArrayList<>();
			paramDataList.add(new ParamData(
					DmpInputMongoHandler.MONGO_BASE_INPUTTASKID,
					DmpInputMongoHandler.MONGO_BASE_INPUTTASKID,
					PannoEnum.EQ,
					getSiblingOutstockTask().getId()));
			paramDataList.add(new ParamData("fulfillment_order_no", "fulfillment_order_no", PannoEnum.IN, orderIdList));
			List<Map<String, Object>> findMongoData = mongoService.findMongoData(paramDataList,
					AliExpressDmpHandlerUtils.getMongoStorageName(dmpBasicSystemEntity, dmpCfgInputService, dmpHandlerCache,
							dmpCfgInputEntity.getSystemId(), SO_OUTSTOCK_CODE));
			if(CollUtil.isNotEmpty(findMongoData)) {
				Map<String, Map<String, Object>> orderNoMainMap = findMongoData.stream().collect(Collectors.toMap(
						f -> f.get("fulfillment_order_no").toString(),
						f -> f,
						(left, right) -> left));
				for(Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
					Map<String, Object> mainMap = orderNoMainMap.get(dmpInputMongoChild.get("fulfillment_order_no"));
					if (mainMap == null) {
						throw new ServiceException("未找到当前批次速卖通发货主单，履约单号:"
								+ dmpInputMongoChild.get("fulfillment_order_no"));
					}
					dmpInputMongoChild.put("warehouseName", mainMap.get("warehouse_name"));
					dmpInputMongoChild.put("thirdOrderCode", mainMap.get("trade_order_no"));
					dmpInputMongoChild.put("platformOrderCode", mainMap.get("trade_order_no"));

					// 价格信息
					// 明细单价
					String unitPriceStr = dmpInputMongoChild.getOrDefault("unit_price", "").toString();
					if (StringUtils.isNotBlank(unitPriceStr)){
						CurrencyUtil.Money unitPrice = CurrencyUtil.Money.init(unitPriceStr);
						dmpInputMongoChild.put("currency" ,unitPrice.getCurrency());
					}

					// 实际明细支付金额
					String skuActualPaidAmountStr = dmpInputMongoChild.getOrDefault("sku_actual_paid_amount", "").toString();
					if (StringUtils.isNotBlank(skuActualPaidAmountStr)){
						CurrencyUtil.Money skuActualPaidAmount = CurrencyUtil.Money.init(skuActualPaidAmountStr);
						dmpInputMongoChild.put("payCurrency", skuActualPaidAmount.getCurrency());
					}

					// 明细折扣金额
					String skuDiscountAmountStr = dmpInputMongoChild.getOrDefault("sku_discount_amount", "").toString();
					if (StringUtils.isNotBlank(skuDiscountAmountStr)){
						CurrencyUtil.Money skuDiscountAmount = CurrencyUtil.Money.init(skuDiscountAmountStr);
						dmpInputMongoChild.put("discountCurrency", skuDiscountAmount.getCurrency());
					}
				}
			}
		}
		return dmpInputMongoChildList;
	}

	/**
	 * 获取当前订单主任务下的发货主单兄弟任务。
	 *
	 * @return 本批次发货主单任务
	 */
	private DmpInputTaskEntity getSiblingOutstockTask() {
		DmpCfgInputEntity outstockInput = AliExpressDmpHandlerUtils.getDmpCfgInputEntity(
				dmpCfgInputService,
				dmpHandlerCache,
				dmpCfgInputEntity.getSystemId(),
				SO_OUTSTOCK_CODE);
		if (outstockInput == null) {
			throw new ServiceException("未找到速卖通soOutstock输入配置");
		}
		DmpInputTaskEntity outstockTask = dmpInputTaskService.lambdaQuery()
				.eq(DmpInputTaskEntity::getParentTaskId, inputTaskId)
				.eq(DmpInputTaskEntity::getCfgInputId, outstockInput.getId())
				.last("limit 1")
				.one();
		if (outstockTask == null) {
			throw new ServiceException("未找到当前批次速卖通soOutstock兄弟任务");
		}
		return outstockTask;
	}
	
	@Override
	protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList) {
		QueryWrapper<DmpSoOutstockEntity> wrapper = new QueryWrapper<>();
		wrapper.eq(INPUT_TASK_ID, inputTaskId);
		List<Map<String, Object>> listMaps = dmpSoOutstockService.listMaps(wrapper);
		Map<String, String> billNoIdMap = new HashMap<>();
		if(CollUtil.isNotEmpty(listMaps)) {
			for(Map<String, Object> listMap : listMaps) {
				billNoIdMap.put(listMap.get("third_code").toString(), listMap.get(BaseEntity.FIELD_ID).toString());
			}
		}
		for(Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
			String billNo = dmpInputMongoChildEntity.get("fulfillment_order_no").toString();
			String dmpId = billNoIdMap.get(billNo);
			dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
		}
	}
}
