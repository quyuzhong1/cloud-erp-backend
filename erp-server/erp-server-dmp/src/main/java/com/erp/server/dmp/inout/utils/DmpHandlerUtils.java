package com.erp.server.dmp.inout.utils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.hutool.core.collection.CollectionUtil;
import org.apache.commons.lang.StringUtils;

import com.baomidou.mybatisplus.core.toolkit.Sequence;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;

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
	
	public static <T> T toBeanIgnoreError(Map<String, Object> source, Class<T> clazz) {
		Set<String> dateClassNameSet = new HashSet<>();
		dateClassNameSet.add(Date.class.getName());
		dateClassNameSet.add(LocalDate.class.getName());
		dateClassNameSet.add(LocalDateTime.class.getName());
		
		Field[] fields = clazz.getDeclaredFields();
		Set<String> dateStringTypeSet = new HashSet<>();
		for(Field field : fields) {
			Class<?> type = field.getType();
			if(dateClassNameSet.contains(type.getName())) {
				String name = field.getName();
				Object object = source.get(name);
				if(object instanceof String) {
					dateStringTypeSet.add(name);
				}
			}
		}
		Map<String, Object> newSource = source;
		if(CollUtil.isNotEmpty(dateStringTypeSet)) {
			newSource = new HashMap<>();
			for(Map.Entry<String, Object> s : source.entrySet()) {
				String key = s.getKey();
				Object value = s.getValue();
				if(dateStringTypeSet.contains(key)) {
					try {
						value = Long.valueOf(value.toString());
					} catch (NumberFormatException e) {}
				}
				newSource.put(key, value);
			}
		}
		return BeanUtil.toBeanIgnoreError(newSource, clazz);
	}

	public static Object getValueByPath(Object data, String field) {
		String[] keys = field.split("\\.");

		for (String key : keys) {
			if (data instanceof Map) {
				data = ((Map<String, Object>) data).get(key);
			} else if (data instanceof List) {
				//如果是数组类型默认取第一个
				List<Map<String, Object>> valueList = (List<Map<String, Object>>) data;
				if (CollectionUtil.isNotEmpty(valueList)) {
					data = valueList.get(0).get(key);
				}
			} else {
				return null;  // 如果路径不正确，返回null
			}
		}

		return data;
	}

	public static Object getValueByPath(Map<String, Object> data, String path, String newKey) {
		String[] keys = path.split("\\.");
		Object value = data;

		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];

			if (value instanceof Map) {
				value = ((Map<String, Object>) value).get(key);
			} else if (value instanceof List) {
				final int currentIndex = i;

				// 递归处理 List 中的每一个元素
				value = ((List<?>) value).stream()
						.map(item -> {
							if (item instanceof Map) {
								// 截取数组的剩余部分并拼接为字符串
								String remainingPath = String.join(".", Arrays.copyOfRange(keys, currentIndex + 1, keys.length));
								return getValueByPath((Map<String, Object>) item, remainingPath, newKey);
							}
							return null;
						})
						.collect(Collectors.toList());
				break; // 处理完 List 后退出循环
			} else {
				return null; // 如果路径不正确，返回null
			}
		}

		if (value instanceof Map) {
			// 替换最后的键为 newKey
			value = ((Map<String, Object>) value).get(newKey);
		}

		return value;
	}
}
