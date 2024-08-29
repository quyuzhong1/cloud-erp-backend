package com.common.business.wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.utils.StringUtil;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;

@Data
public class QueryParam {
    /**
     * 类型
     */
    private QueryTypeEnum type;
    /**
     * 字段名称
     */
    private String name;
    /**
     * 字段名称
     */
    private Object value;
    /**
     * 字段值
     */
    private List<Object> values;
    
    public QueryParam() {
    }

    public QueryParam(QueryTypeEnum type, String name) {
        this.type = type;
        this.name = name;
    }
    
    public QueryParam(QueryTypeEnum type, String name, Object... values) {
    	this.type = type;
        this.name = name;
        if (values != null && values.length > 0) {
            List<Object> valueObjects = Arrays.asList(values);
            if (valueObjects.get(0) instanceof List) {
                List<Object> newValues = (List<Object>) valueObjects.get(0);
                if (!CollectionUtils.isEmpty(newValues)) {
                    this.value = newValues.get(0);
                    this.values = newValues;
                }
            } else if (valueObjects.get(0) instanceof Set) {
                Set<Object> newValues = (Set<Object>) valueObjects.get(0);
                if (!CollectionUtils.isEmpty(newValues)) {
                    this.value = newValues.iterator().next();
                    this.values = new ArrayList<>(newValues);
                }
            } else {
                this.value = values[0];
                this.values = valueObjects;
            }
        }
    }
    
    
    public QueryParam(QueryTypeEnum type, Object... values) {
    	this.type = type;
    	if (values != null && values.length > 0) {
    		List<Object> valueObjects = Arrays.asList(values);
    		if (valueObjects.get(0) instanceof List) {
    			List<Object> newValues = (List<Object>) valueObjects.get(0);
    			if (!CollectionUtils.isEmpty(newValues)) {
    				this.value = newValues.get(0);
    				this.values = newValues;
    			}
    		} else if (valueObjects.get(0) instanceof Set) {
    			Set<Object> newValues = (Set<Object>) valueObjects.get(0);
    			if (!CollectionUtils.isEmpty(newValues)) {
    				this.value = newValues.iterator().next();
    				this.values = new ArrayList<>(newValues);
    			}
    		} else {
    			this.value = values[0];
    			this.values = valueObjects;
    		}
    	}
    }
    
    public static QueryWrapper<?> getQueryWrapper(List<QueryParam> queryParams) {
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
                wrapper.apply(value.toString());
            } else if (QueryTypeEnum.LAST.equals(type)) {
                wrapper.last(value.toString());
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
