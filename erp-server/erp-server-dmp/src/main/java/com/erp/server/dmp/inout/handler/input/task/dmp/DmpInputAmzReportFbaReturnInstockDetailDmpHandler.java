package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpSoOutstockEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.erp.model.dmp.entity.DmpThirdReturnInboundEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzReportFbaReturnInstockDetailDmpHandler extends DmpInputDoNextDmpHandler{

	public static final String THIRD_DETAIL_ID = "thirdDetailId";

	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
		log.debug("DmpInputAmzReportFbaReturnInstockDetailDmpHandler afterConvertData 处理");
		if (null == dmpInputMongoEntity) {
			return Collections.emptyList();
		}
		if (dmpInputMongoEntity.isEmpty()) {
			return Collections.emptyList();
		}
		for (Map.Entry<String, Object> stringObjectEntry : dmpInputMongoEntity.entrySet()) {
			if ("sku".equals(stringObjectEntry.getKey()) && null != stringObjectEntry.getValue()){
				String newSkuValue = stringObjectEntry.getValue()
						.toString()
						.replaceAll("&#8208;", "‐");
				dmpInputMongoEntity.put(stringObjectEntry.getKey(), newSkuValue);
			}
		}

		return Collections.singletonList(dmpInputMongoEntity);
	}

	@Override
	protected void afterConvertData(Map<List<Map<String , Object>>, List<TreeMap<String , Object>>> dmpInputDataDmpRelationMaps) {
		log.debug("DmpInputAmzReportFbaReturnInstockDetailDmpHandler afterConvertData：");
		String parentTableName = SqlHelper.table(DmpThirdReturnInboundEntity.class).getTableName();
		ServiceImpl parentServiceImpl = this.getServiceImpl(parentTableName);
		QueryWrapper<?> wrapper = new QueryWrapper<>();
		wrapper.eq(INPUT_TASK_ID, inputTaskId);
		List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);

		Map<String, String> dmpReturnIdMap = new HashMap<>();
		if(CollectionUtils.isNotEmpty(listMaps)) {
			for(Map<String, Object> listMap : listMaps) {
				String key = CharSequenceUtil.format("{}_{}", listMap.get("platform_order_no").toString(), listMap.get("auth_id").toString());
				dmpReturnIdMap.put(key, listMap.get(BaseEntity.FIELD_ID).toString());
			}
		}
		for (List<TreeMap<String, Object>> dmpInputMongoList : dmpInputDataDmpRelationMaps.values()) {
			for (TreeMap<String, Object> detailMap : dmpInputMongoList) {
				String returnOrderId = CharSequenceUtil.format("{}_{}", detailMap.get("orderId").toString(), detailMap.get("platformShopCode").toString());
				String dmpId = dmpReturnIdMap.get(returnOrderId);
				detailMap.put("mainId", dmpId);
			}
		}
		log.debug("DmpInputAmzReportFbaReturnInstockDetailDmpHandler afterConvertData：");
	}
}
