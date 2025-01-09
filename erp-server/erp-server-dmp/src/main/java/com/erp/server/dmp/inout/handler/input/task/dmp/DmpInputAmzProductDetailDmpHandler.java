package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 亚马逊Listing下一步字段映射转换
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzProductDetailDmpHandler extends DmpInputDoChildDmpHandler {

	public static final String PRODUCT_ID = "productId";
	public static final String NEXT_LEVEL_ID = "nextLevelId";
	public static final String REPORT_ID = "reportId";
	public static final String SHOP_ID = "shopId";
	public static final String SPU_ID = "spu_id";

	public static final String AMAZON_LISTING_DATA = "amazon_listing_data";

	@Override
	protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList, String childMongoStorageName) {
		if (CollectionUtils.isEmpty(dmpInputMongoEntityList)){
			return Collections.emptyList();
		}
		Map<String, Object> reportInfoMap = dmpInputMongoEntityList.get(0);
		String reportId = reportInfoMap.getOrDefault(REPORT_ID, "").toString();
		String shopId = reportInfoMap.getOrDefault(SHOP_ID, "").toString();

		// 当前sku子任务明细所有结果
		List<ParamData> paramDataList = new ArrayList<>();
		paramDataList.add(new ParamData(NEXT_LEVEL_ID, NEXT_LEVEL_ID, PannoEnum.EQ, shopId));
		List<Map<String, Object>> listingDetailMongoData = mongoService.findMongoData(paramDataList, childMongoStorageName);

		// 按listing报告内容
		List<ParamData> chlidParamDataList = new ArrayList<>();
		chlidParamDataList.add(new ParamData(REPORT_ID, REPORT_ID, PannoEnum.EQ, reportId));
		List<Map<String, Object>> listingMongoData = mongoService.findMongoData(chlidParamDataList, AMAZON_LISTING_DATA);
		if (CollectionUtils.isEmpty(listingMongoData)){
			return Collections.emptyList();
		}

		// 组合按listing报告内容和明细
		for (Map<String, Object> listingMongoDataItem : listingMongoData) {
			String listingProductId = listingMongoDataItem.getOrDefault(PRODUCT_ID, "").toString();
			if (StringUtils.isBlank(listingProductId)){
				continue;
			}
			String asin1 = listingMongoDataItem.getOrDefault("asin1", "").toString();
			// 默认asin=asin1
			listingMongoDataItem.put("asin", asin1);
			// 匹配明细
			Map<String, Object> detailMap = listingDetailMongoData
					.stream()
					.filter(e -> e.getOrDefault(PRODUCT_ID, "").toString().equalsIgnoreCase(listingProductId))
					.findFirst()
					.orElse(null);
			if (null == detailMap){
				continue;
			}
			// 移除公共字段
			detailMap.remove("_id");
			detailMap.remove("dataEncrypt");
			detailMap.remove("convertId");
			detailMap.remove("inputTaskId");
			detailMap.remove("mongoCreateTime");
			detailMap.remove("mongoUpdateTime");
			detailMap.remove("nextLevelId");
			detailMap.remove("uniqueEncrypt");
			// 添加到当前
			listingMongoDataItem.putAll(detailMap);
		}
		return listingMongoData;
	}
	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		log.debug("DmpInputAmzProductDetailDmpHandler afterConvertData 处理");
	}

	@Override
	protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList){
		ServiceImpl parentServiceImpl = this.getServiceImpl("dmp_product_info");
		QueryWrapper<?> wrapper = new QueryWrapper<>();
		wrapper.eq(INPUT_TASK_ID, inputTaskId);
		List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
		Map<String, String> billNoIdMap = new HashMap<>();
		if(CollUtil.isNotEmpty(listMaps)) {
			for(Map<String, Object> listMap : listMaps) {
				billNoIdMap.put(listMap.get(SPU_ID).toString(), listMap.get(BaseEntity.FIELD_ID).toString());
			}
		}
		for(Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
			String billNo = dmpInputMongoChildEntity.get(PRODUCT_ID).toString();
			String dmpId = billNoIdMap.get(billNo);
			dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
		}
	}
}
