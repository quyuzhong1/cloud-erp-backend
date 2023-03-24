package com.common.business.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SuperService<T> extends IService<T> {

    Optional<T> getByIdOpt(Serializable id);

    Map<String, T> mapByIds(Collection<String> ids);

    List<T> listByIdsSql(String idsSql);

    Map<String, T> mapByIdsSql(String idsSql);

    @Override
    boolean removeById(Serializable id);

    /**
     * 批量删除
     * @author yl
     * @date 2023-03-24 10:44
     * @param ids
     * @return boolean
     */
    @Override
    boolean removeByIds(Collection<? extends Serializable> ids);


    boolean removeById(Serializable id, Long version);

}