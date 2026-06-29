package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.List;

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

	private static DmpCfgInputEntity getDmpCfgInputEntity(DmpCfgInputService dmpCfgInputService,
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

	private static String getSystemCode(DmpBasicSystemEntity dmpBasicSystemEntity) {
		if (dmpBasicSystemEntity == null || StringUtils.isBlank(dmpBasicSystemEntity.getCode())) {
			return DEFAULT_SYSTEM_CODE;
		}
		return dmpBasicSystemEntity.getCode();
	}
}
