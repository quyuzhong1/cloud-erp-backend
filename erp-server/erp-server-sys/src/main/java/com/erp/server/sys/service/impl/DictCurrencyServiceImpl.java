package com.erp.server.sys.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.server.sys.mapper.DictCurrencyMapper;
import com.erp.server.sys.service.DictCurrencyService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Lambda
 * @Classname DictCurrencyServiceImpl

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

    @Override
    public List<CurrencyDTO.ViewDTO> listCurrencyByKingdeeCodeList(List<String> currCodeList) {
        if (CollectionUtils.isEmpty(currCodeList)) {
            return Collections.EMPTY_LIST;
        }
        List<DictCurrencyEntity> list = lambdaQuery().in(DictCurrencyEntity::getKingdeeCode, currCodeList).list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        return BeanMapperUtils.copyList(CurrencyDTO.ViewDTO.class,list);
    }

    @Override
    public DictCurrencyEntity getCurrencyByNum(String num) {
        if (CharSequenceUtil.isBlank(num)){
            return null;
        }
        return this.lambdaQuery().select(DictCurrencyEntity::getId,DictCurrencyEntity::getCurrencyNum,DictCurrencyEntity::getSymbol)
                .eq(DictCurrencyEntity::getCurrencyNum, num).last("limit 1").one();
    }
}
