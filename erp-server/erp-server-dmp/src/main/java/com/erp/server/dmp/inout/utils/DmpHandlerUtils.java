package com.erp.server.dmp.inout.utils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

import com.baomidou.mybatisplus.core.toolkit.Sequence;

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
}
