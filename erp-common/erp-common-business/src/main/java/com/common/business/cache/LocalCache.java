package com.common.business.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.common.business.constant.BusinessCommonConstants;
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
	
	private static final String BASE_EXTEND_QUERY_SQL_DORIS = " type = 'doris_query_cfg' ";
	
	private static final String BASE_EXTEND_QUERY_SQL_ARCHIVE = " type = 'archive_white_table' ";
	
	private Map<String , DorisQuerySettingDTO> dorisQueryCfgSettingMappingCache;
	
	private Set<String> archiveWhiteTableListCache;
	
	private synchronized void initDorisQueryCfgSetting() {
		if(BusinessCommonConstants.isDynamicEnabled()) {
			try {
				List<Map<String , Object>> dorisQueryCfgSettingEntityList = baseDataMapper.queryDbBySql(TABLE_NAME, BASE_EXTEND_QUERY_SQL_DORIS);
				dorisQueryCfgSettingMappingCache = dorisQueryCfgSettingEntityList.stream().filter(c -> !Boolean.valueOf(c.get("is_deleted").toString())).collect(Collectors.toMap(c -> {
					Object valueObject = c.get("value");
					String value = "";
					if(valueObject != null) {
						value = valueObject.toString();
					}
					if(StringUtils.isBlank(value)) {
						value = c.get("code").toString();
					}
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
			} catch (Exception e) {
				log.error("转换doris配置查询错误" , e);
			}
		}else {
			dorisQueryCfgSettingMappingCache = new HashMap<>();
		}
	}
	
	private synchronized void initArchiveWhiteTableList() {
		if(BusinessCommonConstants.isArchive()) {
			try {
				List<Map<String , Object>> archiveWhiteTableListEntityList = baseDataMapper.queryDbBySql(TABLE_NAME, BASE_EXTEND_QUERY_SQL_ARCHIVE);
				archiveWhiteTableListCache = archiveWhiteTableListEntityList.stream().filter(c -> !Boolean.valueOf(c.get("is_deleted").toString()))
						.map(c -> c.get("name").toString()).collect(Collectors.toSet());
			} catch (Exception e) {
				log.error("归档白名单配置查询错误" , e);
			}
		}else {
			archiveWhiteTableListCache = new HashSet<>();
		}
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
	
	public Set<String> getArchiveWhiteTableList(){
		if(archiveWhiteTableListCache == null) {
			this.initArchiveWhiteTableList();
		}
		return archiveWhiteTableListCache;
	}
	
	@Override
	public void run(String... args) throws Exception {
		this.initCache(freshCacheSwitch);
	}

	public void initCache(boolean isCreateTask) {
		this.initDorisQueryCfgSetting();
		
		this.initArchiveWhiteTableList();
		
		if(isCreateTask) {
			if(BusinessCommonConstants.isDynamicEnabled()) {
				try {
					Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
						String formatDateTime = DateUtil.formatDateTime(DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)));
						List<Map<String , Object>> cfgSettingEntityFreshList = baseDataMapper.queryDbBySql(TABLE_NAME, BASE_EXTEND_QUERY_SQL_DORIS + " and update_time >= '" + formatDateTime + "' ");
						if(CollUtil.isNotEmpty(cfgSettingEntityFreshList)) {
							this.initDorisQueryCfgSetting();
						}
					}, 1, freshCacheTime, TimeUnit.SECONDS);
				} catch (Exception e) {
					log.error("开启转换doris配置查询任务错误" , e);
				}
			}
			
			if(BusinessCommonConstants.isArchive()) {
				try {
					Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
						String formatDateTime = DateUtil.formatDateTime(DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)));
						List<Map<String , Object>> cfgSettingEntityFreshList = baseDataMapper.queryDbBySql(TABLE_NAME, BASE_EXTEND_QUERY_SQL_ARCHIVE + " and update_time >= '" + formatDateTime + "' ");
						if(CollUtil.isNotEmpty(cfgSettingEntityFreshList)) {
							this.initArchiveWhiteTableList();
						}
					}, 2, freshCacheTime, TimeUnit.SECONDS);
				} catch (Exception e) {
					log.error("开启归档白名单配置查询任务错误" , e);
				}
			}
		}
	}
}
