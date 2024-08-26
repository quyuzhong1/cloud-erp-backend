package com.erp.server.dmp.inout.utils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.hutool.core.collection.CollectionUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgOutputConvertMappingEntity;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Sequence;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgInputEntity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;

public class DmpHandlerUtils {
	
	private static String namespace = SpringUtil.getProperty("spring.cloud.nacos.discovery.namespace");

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


	public static Map<String, Object> convertAndMapFields(Object dmpSoInfoObj, Object platformOrderDTO, List<DmpCfgOutputConvertMappingEntity> list) {
		Map<String, Object> dmpSoInfoMap = BeanUtil.beanToMap(dmpSoInfoObj);
		Map<String, Object> platformOrderMap = BeanUtil.beanToMap(platformOrderDTO);

		// 获取所有字段类型信息
		Map<String, String> fields = getFieldsAndTypes(platformOrderDTO);

		// 转换键
/*        List<DmpCfgOutputConvertMappingEntity> list = dmpCfgOutputConvertMappingService.lambdaQuery()
                .eq(DmpCfgOutputConvertMappingEntity::getMainId, "").list();*/



		for (DmpCfgOutputConvertMappingEntity mappingEntity : list) {
			if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(mappingEntity.getOriginalKey()) || com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(mappingEntity.getConvertKey())) {
				continue;
			}

			Object value = null;

			if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isBlank(mappingEntity.getOriginalKey())) {
				value = mappingEntity.getDefaultValue();
			} else {
				// 原始key和转换key的解析
				String[] originalKey = mappingEntity.getOriginalKey().split("\\.");

				// 获取原始数据值
				value =  getValueFromMap(dmpSoInfoMap, originalKey);

				if (originalKey.length > 2) {
					throw new ServiceException("字段：" + mappingEntity.getOriginalKey() + "只能使用一个'.'，只支持两层映射");
				}
			}

			String[] convertKey = mappingEntity.getConvertKey().split("\\.");
			if (convertKey.length > 2) {
				throw new ServiceException("字段：" + mappingEntity.getConvertKey() + "只能使用一个'.'，只支持两层映射");
			}

			// 如果值是数组类型，处理数组多条映射
			if (value instanceof List) {
				List<Object> valueList = (List<Object>) value;
				for (Object val : valueList) {
					setValueToMap(platformOrderMap, convertKey, val, fields);
				}
			} else {
				setValueToMap(platformOrderMap, convertKey, value, fields);
			}
		}
		return platformOrderMap;
	}

	/**
	 * 递归获取 Map 中的值，包括处理数组中的所有元素
	 */
	private static Object getValueFromMap(Object data, String[] keys) {
		Object value = data;
		for (int i = 0; i < keys.length; i++) {
			if (value instanceof Map) {
				value = ((Map<String, Object>) value).get(keys[i]);
			} else if (value instanceof List) {
				List<Object> list = (List<Object>) value;
				List<Object> results = new ArrayList<>();
				for (Object item : list) {
					results.add(getValueFromMap(item, Arrays.copyOfRange(keys, i, keys.length)));
				}
				return results;
			} else {
				return null; // 如果路径不正确，返回null
			}
		}
		return value;
	}

	private static void setValueToMap(Map<String, Object> map, String[] keys, Object value, Map<String, String> fieldTypes) {
		Map<String, Object> currentMap = map;

		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];

			// 到达最后一个 key，设置值
			if (i == keys.length - 1) {
				// 类型转换检查
				if (fieldTypes.containsKey(key)) {
					String fieldType = fieldTypes.get(key);
					value = convertType(value, fieldType);
				}
				currentMap.put(key, value);
			} else {
				// 如果中间路径不存在，创建新的 Map
				currentMap = (Map<String, Object>) currentMap.computeIfAbsent(key, k -> new HashMap<>());
			}
		}
	}

	public static void sendFeiShuMsg(String message) {
		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put("msg_type", "text");
		Map<String, String> contentMap = new HashMap<String, String>();

		contentMap.put("text", "中台【"+ namespace +"】环境告警：" + message);
		bodyMap.put("content", contentMap);
		String url = "https://open.feishu.cn/open-apis/bot/v2/hook/8002a820-b24d-4ed3-87e0-8b5b867cc9e3";
		if("prod".equals(namespace)) {
			url = "https://open.feishu.cn/open-apis/bot/v2/hook/c76b72f8-0bf9-4967-a9ce-0728767c1ccc";
		}
		HttpUtil.post(url, JSON.toJSONString(bodyMap));
	}


	private static Object convertType(Object value, String fieldType) {
		// 根据 fieldType 转换 value 类型（示例中仅处理简单类型）
		switch (fieldType) {
			case "java.lang.String":
				return value.toString();
			case "java.lang.Integer":
				return Integer.parseInt(value.toString());
			case "java.lang.Boolean":
				return Boolean.parseBoolean(value.toString());
			case "java.math.BigDecimal":
				return new BigDecimal(value.toString());
			// 添加其他需要的类型转换
			default:
				return value;
		}
	}

	/**
	 * 获取对象的所有字段及其类型
	 *
	 * @param obj 传入的对象
	 * @return 字段名称及类型的Map
	 */
	public static Map<String, String> getFieldsAndTypes(Object obj) {
		Map<String, String> fieldMap = new HashMap<>();

		// 获取对象的实际类
		Class<?> clazz = obj.getClass();

		// 获取所有声明的字段
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			// 获取字段名称
			String fieldName = field.getName();
			// 获取字段类型
			String fieldType = field.getType().getName();

			// 将字段名称和类型存入Map
			fieldMap.put(fieldName, fieldType);
		}

		return fieldMap;
	}
}
