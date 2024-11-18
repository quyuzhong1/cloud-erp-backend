package com.common.business.aspect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanGenerator;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.annotation.Dict;
import com.common.business.dto.AttachDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ServiceCodeNameEnum;
import com.common.business.feign.BaseDataFeign;
import com.common.business.feign.controller.BaseDataFeignController;
import com.common.business.threadlocal.DictThreadLocal;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.utils.ConvertUtils;
import com.common.business.utils.StringUtil;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.controller.vo.ApiResult;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DictCore {
	public static final String JAVA_LANG_STRING = "java.lang.String";
	@Value("${spring.application.name}")
	private String serviceName;
	
	private static final String ERP_PRI = "erp-";
	
	private Map<ServiceCodeNameEnum, BaseDataFeign> serviceBaseDataFeignMap;

	public BaseDataFeign getBaseDataFeign(ServiceCodeNameEnum serviceCodeNameEnum) {
		if(serviceBaseDataFeignMap == null) {
			serviceBaseDataFeignMap = new HashMap<>();
		}
		BaseDataFeign baseDataFeign = serviceBaseDataFeignMap.get(serviceCodeNameEnum);
		if(baseDataFeign == null) {
			if(serviceCodeNameEnum == null || ServiceCodeNameEnum.DEFAULT == serviceCodeNameEnum || serviceName.equals(ERP_PRI + serviceCodeNameEnum.getCode())) {
				baseDataFeign = ApplicationContextUtils.getBean(BaseDataFeignController.class);
			}else {
				String code = serviceCodeNameEnum.getCode();
				baseDataFeign = ApplicationContextUtils.getBean(String.format("com.erp.rpc.%s.feign.%sBaseDataFeign", code , StringUtils.capitalize(code)), BaseDataFeign.class);
			}
			serviceBaseDataFeignMap.put(serviceCodeNameEnum, baseDataFeign);
		}
		return baseDataFeign;
	}
	
    /**
     * 本方法针对返回对象为Result 的IPage的分页列表数据进行动态字典注入
     * 字典注入实现 通过对实体类添加注解@dict 来标识需要的字典内容,字典分为单字典code即可 ，table字典 code table text配合使用与原来jeecg的用法相同
     * 示例为SysUser   字段为sex 添加了注解@Dict(dicCode = "sex") 会在字典服务立马查出来对应的text 然后在请求list的时候将这个字典text，已字段名称加_dictText形式返回到前端
     * 例输入当前返回值的就会多出一个sex_dictText字段
     * {
     * sex:1,
     * sex_dictText:"男"
     * }
     * 前端直接取值sext_dictText在table里面无需再进行前端的字典转换了
     * customRender:function (text) {
     * if(text==1){
     * return "男";
     * }else if(text==2){
     * return "女";
     * }else{
     * return text;
     * }
     * }
     * 目前vue是这么进行字典渲染到table上的多了就很麻烦了 这个直接在服务端渲染完成前端可以直接用
     *
     * @param result
     */
    @SuppressWarnings("all")
    public void parseDictText(Object result) {
        String lang = "";
        if (result instanceof ApiResult) {

            Object resultData = ((ApiResult) result).getData();

            if (resultData == null) {
                return;
            }

            Object recordObj = this.parseDictTextPlus(resultData, lang , true);
            ((ApiResult) result).setData(recordObj);
        }else if(result instanceof List) {
        	Object recordObj = this.parseDictTextPlus(result, lang , true);
        	List<Object> items = new ArrayList<>();
        	((List) result).clear();
        	for (Object record : ((List) recordObj)) {
        		((List) result).add(record);
            }
        }
    }


    /**
     * 某个对象，解析所有属性
     * @param result
     * @param lang
     * @return
     */
    public Object parseDictTextPlus(Object result, String lang , boolean isFirst) {
        if (result == null) {
            return result;
        }
        if (result instanceof PagingVO<?>) {
        	List<?> list = ((PagingVO<?>)result).getList();
        	if(CollUtil.isNotEmpty(list)) {
        		addDictCache(list.stream().filter(Objects::nonNull).findAny().orElse(null), isFirst);
        		List<Object> items = new ArrayList<>();
                for (Object record : list) {
                	record = this.dealRecord(record, lang);
                    items.add(record);
                }
                ((PagingVO) result).setList(items);
        	}
            return result;

        }else if (result instanceof IPage<?>) {
        	List<?> list = ((IPage<?>)result).getRecords();
        	if(CollUtil.isNotEmpty(list)) {
        		addDictCache(list.stream().filter(Objects::nonNull).findAny().orElse(null), isFirst);
        		List<Object> items = new ArrayList<>();
                for (Object record : list) {
                	record = this.dealRecord(record, lang);
                    items.add(record);
                }
                ((IPage) result).setRecords(items);
        	}
            return result;

        }else if (result instanceof List) {
            List<Object> items = new ArrayList<>();
            addDictCache(((List) result).stream().filter(Objects::nonNull).findAny().orElse(null), isFirst);
            for (Object record : ((List) result)) {
                record = this.dealRecord(record, lang);
                items.add(record);
            }
            return items;

        } else if (result instanceof Map) {
        	addDictCache(((Map<String, Object>) result).values().stream().filter(Objects::nonNull).findAny().orElse(null), isFirst);
            Map<String, Object> items = new HashMap<>();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) result).entrySet()) {
                String key = entry.getKey();
                Object record = entry.getValue();

                record = this.dealRecord(record, lang);
                items.put(key, record);
            }
            return items;


        } else if (result instanceof Set) {
            Set<Object> items = new HashSet<>();
            addDictCache(((Set) result).stream().filter(Objects::nonNull).findAny().orElse(null), isFirst);
            for (Object record : ((Set) result)) {
                record = this.dealRecord(record, lang);
                items.add(record);
            }
            return items;

        } else {
        	addDictCache(result, isFirst);
            Object record = this.dealRecord(result, lang);
            return record;
        }
    }

    public void addDictCache(Object record , boolean isFirst) {
    	if(!isFirst || record == null) {
    		return;
    	}
    	
    	Class<?> clazz = record.getClass();
    	List<DictDto> dictDtoList = new ArrayList<>();
    	getDictDtoList(clazz, dictDtoList);
    	if(CollUtil.isNotEmpty(dictDtoList)) {
    		dictDtoList = dictDtoList.stream().distinct().collect(Collectors.toList());
    		Map<String, List<DictDto>> queryDictMaps = dictDtoList.stream().collect(Collectors.groupingBy(d -> {
    			return d.getServiceCode().getCode() + "_" + d.getTableName() + "_" + d.getQueryFieldName() + "_" + d.getReturnFieldName();
    		}));
    		for(Map.Entry<String, List<DictDto>> queryDictMap : queryDictMaps.entrySet()) {
    			List<DictDto> value = queryDictMap.getValue();
				DictDto dictDto = value.get(0);
    			BaseDataFeign baseDataFeign = getBaseDataFeign(dictDto.getServiceCode());
    			List<Map<String, Object>> data = baseDataFeign.queryValueByType(dictDto.getTableName(), dictDto.getQueryFieldName()
    					,dictDto.getReturnFieldName() , value.stream().map(DictDto::getQueryTypeField).
    					collect(Collectors.joining("','", "'", "'")));
    			
    			Map<String, List<Map<String, Object>>> typeDataMaps = new HashMap<>();
    			for(Map<String, Object> d : data) {
    				String queryType = d.get("type").toString();
    				List<Map<String, Object>> dataList = typeDataMaps.get(queryType);
    				if(dataList == null) {
    					dataList = new ArrayList<>();
    				}
    				dataList.add(d);
					typeDataMaps.put(queryType, dataList);
    			}
    			
    			for(DictDto v : value) {
    				String queryTypeField = v.getQueryTypeField();
    				String queryFieldName = v.getQueryFieldName();
					List<Map<String, Object>> dataList = typeDataMaps.get(queryTypeField);
    				if(CollUtil.isNotEmpty(dataList)) {
    					for(Map<String, Object> d : dataList) {
    						Object queryFieldValue = d.get(queryFieldName);
    						if(queryFieldValue == null) {
    							queryFieldValue = d.get(StringUtil.convertToCamel(queryFieldName));
    						}
							if(queryFieldValue != null) {
    							List<Map<String, Object>> finalData = DictThreadLocal.get(queryTypeField, queryFieldName ,v.getReturnFieldName(), v.getTableName()
        	    						, queryFieldValue.toString(), v.getServiceCode());
    							if(finalData == null) {
    								finalData = new ArrayList<>();
    							}
    							Map<String, Object> oneData = new HashMap<>();
    							oneData.put(v.getReturnFieldName(), d.get(v.getReturnFieldName()));
    							finalData.add(oneData);
    							DictThreadLocal.set(queryTypeField, queryFieldName ,v.getReturnFieldName(), v.getTableName()
        	    						, queryFieldValue.toString(), v.getServiceCode(), finalData);
    						}
    					}
    				}
    			}
    		}
    	}
    }
    
    public void getDictDtoList(Class<?> clazz , List<DictDto> dictDtoList){
    	for (Field field : ConvertUtils.getAllFields(clazz)) {
    		Dict dictAnnotation = field.getAnnotation(Dict.class);
            if (dictAnnotation != null) {
            	Class<?> declaringClass = field.getType();
            	if(!JAVA_LANG_STRING.equals(declaringClass.getName()) && !declaringClass.isEnum()) {
            		if(!declaringClass.isAssignableFrom(clazz)) {
            			getDictDtoList(declaringClass , dictDtoList);
                	}
            		continue;
            	}
            	
            	String queryTypeField = dictAnnotation.queryTypeField();
            	if(StringUtils.isNotBlank(queryTypeField) && StringUtils.isBlank(dictAnnotation.extendQuerySql())) {
            		String queryFieldName = dictAnnotation.queryFieldName();
                    String returnFieldName = dictAnnotation.returnFieldName();
                    ServiceCodeNameEnum serviceCode = dictAnnotation.serviceCode();
                    serviceCode = convertServiceCode(serviceCode);
                    String table = dictAnnotation.tableName();
                    table = convertTable(serviceCode, table);
                    
                    DictDto dictDto = new DictDto();
                    dictDto.setQueryFieldName(queryFieldName);
                    dictDto.setQueryTypeField(queryTypeField);
                    dictDto.setReturnFieldName(returnFieldName);
                    dictDto.setServiceCode(serviceCode);
                    dictDto.setTableName(table);
                    dictDtoList.add(dictDto);
            	}
            }
    	}
    }

    public Object dealRecord(Object record, String lang) {

        //得到拥有@Dict注释的全部字段map
    	DictAspectFieldMap dictAspectFieldMap = this.translatePerItem(record, lang);
    	Map<String, Object> fieldValueMap = dictAspectFieldMap.getFieldValueMap();
    	record = dictAspectFieldMap.getRecord();
        if (fieldValueMap.size() > 0) {
            BeanGenerator beanGenerator = new BeanGenerator();
            //设置类的class
            beanGenerator.setSuperclass(record.getClass());
            //增加新的_dictText字段
            for (Map.Entry<String, Object> entry : fieldValueMap.entrySet()) {
                String key = entry.getKey();
                beanGenerator.addProperty(key, Object.class);
            }
            //创建拥有_dictText字段的类
            Object objHasDictText = beanGenerator.create();

            //复制当前记录的值，给新创建的拥有_dictText字段的新对象
            BeanUtils.copyProperties(record, objHasDictText);

            //为新对象赋值_dictText字段
            BeanMap beanMap = BeanMap.create(objHasDictText);
            for (Map.Entry<String, Object> entry : fieldValueMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                beanMap.put(key, value);
            }

            return objHasDictText;
        } else {
            //将原记录原样返回
            return record;
        }

    }

    /**
     * 翻译字典文本
     *
     * @param code
     * @param text
     * @param table
     * @param key
     * @return
     */
    private List<Map<String, Object>> translateDictValue(String code, String text, String table, String key, ServiceCodeNameEnum serviceCodeNameEnum , String extendQuerySql) {
        if (ConvertUtils.isEmpty(code) || ConvertUtils.isEmpty(text) || ConvertUtils.isEmpty(table)) {
            return null;
        }
        
        if(ConvertUtils.isEmpty(key)) {
			return getNullDataView(table , text);
        }
        
        List<Map<String, Object>> data = DictThreadLocal.get(extendQuerySql , code, text, table, key , serviceCodeNameEnum);
        if(data == null) {
        	try {
				data = getBaseDataFeign(serviceCodeNameEnum).queryValueByValue(table, code, key, text , extendQuerySql);
			} catch (Exception e) {
			}
        	if(data == null) {
        		data = getNullDataView(table, text);
        	}
        	DictThreadLocal.set(extendQuerySql , code, text, table, key , serviceCodeNameEnum , data);
        }
		return data.stream().map(da -> {
			Map<String, Object> m = new HashMap<>();
			if(da == null) {
				Map<String, Object> map = getNullDataView(table , text).get(0);
				for(Map.Entry<String, Object> d : map.entrySet()) {
					m.put(d.getKey(), d.getValue());
				}
			}else {
				for(Map.Entry<String, Object> d : da.entrySet()) {
					String value = "";
					if(d.getValue() != null) {
						value = d.getValue().toString();
					}
					m.put(StringUtil.convertToCamel(table) + "_" + StringUtil.convertToCamel(d.getKey()), value);
    			}
			}
			return m;
		}).collect(Collectors.toList());
    }

    private static List<Map<String, Object>> getNullDataView(String table , String text) {
    	return new ArrayList<>();
//    	Map<String, String> nullDataMap = new HashMap<>();
//    	String[] texts = text.split(",");
//    	for(int i = 0; i < texts.length; i++) {
//    		nullDataMap.put(StringUtil.convertToCamel(table) + "_" + StringUtil.convertToCamel(texts[i]), "-");
//    	}
//    	return Arrays.asList(nullDataMap);
    }
    

    private DictAspectFieldMap translatePerItem(Object record, String lang) {
    	DictAspectFieldMap dictAspectFieldMap = new DictAspectFieldMap();
        Map<String, Object> fieldValueMap = new HashMap<>();

        for (Field field : ConvertUtils.getAllFields(record)) {
            Dict dictAnnotation = field.getAnnotation(Dict.class);
            if (dictAnnotation != null) {
                String code = dictAnnotation.queryFieldName();
                String text = dictAnnotation.returnFieldName();
                Class<? extends EnumMessage> enumClass = dictAnnotation.enumClass();
                boolean dictDefaultOriginalValue = dictAnnotation.dictDefaultOriginalValue();
                ServiceCodeNameEnum serviceCode = dictAnnotation.serviceCode();
                serviceCode = convertServiceCode(serviceCode);
                String table = dictAnnotation.tableName();
                table = convertTable(serviceCode, table);
                String type = dictAnnotation.queryTypeField();
                String extendQuerySql = dictAnnotation.extendQuerySql();
                String dictFieldName = dictAnnotation.dictFieldName();

                field.setAccessible(true);

                // 深层翻译
                Object keyObject = null;
				try {
					keyObject = field.get(record);
				} catch (Exception e1) {
				}
				//翻译字典值对应的txt
				if(keyObject != null) {
					String textValue = "";
					boolean isEnum = keyObject.getClass().isEnum();
					if (!(keyObject instanceof String) && !isEnum) {
	                    try {
	                        Object childrenObj = keyObject;
	                        // 翻译子属性值
	                        Object dictChildrenObj = this.parseDictTextPlus(childrenObj, lang , false);

	                        BeanGenerator beanGenerator = new BeanGenerator();
	                        //设置类的class
	                        beanGenerator.setSuperclass(record.getClass());
	                        //创建拥有_dictText字段的类
	                        Object objHasDictText = beanGenerator.create();
	                        //复制当前记录的值，给新创建的拥有_dictText字段的新对象
	                        BeanUtils.copyProperties(record, objHasDictText);
	                        //为新对象赋值_dictText字段
	                        BeanMap beanMap = BeanMap.create(objHasDictText);
	                        beanMap.put(field.getName(), dictChildrenObj);
	                        // 替换原record
	                        record = objHasDictText;

	                    } catch (Exception ignored) {
	                    }
	                    continue;
	                }

                	if(isEnum) {
                        try {
                            textValue = getFieldVal(keyObject, enumClass);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                	}
                	
                	if(StringUtils.isBlank(textValue)) {
                		String key = keyObject.toString();
                		if(StringUtils.isNotBlank(key)) {
                			List<Map<String, Object>> translateDictValue = null;
                			if(StringUtils.isNotBlank(type) && StringUtils.isBlank(extendQuerySql)) {
                				translateDictValue =  DictThreadLocal.get(type , code, text, table, key , serviceCode);
                			}else {
                				translateDictValue = translateDictValue(code, text, table, key , serviceCode , extendQuerySql);
                			}
                			
                			if(CollUtil.isNotEmpty(translateDictValue)) {
                				Map<String, Object> map = translateDictValue.get(0);
                				if(map != null && !map.isEmpty()) {
                					textValue = map.values().stream().filter(o -> o != null).map(Object::toString).collect(Collectors.joining(","));
                				}
                			}
                			
                		}
                    }
                	
                	if(StringUtils.isBlank(textValue) && enumClass != EnumMessage.class) {
                		try {
                            textValue = getFieldVal(keyObject, enumClass);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                	}
                	
                	if(dictDefaultOriginalValue && StringUtils.isBlank(textValue)) {
                    	textValue = keyObject.toString();
                    }
                	if(StringUtils.isBlank(dictFieldName)) {
                		String fieldName = field.getName();
                		if(fieldName.endsWith("Id")) {
                			fieldName = fieldName.substring(0, fieldName.length() - 2);
                		}
                		dictFieldName = fieldName + "Name";
                	}
                	fieldValueMap.put(dictFieldName, textValue);
				}
                
            }

        }
        
        dictAspectFieldMap.setRecord(record);
        dictAspectFieldMap.setFieldValueMap(fieldValueMap);
        
        return dictAspectFieldMap;
    }

    private  ServiceCodeNameEnum convertServiceCode(ServiceCodeNameEnum serviceCode) {
    	if(serviceCode == ServiceCodeNameEnum.DEFAULT) {
    		return EnumMessage.getByCode(ServiceCodeNameEnum.class, serviceName.replace(ERP_PRI, ""));
        }
    	return serviceCode;
    }
    
    private String convertTable(ServiceCodeNameEnum serviceCode , String table) {
    	if(serviceCode == ServiceCodeNameEnum.DEFAULT) {
    		serviceCode = convertServiceCode(serviceCode);
    	}
    	if(serviceCode == ServiceCodeNameEnum.PLM && "dict_basic".equals(table)) {
			table = "basic_dict";
		}
    	return table;
    }
    
    public static String getFieldVal(Object targetClass, Class<? extends EnumMessage> objClass) throws Exception {
        if(targetClass == null){
            return "";
        }
        Class<?> clazz = targetClass.getClass();
        String getMethodName = "getName";
        Object result = "";
        if (clazz.isEnum()){
        	 try {
                 Method method = clazz.getMethod(getMethodName);
                 result = method.invoke(targetClass);
             } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
             }
        }else {
        	result = EnumMessage.getNameByCode(objClass, targetClass);
        }
        if(result == null) {
        	return "";
        }else {
        	return result.toString();
        }
    }

    @Data
    public static class DictAspectFieldMap{
    	private Object record;
    	private Map<String, Object> fieldValueMap;
    }
    
    @Data
    public static class DictDto{
    	private String queryFieldName;
    	private String queryTypeField;
    	private String returnFieldName;
    	private String tableName;
    	private ServiceCodeNameEnum serviceCode;
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DictDto other = (DictDto) obj;
			if (queryFieldName == null) {
				if (other.queryFieldName != null)
					return false;
			} else if (!queryFieldName.equals(other.queryFieldName))
				return false;
			if (queryTypeField == null) {
				if (other.queryTypeField != null)
					return false;
			} else if (!queryTypeField.equals(other.queryTypeField))
				return false;
			if (returnFieldName == null) {
				if (other.returnFieldName != null)
					return false;
			} else if (!returnFieldName.equals(other.returnFieldName))
				return false;
			if (serviceCode != other.serviceCode)
				return false;
			if (tableName == null) {
				if (other.tableName != null)
					return false;
			} else if (!tableName.equals(other.tableName))
				return false;
			return true;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((queryFieldName == null) ? 0 : queryFieldName.hashCode());
			result = prime * result + ((queryTypeField == null) ? 0 : queryTypeField.hashCode());
			result = prime * result + ((returnFieldName == null) ? 0 : returnFieldName.hashCode());
			result = prime * result + ((serviceCode == null) ? 0 : serviceCode.hashCode());
			result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
			return result;
		}
    	
    	
    }
}
