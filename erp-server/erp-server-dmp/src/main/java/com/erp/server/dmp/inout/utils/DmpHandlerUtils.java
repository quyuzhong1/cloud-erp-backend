package com.erp.server.dmp.inout.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

import com.baomidou.mybatisplus.core.toolkit.Sequence;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;

public class DmpHandlerUtils {
	
	private static Sequence sequence = new Sequence();
	
	public static String getId() {
		long nextId = sequence.nextId();
		return Long.valueOf(nextId).toString();
	}
	
	public static String dealBeanClass(String beanClass) {
		String[] split = beanClass.split("\\.");
		return StringUtils.uncapitalize(split[split.length - 1]);
	}
	
	/**
	 * 获取唯一字段信息
	 * @param uniqueFieldName
	 * @param uniqueFieldSet
	 * @return
	 */
	public static boolean getAllFieldFlag(String uniqueFieldName , Set<String> uniqueFieldSet){
		if(StringUtils.isNotBlank(uniqueFieldName)) {
			if("{all}".equals(uniqueFieldName)) {
				return true;
			}else {
				uniqueFieldSet.addAll(Stream.of(uniqueFieldName.split(",")).collect(Collectors.toSet()));
			}
		}else {
			return true;
		}
		return false;
	}
	
	public static String getMongoStorageName(DmpBasicSystemEntity dmpBasicSystemEntity , DmpCfgInputEntity dmpCfgInputEntity , DmpCfgInputConvertEntity dmpCfgInputConvertEntity) {
		List<String> mongoStorageNameList = new ArrayList<>();
		mongoStorageNameList.add(dmpBasicSystemEntity.getCode());
		mongoStorageNameList.add(dmpCfgInputEntity.getCode());
		mongoStorageNameList.add(dmpCfgInputConvertEntity.getStorageName());
		return String.join("_", mongoStorageNameList);
	}
}
