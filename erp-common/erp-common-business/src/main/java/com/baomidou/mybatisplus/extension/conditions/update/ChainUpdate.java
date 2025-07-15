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
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.ChainWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.exception.ServiceException;

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
		Wrapper<T> wrapper = this.getWrapper();
    	if(wrapper instanceof LambdaUpdateWrapper) {
    		LambdaUpdateWrapper lambdaUpdateWrapper = (LambdaUpdateWrapper)wrapper;
    		boolean emptyOfWhere = lambdaUpdateWrapper.isEmptyOfWhere();
    		if(emptyOfWhere) {
    			throw new ServiceException("不允许没有条件更新数据，如有需要，请联系实施人员");
    		}
    		if(entity == null) {
    			String sqlSet = lambdaUpdateWrapper.getSqlSet();
        		if(StringUtils.isNotBlank(sqlSet)) {
        			String notBlankSqlSet = sqlSet.replace(" ", "");
        			Object object = null;
					try {
						Field field = lambdaUpdateWrapper.getClass().getDeclaredField("sqlSet");
						field.setAccessible(true);
						object = field.get(lambdaUpdateWrapper);
						field.setAccessible(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					LoginUser loginUser = UserContext.getNonLoginUser();
	                String userId = loginUser.getUid();
	                String userName = loginUser.getUserName();
        			if(object != null) {
        				List<String> sqlSetList = (List<String>)object;
        				if(!notBlankSqlSet.contains("update_time=")) {
        					sqlSetList.add("update_time='" + LocalDateTime.now()+"'");
        				}
        				if(!notBlankSqlSet.contains("update_user_id=") && StringUtils.isNotBlank(userId)) {
        					sqlSetList.add("update_user_id='" + userId+"'");
        				}
        				if(!notBlankSqlSet.contains("update_user_name=") && StringUtils.isNotBlank(userName)) {
        					sqlSetList.add("update_user_name='" + userName+"'");
        				}
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
    	Wrapper<T> wrapper = this.getWrapper();
    	if(wrapper instanceof LambdaUpdateWrapper) {
    		LambdaUpdateWrapper lambdaUpdateWrapper = (LambdaUpdateWrapper)wrapper;
    		boolean emptyOfWhere = lambdaUpdateWrapper.isEmptyOfWhere();
    		if(emptyOfWhere) {
    			throw new ServiceException("不允许没有条件删除数据，如有需要，请联系实施人员");
    		}
    	}
        return SqlHelper.retBool(getBaseMapper().delete(getWrapper()));
    }
}
