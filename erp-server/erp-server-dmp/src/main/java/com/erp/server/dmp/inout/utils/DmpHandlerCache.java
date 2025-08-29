package com.erp.server.dmp.inout.utils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.DorisQuerySettingDTO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.dto.DmpCfgInputConvertValueDTO;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.entity.DmpCfgDbEntity;
import com.erp.model.dmp.entity.DmpCfgEtlEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertMappingEntity;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgMqEntity;
import com.erp.model.dmp.entity.DmpCfgOutputBlackEntity;
import com.erp.model.dmp.entity.DmpCfgOutputDataEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.enums.DmpCfgMqMqTypeEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.DmpBasicSystemService;
import com.erp.server.dmp.service.DmpCfgApiService;
import com.erp.server.dmp.service.DmpCfgDbService;
import com.erp.server.dmp.service.DmpCfgEtlService;
import com.erp.server.dmp.service.DmpCfgInputConvertMappingService;
import com.erp.server.dmp.service.DmpCfgInputConvertService;
import com.erp.server.dmp.service.DmpCfgInputConvertValueService;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.erp.server.dmp.service.DmpCfgInputService;
import com.erp.server.dmp.service.DmpCfgMqService;
import com.erp.server.dmp.service.DmpCfgOutputBlackService;
import com.erp.server.dmp.service.DmpCfgOutputDataService;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.netflix.client.ClientException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DmpHandlerCache implements CommandLineRunner{
	
	@Value("${dmp.input.fresh.cache.time:5}")
    private int freshCacheTime;
	
	@Value("${dmp.input.fresh.cache.switch:true}")
    private boolean freshCacheSwitch;
	
	private String namespace = SpringUtil.getProperty("spring.cloud.nacos.discovery.namespace");
	
	private List<DmpBasicSystemEntity> dmpBasicSystemCache;
	
	private List<DmpCfgInputEntity> dmpCfgInputCache;
	
	private List<DmpCfgEtlEntity> dmpCfgEtlCache;
	
	private List<DmpCfgInputDetailEntity> dmpCfgInputDetailCache;
	
	private List<DmpCfgInputConvertEntity> dmpCfgInputConvertCache;
	
	private List<DmpCfgInputConvertMappingEntity> dmpCfgInputConvertMappingCache;
	private Map<String, Map<String, List<String>>> convertMappingCache;

	private List<DmpCfgInputConvertValueDTO.MappingAndValueDTO> dmpCfgInputConvertValueCache;
	private Map<String, List<DmpCfgInputConvertValueDTO.MappingAndValueDTO>> convertValueCache;

	private Map<String , RocketMQTemplate> rocketMQTemplateMap;
	
	private Map<String , DmpCfgMqEntity> rocketMQDmpCfgMqCache;
	
	private List<DmpCfgOutputBlackEntity> dmpCfgOutputBlackCache;
	
	private List<DmpCfgOutputDataEntity> dmpCfgOutputDataCache;
	
	private List<OverseasProviderEntity> overseasProviderEntityCache;
	
	private List<DmpCfgApiEntity> dmpCfgApiEntityCache;
	
	private List<DmpCfgOutputEntity> dmpCfgOutputEntityCache;
	
	private Map<String, DataSource> dmpCfgDbDataSourceMap;
	
	private List<Map<String, Object>> dorisQueryCfgSettingEntityCache;
	
	private Map<String , DorisQuerySettingDTO> dorisQueryCfgSettingMappingCache;

	@Autowired
	private DmpBasicSystemService dmpBasicSystemService;
	@Autowired
	private DmpCfgInputService dmpCfgInputService;
	@Autowired
	private DmpCfgEtlService dmpCfgEtlService;
	@Autowired
	private DmpCfgInputConvertService dmpCfgInputConvertService;
	@Autowired
	private DmpCfgInputDetailService dmpCfgInputDetailService;
	@Autowired
	private DmpCfgInputConvertMappingService dmpCfgInputConvertMappingService;
	@Autowired
	private DmpCfgInputConvertValueService dmpCfgInputConvertValueService;
	@Autowired
	private DmpCfgMqService dmpCfgMqService;
	@Autowired
	private DmpCfgOutputBlackService dmpCfgOutputBlackService;
	@Autowired
	private DmpCfgOutputDataService dmpCfgOutputDataService;
	@Autowired
	private DmpCfgApiService dmpCfgApiService;
	@Autowired
	private DmpCfgOutputService dmpCfgOutputService;
	@Autowired
	private DmpCfgDbService dmpCfgDbService;
	@Autowired
	private CfgSettingService cfgSettingService;
	
	public List<DmpBasicSystemEntity> getDmpBasicSystemEntityList(Predicate<? super DmpBasicSystemEntity> paramPredicate) {
		if(dmpBasicSystemCache == null) {
			dmpBasicSystemCache = dmpBasicSystemService.lambdaQuery()
					.eq(DmpBasicSystemEntity::getDisabled, false).list();
		}
		return dmpBasicSystemCache.stream().filter(paramPredicate).collect(Collectors.toList());
	}
	
	public List<DmpCfgInputEntity> getDmpCfgInputEntityList(Predicate<? super DmpCfgInputEntity> paramPredicate) {
		if(dmpCfgInputCache == null) {
			dmpCfgInputCache = dmpCfgInputService.lambdaQuery()
					.eq(DmpCfgInputEntity::getDisabled, false).list();
		}
		return dmpCfgInputCache.stream().filter(paramPredicate).collect(Collectors.toList());
	}
	
	public List<DmpCfgEtlEntity> getDmpCfgEtlEntityList(Predicate<? super DmpCfgEtlEntity> paramPredicate) {
		if(dmpCfgEtlCache == null) {
			dmpCfgEtlCache = dmpCfgEtlService.lambdaQuery()
					.eq(DmpCfgEtlEntity::getDisabled, false).list();
		}
		return dmpCfgEtlCache.stream().filter(paramPredicate).collect(Collectors.toList());
	}
	
	public List<DmpCfgInputConvertEntity> getDmpCfgInputConvertEntityList(Predicate<? super DmpCfgInputConvertEntity> paramPredicate) {
		if(dmpCfgInputConvertCache == null) {
			dmpCfgInputConvertCache = dmpCfgInputConvertService.lambdaQuery()
					.eq(DmpCfgInputConvertEntity::getDisabled, false).list();
		}
		return dmpCfgInputConvertCache.stream().filter(paramPredicate).collect(Collectors.toList());
	}
	
	
	public List<DmpCfgInputDetailEntity> getDmpCfgInputDetailEntityList(Predicate<? super DmpCfgInputDetailEntity> paramPredicate) {
		if(dmpCfgInputDetailCache == null) {
			dmpCfgInputDetailCache = dmpCfgInputDetailService.lambdaQuery()
					.eq(DmpCfgInputDetailEntity::getDisabled, false).list();
		}
		return dmpCfgInputDetailCache.stream().filter(paramPredicate).collect(Collectors.toList());
	}
	
	public Map<String, List<String>> getDmpCfgInputConvertMapping(String mainId){
		if(convertMappingCache == null) {
			this.dealConvertMappingCache();
		}
		return convertMappingCache.get(mainId);
	}
	
	public List<DmpCfgApiEntity> getDmpCfgApiEntityList(Predicate<? super DmpCfgApiEntity> paramPredicate) {
		if(dmpCfgApiEntityCache == null) {
			dmpCfgApiEntityCache = dmpCfgApiService.lambdaQuery()
					.eq(DmpCfgApiEntity::getDisabled, false).list();
		}
		return dmpCfgApiEntityCache.stream().filter(paramPredicate).collect(Collectors.toList());
	}

	/**
	 * 获取映射字段配置的映射值
	 * @Author Luo_WG
	 * @Date 2024/8/8 16:30
	 * @param convertId
	 * @return java.util.Map<java.lang.String,java.util.List<java.lang.String>>
	 **/
	public List<DmpCfgInputConvertValueDTO.MappingAndValueDTO> getDmpCfgInputConvertValue(String convertId){
		if(convertValueCache == null) {
			this.dealConvertValueCache();
		}

		return convertValueCache.get(convertId);
	}

	public DmpCfgMqEntity getRocketMQDmpCfgMqCache(String mqId){
		if(rocketMQDmpCfgMqCache == null) {
			this.initRocketMQTemplate();
		}
		return rocketMQDmpCfgMqCache.get(mqId);
	}
	
	public RocketMQTemplate getRocketMQTemplate(String mqId){
		if(rocketMQTemplateMap == null) {
			this.initRocketMQTemplate();
		}
		return rocketMQTemplateMap.get(mqId);
	}
	
	public List<DmpCfgOutputBlackEntity> getDmpCfgOutputBlackEntityList(Predicate<? super DmpCfgOutputBlackEntity> paramPredicate) {
		if(dmpCfgOutputBlackCache == null) {
			dmpCfgOutputBlackCache = dmpCfgOutputBlackService.lambdaQuery()
					.eq(DmpCfgOutputBlackEntity::getDisabled, false).list();
		}
		return dmpCfgOutputBlackCache.stream().filter(paramPredicate).collect(Collectors.toList());
	}
	
	public List<DmpCfgOutputDataEntity> getDmpCfgOutputDataEntityList(Predicate<? super DmpCfgOutputDataEntity> paramPredicate) {
		if(dmpCfgOutputDataCache == null) {
			dmpCfgOutputDataCache = dmpCfgOutputDataService.lambdaQuery()
					.eq(DmpCfgOutputDataEntity::getDisabled, false).list();
		}
		return dmpCfgOutputDataCache.stream().filter(paramPredicate).collect(Collectors.toList());
	}
	
	public List<DmpCfgOutputEntity> getDmpCfgOutputEntityList(Predicate<? super DmpCfgOutputEntity> paramPredicate) {
		if(dmpCfgOutputEntityCache == null) {
			dmpCfgOutputEntityCache = dmpCfgOutputService.lambdaQuery()
					.eq(DmpCfgOutputEntity::getDisabled, false).list();
		}
		return dmpCfgOutputEntityCache.stream().filter(paramPredicate).collect(Collectors.toList());
	}
	
	public DataSource getDataSource(String dbId) {
		if(dmpCfgDbDataSourceMap == null) {
			this.initDataSource();
		}
		return dmpCfgDbDataSourceMap.get(dbId);
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
	
	public List<OverseasProviderEntity> getOverseasProviderEntityList(Predicate<? super OverseasProviderEntity> paramPredicate) {
		int i = 0;
		while(overseasProviderEntityCache == null) {
			try {
				overseasProviderEntityCache = FeignQuery.create(OverseasProviderEntity.class)
						.eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
						.list();
			} catch (Exception e) {
				Throwable cause = e.getCause();
				if(cause instanceof ClientException && i < 3) {
					try {Thread.sleep(10000);} catch (InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				}else {
					throw e;
				}
			}
			i = i + 1;
		}
		return overseasProviderEntityCache.stream().filter(paramPredicate).collect(Collectors.toList());
	}

	private synchronized void dealRocketMQTemplate(List<DmpCfgMqEntity> updateRocketMQDmpCfgMqEntity) {
		if(CollUtil.isNotEmpty(updateRocketMQDmpCfgMqEntity)) {
			List<DmpCfgMqEntity> disabledList = updateRocketMQDmpCfgMqEntity.stream().filter(d -> Boolean.TRUE.equals(d.getDisabled())).collect(Collectors.toList());
			if(CollUtil.isNotEmpty(disabledList)) {
				for(DmpCfgMqEntity dmpCfgMqEntity : disabledList) {
					this.removeRocketMQTemplate(dmpCfgMqEntity.getId());
				}
			}
			List<DmpCfgMqEntity> abledList = updateRocketMQDmpCfgMqEntity.stream().filter(d -> Boolean.FALSE.equals(d.getDisabled())).collect(Collectors.toList());
			if(CollUtil.isNotEmpty(abledList)) {
				for(DmpCfgMqEntity dmpCfgMqEntity : abledList) {
					String id = dmpCfgMqEntity.getId();
					this.removeRocketMQTemplate(id);
					
					DefaultMQProducer producer = new DefaultMQProducer(dmpCfgMqEntity.getMqGroup());
					producer.setNamesrvAddr(dmpCfgMqEntity.getHost() + ":" + dmpCfgMqEntity.getPort());
					try {
						producer.start();
					} catch (MQClientException e) {
						log.error("创建RocketMQTemplate失败，id={}" , id , e);
					}
					RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
			        rocketMQTemplate.setProducer(producer);
			        
			        rocketMQTemplateMap.put(id, rocketMQTemplate);
			        dmpCfgMqEntity.setTopic(namespace + "-" + dmpCfgMqEntity.getTopic());
			        dmpCfgMqEntity.setTag(namespace + "-" + dmpCfgMqEntity.getTag());
			        rocketMQDmpCfgMqCache.put(id, dmpCfgMqEntity);
				}
			}
		}
	}
	
	private synchronized void dealDataSource(List<DmpCfgDbEntity> updatePgDmpCfgDbEntityList) {
		if(CollUtil.isNotEmpty(updatePgDmpCfgDbEntityList)) {
			List<DmpCfgDbEntity> disabledList = updatePgDmpCfgDbEntityList.stream().filter(d -> Boolean.TRUE.equals(d.getDisabled())).collect(Collectors.toList());
			if(CollUtil.isNotEmpty(disabledList)) {
				for(DmpCfgDbEntity dmpCfgDbEntity : disabledList) {
					dmpCfgDbDataSourceMap.remove(dmpCfgDbEntity.getId());
				}
			}
			
			List<DmpCfgDbEntity> abledList = updatePgDmpCfgDbEntityList.stream().filter(d -> Boolean.FALSE.equals(d.getDisabled())).collect(Collectors.toList());
			for(DmpCfgDbEntity dmpCfgDbEntity : abledList) {
				String dbId = dmpCfgDbEntity.getId();
				try {
					HikariConfig config = new HikariConfig();
					config.setJdbcUrl("jdbc:" + dmpCfgDbEntity.getDbType() + "://" + dmpCfgDbEntity.getHost() + ":" + dmpCfgDbEntity.getPort() + "/" + dmpCfgDbEntity.getDbName() + "?autoReconnect=true&useSSL=false&serverTimezone=GMT%2B8&stringtype=unspecified");
					config.setUsername(dmpCfgDbEntity.getUserName());
					config.setPassword(dmpCfgDbEntity.getPassWord());

					// 设置其他连接池参数，例如最大连接数、最小空闲连接数等
					config.setMinimumIdle(dmpCfgDbEntity.getMinConnectionSize());
					config.setMaximumPoolSize(dmpCfgDbEntity.getMaxConnectionSize());
					dmpCfgDbDataSourceMap.put(dbId, new HikariDataSource(config));
				} catch (Exception e) {
					log.error("创建dmpCfgDbEntity数据库连接失败，id={}" , dbId , e);
				}
			}
		}
	}
	
	@Override
	public void run(String... args) throws Exception {
		this.initCache(freshCacheSwitch);
	}

	public void initCache(boolean isCreateTask) {
		dmpBasicSystemCache = dmpBasicSystemService.lambdaQuery()
				.eq(DmpBasicSystemEntity::getDisabled, false).list();
		dmpCfgInputCache = dmpCfgInputService.lambdaQuery()
				.eq(DmpCfgInputEntity::getDisabled, false).list();
		dmpCfgEtlCache = dmpCfgEtlService.lambdaQuery()
				.eq(DmpCfgEtlEntity::getDisabled, false).list();
		dmpCfgInputConvertCache = dmpCfgInputConvertService.lambdaQuery()
				.eq(DmpCfgInputConvertEntity::getDisabled, false).list();
		dmpCfgInputDetailCache = dmpCfgInputDetailService.lambdaQuery()
				.eq(DmpCfgInputDetailEntity::getDisabled, false).list();
		dmpCfgInputConvertMappingCache = dmpCfgInputConvertMappingService.lambdaQuery()
				.eq(DmpCfgInputConvertMappingEntity::getDisabled, false)
				.list();
		dmpCfgInputConvertValueCache = dmpCfgInputConvertValueService.listMappingAndValue();
		this.dealConvertMappingCache();

		this.dealConvertValueCache();

		this.initRocketMQTemplate();
		
		this.initDataSource();
		
		dmpCfgOutputBlackCache = dmpCfgOutputBlackService.lambdaQuery()
				.eq(DmpCfgOutputBlackEntity::getDisabled, false).list();
		
		dmpCfgApiEntityCache = dmpCfgApiService.lambdaQuery()
				.eq(DmpCfgApiEntity::getDisabled, false).list();
		
		dmpCfgOutputEntityCache = dmpCfgOutputService.lambdaQuery()
				.eq(DmpCfgOutputEntity::getDisabled, false).list();
		
		this.initDorisQueryCfgSetting();
		
		
		try {
			overseasProviderEntityCache = FeignQuery.create(OverseasProviderEntity.class)
													.eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
													.list();
		} catch (Exception e) {
		}

		if(isCreateTask) {
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				List<DmpBasicSystemEntity> dmpBasicSystemEntityFreshList = dmpBasicSystemService.lambdaQuery()
						.gt(DmpBasicSystemEntity::getUpdateTime, DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)))
						.list();
				if(CollUtil.isNotEmpty(dmpBasicSystemEntityFreshList)) {
					List<String> newIds = dmpBasicSystemEntityFreshList.stream().map(DmpBasicSystemEntity::getId).collect(Collectors.toList());
					dmpBasicSystemCache.removeIf(d -> newIds.contains(d.getId()));
					dmpBasicSystemCache.addAll(dmpBasicSystemEntityFreshList.stream()
							.filter(d -> Boolean.FALSE.equals(d.getDisabled())).collect(Collectors.toList()));
				}
				
			}, 0, freshCacheTime, TimeUnit.SECONDS);
			
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				List<DmpCfgInputEntity> dmpCfgInputEntityFreshList = dmpCfgInputService.lambdaQuery()
						.gt(DmpCfgInputEntity::getUpdateTime, DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)))
						.list();
				if(CollUtil.isNotEmpty(dmpCfgInputEntityFreshList)) {
					List<String> newIds = dmpCfgInputEntityFreshList.stream().map(DmpCfgInputEntity::getId).collect(Collectors.toList());
					dmpCfgInputCache.removeIf(d -> newIds.contains(d.getId()));
					dmpCfgInputCache.addAll(dmpCfgInputEntityFreshList.stream()
							.filter(d -> Boolean.FALSE.equals(d.getDisabled())).collect(Collectors.toList()));
				}
				
			}, 1, freshCacheTime, TimeUnit.SECONDS);
			
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				List<DmpCfgEtlEntity> dmpCfgEtlEntityFreshList = dmpCfgEtlService.lambdaQuery()
						.gt(DmpCfgEtlEntity::getUpdateTime, DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)))
						.list();
				if(CollUtil.isNotEmpty(dmpCfgEtlEntityFreshList)) {
					List<String> newIds = dmpCfgEtlEntityFreshList.stream().map(DmpCfgEtlEntity::getId).collect(Collectors.toList());
					dmpCfgEtlCache.removeIf(d -> newIds.contains(d.getId()));
					dmpCfgEtlCache.addAll(dmpCfgEtlEntityFreshList.stream()
							.filter(d -> Boolean.FALSE.equals(d.getDisabled())).collect(Collectors.toList()));
				}
				
			}, 1, freshCacheTime, TimeUnit.SECONDS);
			
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				List<DmpCfgInputConvertEntity> dmpCfgInputConvertEntityFreshList = dmpCfgInputConvertService.lambdaQuery()
						.gt(DmpCfgInputConvertEntity::getUpdateTime, DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)))
						.list();
				if(CollUtil.isNotEmpty(dmpCfgInputConvertEntityFreshList)) {
					List<String> newIds = dmpCfgInputConvertEntityFreshList.stream().map(DmpCfgInputConvertEntity::getId).collect(Collectors.toList());
					dmpCfgInputConvertCache.removeIf(d -> newIds.contains(d.getId()));
					dmpCfgInputConvertCache.addAll(dmpCfgInputConvertEntityFreshList.stream()
							.filter(d -> Boolean.FALSE.equals(d.getDisabled())).collect(Collectors.toList()));
				}
				
			}, 2, freshCacheTime, TimeUnit.SECONDS);
			
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				List<DmpCfgInputDetailEntity> dmpCfgInputDetailEntityFreshList = dmpCfgInputDetailService.lambdaQuery()
						.gt(DmpCfgInputDetailEntity::getUpdateTime, DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)))
						.list();
				if(CollUtil.isNotEmpty(dmpCfgInputDetailEntityFreshList)) {
					List<String> newIds = dmpCfgInputDetailEntityFreshList.stream().map(DmpCfgInputDetailEntity::getId).collect(Collectors.toList());
					dmpCfgInputDetailCache.removeIf(d -> newIds.contains(d.getId()));
					dmpCfgInputDetailCache.addAll(dmpCfgInputDetailEntityFreshList.stream()
							.filter(d -> Boolean.FALSE.equals(d.getDisabled())).collect(Collectors.toList()));
				}
				
			}, 3, freshCacheTime, TimeUnit.SECONDS);
			
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				List<DmpCfgInputConvertMappingEntity> dmpCfgInputConvertMappingEntityFreshList = dmpCfgInputConvertMappingService.lambdaQuery()
						.gt(DmpCfgInputConvertMappingEntity::getUpdateTime, DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)))
						.list();
				if(CollUtil.isNotEmpty(dmpCfgInputConvertMappingEntityFreshList)) {
					List<String> newIds = dmpCfgInputConvertMappingEntityFreshList.stream().map(DmpCfgInputConvertMappingEntity::getId).collect(Collectors.toList());
					dmpCfgInputConvertMappingCache.removeIf(d -> newIds.contains(d.getId()));
					dmpCfgInputConvertMappingCache.addAll(dmpCfgInputConvertMappingEntityFreshList.stream()
							.filter(d -> Boolean.FALSE.equals(d.getDisabled())).collect(Collectors.toList()));
					this.dealConvertMappingCache();
				}
				
			}, 4, freshCacheTime, TimeUnit.SECONDS);
			
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				this.dealRocketMQTemplate(dmpCfgMqService.lambdaQuery()
						.eq(DmpCfgMqEntity::getMqType, DmpCfgMqMqTypeEnum.ROCKETMQ.getCode())
						.gt(DmpCfgMqEntity::getUpdateTime, DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)))
						.list());
			}, 5, freshCacheTime, TimeUnit.SECONDS);

			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				List<DmpCfgInputConvertValueDTO.MappingAndValueDTO> dmpCfgInputConvertMappingEntityFreshList = dmpCfgInputConvertValueService.listMappingAndValueByFreshCacheTime(DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)));
				if(CollUtil.isNotEmpty(dmpCfgInputConvertMappingEntityFreshList)) {
					List<String> newIds = dmpCfgInputConvertMappingEntityFreshList.stream().map(DmpCfgInputConvertValueDTO.MappingAndValueDTO::getId).collect(Collectors.toList());
					dmpCfgInputConvertValueCache.removeIf(d -> newIds.contains(d.getId()));
					dmpCfgInputConvertValueCache.addAll(dmpCfgInputConvertMappingEntityFreshList);
					this.dealConvertValueCache();
				}

			}, 6, freshCacheTime, TimeUnit.SECONDS);

			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				List<DmpCfgOutputBlackEntity> dmpCfgOutputBlackEntityFreshList = dmpCfgOutputBlackService.lambdaQuery()
						.gt(DmpCfgOutputBlackEntity::getUpdateTime, DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)))
						.list();
				if(CollUtil.isNotEmpty(dmpCfgOutputBlackEntityFreshList)) {
					List<String> newIds = dmpCfgOutputBlackEntityFreshList.stream().map(DmpCfgOutputBlackEntity::getId).collect(Collectors.toList());
					dmpCfgOutputBlackCache.removeIf(d -> newIds.contains(d.getId()));
					dmpCfgOutputBlackCache.addAll(dmpCfgOutputBlackEntityFreshList.stream()
							.filter(d -> Boolean.FALSE.equals(d.getDisabled())).collect(Collectors.toList()));
				}
				
			}, 0, freshCacheTime, TimeUnit.SECONDS);

			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				List<OverseasProviderEntity> overseasProviderEntityFreshList = null;
				try {
					overseasProviderEntityFreshList = FeignQuery.create(OverseasProviderEntity.class)
							.gt(DmpCfgOutputBlackEntity::getUpdateTime, DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)))
							.list();
				} catch (Exception e) {
				}
				if(CollUtil.isNotEmpty(overseasProviderEntityFreshList)) {
					List<String> newIds = overseasProviderEntityFreshList.stream().map(OverseasProviderEntity::getId).collect(Collectors.toList());
					overseasProviderEntityCache.removeIf(d -> newIds.contains(d.getId()));
					overseasProviderEntityCache.addAll(overseasProviderEntityFreshList.stream()
							.filter(d -> AuthStatusEnum.ALREADY.getCode().equals(d.getAuthStatus())).collect(Collectors.toList()));
				}

			}, 1, freshCacheTime, TimeUnit.SECONDS);
			
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				List<DmpCfgApiEntity> dmpCfgApiEntityFreshList = dmpCfgApiService.lambdaQuery()
						.gt(DmpCfgApiEntity::getUpdateTime, DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)))
						.list();
				if(CollUtil.isNotEmpty(dmpCfgApiEntityFreshList)) {
					List<String> newIds = dmpCfgApiEntityFreshList.stream().map(DmpCfgApiEntity::getId).collect(Collectors.toList());
					dmpCfgApiEntityCache.removeIf(d -> newIds.contains(d.getId()));
					dmpCfgApiEntityCache.addAll(dmpCfgApiEntityFreshList.stream()
							.filter(d -> Boolean.FALSE.equals(d.getDisabled())).collect(Collectors.toList()));
				}
				
			}, 2, freshCacheTime, TimeUnit.SECONDS);
			
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				List<DmpCfgOutputEntity> dmpCfgOutputEntityFreshList = dmpCfgOutputService.lambdaQuery()
						.gt(DmpCfgOutputEntity::getUpdateTime, DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)))
						.list();
				if(CollUtil.isNotEmpty(dmpCfgOutputEntityFreshList)) {
					List<String> newIds = dmpCfgOutputEntityFreshList.stream().map(DmpCfgOutputEntity::getId).collect(Collectors.toList());
					dmpCfgOutputEntityCache.removeIf(d -> newIds.contains(d.getId()));
					dmpCfgOutputEntityCache.addAll(dmpCfgOutputEntityFreshList.stream()
							.filter(d -> Boolean.FALSE.equals(d.getDisabled())).collect(Collectors.toList()));
				}
				
			}, 3, freshCacheTime, TimeUnit.SECONDS);

			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				this.dealDataSource(dmpCfgDbService.lambdaQuery()
						.gt(DmpCfgDbEntity::getUpdateTime, DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)))
						.list());
			}, 4, freshCacheTime, TimeUnit.SECONDS);
			
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				List<DmpCfgOutputDataEntity> dmpCfgOutputDataEntityFreshList = dmpCfgOutputDataService.lambdaQuery()
						.gt(DmpCfgOutputDataEntity::getUpdateTime, DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)))
						.list();
				if(CollUtil.isNotEmpty(dmpCfgOutputDataEntityFreshList)) {
					List<String> newIds = dmpCfgOutputDataEntityFreshList.stream().map(DmpCfgOutputDataEntity::getId).collect(Collectors.toList());
					dmpCfgOutputDataCache.removeIf(d -> newIds.contains(d.getId()));
					dmpCfgOutputDataCache.addAll(dmpCfgOutputDataEntityFreshList.stream()
							.filter(d -> Boolean.FALSE.equals(d.getDisabled())).collect(Collectors.toList()));
				}
				
			}, 5, freshCacheTime, TimeUnit.SECONDS);
			
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				LambdaQueryWrapper<CfgSettingEntity> updateQueryWrapper = new LambdaQueryWrapper<>();
				updateQueryWrapper.eq(CfgSettingEntity::getType, SettingEnum.DORIS_QUERY_CFG);
				updateQueryWrapper.gt(CfgSettingEntity::getUpdateTime, DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)));
				List<Map<String , Object>> cfgSettingEntityFreshList = cfgSettingService.listMaps(updateQueryWrapper);
				if(CollUtil.isNotEmpty(cfgSettingEntityFreshList)) {
					this.initDorisQueryCfgSetting();
				}
			}, 1, freshCacheTime, TimeUnit.SECONDS);
		}
	}
	
	private void removeRocketMQTemplate(String mqId) {
		rocketMQDmpCfgMqCache.remove(mqId);
		RocketMQTemplate rocketMQTemplate = rocketMQTemplateMap.get(mqId);
		if(rocketMQTemplate != null) {
			DefaultMQProducer producer = rocketMQTemplate.getProducer();
			if(producer != null) {
				producer.shutdown();
			}
			rocketMQTemplateMap.remove(mqId);
		}
	}
	
	private void dealConvertMappingCache() {
		convertMappingCache = new HashMap<>();
		if(dmpCfgInputConvertMappingCache == null) {
			dmpCfgInputConvertMappingCache = dmpCfgInputConvertMappingService.lambdaQuery()
					.eq(DmpCfgInputConvertMappingEntity::getDisabled, false)
					.list();
		}
		Map<String, List<DmpCfgInputConvertMappingEntity>> mainEntityMap = dmpCfgInputConvertMappingCache.stream().collect(Collectors.groupingBy(DmpCfgInputConvertMappingEntity::getMainId));

		for(Map.Entry<String, List<DmpCfgInputConvertMappingEntity>> mainEntity : mainEntityMap.entrySet()) {
			Map<String, List<String>> map = new HashMap<>();
			Map<String, List<DmpCfgInputConvertMappingEntity>> mapping = mainEntity.getValue().stream().collect(Collectors.groupingBy(d -> d.getOriginalKey().replace(".", "")));
			for(Map.Entry<String, List<DmpCfgInputConvertMappingEntity>> m : mapping.entrySet()) {
				map.put(m.getKey(), m.getValue().stream().map(d -> StrUtils.underlineToCamel(d.getConvertKey(), true)).collect(Collectors.toList()));
			}
			convertMappingCache.put(mainEntity.getKey(), map);
		}
	}

	private void dealConvertValueCache() {
		convertValueCache = new HashMap<>();
		//映射值信息
		if(dmpCfgInputConvertValueCache == null) {
			dmpCfgInputConvertValueCache = dmpCfgInputConvertValueService.listMappingAndValue();
		}

		Map<String, List<DmpCfgInputConvertValueDTO.MappingAndValueDTO>> mainEntityMap = dmpCfgInputConvertValueCache.stream().collect(Collectors.groupingBy(DmpCfgInputConvertValueDTO.MappingAndValueDTO::getConvertId));

		for(Map.Entry<String, List<DmpCfgInputConvertValueDTO.MappingAndValueDTO>> mainEntity : mainEntityMap.entrySet()) {
			convertValueCache.put(mainEntity.getKey(), mainEntity.getValue());
		}

	}

	private void initRocketMQTemplate() {
		if(rocketMQTemplateMap == null) {
			rocketMQTemplateMap = new HashMap<>();
		}
		if(rocketMQDmpCfgMqCache == null) {
			rocketMQDmpCfgMqCache = new HashMap<>();
		}
		this.dealRocketMQTemplate(dmpCfgMqService.lambdaQuery()
					.eq(DmpCfgMqEntity::getMqType, DmpCfgMqMqTypeEnum.ROCKETMQ.getCode())
					.eq(DmpCfgMqEntity::getDisabled, false).list());
	}
	
	private void initDataSource() {
		if(dmpCfgDbDataSourceMap == null) {
			dmpCfgDbDataSourceMap = new HashMap<>();
		}
		this.dealDataSource(dmpCfgDbService.lambdaQuery()
				.eq(DmpCfgDbEntity::getDisabled, false).list());
	}
	
	private synchronized void initDorisQueryCfgSetting() {
		LambdaQueryWrapper<CfgSettingEntity> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(CfgSettingEntity::getType, SettingEnum.DORIS_QUERY_CFG);
		queryWrapper.eq(CfgSettingEntity::getStatus, true);
		dorisQueryCfgSettingEntityCache = cfgSettingService.listMaps(queryWrapper);
		dorisQueryCfgSettingMappingCache = dorisQueryCfgSettingEntityCache.stream().collect(Collectors.toMap(c -> {
			String key = c.get("key").toString();
			if(!key.startsWith("/")) {
				key = "/" + key;
			}
			return key;
		}, c -> {
			DorisQuerySettingDTO d = new DorisQuerySettingDTO();
			String value = c.get("value").toString();
			if(StringUtils.isNotBlank(value)) {
				try {
					d = JSON.parseObject(value, DorisQuerySettingDTO.class);
				} catch (Exception e) {
					log.error("转换doris配置查询错误" , e);
				}
			}
			return d;
		} , (c1 , c2) -> c1));
	}


	/**
	 * 查询国家信息
	 * @param country 国家二字码/国家中文名/国家英文名
	 */
	public List<DictCountryEntity> getDictCountry(String country) {
		if (StringUtils.isEmpty(country)) {
			return Collections.emptyList();
		}
		List<DictCountryEntity> list = FeignQuery.list(
				FeignQuery.create(DictCountryEntity.class)
						.eq(DictCountryEntity::getId, country)
						.last("or name_cn = '" + country + "' or name_en = '" + country + "'")
		);
		return list;
	}
	
	public List<Map<String, Object>> getDorisQueryCfgSettingEntityCache(){
		return dorisQueryCfgSettingEntityCache;
	}
}
