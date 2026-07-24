package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.utils.DmpHandlerCache;
import com.erp.server.dmp.inout.utils.DmpHandlerUtils;
import com.erp.server.dmp.service.DmpCfgInputService;

import cn.hutool.core.collection.CollUtil;

public final class AliExpressDmpHandlerUtils {

	private static final String DEFAULT_SYSTEM_CODE = "aliexpress";
	private static final String DATA_STORAGE_NAME = "data";

	private AliExpressDmpHandlerUtils() {
	}

	public static String getSourcePlatform(DmpBasicSystemEntity dmpBasicSystemEntity) {
		String systemCode = getSystemCode(dmpBasicSystemEntity);
		if (DmpBasicSystemCodeEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(systemCode)) {
			return DmpBasicSystemCodeEnum.ALI_EXPRESS.getCode();
		}
		return systemCode;
	}

	public static String getMongoStorageName(DmpBasicSystemEntity dmpBasicSystemEntity,
			DmpCfgInputService dmpCfgInputService,
			DmpHandlerCache dmpHandlerCache,
			String systemId,
			String inputCode) {
		DmpCfgInputEntity dmpCfgInputEntity = getDmpCfgInputEntity(dmpCfgInputService, dmpHandlerCache, systemId, inputCode);
		if (dmpCfgInputEntity == null) {
			return String.join("_", getSystemCode(dmpBasicSystemEntity), inputCode, DATA_STORAGE_NAME);
		}
		List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityList =
				dmpHandlerCache.getDmpCfgInputConvertEntityList(convert -> dmpCfgInputEntity.getId().equals(convert.getMainId())
						&& DmpInputTaskStatusEnum.MONGO.getCode().equals(convert.getInputStatus()));
		if (CollUtil.isEmpty(dmpCfgInputConvertEntityList)) {
			return String.join("_", getSystemCode(dmpBasicSystemEntity), inputCode, DATA_STORAGE_NAME);
		}
		if (dmpBasicSystemEntity == null) {
			return String.join("_", getSystemCode(null), inputCode, DATA_STORAGE_NAME);
		}
		return DmpHandlerUtils.getMongoStorageName(dmpBasicSystemEntity, dmpCfgInputEntity, dmpCfgInputConvertEntityList.get(0));
	}

	public static List<String> collectParentAndChildOrderIds(List<Map<String, Object>> orderDataList) {
		Set<String> orderIds = new LinkedHashSet<>();
		if (CollUtil.isNotEmpty(orderDataList)) {
			for (Map<String, Object> orderData : orderDataList) {
				orderIds.addAll(collectParentAndChildOrderIds(orderData));
			}
		}
		return new ArrayList<>(orderIds);
	}

	public static List<String> collectParentAndChildOrderIds(Map<String, Object> orderData) {
		Set<String> orderIds = new LinkedHashSet<>();
		addOrderIdFields(orderIds, orderData);
		addOrderIdsFromValue(orderIds, orderData == null ? null : orderData.get("product_list"));
		addOrderIdsFromValue(orderIds, orderData == null ? null : orderData.get("child_order_list"));
		return new ArrayList<>(orderIds);
	}

	public static List<String> collectCurrentOrderIds(Map<String, Object> parentOrderData, Map<String, Object> childOrderData) {
		Set<String> orderIds = new LinkedHashSet<>();
		addOrderIdFields(orderIds, parentOrderData);
		addOrderIdFields(orderIds, childOrderData);
		return new ArrayList<>(orderIds);
	}

	/**
	 * 根据系统和输入编码获取有效的输入配置。
	 *
	 * @param dmpCfgInputService 输入配置服务
	 * @param dmpHandlerCache DMP 配置缓存
	 * @param systemId 系统 ID
	 * @param inputCode 输入编码
	 * @return 输入配置，不存在时返回 null
	 */
	public static DmpCfgInputEntity getDmpCfgInputEntity(DmpCfgInputService dmpCfgInputService,
			DmpHandlerCache dmpHandlerCache,
			String systemId,
			String inputCode) {
		List<DmpCfgInputEntity> cacheList = dmpHandlerCache.getDmpCfgInputEntityList(input ->
				systemId.equals(input.getSystemId()) && inputCode.equals(input.getCode()));
		if (CollUtil.isNotEmpty(cacheList)) {
			return cacheList.get(0);
		}
		QueryWrapper<DmpCfgInputEntity> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(DmpCfgInputEntity.SYSTEM_ID, systemId)
				.eq(DmpCfgInputEntity.CODE, inputCode)
				.eq(BaseEntity.IS_DELETED, false)
				.last("limit 1");
		return dmpCfgInputService.getOne(queryWrapper);
	}

	@SuppressWarnings("unchecked")
	private static void addOrderIdsFromValue(Set<String> orderIds, Object value) {
		if (value instanceof List) {
			for (Object item : (List<?>) value) {
				if (item instanceof Map) {
					addOrderIdFields(orderIds, (Map<String, Object>) item);
				}
			}
		} else if (value instanceof Map) {
			addOrderIdFields(orderIds, (Map<String, Object>) value);
		}
	}

	private static void addOrderIdFields(Set<String> orderIds, Map<String, Object> data) {
		if (data == null) {
			return;
		}
		addIfNotBlank(orderIds, data.get("order_id"));
		addIfNotBlank(orderIds, data.get("child_id"));
		addIfNotBlank(orderIds, data.get("child_order_id"));
	}

	private static void addIfNotBlank(Set<String> values, Object value) {
		if (value == null) {
			return;
		}
		String valueStr = value.toString();
		if (StringUtils.isNotBlank(valueStr)) {
			values.add(valueStr);
		}
	}

	private static String getSystemCode(DmpBasicSystemEntity dmpBasicSystemEntity) {
		if (dmpBasicSystemEntity == null || StringUtils.isBlank(dmpBasicSystemEntity.getCode())) {
			return DEFAULT_SYSTEM_CODE;
		}
		return dmpBasicSystemEntity.getCode();
	}
}
