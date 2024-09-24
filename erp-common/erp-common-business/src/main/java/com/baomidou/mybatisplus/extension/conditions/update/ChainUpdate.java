/*
 * Copyright (c) 2011-2021, baomidou (jobob@qq.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baomidou.mybatisplus.extension.conditions.update;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.ChainWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.core.entity.BaseEntity;

import cn.hutool.core.date.LocalDateTimeUtil;

/**
 * 具有更新方法的定义
 *
 * @author miemie
 * @since 2018-12-19
 */
public interface ChainUpdate<T> extends ChainWrapper<T> {

    /**
     * 更新数据
     *
     * @return 是否成功
     */
    default boolean update() {
		return this.update(null);
    }

    /**
     * 更新数据
     *
     * @param entity 实体类
     * @return 是否成功
     */
    default boolean update(T entity) {
    	if(entity == null) {
    		Wrapper<T> wrapper = this.getWrapper();
        	if(wrapper instanceof LambdaUpdateWrapper) {
        		LambdaUpdateWrapper lambdaUpdateWrapper = (LambdaUpdateWrapper)wrapper;
        		String sqlSet = lambdaUpdateWrapper.getSqlSet();
        		if(!sqlSet.replace(" ", "").contains("update_time=")) {
        			Object object = null;
					try {
						Field field = lambdaUpdateWrapper.getClass().getDeclaredField("sqlSet");
						field.setAccessible(true);
						object = field.get(lambdaUpdateWrapper);
						field.setAccessible(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
        			if(object != null) {
        				List<String> sqlSetList = (List<String>)object;
        				sqlSetList.add("update_time='" + LocalDateTime.now()+"'");
        			}
        		}
        	}
    	}
        return SqlHelper.retBool(getBaseMapper().update(entity, getWrapper()));
    }

    /**
     * 删除数据
     *
     * @return 是否成功
     */
    default boolean remove() {
        return SqlHelper.retBool(getBaseMapper().delete(getWrapper()));
    }
}
