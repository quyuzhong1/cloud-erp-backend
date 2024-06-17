package com.erp.server.dmp.inout.utils;

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
}
