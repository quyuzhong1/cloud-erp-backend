package com.common.business.wrapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.common.business.aspect.DictCore;
import com.common.business.enums.ServiceCodeNameEnum;
import com.common.business.feign.BaseDataFeign;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.utils.FunctionUtil;
import com.common.business.utils.SFunction;
import com.common.business.utils.StringUtil;
import com.common.core.constant.EnumMessage;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;

@Data
public class FeignBuilder {

    private final List<QueryParam> queryParams = new ArrayList<>();
    
    private Class<?> clazz;
    
    public <T extends BaseEntity<T>> FeignBuilder eq(SFunction<T, ?> function, Object value) {
        queryParams.add(new QueryParam(QueryTypeEnum.EQ, getColumn(function), value));
        return this;
    }

    public FeignBuilder() {
    	
    }
    
    private FeignBuilder(Class<?> clazz) {
    	this.clazz = clazz;
    }
    
    public static FeignBuilder create(Class<?> clazz) {
    	return new FeignBuilder(clazz);
    }
    
    public <T extends BaseEntity<T>> FeignBuilder eq(boolean isTure, SFunction<T, ?> function, Object value) {
        if (isTure) {
            queryParams.add(new QueryParam(QueryTypeEnum.EQ, getColumn(function), value));
        }
        return this;
    }

    
    public <T extends BaseEntity<T>> FeignBuilder in(SFunction<T, ?> function, Object... values) {
        queryParams.add(new QueryParam(QueryTypeEnum.IN, getColumn(function), values));
        return this;
    }
    
    public <T extends BaseEntity<T>> FeignBuilder notIn(SFunction<T, ?> function, Object... values) {
        queryParams.add(new QueryParam(QueryTypeEnum.NOT_IN, getColumn(function), values));
        return this;
    }

    
    public <T extends BaseEntity<T>> FeignBuilder notIn(boolean isTure, SFunction<T, ?> function, Object... values) {
        if (isTure) {
            queryParams.add(new QueryParam(QueryTypeEnum.NOT_IN, getColumn(function), values));
        }
        return this;
    }

    
    public <T extends BaseEntity<T>> FeignBuilder groupBy(SFunction<T, ?>... columns) {
    	List<Object> list = new ArrayList<Object>();
		for(SFunction<T, ?>  column : columns) {
			list.add(getColumn(column));
		}
		queryParams.add(new QueryParam(QueryTypeEnum.GROUP_BY , list));
        return this;
    }

    
    public <T extends BaseEntity<T>> FeignBuilder orderByAsc(SFunction<T, ?> function) {
        queryParams.add(new QueryParam(QueryTypeEnum.ORDER_BY_ASC, getColumn(function)));
        return this;
    }

    
    public <T extends BaseEntity<T>> FeignBuilder orderByDesc(SFunction<T, ?> function) {
        queryParams.add(new QueryParam(QueryTypeEnum.ORDER_BY_DESC, getColumn(function)));
        return this;
    }


    
    public <T extends BaseEntity<T>> FeignBuilder apply(String data) {
        queryParams.add(new QueryParam(QueryTypeEnum.APPLY, null,data));
        return this;
    }

    
    public <T extends BaseEntity<T>> FeignBuilder last(String data) {
        queryParams.add(new QueryParam(QueryTypeEnum.LAST, null,data));
        return this;
    }
    
    public <T extends BaseEntity<T>> FeignBuilder isNull(String data) {
        queryParams.add(new QueryParam(QueryTypeEnum.IS_NULL, data));
        return this;
    }
    
    public <T extends BaseEntity<T>> FeignBuilder isNotNull(String data) {
        queryParams.add(new QueryParam(QueryTypeEnum.IS_NOT_NULL, data));
        return this;
    }
    
    public <T extends BaseEntity<T>> FeignBuilder isNull(SFunction<T, ?> function) {
        queryParams.add(new QueryParam(QueryTypeEnum.IS_NULL, getColumn(function)));
        return this;
    }
    
