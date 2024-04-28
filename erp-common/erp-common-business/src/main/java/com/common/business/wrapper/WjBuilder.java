package com.common.business.wrapper;

import java.util.ArrayList;
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
public class WjBuilder {

    private final List<QueryParam> queryParams = new ArrayList<>();
    
    private Class<? extends BaseEntity<?>> clazz;
    
    public <T extends BaseEntity<T>> WjBuilder eq(SFunction<T, ?> function, Object value) {
        queryParams.add(new QueryParam(QueryTypeEnum.EQ, getColumn(function), value));
        return this;
    }

    public WjBuilder() {
    	
    }
    
    private WjBuilder(Class<? extends BaseEntity<?>> clazz) {
    	this.clazz = clazz;
    }
    
    public static WjBuilder create(Class<? extends BaseEntity<?>> clazz) {
    	return new WjBuilder(clazz);
    }
    
    public <T extends BaseEntity<T>> WjBuilder eq(boolean isTure, SFunction<T, ?> function, Object value) {
        if (isTure) {
            queryParams.add(new QueryParam(QueryTypeEnum.EQ, getColumn(function), value));
        }
        return this;
    }

    
    public <T extends BaseEntity<T>> WjBuilder in(SFunction<T, ?> function, Object... values) {
        queryParams.add(new QueryParam(QueryTypeEnum.IN, getColumn(function), values));
        return this;
    }
    
    public <T extends BaseEntity<T>> WjBuilder notIn(SFunction<T, ?> function, Object... values) {
        queryParams.add(new QueryParam(QueryTypeEnum.NOT_IN, getColumn(function), values));
        return this;
    }

    
    public <T extends BaseEntity<T>> WjBuilder notIn(boolean isTure, SFunction<T, ?> function, Object... values) {
        if (isTure) {
            queryParams.add(new QueryParam(QueryTypeEnum.NOT_IN, getColumn(function), values));
        }
        return this;
    }

    
    public <T extends BaseEntity<T>> WjBuilder groupBy(SFunction<T, ?>... columns) {
    	List<Object> list = new ArrayList<Object>();
		for(SFunction<T, ?>  column : columns) {
			list.add(getColumn(column));
		}
		queryParams.add(new QueryParam(QueryTypeEnum.GROUP_BY , list));
        return this;
    }

    
    public <T extends BaseEntity<T>> WjBuilder orderByAsc(SFunction<T, ?> function) {
        queryParams.add(new QueryParam(QueryTypeEnum.ORDER_BY_ASC, getColumn(function)));
        return this;
    }

    
    public <T extends BaseEntity<T>> WjBuilder orderByDesc(SFunction<T, ?> function) {
        queryParams.add(new QueryParam(QueryTypeEnum.ORDER_BY_DESC, getColumn(function)));
        return this;
    }


    
    public <T extends BaseEntity<T>> WjBuilder apply(String data) {
        queryParams.add(new QueryParam(QueryTypeEnum.APPLY, data));
        return this;
    }

    
    public <T extends BaseEntity<T>> WjBuilder last(String data) {
        queryParams.add(new QueryParam(QueryTypeEnum.LAST, data));
        return this;
    }
    
    public <T extends BaseEntity<T>> WjBuilder isNull(String data) {
        queryParams.add(new QueryParam(QueryTypeEnum.IS_NULL, data));
        return this;
    }
    
    public <T extends BaseEntity<T>> WjBuilder isNotNull(String data) {
        queryParams.add(new QueryParam(QueryTypeEnum.IS_NOT_NULL, data));
        return this;
    }
    
    public <T extends BaseEntity<T>> WjBuilder isNull(SFunction<T, ?> function) {
        queryParams.add(new QueryParam(QueryTypeEnum.IS_NULL, getColumn(function)));
        return this;
    }
    
