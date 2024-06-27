package com.erp.server.dmp.inout.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
import com.erp.server.dmp.service.DmpBasicSystemService;
import com.erp.server.dmp.service.DmpCfgInputConvertMappingService;
import com.erp.server.dmp.service.DmpCfgInputConvertService;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.erp.server.dmp.service.DmpCfgInputService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;

@Component
public class DmpHandlerCache implements CommandLineRunner{
	
	@Value("${dmp.input.fresh.cache.time:5}")
    private int freshCacheTime;
	
	private volatile List<DmpBasicSystemEntity> dmpBasicSystemCache;
	
	private volatile List<DmpCfgInputEntity> dmpCfgInputCache;
	
	private volatile List<DmpCfgInputDetailEntity> dmpCfgInputDetailCache;
	
	private volatile List<DmpCfgInputConvertEntity> dmpCfgInputConvertCache;
	
	private volatile List<DmpCfgInputConvertMappingEntity> dmpCfgInputConvertMappingCache;
	private volatile Map<String, Map<String, List<String>>> convertMappingCache;
	
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
	
	public List<DmpBasicSystemEntity> getDmpBasicSystemEntityList(Predicate<? super DmpBasicSystemEntity> paramPredicate) {
		return dmpBasicSystemCache.stream().filter(paramPredicate).collect(Collectors.toList());
	}
	
	public List<DmpCfgInputConvertEntity> getDmpCfgInputConvertEntityList(Predicate<? super DmpCfgInputConvertEntity> paramPredicate) {
		return dmpCfgInputConvertCache.stream().filter(paramPredicate).collect(Collectors.toList());
	}
	
	public List<DmpCfgInputEntity> getDmpCfgInputEntityList(Predicate<? super DmpCfgInputEntity> paramPredicate) {
		return dmpCfgInputCache.stream().filter(paramPredicate).collect(Collectors.toList());
	}
	
	public List<DmpCfgInputDetailEntity> getDmpCfgInputDetailEntityList(Predicate<? super DmpCfgInputDetailEntity> paramPredicate) {
		return dmpCfgInputDetailCache.stream().filter(paramPredicate).collect(Collectors.toList());
	}
	
	public Map<String, List<String>> getDmpCfgInputConvertMapping(String mainId){
		return convertMappingCache.get(mainId);
	}
	
	private void dealConvertMappingCache() {
		convertMappingCache = new HashMap<>();
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
	}

}
