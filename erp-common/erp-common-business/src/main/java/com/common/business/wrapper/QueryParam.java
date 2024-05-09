package com.common.business.wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.util.CollectionUtils;

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
}
