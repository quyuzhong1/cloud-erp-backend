package com.common.business.cache;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.DorisQuerySettingDTO;
import com.common.business.mapper.BaseDataMapper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LocalCache implements CommandLineRunner{

	@Value("${fresh.local.cache.time:5}")
    private int freshCacheTime;
	
	@Value("${fresh.local.cache.switch:true}")
    private boolean freshCacheSwitch;
	
	@Autowired(required = false)
	private BaseDataMapper baseDataMapper;
	
	private static final String TABLE_NAME = "dict_basic";
	
	private static final String BASE_EXTEND_QUERY_SQL = " type = 'doris_query_cfg' ";
	
	private Map<String , DorisQuerySettingDTO> dorisQueryCfgSettingMappingCache;
	
	private synchronized void initDorisQueryCfgSetting() {
		List<Map<String , Object>> dorisQueryCfgSettingEntityList = baseDataMapper.queryDbBySql(TABLE_NAME, BASE_EXTEND_QUERY_SQL);
		dorisQueryCfgSettingMappingCache = dorisQueryCfgSettingEntityList.stream().filter(c -> Boolean.valueOf(c.get("is_deleted").toString())).collect(Collectors.toMap(c -> {
			String value = c.get("value").toString();
			if(!value.startsWith("/")) {
				value = "/" + value;
			}
			return value;
		}, c -> {
			DorisQuerySettingDTO d = new DorisQuerySettingDTO();
			String name = c.get("name").toString();
			if(StringUtils.isNotBlank(name)) {
				try {
					d = JSON.parseObject(name, DorisQuerySettingDTO.class);
				} catch (Exception e) {
					log.error("转换doris配置查询错误" , e);
				}
			}
			return d;
		} , (c1 , c2) -> c1));
	}
	
	public DorisQuerySettingDTO getDorisQuerySettingDTO(String requestURI) {
		if(dorisQueryCfgSettingMappingCache == null) {
			this.initDorisQueryCfgSetting();
		}
		if(!requestURI.startsWith("/")) {
			requestURI = "/" + requestURI;
		}
		return dorisQueryCfgSettingMappingCache.get(requestURI);
	}
	
	@Override
	public void run(String... args) throws Exception {
		this.initCache(freshCacheSwitch);
	}

	public void initCache(boolean isCreateTask) {
		this.initDorisQueryCfgSetting();
		if(isCreateTask) {
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				String formatDateTime = DateUtil.formatDateTime(DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)));
				List<Map<String , Object>> cfgSettingEntityFreshList = baseDataMapper.queryDbBySql(TABLE_NAME, BASE_EXTEND_QUERY_SQL + " and update_time >= '" + formatDateTime + "' ");
				if(CollUtil.isNotEmpty(cfgSettingEntityFreshList)) {
					this.initDorisQueryCfgSetting();
				}
			}, 1, freshCacheTime, TimeUnit.SECONDS);
		}
	}
}