    public <T extends BaseEntity<T>> WjBuilder isNotNull(SFunction<T, ?> function) {
        queryParams.add(new QueryParam(QueryTypeEnum.IS_NOT_NULL, getColumn(function)));
        return this;
    }

    
    public <T extends BaseEntity<T>> WjBuilder ne(SFunction<T, ?> function, Object value) {
        queryParams.add(new QueryParam(QueryTypeEnum.NE, getColumn(function), value));
        return this;
    }

    
    public <T extends BaseEntity<T>> WjBuilder ne(boolean isTure, SFunction<T, ?> function, Object value) {
        if (isTure) {
            queryParams.add(new QueryParam(QueryTypeEnum.NE, getColumn(function), value));
        }
        return this;
    }

    
    public <T extends BaseEntity<T>> WjBuilder le(SFunction<T, ?> function, Object value) {
        queryParams.add(new QueryParam(QueryTypeEnum.LE, getColumn(function), value));
        return this;
    }

    
    public <T extends BaseEntity<T>> WjBuilder le(boolean isTure, SFunction<T, ?> function, Object value) {
        if (isTure) {
            queryParams.add(new QueryParam(QueryTypeEnum.LE, getColumn(function), value));
        }
        return this;
    }


    
    public <T extends BaseEntity<T>> WjBuilder ge(SFunction<T, ?> function, Object value) {
        queryParams.add(new QueryParam(QueryTypeEnum.GE, getColumn(function), value));
        return this;
    }

    
    public <T extends BaseEntity<T>> WjBuilder ge(boolean isTure, SFunction<T, ?> function, Object value) {
        if (isTure) {
            queryParams.add(new QueryParam(QueryTypeEnum.GE, getColumn(function), value));
        }
        return this;
    }


    
    public <T extends BaseEntity<T>> WjBuilder lt(SFunction<T, ?> function, Object value) {
        queryParams.add(new QueryParam(QueryTypeEnum.LT, getColumn(function), value));
        return this;
    }

    
    public <T extends BaseEntity<T>> WjBuilder lt(boolean isTure, SFunction<T, ?> function, Object value) {
        if (isTure) {
            queryParams.add(new QueryParam(QueryTypeEnum.LT, getColumn(function), value));
        }
        return this;
    }

    
	public <T extends BaseEntity<T>> WjBuilder like(SFunction<T, ?> function, Object value) {
    	queryParams.add(new QueryParam(QueryTypeEnum.LIKE, getColumn(function), value));
    	return this;
	}

	
	public <T extends BaseEntity<T>> WjBuilder like(boolean isTure, SFunction<T, ?> function, Object value) {
		if (isTure) {
            queryParams.add(new QueryParam(QueryTypeEnum.LIKE, getColumn(function), value));
        }
        return this;
	}

    
    public <T extends BaseEntity<T>> WjBuilder gt(SFunction<T, ?> function, Object value) {
        queryParams.add(new QueryParam(QueryTypeEnum.GT, getColumn(function), value));
        return this;
    }

    
    public <T extends BaseEntity<T>> WjBuilder gt(boolean isTure, SFunction<T, ?> function, Object value) {
        if (isTure) {
            queryParams.add(new QueryParam(QueryTypeEnum.GT, getColumn(function), value));
        }
        return this;
    }

	public <T extends BaseEntity<T>> WjBuilder select(Object... values) {
		queryParams.add(new QueryParam(QueryTypeEnum.SELECT , values));
        return this;
	}
	
	public <T extends BaseEntity<T>> WjBuilder select(SFunction<T, ?>... columns) {
		List<Object> list = new ArrayList<Object>();
		for(SFunction<T, ?>  column : columns) {
			list.add(getColumn(column));
		}
		queryParams.add(new QueryParam(QueryTypeEnum.SELECT , list));
        return this;
	}
	
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

	private static <T> String getColumn(SFunction<T, ?> function) {
        return StringUtil.camelToUnderline(FunctionUtil.getFieldName(function));
    }

}
