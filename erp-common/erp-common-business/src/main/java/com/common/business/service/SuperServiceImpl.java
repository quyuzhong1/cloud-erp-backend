package com.common.business.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.common.core.entity.BaseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 业务层基类实现
 *
 * @author Cloud
 */
public class SuperServiceImpl<M extends BaseMapper<T>, T extends BaseEntity<T>> extends ServiceImpl<M, T> implements SuperService<T> {

    @Override
    public Optional<T> getByIdOpt(Serializable id) {
        return Optional.ofNullable(super.getById(id));
    }

    @Override
    public Map<String, T> mapByIds(Collection<String> ids) {
        List<T> list;
        if (CollectionUtils.isEmpty(ids)) {
            list = list();
        } else {
            list = listByIds(ids);
        }
        return list.stream().collect(Collectors.toMap(T::getId, Function.identity()));
    }

    @Override
    public List<T> listByIdsSql(String idsSql) {
        return query().inSql(T.ID, idsSql).list();
    }

    @Override
    public Map<String, T> mapByIdsSql(String idsSql) {
        List<T> list = listByIdsSql(idsSql);
        return list.stream().collect(Collectors.toMap(T::getId, Function.identity()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        return removeById(id, null);
    }

    
    /**
     * 批量删除
     * @author yl
     * @date 2023-03-24 10:45
     * @param ids
     * @return boolean
     */
    @Override
    public boolean removeByIds(Collection<? extends Serializable> ids) {
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        return update().set(T.IS_DELETED, true)
                .set(T.UPDATE_TIME, LocalDateTime.now())
                .set(T.UPDATE_USER_ID, loginUser != null ? loginUser.getUid() : "")
                .set(T.UPDATE_USER_NAME, loginUser != null ? loginUser.getUserName() : "")
                .in(T.ID, ids)
                .update();

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id, Long version) {
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        return update().set(T.IS_DELETED, true)
                .set(T.UPDATE_TIME, LocalDateTime.now())
                .set(T.UPDATE_USER_ID, loginUser != null ? loginUser.getUid() : "")
                .set(T.UPDATE_USER_NAME, loginUser != null ? loginUser.getUserName() : "")
                .setSql(version != null, StrUtil.format("{}={}+1", T.VERSION, T.VERSION))
                .eq(T.ID, id)
                .eq(version != null, T.VERSION, version)
                .update();
    }
}