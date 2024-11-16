package com.common.business.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.SortParamDTO;
import com.common.business.service.SuperService;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.entity.BaseEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
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
     *
     * @param ids
     * @return boolean
     * @author yl
     * @date 2023-03-24 10:45
     */
    @Override
    public boolean removeByIds(Collection<? extends Serializable> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            LoginUser loginUser = UserContext.getDefaultLoginUser();
            return update().set(T.IS_DELETED, true)
                    .set(T.UPDATE_TIME, LocalDateTime.now())
                    .set(T.UPDATE_USER_ID, loginUser.getUid())
                    .set(T.UPDATE_USER_NAME, loginUser.getUserName())
                    .in(T.ID, ids)
                    .update();
        }
        return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id, Long version) {
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        return update().set(T.IS_DELETED, true)
                .set(T.UPDATE_TIME, LocalDateTime.now())
                .set(T.UPDATE_USER_ID, loginUser.getUid())
                .set(T.UPDATE_USER_NAME, loginUser.getUserName())
                .setSql(version != null, CharSequenceUtil.format("{}={}+1", T.VERSION, T.VERSION))
                .eq(T.ID, id)
                .eq(version != null, T.VERSION, version)
                .update();
    }

    protected List<OrderItem> buildOrders(List<SortParamDTO> sortList) {
        if (CollectionUtils.isEmpty(sortList)){
            return Collections.emptyList();
        }
        List<OrderItem> orderItems = new ArrayList<>(sortList.size());
        sortList.stream().forEach(sortParamDTO -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setColumn(sortParamDTO.getField());
            orderItem.setAsc(sortParamDTO.getSort().equals("ASC"));
            orderItems.add(orderItem);
        });
        return orderItems;
    }
}