    public <T extends BaseEntity<T>> FeignBuilder isNotNull(SFunction<T, ?> function) {
        queryParams.add(new QueryParam(QueryTypeEnum.IS_NOT_NULL, getColumn(function)));
        return this;
    }

    
    public <T extends BaseEntity<T>> FeignBuilder ne(SFunction<T, ?> function, Object value) {
        queryParams.add(new QueryParam(QueryTypeEnum.NE, getColumn(function), value));
        return this;
    }

    
    public <T extends BaseEntity<T>> FeignBuilder ne(boolean isTure, SFunction<T, ?> function, Object value) {
        if (isTure) {
            queryParams.add(new QueryParam(QueryTypeEnum.NE, getColumn(function), value));
        }
        return this;
    }

    
    public <T extends BaseEntity<T>> FeignBuilder le(SFunction<T, ?> function, Object value) {
        queryParams.add(new QueryParam(QueryTypeEnum.LE, getColumn(function), value));
        return this;
    }

    
    public <T extends BaseEntity<T>> FeignBuilder le(boolean isTure, SFunction<T, ?> function, Object value) {
        if (isTure) {
            queryParams.add(new QueryParam(QueryTypeEnum.LE, getColumn(function), value));
        }
        return this;
    }


    
    public <T extends BaseEntity<T>> FeignBuilder ge(SFunction<T, ?> function, Object value) {
        queryParams.add(new QueryParam(QueryTypeEnum.GE, getColumn(function), value));
        return this;
    }

    
    public <T extends BaseEntity<T>> FeignBuilder ge(boolean isTure, SFunction<T, ?> function, Object value) {
        if (isTure) {
            queryParams.add(new QueryParam(QueryTypeEnum.GE, getColumn(function), value));
        }
        return this;
    }


    
    public <T extends BaseEntity<T>> FeignBuilder lt(SFunction<T, ?> function, Object value) {
        queryParams.add(new QueryParam(QueryTypeEnum.LT, getColumn(function), value));
        return this;
    }

    
    public <T extends BaseEntity<T>> FeignBuilder lt(boolean isTure, SFunction<T, ?> function, Object value) {
        if (isTure) {
            queryParams.add(new QueryParam(QueryTypeEnum.LT, getColumn(function), value));
        }
        return this;
    }

    
	public <T extends BaseEntity<T>> FeignBuilder like(SFunction<T, ?> function, Object value) {
    	queryParams.add(new QueryParam(QueryTypeEnum.LIKE, getColumn(function), value));
    	return this;
	}

	
	public <T extends BaseEntity<T>> FeignBuilder like(boolean isTure, SFunction<T, ?> function, Object value) {
		if (isTure) {
            queryParams.add(new QueryParam(QueryTypeEnum.LIKE, getColumn(function), value));
        }
        return this;
	}

    
    public <T extends BaseEntity<T>> FeignBuilder gt(SFunction<T, ?> function, Object value) {
        queryParams.add(new QueryParam(QueryTypeEnum.GT, getColumn(function), value));
        return this;
    }

    
    public <T extends BaseEntity<T>> FeignBuilder gt(boolean isTure, SFunction<T, ?> function, Object value) {
        if (isTure) {
            queryParams.add(new QueryParam(QueryTypeEnum.GT, getColumn(function), value));
        }
        return this;
    }

	public <T extends BaseEntity<T>> FeignBuilder select(Object... values) {
		queryParams.add(new QueryParam(QueryTypeEnum.SELECT , values));
        return this;
	}
	
	public <T extends BaseEntity<T>> FeignBuilder select(SFunction<T, ?>... columns) {
		List<Object> list = new ArrayList<Object>();
		for(SFunction<T, ?>  column : columns) {
			list.add(getColumn(column));
		}
		queryParams.add(new QueryParam(QueryTypeEnum.SELECT , list));
        return this;
	}
	
	/**
	 * 查询远程数据
	 * @param <T>
	 * @return
	 */
	public <T extends BaseEntity<T>> List<T> list() {
		if(clazz == null) {
			throw new RuntimeException("实体类名未设置");
		}
		DictCore dictCore = ApplicationContextUtils.getBean(DictCore.class);
		String serviceCode = clazz.getName().split("\\.")[3];
    	ServiceCodeNameEnum serviceCodeNameEnum = EnumMessage.getByCode(ServiceCodeNameEnum.class, serviceCode);
    	if(serviceCodeNameEnum == null) {
    		throw new RuntimeException("获取远程基础信息查询失败，截取到的服务名是：" + serviceCode);
    	}
		BaseDataFeign queryEntityDataFeign = dictCore.getBaseDataFeign(serviceCodeNameEnum);
		ApiResult<List<T>> result = JSON.parseObject(queryEntityDataFeign.list(this) , ApiResult.class);
		List<T> data = null;
		if(result.isSuccess()) {
			data = result.getData();
			if(CollUtil.isNotEmpty(data)) {
				data = (List<T>) JSON.parseArray(JSON.toJSONString(data), clazz);
			}
		}else {
			data = new ArrayList<>();
		}
		return data;
	}

	public <T> T invoke(FeignInvoke feignInvoke) {
		ApiResult<?> result = invokeFeign(feignInvoke);
    	T data = null;
    	if(result.isSuccess()) {
    		if(result.getData() != null) {
    			data = (T) JSON.parseObject(JSON.toJSONString(result.getData()) , clazz);
    		}
    	}
    	return data;
	}
	
	public <T> List<T> invokeList(FeignInvoke feignInvoke) {
		ApiResult<?> result = invokeFeign(feignInvoke);
		List<T> data = null;
    	if(result.isSuccess()) {
    		if(result.getData() != null) {
    			data = (List<T>) JSON.parseArray(JSON.toJSONString(result.getData()), clazz);
    		}
    	}
    	return data;
	}
	
	private ApiResult<?> invokeFeign(FeignInvoke feignInvoke){
		DictCore dictCore = ApplicationContextUtils.getBean(DictCore.class);
		String serviceCode = feignInvoke.getClassName().split("\\.")[3];
    	ServiceCodeNameEnum serviceCodeNameEnum = EnumMessage.getByCode(ServiceCodeNameEnum.class, serviceCode);
    	if(serviceCodeNameEnum == null) {
    		throw new RuntimeException("获取远程基础信息查询失败，截取到的服务名是：" + serviceCode);
    	}
    	BaseDataFeign baseDataFeign = dictCore.getBaseDataFeign(serviceCodeNameEnum);
    	return JSON.parseObject(baseDataFeign.invoke(feignInvoke) , ApiResult.class);
	}
	
	private static <T> String getColumn(SFunction<T, ?> function) {
        return StringUtil.camelToUnderline(FunctionUtil.getFieldName(function));
    }

}
