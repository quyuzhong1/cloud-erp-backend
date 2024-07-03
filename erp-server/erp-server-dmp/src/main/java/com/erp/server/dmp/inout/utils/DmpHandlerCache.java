package com.erp.server.dmp.inout.utils;

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

import com.common.core.utils.StrUtils;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertMappingEntity;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;
import com.erp.model.dmp.entity.DmpCfgMqEntity;
import com.erp.model.dmp.entity.DmpCfgOutputBlackEntity;
import com.erp.model.dmp.enums.DmpCfgMqMqTypeEnum;
import com.erp.server.dmp.service.DmpBasicSystemService;
import com.erp.server.dmp.service.DmpCfgInputConvertMappingService;
import com.erp.server.dmp.service.DmpCfgInputConvertService;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.erp.server.dmp.service.DmpCfgInputService;
import com.erp.server.dmp.service.DmpCfgMqService;
import com.erp.server.dmp.service.DmpCfgOutputBlackService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DmpHandlerCache implements CommandLineRunner{
	
	@Value("${dmp.input.fresh.cache.time:5}")
    private int freshCacheTime;
	
	private String namespace = SpringUtil.getProperty("spring.cloud.nacos.discovery.namespace");
	
	private volatile List<DmpBasicSystemEntity> dmpBasicSystemCache;
	
	private volatile List<DmpCfgInputEntity> dmpCfgInputCache;
	
	private volatile List<DmpCfgInputDetailEntity> dmpCfgInputDetailCache;
	
	private volatile List<DmpCfgInputConvertEntity> dmpCfgInputConvertCache;
	
	private volatile List<DmpCfgInputConvertMappingEntity> dmpCfgInputConvertMappingCache;
	private volatile Map<String, Map<String, List<String>>> convertMappingCache;
	
	private volatile Map<String , RocketMQTemplate> rocketMQTemplateMap;
	
	private volatile Map<String , DmpCfgMqEntity> rocketMQDmpCfgMqCache;
	
	private volatile List<DmpCfgOutputBlackEntity> dmpCfgOutputBlackCache;
	
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
	private DmpCfgMqService dmpCfgMqService;
	@Autowired
	private DmpCfgOutputBlackService dmpCfgOutputBlackService;
	
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
		dmpBasicSystemCache = dmpBasicSystemService.lambdaQuery()
				.eq(DmpBasicSystemEntity::getDisabled, false).list();
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
		
		dmpCfgInputCache = dmpCfgInputService.lambdaQuery()
				.eq(DmpCfgInputEntity::getDisabled, false).list();
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
		
		dmpCfgInputConvertCache = dmpCfgInputConvertService.lambdaQuery()
				.eq(DmpCfgInputConvertEntity::getDisabled, false).list();
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
		
		
		dmpCfgInputDetailCache = dmpCfgInputDetailService.lambdaQuery()
				.eq(DmpCfgInputDetailEntity::getDisabled, false).list();
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
		
		dmpCfgInputConvertMappingCache = dmpCfgInputConvertMappingService.lambdaQuery()
				.eq(DmpCfgInputConvertMappingEntity::getDisabled, false)
				.list();
		this.dealConvertMappingCache();
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
		
		this.initRocketMQTemplate();
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
			this.dealRocketMQTemplate(dmpCfgMqService.lambdaQuery()
					.eq(DmpCfgMqEntity::getMqType, DmpCfgMqMqTypeEnum.ROCKETMQ.getCode())
					.gt(DmpCfgMqEntity::getUpdateTime, DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)))
					.list());
		}, 5, freshCacheTime, TimeUnit.SECONDS);
		
		dmpCfgOutputBlackCache = dmpCfgOutputBlackService.lambdaQuery()
				.eq(DmpCfgOutputBlackEntity::getDisabled, false).list();
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
}
