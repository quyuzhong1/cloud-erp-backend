package com.erp.server.dmp.inout.handler.input.task.mongo;

import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpInputTaskFileContentTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * dmp输入任务mongo转换处理器, 无法判断的重复记录, 根据唯一键组合添加出现索引index号
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAmzListingCheckDeletedMongoHandler extends DmpInputBaseMongoHandler{

	public static final String AMAZON_LISTING_DATA = "amazon_listing_data";

	@Override
	protected List<Map<String, Object>> getDataList(DmpInputTaskFileContentTypeEnum contentType , List<String> resultList) {
		List<Map<String, Object>> dataList = super.getDataList(contentType, resultList);
		if (CollectionUtils.isEmpty(dataList)) {
			return dataList;
		}
		String requestShopId = dataList.get(0).getOrDefault("requestShopId", "").toString();
		if (StringUtils.isBlank(requestShopId)) {
			ServiceException.runError("requestShopId信息为空");
		}
		// 按listing报告内容
		List<ParamData> chlidParamDataList = new ArrayList<>();
		chlidParamDataList.add(new ParamData("requestShopId", "requestShopId", PannoEnum.EQ, requestShopId));
		List<Map<String, Object>> listingMongoData = mongoService.findMongoData(chlidParamDataList, AMAZON_LISTING_DATA);
		if (CollectionUtils.isEmpty(listingMongoData)) {
			return dataList;
		}
		List<String> newListingIdList = dataList.stream()
				.map(e -> e.getOrDefault("listingId", "").toString())
				.filter(e -> !StringUtils.isEmpty(e))
				.collect(Collectors.toList());
		for (Map<String, Object> listingMongoDataItem : listingMongoData) {
			String listingId = listingMongoDataItem.getOrDefault("listingId", "").toString();
			if (newListingIdList.contains(listingId)) {
				continue;
			}
			//标记为删除状态
			listingMongoDataItem.put("status", "Delete");
			dataList.add(listingMongoDataItem);
		}
		return dataList;
	}
}
