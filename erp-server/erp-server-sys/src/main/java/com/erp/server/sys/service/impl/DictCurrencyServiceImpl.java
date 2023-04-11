package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.server.sys.mapper.DictCurrencyMapper;
import com.erp.server.sys.service.DictCurrencyService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lambda
 * @Classname DictCurrencyServiceImpl
 * @Description TODO
 * @Date 2023-03-21 17:22
 * @Created by yl
 */
@Service
public class DictCurrencyServiceImpl extends SuperServiceImpl<DictCurrencyMapper, DictCurrencyEntity> implements DictCurrencyService {


    /**
     * 获取货币列表
     *
     * @param
     * @return java.util.List<com.erp.model.sys.dto.CurrencyDTO.ViewDTO>
     * @author yl
     * @date 2023-03-21 17:27
     */
    @Override
    public List<CurrencyDTO.ViewDTO> getList() {
        LambdaQueryWrapper<DictCurrencyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(DictCurrencyEntity::getIndex);
        List<DictCurrencyEntity> list = this.list(queryWrapper);
        return BeanMapper.copyList(list, CurrencyDTO.ViewDTO.class);
    }

    @Override
    public List<CurrencyDTO.ViewDTO> listByCurrency(List<String> currencyList) {
        List<DictCurrencyEntity> list = lambdaQuery().in(CollectionUtils.isNotEmpty(currencyList),DictCurrencyEntity::getId,currencyList).list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return BeanMapper.copyList(list, CurrencyDTO.ViewDTO.class);
    }
}
