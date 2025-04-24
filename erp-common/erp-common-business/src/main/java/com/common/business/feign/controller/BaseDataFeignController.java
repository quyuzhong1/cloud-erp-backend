package com.common.business.feign.controller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.hutool.core.exceptions.ExceptionUtil;
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
		List list = bean.list(QueryParam.getQueryWrapper(builder.getQueryParams()));
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
				Object[] paramVarArgs = parseParamVarArgs(param, invokeMethod);
				result = invokeMethod.invoke(bean , paramVarArgs);
			}else {
				Method method = clazz.getMethod(methodName);
				result = method.invoke(bean);
			}
		} catch (ClassNotFoundException e) {
			log.error("调用远程类不存在{}" , ExceptionUtil.stacktraceToString(e));
			throw new ServiceException("调用"+ className +"远程类不存在");
		} catch (NoSuchMethodException e) {
			log.error("调用远程类方法不存在{}" , ExceptionUtil.stacktraceToString(e));
			throw new ServiceException("调用远程"+ className + "#" + methodName +"方法不存在");
		} catch (IllegalArgumentException e) {
			log.error("调用远程方法参数错误{}" , ExceptionUtil.stacktraceToString(e));
			throw new ServiceException("调用远程"+ className + "#" + methodName +"方法参数错误");
		} catch (ServiceException e) {
			throw e;
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			log.error("调用远程方法错误" , cause);
			throw new ServiceException("调用远程"+ className + "#" + methodName +"方法错误");
		} catch (Exception e) {
			log.error("调用远程方法错误{}" , ExceptionUtil.stacktraceToString(e));
			throw new ServiceException("调用远程"+ className + "#" + methodName +"方法错误");
		} 
		return JSON.toJSONString(success(result));
	}

	private static Object[] parseParamVarArgs(List<Object> param, Method invokeMethod) {
		Object [] paramVarArgs = new Object[param.size()];
		Class<?>[] parameterTypes = invokeMethod.getParameterTypes();
		for(int i = 0; i < parameterTypes.length; i++) {
			String s = "{}";
			Object p = param.get(i);
			if(p != null) {
				s = p.toString();
			}
			paramVarArgs[i] = JSON.parseObject(s, parameterTypes[i]);
		}
		return paramVarArgs;
	}

}
