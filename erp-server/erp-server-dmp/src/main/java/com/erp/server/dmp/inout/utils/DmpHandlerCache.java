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

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.alibaba.excel.util.StringUtils;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.StrUtils;
import com.erp.model.dmp.dto.DmpCfgInputConvertValueDTO;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertMappingEntity;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgMqEntity;
import com.erp.model.dmp.entity.DmpCfgOutputBlackEntity;
import com.erp.model.dmp.enums.DmpCfgMqMqTypeEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.dmp.service.DmpBasicSystemService;
import com.erp.server.dmp.service.DmpCfgApiService;
import com.erp.server.dmp.service.DmpCfgInputConvertMappingService;
import com.erp.server.dmp.service.DmpCfgInputConvertService;
import com.erp.server.dmp.service.DmpCfgInputConvertValueService;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.erp.server.dmp.service.DmpCfgInputService;
import com.erp.server.dmp.service.DmpCfgMqService;
import com.erp.server.dmp.service.DmpCfgOutputBlackService;
import com.netflix.client.ClientException;

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
	
	private volatile List<DmpBasicSystemEntity> dmpBasicSystemCache;
	
	private volatile List<DmpCfgInputEntity> dmpCfgInputCache;
	
	private volatile List<DmpCfgInputDetailEntity> dmpCfgInputDetailCache;
	
	private volatile List<DmpCfgInputConvertEntity> dmpCfgInputConvertCache;
	
	private volatile List<DmpCfgInputConvertMappingEntity> dmpCfgInputConvertMappingCache;
	private Map<String, Map<String, List<String>>> convertMappingCache;

	private volatile List<DmpCfgInputConvertValueDTO.MappingAndValueDTO> dmpCfgInputConvertValueCache;
	private volatile Map<String, List<DmpCfgInputConvertValueDTO.MappingAndValueDTO>> convertValueCache;

	private volatile Map<String , RocketMQTemplate> rocketMQTemplateMap;
	
	private volatile Map<String , DmpCfgMqEntity> rocketMQDmpCfgMqCache;
	
	private volatile List<DmpCfgOutputBlackEntity> dmpCfgOutputBlackCache;
	
	private volatile List<OverseasProviderEntity> overseasProviderEntityCache;
	
	private volatile List<DmpCfgApiEntity> dmpCfgApiEntityCache;

	@Autowired
	private DmpBasicSystemService dmpBasicSystemService;
	@Autowired
	private DmpCfgInputService dmpCfgInputService;
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
	private DmpCfgApiService dmpCfgApiService;
	
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

	private void dealRocketMQTemplate(List<DmpCfgMqEntity> updateRocketMQDmpCfgMqEntity) {
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
	
	@Override
	public void run(String... args) throws Exception {
		this.initCache(freshCacheSwitch);
	}

	public void initCache(boolean isCreateTask) {
		dmpBasicSystemCache = dmpBasicSystemService.lambdaQuery()
				.eq(DmpBasicSystemEntity::getDisabled, false).list();
		dmpCfgInputCache = dmpCfgInputService.lambdaQuery()
				.eq(DmpCfgInputEntity::getDisabled, false).list();
		dmpCfgInputConvertCache = dmpCfgInputConvertService.lambdaQuery()
				.eq(DmpCfgInputConvertEntity::getDisabled, false).list();
		dmpCfgInputDetailCache = dmpCfgInputDetailService.lambdaQuery()
				.eq(DmpCfgInputDetailEntity::getDisabled, false).list();
		dmpCfgInputConvertMappingCache = dmpCfgInputConvertMappingService.lambdaQuery()
				.eq(DmpCfgInputConvertMappingEntity::getDisabled, false)
				.list();
		dmpCfgInputConvertValueCache = dmpCfgInputConvertValueService.listMappingAndValue();
		this.dealConvertMappingCache();

		this.dealConvertValueCache();;

		this.initRocketMQTemplate();
		
		dmpCfgOutputBlackCache = dmpCfgOutputBlackService.lambdaQuery()
				.eq(DmpCfgOutputBlackEntity::getDisabled, false).list();
		
		dmpCfgApiEntityCache = dmpCfgApiService.lambdaQuery()
				.eq(DmpCfgApiEntity::getDisabled, false).list();
		
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
}
