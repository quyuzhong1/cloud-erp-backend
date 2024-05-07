package com.common.business.feign.controller;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.feign.BaseDataFeign;
import com.common.business.mapper.BaseDataMapper;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.utils.StringUtil;
import com.common.business.wrapper.FeignBuilder;
import com.common.business.wrapper.FeignInvoke;
import com.common.business.wrapper.QueryParam;
import com.common.business.wrapper.QueryTypeEnum;
import com.common.core.controller.BaseController;
import com.common.core.exception.ServiceException;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("feign/baseData")
public class BaseDataFeignController extends BaseController implements BaseDataFeign{

	@Autowired(required = false)
	private BaseDataMapper baseDataMapper;
	
	@GetMapping("/queryValueByValue")
	@Override
	public List<Map<String, Object>> queryValueByValue(String tableName, String queryFieldName, String queryValue, String returnFieldName , String extendQuerySql) {
		return baseDataMapper.queryValueByValue(tableName, queryFieldName, queryValue, returnFieldName , extendQuerySql);
	}

	@GetMapping("/queryValueByType")
	@Override
	public List<Map<String, Object>> queryValueByType(String tableName, String queryFieldName,
			String returnFieldName, String queryTypeField) {
		return baseDataMapper.queryValueByType(tableName, queryFieldName, returnFieldName , queryTypeField);
	}

	@PostMapping("/list")
	@Override
	public String list(FeignBuilder builder) {
		String[] classPackeNames = builder.getClazz().getName().split("\\.");
		ServiceImpl bean = ApplicationContextUtils.getBean(StringUtils.uncapitalize(classPackeNames[classPackeNames.length - 1].replace("Entity", "")) + "ServiceImpl" , ServiceImpl.class);
		List list = bean.list(getQueryWrapper(builder.getQueryParams()));
		return JSON.toJSONString(success(list));
	}
	
	@PostMapping("/invoke")
	@Override
	public String invoke(FeignInvoke feignInvoke) {
		String className = feignInvoke.getClassName();
		String methodName = feignInvoke.getMethodName();
		List<Object> param = feignInvoke.getParam();
		Object result = null;
		try {
			Class<?> clazz = Class.forName(className);
			Object bean = ApplicationContextUtils.getBean(clazz);
			if(CollUtil.isNotEmpty(param)) {
				Method[] methods = clazz.getMethods();
				Method invokeMethod = null;
				for(Method method : methods) {
					if(method.getName().equals(methodName) && method.getParameterCount() == param.size()) {
						invokeMethod = method;
						break;
					}
				}
				if(invokeMethod == null) {
					throw new ServiceException("调用远程"+ className + "#" + methodName +"方法不存在");
				}
				Object [] paramVarArgs = new Object[param.size()];
				Class<?>[] parameterTypes = invokeMethod.getParameterTypes();
				for(int i = 0; i < parameterTypes.length; i++) {
					paramVarArgs[i] = JSON.parseObject(JSON.toJSONString(param.get(i)), parameterTypes[i]);
				}
				result = invokeMethod.invoke(bean , paramVarArgs);
			}else {
				Method method = clazz.getMethod(methodName);
				result = method.invoke(bean);
			}
		} catch (ClassNotFoundException e) {
			log.error("调用远程类不存在{}" , e);
			throw new ServiceException("调用"+ className +"远程类不存在");
		} catch (NoSuchMethodException e) {
			log.error("调用远程类方法不存在{}" , e);
			throw new ServiceException("调用远程"+ className + "#" + methodName +"方法不存在");
		} catch (IllegalArgumentException e) {
			log.error("调用远程方法参数错误{}" , e);
			throw new ServiceException("调用远程"+ className + "#" + methodName +"方法参数错误");
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("调用远程方法错误{}" , e);
			throw new ServiceException("调用远程"+ className + "#" + methodName +"方法错误");
		} 
		return JSON.toJSONString(success(result));
	}
	
	private QueryWrapper<?> getQueryWrapper(List<QueryParam> queryParams) {
        QueryWrapper<?> wrapper = new QueryWrapper<>();

        if (CollUtil.isEmpty(queryParams)) {
            return wrapper;
        }
        // 遍历设置查询条件
        for (QueryParam queryParam : queryParams) {
        	QueryTypeEnum type = queryParam.getType();
            String name = queryParam.getName();
            Object value = queryParam.getValue();
            List<Object> values = queryParam.getValues();
            List<QueryTypeEnum> notNeedValueType = Arrays.asList(QueryTypeEnum.GROUP_BY , QueryTypeEnum.ORDER_BY_ASC , QueryTypeEnum.ORDER_BY_DESC ,
            		QueryTypeEnum.APPLY , QueryTypeEnum.LAST , QueryTypeEnum.IS_NULL , QueryTypeEnum.IS_NOT_NULL , QueryTypeEnum.SELECT);
            if ((StringUtils.isBlank(name) && CollUtil.isEmpty(values)) || (!notNeedValueType.contains(type) && 
            		(Objects.isNull(value) || "".equals(value.toString())) && CollUtil.isEmpty(values))) {
                continue;
            }
            name = StringUtil.camelToUnderline(name);
            if (QueryTypeEnum.EQ.equals(type)) {
                wrapper.eq(name, value);
            } else if (QueryTypeEnum.GROUP_BY.equals(type)) {
                wrapper.groupBy(values.toArray(new String[] {}));
            } else if (QueryTypeEnum.IN.equals(type)) {
                if (!CollectionUtils.isEmpty(values)) {
                    wrapper.in(name, values);
                }
            }else if (QueryTypeEnum.NOT_IN.equals(type)) {
                if (!CollectionUtils.isEmpty(values)) {
                    wrapper.notIn(name, values);
                }
            } else if (QueryTypeEnum.ORDER_BY_ASC.equals(type)) {
                wrapper.orderByAsc(name);
            } else if(QueryTypeEnum.ORDER_BY_DESC.equals(type)) {
                wrapper.orderByDesc(name);
            } else if (QueryTypeEnum.APPLY.equals(type)) {
                wrapper.apply(name);
            } else if (QueryTypeEnum.LAST.equals(type)) {
                wrapper.last(name);
            } else if (QueryTypeEnum.LIKE.equals(type)) {
                wrapper.like(name , value);
            } else if (QueryTypeEnum.NOT_LIKE.equals(type)) {
                wrapper.notLike(name , value);
            } else if (QueryTypeEnum.GT.equals(type)) {
                wrapper.gt(name , value);
            } else if (QueryTypeEnum.LT.equals(type)) {
                wrapper.lt(name , value);
            } else if (QueryTypeEnum.GE.equals(type)) {
                wrapper.ge(name , value);
            } else if (QueryTypeEnum.LE.equals(type)) {
                wrapper.le(name , value);
            } else if (QueryTypeEnum.SELECT.equals(type)) {
            	if (!CollectionUtils.isEmpty(values)) {
            		String selectSql = values.stream().filter(v -> v != null).map(v -> StringUtil.camelToUnderline(v.toString())).collect(Collectors.joining(","));
            		if(StringUtils.isNotBlank(selectSql)) {
            			wrapper.select(selectSql);
            		}
                }
            }else if (QueryTypeEnum.IS_NULL.equals(type)) {
            	wrapper.isNull(name);
            }else if (QueryTypeEnum.IS_NOT_NULL.equals(type)) {
            	wrapper.isNotNull(name);
            }else if (QueryTypeEnum.NE.equals(type)) {
            	wrapper.ne(name , value);
            }

        }
        return wrapper;
    }

}
