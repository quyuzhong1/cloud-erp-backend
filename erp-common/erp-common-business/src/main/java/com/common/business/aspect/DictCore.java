package com.common.business.aspect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanGenerator;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;

import com.common.business.annotation.Dict;
import com.common.business.enums.ServiceCodeNameEnum;
import com.common.business.feign.BaseDataFeign;
import com.common.business.feign.controller.BaseDataFeignController;
import com.common.business.threadlocal.DictThreadLocal;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.utils.ConvertUtils;
import com.common.business.utils.StringUtil;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DictCore {
	@Value("${spring.application.name}")
	private String serviceName;
	
	private Map<ServiceCodeNameEnum, BaseDataFeign> serviceBaseDataFeignMap;

	public BaseDataFeign getBaseDataFeign(ServiceCodeNameEnum serviceCodeNameEnum) {
		if(serviceBaseDataFeignMap == null) {
			serviceBaseDataFeignMap = new HashMap<>();
		}
		BaseDataFeign baseDataFeign = serviceBaseDataFeignMap.get(serviceCodeNameEnum);
		if(baseDataFeign == null) {
			if(serviceCodeNameEnum == null || ServiceCodeNameEnum.DEFAULT == serviceCodeNameEnum || serviceName.equals("erp-" + serviceCodeNameEnum.getCode())) {
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

            Object recordObj = this.parseDictTextPlus(resultData, lang);
            ((ApiResult) result).setData(recordObj);
        }
    }


    /**
     * 某个对象，解析所有属性
     * @param result
     * @param lang
     * @return
     */
    public Object parseDictTextPlus(Object result, String lang) {
        if (result == null) {
            return result;
        }
        if (result instanceof PagingVO<?>) {
        	List<?> list = ((PagingVO<?>)result).getList();
        	if(CollUtil.isNotEmpty(list)) {
        		List<Object> items = new ArrayList<>();
                for (Object record : list) {
                    record = this.dealRecord(record, lang);
                    items.add(record);
                }
                ((PagingVO) result).setList(items);
        	}
            return result;

        }else if (result instanceof List) {

            List<Object> items = new ArrayList<>();
            for (Object record : ((List) result)) {
                record = this.dealRecord(record, lang);
                items.add(record);
            }
            return items;

        } else if (result instanceof Map) {

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
            for (Object record : ((Set) result)) {
                record = this.dealRecord(record, lang);
                items.add(record);
            }
            return items;

        } else {
            Object record = this.dealRecord(result, lang);
            return record;
        }
    }


    public Object dealRecord(Object record, String lang) {

        //得到拥有@Dict注释的全部字段map
    	Map<String, Object> fieldValueMap = this.translatePerItem(record, lang);
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
    private List<Map<String, Object>> translateDictValue(String code, String text, String table, String key, ServiceCodeNameEnum serviceCodeNameEnum) {
        if (ConvertUtils.isEmpty(code) || ConvertUtils.isEmpty(text) || ConvertUtils.isEmpty(table)) {
            return null;
        }
        
        if(ConvertUtils.isEmpty(key)) {
			return getNullDataView(table , text);
        }
        
        List<Map<String, Object>> data = DictThreadLocal.get(code, text, table, key , serviceCodeNameEnum);
        if(data == null) {
        	try {
				data = getBaseDataFeign(serviceCodeNameEnum).queryValueByValue(table, code, key, text);
			} catch (Exception e) {
			}
        	if(data == null) {
        		data = getNullDataView(table, text);
        	}
        	DictThreadLocal.set(code, text, table, key , serviceCodeNameEnum , data);
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
    

    private Map<String, Object> translatePerItem(Object record, String lang) {
        Map<String, Object> fieldValueMap = new HashMap<>();

        for (Field field : ConvertUtils.getAllFields(record)) {
            Dict dictAnnotation = field.getAnnotation(Dict.class);
            if (dictAnnotation != null) {
                String code = dictAnnotation.queryFieldName();
                String table = dictAnnotation.tableName();
                String text = dictAnnotation.returnFieldName();
                String enumClass = dictAnnotation.enumClass();
                boolean dictChildren = dictAnnotation.dictChildren();
                boolean dictDefaultOriginalValue = dictAnnotation.dictDefaultOriginalValue();
                ServiceCodeNameEnum serviceCode = dictAnnotation.serviceCode();

                field.setAccessible(true);

                // 深层翻译
                Object keyObject = null;
				try {
					keyObject = field.get(record);
				} catch (Exception e1) {
				} 
				if (dictChildren) {
                    try {
                        Object childrenObj = keyObject;
                        // 翻译子属性值
                        Object dictChildrenObj = this.parseDictTextPlus(childrenObj, lang);

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

                //翻译字典值对应的txt
                String textValue = "";
                boolean isEnum = (StringUtils.isNotBlank(enumClass) || keyObject.getClass().isEnum());
                boolean isNotDict = (serviceCode == null || serviceCode == ServiceCodeNameEnum.DEFAULT);
                if(keyObject != null) {
                	if(isEnum && isNotDict) {
                        try {
                            textValue = getFieldVal(keyObject, enumClass);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                	}
                	
                	if(StringUtils.isBlank(textValue)) {
                		String key = keyObject.toString();
                		if(StringUtils.isNotBlank(key)) {
                			if(serviceCode == ServiceCodeNameEnum.PLM && "dict_basic".equals(table)) {
                				table = "basic_dict";
                			}
                			List<Map<String, Object>> translateDictValue = translateDictValue(code, text, table, key , serviceCode);
                			if(CollUtil.isNotEmpty(translateDictValue)) {
                				Map<String, Object> map = translateDictValue.get(0);
                				if(map != null && !map.isEmpty()) {
                					textValue = map.values().stream().filter(o -> o != null).map(Object::toString).collect(Collectors.joining(","));
                				}
                			}
                		}
                    }
                	
                	if(dictDefaultOriginalValue && StringUtils.isBlank(textValue)) {
                    	textValue = keyObject.toString();
                    }
                }
                
                fieldValueMap.put(field.getName() + "_Name", textValue);
            }

        }
        
        return fieldValueMap;
    }

    public static String getFieldVal(Object targetClass, String objClass) throws Exception {
        if(targetClass == null){
            return "";
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(objClass)
                && targetClass instanceof String){
            String[] split = targetClass.toString().split(",");
            StringBuilder result = new StringBuilder();
            Class<?> clazz = Class.forName(objClass);
            Method method = clazz.getMethod("getName", String.class);
            for (String s : split) {
                try {
                    Object invoke = method.invoke(null , s);
                    result.append(invoke.toString()).append(",");
                } catch (Exception e) {
                    result.append(s).append(",");
                }
            }
            return result.length() == 0? result.toString() : result.substring(0, result.length() -1);
        }
        Class<?> clazz = targetClass.getClass();
        String getMethodName = "getName";
        Object result = "";
        try {
            Method method = clazz.getMethod(getMethodName);
            result = method.invoke(targetClass);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
        }
        if(result == null) {
        	return "";
        }else {
        	return result.toString();
        }
    }

}
