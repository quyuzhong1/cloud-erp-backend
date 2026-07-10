package com.erp.server.dmp.inout.handler.input.task.dmp.weishi;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.erp.model.dmp.dto.CfgSettingDTO;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDbConvertDmpHandler;
import com.erp.server.dmp.service.CfgSettingService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class WeiShiOutBoundDmpHandler extends DmpInputDbConvertDmpHandler {

	@Resource
	private CfgSettingService cfgSettingService;

	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		super.afterConvertData(dmpInputDataDmpRelationMaps);
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
			Map<String, Object> mongoData = mongoDataMaps.get(0);
			if(mongoData.containsKey("checkOutDate") && Objects.nonNull(mongoData.get("checkOutDate"))){
				String shippedTimeStr = String.valueOf(mongoData.get("checkOutDate"));
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				// 解析为 LocalDateTime 对象
				LocalDateTime shippedTime = LocalDateTime.parse(shippedTimeStr, formatter);
				String warehouseCode = String.valueOf(mongoData.get("warehouseCode"));
				shippedTime = convertShippedTime(warehouseCode, shippedTime);
				for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
					dmpDataMap.put("dateShipping", shippedTime);
				}
			}
		}
	}

	private LocalDateTime convertShippedTime(String warehouseCode, LocalDateTime shippedTime) {
		CfgSettingDTO.ShippedTimeZoneSettingDTO setting = getShippedTimeZoneSetting();
		if (Objects.isNull(setting) || CollUtil.isEmpty(setting.getWarehouseCodes())
				|| !setting.getWarehouseCodes().contains(warehouseCode)
				|| setting.getTargetOffsetHours() == null) {
			return shippedTime;
		}
		String sourceZone = CharSequenceUtil.blankToDefault(setting.getSourceZone(), "Asia/Shanghai");
		ZonedDateTime sourceZoned = shippedTime.atZone(ZoneId.of(sourceZone));
		ZonedDateTime targetZoned = sourceZoned.withZoneSameInstant(ZoneOffset.ofHours(setting.getTargetOffsetHours()));
		return targetZoned.toLocalDateTime();
	}

	private CfgSettingDTO.ShippedTimeZoneSettingDTO getShippedTimeZoneSetting() {
		String cfgValue = cfgSettingService.getValue(SettingEnum.WEISHI_OUTBOUND_SHIPPED_TIME_ZONE);
		if (CharSequenceUtil.isBlank(cfgValue)) {
			return null;
		}
		return JSONUtil.toBean(cfgValue, CfgSettingDTO.ShippedTimeZoneSettingDTO.class);
	}
}
