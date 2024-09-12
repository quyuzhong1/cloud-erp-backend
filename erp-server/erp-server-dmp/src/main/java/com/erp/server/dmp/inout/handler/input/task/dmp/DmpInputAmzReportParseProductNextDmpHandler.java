package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 亚马逊Listing下一步字段映射转换
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzReportParseProductNextDmpHandler extends DmpInputDoNextDmpHandler {

	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
		return Collections.singletonList(dmpInputMongoEntity);
	}

	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		log.debug("DmpInputAmzReportParseProductNextDmpHandler afterConvertData 处理");
	}
}
